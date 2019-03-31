package org.springframework.samples.petclinic.toggles;


import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class FeatureToggleManager {

    public static boolean DO_DROP_TABLES_UPON_FORKLIFT = true;
    public static boolean DO_RUN_CONSISTENCY_CHECKER = true;
	public static boolean DOING_MIGRATION_TEST = false;

	// Owner Toggles
    public static boolean DO_DISPLAY_LINK_TO_OWNER_LIST = true;


    // Gets all toggles using Java Reflection
    public static List getToggles() throws IllegalAccessException {
        List<Toggle> toggles = new ArrayList<Toggle>();
        Field[] allFields = FeatureToggleManager.class.getDeclaredFields();
        for (Field field : allFields) {
            // if field is static
            if (java.lang.reflect.Modifier.isStatic(field.getModifiers())){
                toggles.add(new Toggle(field.getName(), field.getBoolean(null)));
            }
        }
        return toggles;
    }

    public static boolean toggleByName(String toggleName) throws NoSuchFieldException, IllegalAccessException {
        Field field = FeatureToggleManager.class.getDeclaredField(toggleName);
        Boolean toggleValue = (Boolean) field.get(null);
        field.setBoolean(null, !toggleValue);
        return true;
    }
}
