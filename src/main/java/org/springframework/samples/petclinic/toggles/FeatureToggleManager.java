package org.springframework.samples.petclinic.toggles;



import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class FeatureToggleManager {

    public static boolean DO_DROP_TABLES_UPON_FORKLIFT = true;
    public static boolean DO_RUN_CONSISTENCY_CHECKER = false;
	public static boolean DOING_MIGRATION_TEST = false;
    public static boolean DO_SHADOW_READ = false;
    public static boolean DO_REDIRECT_TO_NEW_PET_PAGE_AFTER_OWNER_CREATION = false;
    public static boolean DO_REDIRECT_TO_NEW_VISIT_PAGE_AFTER_PET_CREATION = false;
    public static boolean DO_REDIRECT_TO_VIEW_OWNERS_AFTER_CLICKING_FIND_OWNERS = false;

	// Owner Toggles
    public static boolean DO_DISPLAY_LINK_TO_OWNER_LIST = true;
    public static boolean DO_ENABLE_FIRST_NAME_SEARCH = false;


    // should not appear in toggle list
    public static boolean[] jacocoArrTest = {true, true, false};

    // Gets all toggles using Java Reflection
    public static List<Toggle> getToggles() throws IllegalAccessException {
        List<Toggle> toggles = new ArrayList<Toggle>();
        Field[] allFields = FeatureToggleManager.class.getDeclaredFields();
        for (Field field : allFields) {
            if (!field.getType().toString().equals("boolean")) continue;

            // if field is static
            if (java.lang.reflect.Modifier.isStatic(field.getModifiers()) ){
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
