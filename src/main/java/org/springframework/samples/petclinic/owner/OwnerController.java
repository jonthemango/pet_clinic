/*
 * Copyright 2012-2018 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.springframework.samples.petclinic.owner;

import org.springframework.samples.petclinic.toggles.ABTestingLogger;
import org.springframework.samples.petclinic.toggles.FeatureToggleManager;
import org.springframework.beans.PropertyEditorRegistry;
import org.springframework.samples.petclinic.migration.*;

import org.springframework.samples.petclinic.migration.ConsistencyChecker;
import org.springframework.samples.petclinic.migration.SqlDB;
import org.springframework.samples.petclinic.migration.TableDataGateway;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.validation.Errors;
import org.springframework.validation.FieldError;
import org.springframework.validation.ObjectError;
import org.springframework.web.bind.WebDataBinder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.InitBinder;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.servlet.ModelAndView;


import javax.validation.Valid;

import java.beans.PropertyEditor;
import java.sql.ResultSet;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

/**
 * @author Juergen Hoeller
 * @author Ken Krebs
 * @author Arjen Poutsma
 * @author Michael Isvy
 */
@Controller
public class OwnerController {

    private SqlDB db;
    private TableDataGateway tdg;

    private static final String VIEWS_OWNER_CREATE_OR_UPDATE_FORM = "owners/createOrUpdateOwnerForm";
    private final OwnerRepository owners;

    public static boolean SYSTEM_UNDER_TEST = false;

    public OwnerController(OwnerRepository owners) {
        this.owners = owners;
    }

    @InitBinder
    public void setAllowedFields(WebDataBinder dataBinder) {
        dataBinder.setDisallowedFields("id");
    }

    @GetMapping("/owners/new")
    public String initCreationForm(Map<String, Object> model) {
        if (FeatureToggleManager.DO_REDIRECT_TO_NEW_PET_PAGE_AFTER_OWNER_CREATION) {
            ABTestingLogger.log("Owner being created", "", "b");
        }
        else {
            ABTestingLogger.log("Owner being created", "", "a");
        }
        Owner owner = new Owner();
        model.put("owner", owner);
        return VIEWS_OWNER_CREATE_OR_UPDATE_FORM;
    }

    @PostMapping("/owners/new")
    public String processCreationForm(@Valid Owner owner, BindingResult result) {
        ABTestingLogger.log("POST Occurred","","a");
        if (result.hasErrors()) {
            return VIEWS_OWNER_CREATE_OR_UPDATE_FORM;
        } else {
            // insert owner into old db
            this.owners.save(owner); 

            // check if feature toggle is on
            if(FeatureToggleManager.DO_RUN_CONSISTENCY_CHECKER)
            {            

                if(!FeatureToggleManager.DOING_MIGRATION_TEST){      
                db = new SQLiteDB();
                tdg = new TableDataGateway(db);
                }
                
                // insert into new SQLite db
                tdg.insertOwner(owner);
            }

            if (FeatureToggleManager.DO_REDIRECT_TO_NEW_PET_PAGE_AFTER_OWNER_CREATION) {
                ABTestingLogger.log("Owner created", "", "b");
                return "redirect:/owners/" + owner.getId() + "/pets/new";
            }
            else {
                ABTestingLogger.log("Owner created", "", "a");
                return "redirect:/owners/" + owner.getId();
            }
        }
    }

    // To mock DB's
    public void setDbForTest(SqlDB db, TableDataGateway tdg) {
        this.db = db;
        this.tdg = tdg;
    }


    @GetMapping("/owners/find")
    public String initFindForm(Owner owner, BindingResult result,Map<String, Object> model) {

    if(FeatureToggleManager.DO_REDIRECT_TO_VIEW_OWNERS_AFTER_CLICKING_FIND_OWNERS && FeatureToggleManager.DO_ENABLE_FIRST_NAME_SEARCH){

        ABTestingLogger.logNoObject("Redirect to view Owners " ,"b");
        ABTestingLogger.logNoObject("Search by first name enable" ,"b");

        // allow parameterless GET request for /owners to return all records
        if (owner.getLastName() == null) {
            owner.setLastName(""); // empty string signifies broadest possible search
        }

        // find owners by last name
        Collection<Owner> results = this.owners.findByLastName(owner.getLastName());
        db = new SQLiteDB();
        tdg = new TableDataGateway(db);
        ResultSet resultSet = this.tdg.getOwnersByLastName(owner.getLastName());
        Iterator<Owner> oldIterator = results.iterator();

        if (results.isEmpty()) {
            if(SYSTEM_UNDER_TEST){
                return "owners/ownersList";
            }else{
            // no owners found
            result.rejectValue("lastName", "notFound", "not found");
            return "owners/findOwners";
            }
        } else if (results.size() == 1) {
            // 1 owner found
            owner = results.iterator().next();
            Integer expectedId = owner.getId();
            try {
                Integer actualId = resultSet.getInt("id");
                if (!expectedId.equals(actualId)) {
                    this.tdg.updateInconsistencies(actualId, "owners", "id", expectedId);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
            return "redirect:/owners/" + owner.getId();
        } else {
            // multiple owners found
            model.put("selections", results);
            try {
                while (oldIterator.hasNext() && resultSet.next()) {
                    if (!oldIterator.next().getId().equals(resultSet.getInt("id"))) {
                        this.tdg.updateInconsistencies(resultSet.getInt("id"), "owners", "id", oldIterator.next().getId());
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
            return "owners/ownersList";
        }
   
        
    }else{
        
        ABTestingLogger.logNoObject("Redirect to view Owners " ,"a");
        ABTestingLogger.logNoObject("Search by first name enable" ,"a");
        model.put("owner", new Owner());
        model.put("DO_DISPLAY_LINK_TO_OWNER_LIST", FeatureToggleManager.DO_DISPLAY_LINK_TO_OWNER_LIST);
        model.put("DO_ENABLE_FIRST_NAME_SEARCH", FeatureToggleManager.DO_ENABLE_FIRST_NAME_SEARCH);
        return "owners/findOwners";
    }
    
    }

    @GetMapping("/owners/finds")
    public String initFindForm(Map<String,Object>model){
        model.put("owner", new Owner());
        model.put("DO_DISPLAY_LINK_TO_OWNER_LIST", FeatureToggleManager.DO_DISPLAY_LINK_TO_OWNER_LIST);
        return "owners/findOwners";
    }


    
    @GetMapping("/owners")
    public String processFindForm(Owner owner, BindingResult result, Map<String, Object> model) {
        // allow parameterless GET request for /owners to return all records
        if (owner.getLastName() == null) {
            owner.setLastName(""); // empty string signifies broadest possible search
        }

        // find owners by last name
        Collection<Owner> results = this.owners.findByLastName(owner.getLastName());
        db = new SQLiteDB();
        tdg = new TableDataGateway(db);
        ResultSet resultSet = this.tdg.getOwnersByLastName(owner.getLastName());
        Iterator<Owner> oldIterator = results.iterator();

        if (results.isEmpty()) {
            // no owners found
            result.rejectValue("lastName", "notFound", "not found");
            return "owners/findOwners";
        } else if (results.size() == 1) {
            // 1 owner found
            owner = results.iterator().next();
            Integer expectedId = owner.getId();
            try {
                Integer actualId = resultSet.getInt("id");
                if (!expectedId.equals(actualId)) {
                    this.tdg.updateInconsistencies(actualId, "owners", "id", expectedId);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
            return "redirect:/owners/" + owner.getId();
        } else {
            // multiple owners found
            model.put("selections", results);
            try {
                while (oldIterator.hasNext() && resultSet.next()) {
                    if (!oldIterator.next().getId().equals(resultSet.getInt("id"))) {
                        this.tdg.updateInconsistencies(resultSet.getInt("id"), "owners", "id", oldIterator.next().getId());
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
            return "owners/ownersList";
        }

    }

    @GetMapping("/owners2")
    public String processFindFormFN(Owner owner, BindingResult result, Map<String, Object> model) {

        if(FeatureToggleManager.DO_ENABLE_FIRST_NAME_SEARCH){
            //if no first name is specified, will return all owners
            if (owner.getFirstName() == null) {
                owner.setFirstName("");
            }
            //retrieve owners from both database with the specified first name
            Collection<Owner> results = this.owners.findByFirstName(owner.getFirstName());
            db = new SQLiteDB();
            tdg = new TableDataGateway(db);
            ResultSet resultSet = this.tdg.getOwnersByFirstName(owner.getFirstName());
            Iterator<Owner> oldIterator = results.iterator();

            if (results.isEmpty()) {
                // no owners found
                result.rejectValue("firstName", "notFound", "not found");
                return "owners/findOwners";
            } else if (results.size() == 1) {
                // 1 owner found
                owner = results.iterator().next();
                Integer expectedId = owner.getId();
                try {
                    Integer actualId = resultSet.getInt("id");
                    if (!expectedId.equals(actualId)) {
                        this.tdg.updateInconsistencies(actualId, "owners", "id", expectedId);
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
                return "redirect:/owners/" + owner.getId();
            } else {
                // multiple owners found
                model.put("selections", results);
                try {
                    while (oldIterator.hasNext() && resultSet.next()) {
                        if (!oldIterator.next().getId().equals(resultSet.getInt("id"))) {
                            this.tdg.updateInconsistencies(resultSet.getInt("id"), "owners", "id", oldIterator.next().getId());
                        }
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
                return "owners/ownersList";
            }
        }else{
            return "/error";
        }

    }

    @GetMapping("/owners/{ownerId}/edit")
    public String initUpdateOwnerForm(@PathVariable("ownerId") int ownerId, Model model) {
        Owner owner = this.owners.findById(ownerId); // find owner to update
        model.addAttribute(owner);
        db = new SQLiteDB();
        tdg = new TableDataGateway(db);
        ResultSet resultSet = this.tdg.getById(ownerId, "owners");
        try {
            String firstName = resultSet.getString("first_name");
            String lastName = resultSet.getString("last_name");
            String address = resultSet.getString("address");
            String city = resultSet.getString("city");
            String telephone = resultSet.getString("telephone");

            checkAndUpdate(ownerId, owner, firstName, lastName, address, city, telephone);

        } catch (Exception e) {
            e.printStackTrace();
        }
        return VIEWS_OWNER_CREATE_OR_UPDATE_FORM;
    }

    @PostMapping("/owners/{ownerId}/edit")
    public String processUpdateOwnerForm(@Valid Owner owner, BindingResult result, @PathVariable("ownerId") int ownerId) {
        if (result.hasErrors()) {
            return VIEWS_OWNER_CREATE_OR_UPDATE_FORM;
        } else {
            owner.setId(ownerId);
            this.owners.save(owner); // update owner
            return "redirect:/owners/{ownerId}";
        }
    }

    /**
     * Custom handler for displaying an owner.
     *
     * @param ownerId the ID of the owner to display
     * @return a ModelMap with the model attributes for the view
     */
    @GetMapping("/owners/{ownerId}")
    public ModelAndView showOwner(@PathVariable("ownerId") int ownerId) {
        ModelAndView mav = new ModelAndView("owners/ownerDetails");

        mav.addObject(this.owners.findById(ownerId)); // find owner to display

        db = new SQLiteDB();
        tdg = new TableDataGateway(db);
        ResultSet resultSet = this.tdg.getById(ownerId, "owners");
        try {
            String firstName = resultSet.getString("first_name");
            String lastName = resultSet.getString("last_name");
            String address = resultSet.getString("address");
            String city = resultSet.getString("city");
            String telephone = resultSet.getString("telephone");

            checkAndUpdate(ownerId, this.owners.findById(ownerId), firstName, lastName, address, city, telephone);

        } catch (Exception e) {
            e.printStackTrace();
        }
        return mav;
    }

    private void checkAndUpdate(Integer ownerId, Owner owner, String firstName, String lastName, String address, String city, String telephone) {
        if (!owner.getFirstName().equals(firstName)) {
            this.tdg.updateInconsistencies(ownerId, "owners", "first_name", owner.getFirstName());
        }
        if (!owner.getLastName().equals(lastName)) {
            this.tdg.updateInconsistencies(ownerId, "owners", "last_name", owner.getLastName());
        }
        if (!owner.getAddress().equals(address)) {
            this.tdg.updateInconsistencies(ownerId, "owners", "address", owner.getAddress());
        }
        if (!owner.getCity().equals(city)) {
            this.tdg.updateInconsistencies(ownerId, "owners", "city", owner.getCity());
        }
        if (!owner.getTelephone().equals(telephone)) {
            this.tdg.updateInconsistencies(ownerId, "owners", "telephone", owner.getTelephone());
        }
    }

}
