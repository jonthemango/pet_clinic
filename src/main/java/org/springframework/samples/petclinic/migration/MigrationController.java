package org.springframework.samples.petclinic.migration;


import org.springframework.samples.petclinic.owner.OwnerRepository;
import org.springframework.samples.petclinic.owner.Pet;
import org.springframework.samples.petclinic.owner.PetRepository;
import org.springframework.samples.petclinic.vet.VetRepository;
import org.springframework.samples.petclinic.visit.VisitRepository;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.servlet.ModelAndView;

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
}
