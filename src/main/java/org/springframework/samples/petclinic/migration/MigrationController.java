package org.springframework.samples.petclinic.migration;


import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.servlet.ModelAndView;

@Controller
class MigrationController {


    @GetMapping("/migrations")
    public ModelAndView migrations() {

        String response = Driver.execute();

        ModelAndView mav = new ModelAndView("migrations/migrations");
        mav.addObject("code", "\n" + response);
        return mav;
    }
}
