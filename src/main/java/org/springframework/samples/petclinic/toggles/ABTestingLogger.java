package org.springframework.samples.petclinic.toggles;

import net.minidev.json.JSONObject;
import org.springframework.samples.petclinic.migration.SQLiteDB;
import org.springframework.samples.petclinic.migration.SqlDB;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

public class ABTestingLogger {

    private static String dbName = "a_b_testing.db";

    /***
     *
     * @param logName Name of thing being logged
     * @param object Log any object needed
     * @param a_or_b Use the string "a" or the string "b" to denote the two classifications of experiment
     * @return Example JSONObject return value
     * {
     *     "a_or_b": "a",
     *     "logName": "logName",
     *     "toggles": {
     *         "DO_RUN_CONSISTENCY_CHECKER": false,
     *         "DO_DROP_TABLES_UPON_FORKLIFT": true,
     *         "DO_SHADOW_READ": false,
     *         "DO_DISPLAY_LINK_TO_OWNER_LIST": true,
     *         "DOING_MIGRATION_TEST": false
     *     },
     *     "time": "2019/04/07 22:19:01",
     *     "object": "value"
     * }
     */


    public static JSONObject log(String logName, Object object, String a_or_b){
        // Get current time
        DateTimeFormatter dtf = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        LocalDateTime now = LocalDateTime.now();

        // Add keys to the JSON object
        JSONObject obj = new JSONObject();
        obj.put("logName", logName);
        obj.put("object", object.toString());
        obj.put("time",dtf.format(now));
        obj.put("a_or_b", a_or_b);

        // Add current state of feature toggles to JSON object
        try {
            List<Toggle> toggles = FeatureToggleManager.getToggles();
            JSONObject toggleObj = new JSONObject();
            for (Toggle toggle : toggles){
                toggleObj.put(toggle.name, toggle.value);
            }
            obj.put("toggles", toggleObj);
        } catch (IllegalAccessException e) {
            System.out.print("ERROR: Toggle object failed.");
            //e.printStackTrace();
        }

        // Print it
        // System.out.println(obj.toString());

        // Save it
        SqlDB db = new SQLiteDB(dbName);
        db.execute("CREATE TABLE IF NOT EXISTS `logs` ( `id` INTEGER PRIMARY KEY AUTOINCREMENT, `log` TEXT)");
        db.execute(String.format("INSERT INTO `logs` ( log ) VALUES ('%s')", obj.toString()));
        db.close();

        return obj;
    }

    public static void resetLogger(){
        // Drop logs
        SqlDB db = new SQLiteDB(dbName);
        db.execute("DROP TABLE IF EXISTS logs");
        db.close();
    }

}
