package org.springframework.samples.petclinic.migration;

public class Forklift {

    private String  initSchemaStatements[] = {
        "CREATE TABLE IF NOT EXISTS `vets` ( `id` INTEGER PRIMARY KEY AUTOINCREMENT, `first_name` TEXT, `last_name` TEXT )",
        "CREATE INDEX IF NOT EXISTS `last_name_vets` ON `vets` ( `last_name` )",
        "CREATE TABLE IF NOT EXISTS `specialties` ( `id` INTEGER PRIMARY KEY AUTOINCREMENT, `name` TEXT )",
        "CREATE INDEX IF NOT EXISTS `name_specialities` ON `specialties` ( `name` )",
        "CREATE TABLE IF NOT EXISTS `vet_specialties` ( `vet_id` INTEGER NOT NULL, `specialty_id` INTEGER NOT NULL, FOREIGN KEY(`specialty_id`) REFERENCES `specialties`(`id`), FOREIGN KEY(`vet_id`) REFERENCES `vets`(`id`) )",
        "CREATE TABLE IF NOT EXISTS `types` ( `id` INTEGER PRIMARY KEY AUTOINCREMENT, `name` TEXT )",
        "CREATE INDEX IF NOT EXISTS `name_types` ON `types` ( `name` )",
        "CREATE TABLE IF NOT EXISTS `owners` ( `id` INTEGER PRIMARY KEY AUTOINCREMENT, `first_name` TEXT, `last_name` TEXT, `address` TEXT, `city` TEXT, `telephone` TEXT )",
        "CREATE INDEX IF NOT EXISTS `last_name` ON `owners` ( `last_name` )",
        "CREATE TABLE IF NOT EXISTS `pets` ( `id` INTEGER PRIMARY KEY AUTOINCREMENT, `name` TEXT, `birth_date` TEXT, `type_id` INTEGER NOT NULL, `owner_id` INTEGER NOT NULL, FOREIGN KEY(`owner_id`) REFERENCES `owners`(`id`) )",
        "CREATE INDEX IF NOT EXISTS `name_pets` ON `pets` ( `name` )",
        "CREATE TABLE IF NOT EXISTS `visits` ( `id` INTEGER PRIMARY KEY AUTOINCREMENT, `pet_id` INTEGER, `visit_date` TEXT, `description` TEXT, FOREIGN KEY(`pet_id`) REFERENCES `pets`(`id`) )"
    };

    private SqlDB db;


    public Forklift(SqlDB db){
        this.db = db;
    }

    public void initSchema(){

        String statement;
        for (int i=0; i < this.initSchemaStatements.length; i++){
            statement = this.initSchemaStatements[i];
            db.execute(statement);
        }
        db.close();
    }

    public void initData(){

    }
}
