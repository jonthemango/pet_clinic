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

import org.springframework.samples.petclinic.migration.ConsistencyChecker;
import org.springframework.samples.petclinic.migration.SQLiteDB;
import org.springframework.samples.petclinic.migration.SqlDB;
import org.springframework.samples.petclinic.migration.TableDataGateway;
import org.springframework.samples.petclinic.toggles.FeatureToggleManager;
import org.springframework.samples.petclinic.visit.Visit;
import org.springframework.samples.petclinic.visit.VisitRepository;
import org.springframework.stereotype.Controller;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.WebDataBinder;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.sql.ResultSet;
import java.util.Map;

/**
 * @author Juergen Hoeller
 * @author Ken Krebs
 * @author Arjen Poutsma
 * @author Michael Isvy
 * @author Dave Syer
 */
@Controller
public class VisitController {

    private final VisitRepository visits;
    private final PetRepository pets;

    private SqlDB db;
    private TableDataGateway tdg;
    private ConsistencyChecker cc;

    public VisitController(VisitRepository visits, PetRepository pets) {
        this.visits = visits;
        this.pets = pets;
    }

    @InitBinder
    public void setAllowedFields(WebDataBinder dataBinder) {
        dataBinder.setDisallowedFields("id");
    }

    public void setDbForTest(SqlDB db, TableDataGateway tdg) {
        this.db = db;
        this.tdg = tdg;
    }

    /**
     * Called before each and every @RequestMapping annotated method.
     * 2 goals:
     * - Make sure we always have fresh data
     * - Since we do not use the session scope, make sure that Pet object always has an id
     * (Even though id is not part of the form fields)
     *
     * @param petId
     * @return Pet
     */
    @ModelAttribute("visit")
    public Visit loadPetWithVisit(@PathVariable("petId") int petId, Map<String, Object> model) {
        Pet pet = this.pets.findById(petId);
        db = new SQLiteDB();
        tdg = new TableDataGateway(db);
        ResultSet resultSet = this.tdg.getById(petId, "pets");
        try {
            String name = resultSet.getString("name");
            String birthDate = resultSet.getString("birth_date");
            String typeId = tdg.getPetType(resultSet.getInt("type_id"));
            Integer ownerId = resultSet.getInt("owner_id");

            checkAndUpdate(pet, petId, name, birthDate, typeId, ownerId);
        } catch (Exception e) {
            e.printStackTrace();
        }

        model.put("pet", pet);
        Visit visit = new Visit();
        pet.addVisit(visit);
        return visit;
    }

    // Spring MVC calls method loadPetWithVisit(...) before initNewVisitForm is called
    @GetMapping("/owners/*/pets/{petId}/visits/new")
    public String initNewVisitForm(@PathVariable("petId") int petId, Map<String, Object> model) {
        return "pets/createOrUpdateVisitForm";
    }

    // Spring MVC calls method loadPetWithVisit(...) before processNewVisitForm is called
    @PostMapping("/owners/{ownerId}/pets/{petId}/visits/new")
    public String processNewVisitForm(@Valid Visit visit, BindingResult result) {
        if (result.hasErrors()) {
            return "pets/createOrUpdateVisitForm";
        } else {
            // insert into old db
            this.visits.save(visit);

            // Check if feature toggle is on
            if(FeatureToggleManager.DO_RUN_CONSISTENCY_CHECKER)
            {
                if(!FeatureToggleManager.DOING_MIGRATION_TEST){      
                db = new SQLiteDB();
                tdg = new TableDataGateway(db);
                }

                // insert into new SQLite db
                tdg.insertVisit(visit);
                }

            return "redirect:/owners/{ownerId}";
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
