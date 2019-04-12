package org.springframework.samples.petclinic.migration;


import org.springframework.samples.petclinic.owner.OwnerRepository;
import org.springframework.samples.petclinic.owner.PetRepository;
import org.springframework.samples.petclinic.toggles.FeatureToggleManager;
import org.springframework.samples.petclinic.vet.VetRepository;
import org.springframework.samples.petclinic.visit.VisitRepository;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.servlet.ModelAndView;

import java.util.Collection;

@Controller
class MigrationController {

    private Driver driver;
    public MigrationController(VetRepository vets, OwnerRepository owners, PetRepository pets, VisitRepository visits) {
        this.driver = new Driver(vets,owners,pets,visits);
    }

    @GetMapping("/migrations")
    public ModelAndView migrations() {

        String response = "\n";
        response += driver.listDB();

        ModelAndView mav = new ModelAndView("migrations/migrations");
        mav.addObject("code", "\n" + response);
        return mav;
    }

    @GetMapping("/toggles")
    public ModelAndView toggles() {

        String response = "\n";

        Collection toggles = null;
        try {
            toggles = FeatureToggleManager.getToggles();
        } catch (IllegalAccessException e) {
            //e.printStackTrace();
        }


        ModelAndView mav = new ModelAndView("migrations/toggles");
        mav.addObject("code", "\n" + response);
        mav.addObject("toggles",toggles);
        return mav;
    }

    @GetMapping("/toggles/{toggleName}")
    public String toggle(@PathVariable("toggleName") String toggleName){
        Boolean didWork = false;
        try {
            didWork = FeatureToggleManager.toggleByName(toggleName);
        } catch (NoSuchFieldException e) {
            //e.printStackTrace();
        } catch (IllegalAccessException e) {
            //e.printStackTrace();
        }
        return "redirect:/toggles;";
    }

    @GetMapping("/forklift")
    public ModelAndView forklift(){
        String response = driver.forklift();

        response += "\n";
        response += driver.listDB();

        ModelAndView mav = new ModelAndView("migrations/migrations");
        mav.addObject("code", "\n" + response);
        return mav;
    }

    @GetMapping("/clearDB")
    public ModelAndView clearDB(){
        String response = driver.emptyDB();
        response += "\n" + driver.listDB();
        ModelAndView mav = new ModelAndView("migrations/migrations");
        mav.addObject("code", "\n" + response);
        return mav;
    }

    @GetMapping("/consistencyChecker")
    public ModelAndView consistencyChecker(){
        String response = driver.consistencyChecker();

        ModelAndView mav = new ModelAndView("migrations/migrations");
        mav.addObject("code", "\n" + response);
        return mav;
    }

    
}
