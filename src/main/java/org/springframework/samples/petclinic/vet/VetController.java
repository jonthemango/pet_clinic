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
package org.springframework.samples.petclinic.vet;

import org.springframework.samples.petclinic.migration.SQLiteDB;
import org.springframework.samples.petclinic.migration.SqlDB;
import org.springframework.samples.petclinic.migration.TableDataGateway;
import org.springframework.samples.petclinic.owner.Owner;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import java.sql.ResultSet;
import java.util.Collection;
import java.util.Iterator;
import java.util.Map;

/**
 * @author Juergen Hoeller
 * @author Mark Fisher
 * @author Ken Krebs
 * @author Arjen Poutsma
 */
@Controller
class VetController {

    private SqlDB db = new SQLiteDB();
    private TableDataGateway tdg = new TableDataGateway(db);
    private final VetRepository vets;

    public VetController(VetRepository clinicService) {
        this.vets = clinicService;
    }

    @GetMapping("/vets.html")
    public String showVetList(Map<String, Object> model) {
        // Here we are returning an object of type 'Vets' rather than a collection of Vet
        // objects so it is simpler for Object-Xml mapping
        Vets vets = new Vets();
        vets.getVetList().addAll(this.vets.findAll());
        db = new SQLiteDB();
        tdg = new TableDataGateway(db);
        ResultSet resultSet = this.tdg.selectTable("vets");
        shadowReadVets(resultSet);
        model.put("vets", vets);
        return "vets/vetList";
    }

    @GetMapping({ "/vets" })
    public @ResponseBody Vets showResourcesVetList() {
        // Here we are returning an object of type 'Vets' rather than a collection of Vet
        // objects so it is simpler for JSon/Object mapping
        Vets vets = new Vets();
        vets.getVetList().addAll(this.vets.findAll());
        db = new SQLiteDB();
        tdg = new TableDataGateway(db);
        ResultSet resultSet = this.tdg.selectTable("vets");
        shadowReadVets(resultSet);
        return vets;
    }

    private void shadowReadVets(ResultSet resultSet){
        Collection<Vet> result = this.vets.findAll();
        Iterator<Vet> oldIterator = result.iterator();
        try {
            System.out.println("going into try");
            while(resultSet.next() && oldIterator.hasNext()){
                String firstName = resultSet.getString("first_name");
                String lastName = resultSet.getString("last_name");
                Integer id = resultSet.getInt("id");
                Vet nextVert = oldIterator.next();
                if (!nextVert.getFirstName().equals(firstName)) {
                    this.tdg.updateInconsistencies(id, "vets", "first_name", nextVert.getFirstName());
                }
                if (!nextVert.getLastName().equals(lastName)) {
                    this.tdg.updateInconsistencies(id, "vets", "last_name", nextVert.getLastName());
                }
            }

        } catch (Exception e) {
            System.out.println("going into exception here sadly");
            e.printStackTrace();
        }
    }

}
