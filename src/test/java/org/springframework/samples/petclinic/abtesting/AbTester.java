package org.springframework.samples.petclinic.abtesting;

import org.junit.Test;
import org.springframework.samples.petclinic.toggles.ABTestingLogger;
import org.springframework.samples.petclinic.toggles.FeatureToggleManager;

import java.util.Locale;

public class AbTester {

    @Test
    public void DO_REDIRECT_TO_NEW_PET_PAGE_AFTER_OWNER_CREATION() {

        FeatureToggleManager.DO_REDIRECT_TO_NEW_PET_PAGE_AFTER_OWNER_CREATION = false;

        ABTestingLogger.log("test","value", "a");

        FeatureToggleManager.DO_REDIRECT_TO_NEW_PET_PAGE_AFTER_OWNER_CREATION = true;
        FeatureToggleManager.DO_REDIRECT_TO_NEW_PET_PAGE_AFTER_OWNER_CREATION = false;
    }
}
