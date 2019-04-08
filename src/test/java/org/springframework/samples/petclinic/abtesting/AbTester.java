package org.springframework.samples.petclinic.abtesting;

import org.junit.Test;
import org.springframework.samples.petclinic.toggles.ABTestingLogger;

import java.util.Locale;

public class AbTester {

    @Test
    public void test() {
        ABTestingLogger.log("test","value", "a");
    }
}
