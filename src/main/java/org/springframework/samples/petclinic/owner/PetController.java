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
import org.springframework.samples.petclinic.toggles.FeatureToggleManager;
import org.springframework.samples.petclinic.migration.*;

import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.util.StringUtils;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.WebDataBinder;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.sql.ResultSet;
import java.util.Collection;
import java.util.Iterator;

/**
 * @author Juergen Hoeller
 * @author Ken Krebs
 * @author Arjen Poutsma
 */
@Controller
@RequestMapping("/owners/{ownerId}")
public class PetController {

    private static final String VIEWS_PETS_CREATE_OR_UPDATE_FORM = "pets/createOrUpdatePetForm";
    private final PetRepository pets;
    private final OwnerRepository owners;
    private SqlDB db;
    private TableDataGateway tdg;

    public PetController(PetRepository pets, OwnerRepository owners) {
        this.pets = pets;
        this.owners = owners;
    }

    public void setDbForTest(SqlDB db, TableDataGateway tdg) {
        this.db = db;
        this.tdg = tdg;
    }


    @ModelAttribute("types")
    public Collection<PetType> populatePetTypes() {


        db = new SQLiteDB();
        tdg = new TableDataGateway(db);
        ResultSet resultSet = this.tdg.selectTable("types");
        shadowReadPetTypes(resultSet);
        db.close();
        return this.pets.findPetTypes();
    }

    @ModelAttribute("owner")
    public Owner findOwner(@PathVariable("ownerId") int ownerId) {
        return this.owners.findById(ownerId);
    }

    @InitBinder("owner")
    public void initOwnerBinder(WebDataBinder dataBinder) {
        dataBinder.setDisallowedFields("id");
    }

    @InitBinder("pet")
    public void initPetBinder(WebDataBinder dataBinder) {
        dataBinder.setValidator(new PetValidator());
    }

    @GetMapping("/pets/new")
    public String initCreationForm(Owner owner, ModelMap model) {
        System.out.println("1");
        Pet pet = new Pet();
        owner.addPet(pet);
        model.put("pet", pet);
        return VIEWS_PETS_CREATE_OR_UPDATE_FORM;
    }

    @PostMapping("/pets/new")
    public String processCreationForm(Owner owner, @Valid Pet pet, BindingResult result, ModelMap model) {
        if (StringUtils.hasLength(pet.getName()) && pet.isNew() && owner.getPet(pet.getName(), true) != null){
            result.rejectValue("name", "duplicate", "already exists");
        }
        owner.addPet(pet);
        if (result.hasErrors()) {
            model.put("pet", pet);
            return VIEWS_PETS_CREATE_OR_UPDATE_FORM;
        } else {
            // insert in old db
            this.pets.save(pet);

            // check if feature toggle is on
            if(FeatureToggleManager.DO_RUN_CONSISTENCY_CHECKER)
            {
            	if(!FeatureToggleManager.DOING_MIGRATION_TEST){
                    db = new SQLiteDB();
                    tdg = new TableDataGateway(db);
                }

            	// insert into new SQLite db
                tdg.insertPet(pet);

            }
            return "redirect:/owners/{ownerId}";
        }
    }

    @GetMapping("/pets/{petId}/edit")
    public String initUpdateForm(@PathVariable("petId") int petId, ModelMap model) {
        Pet pet = this.pets.findById(petId);
        db = new SQLiteDB();
        tdg = new TableDataGateway(db);
        ResultSet resultSet = this.tdg.getById(petId, "pets");
        model.put("pet", pet);
        try {
            String name = resultSet.getString("name");
            String birthDate = resultSet.getString("birth_date");
            String typeId = tdg.getPetType(resultSet.getInt("type_id"));
            Integer ownerId = resultSet.getInt("owner_id");

            checkAndUpdate(pet, petId, name, birthDate, typeId, ownerId);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return VIEWS_PETS_CREATE_OR_UPDATE_FORM;
    }

    @PostMapping("/pets/{petId}/edit")
    public String processUpdateForm(@Valid Pet pet, BindingResult result, Owner owner, ModelMap model) {
        if (result.hasErrors()) {
            pet.setOwner(owner);
            model.put("pet", pet);
            return VIEWS_PETS_CREATE_OR_UPDATE_FORM;
        } else {
            owner.addPet(pet);
            this.pets.save(pet);
            return "redirect:/owners/{ownerId}";
        }
    }

    private void shadowReadPetTypes(ResultSet resultSet){
        if (FeatureToggleManager.DO_SHADOW_READ){
            Collection<PetType> result = this.pets.findPetTypes();
            Iterator<PetType> oldIterator = result.iterator();
            try {
                System.out.println("Shadow Read Pet Types");
                while(resultSet.next() && oldIterator.hasNext()){
                    String name = resultSet.getString("name");
                    Integer id = resultSet.getInt("id");
                    PetType nextPetType = oldIterator.next();
                    if (!nextPetType.getName().equals(name)) {
                        this.tdg.updateInconsistencies(id, "types", "name", nextPetType.getName());
                    }
                    if (!nextPetType.getId().equals(id)) {
                        this.tdg.updateInconsistencies(id, "types", "id", nextPetType.getId());
                    }
                }

            } catch (Exception e) {
                System.out.println("Shadow Read Pet Types Exception");
                e.printStackTrace();
            }
        }
    }

    public void checkAndUpdate(Pet pet, Integer petId, String name, String birthDate, String typeId, Integer ownerId) {
        if (!pet.getName().equals(name)) {
            this.tdg.updateInconsistencies(petId, "pets", "name", pet.getName());
        }
        if (!pet.getBirthDate().toString().equals(birthDate)) {
            this.tdg.updateInconsistencies(petId, "pets", "birth_date", pet.getBirthDate().toString());
        }
        if (!(pet.getType().toString().equals(typeId))) {
            this.tdg.updateInconsistencies(petId, "pets", "type_id", pet.getType().toString());
        }
        if (!pet.getOwner().getId().equals(ownerId)) {
            this.tdg.updateInconsistencies(petId, "pets", "owner_id", pet.getOwner().getId());
        }
    }
}
