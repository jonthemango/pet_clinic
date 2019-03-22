
-- Install 'DB Browser for SQLite' https://sqlitebrowser.org/dl/
-- Create a new database called migration.db at the root of the project (same place as .gitignore / .travis.yml / pom.xml / mvnw
-- Click x on the prompt to insert a table
-- Navigate to execute SQL and copy this script below.
-- Proceed to data.sql and execute that script as well.

CREATE TABLE IF NOT EXISTS `vets` (
    `id` INTEGER PRIMARY KEY AUTOINCREMENT,
    `first_name` TEXT,
    `last_name` TEXT
);

CREATE INDEX IF NOT EXISTS `last_name_vets` ON `vets` ( `last_name` );

CREATE TABLE IF NOT EXISTS `specialties` (
    `id` INTEGER PRIMARY KEY AUTOINCREMENT,
    `name` TEXT
);

CREATE INDEX IF NOT EXISTS `name_specialities` ON `specialties` ( `name` );


CREATE TABLE IF NOT EXISTS `vet_specialties` (
	`vet_id`	INTEGER NOT NULL,
	`specialty_id`	INTEGER NOT NULL,
	FOREIGN KEY(`specialty_id`) REFERENCES `specialties`(`id`),
	FOREIGN KEY(`vet_id`) REFERENCES `vets`(`id`)
);

CREATE TABLE IF NOT EXISTS `types` (
	`id`	INTEGER PRIMARY KEY AUTOINCREMENT,
	`name`	TEXT
);
CREATE INDEX IF NOT EXISTS `name_types` ON `types` ( `name` );

CREATE TABLE IF NOT EXISTS `owners` (
	`id`	INTEGER PRIMARY KEY AUTOINCREMENT,
	`first_name`	TEXT,
	`last_name`	TEXT,
	`address`	TEXT,
	`city`	TEXT,
	`telephone`	TEXT
);

CREATE INDEX IF NOT EXISTS `last_name` ON `owners` ( `last_name` );

CREATE TABLE IF NOT EXISTS `pets` (
	`id`	INTEGER PRIMARY KEY AUTOINCREMENT,
	`name`	TEXT,
	`birth_date`	TEXT,
	`type_id`	INTEGER NOT NULL,
	`owner_id`	INTEGER NOT NULL,
	FOREIGN KEY(`owner_id`) REFERENCES `owners`(`id`)
);
CREATE INDEX IF NOT EXISTS `name_pets` ON `pets` ( `name` );

CREATE TABLE IF NOT EXISTS `visits` (
	`id`	INTEGER PRIMARY KEY AUTOINCREMENT,
	`pet_id`	INTEGER,
	`visit_date`	TEXT,
	`description`	TEXT,
	FOREIGN KEY(`pet_id`) REFERENCES `pets`(`id`)
);
