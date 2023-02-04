CREATE SCHEMA myLibrary;

USE myLibrary;

CREATE TABLE `university` (
    `universityID` INT NOT NULL AUTO_INCREMENT,
    `name` VARCHAR(45) NOT NULL,
    `address` VARCHAR(45) NOT NULL,
    `city` VARCHAR(45) NOT NULL,
    `country` VARCHAR(45) NOT NULL,
    `contact` VARCHAR(45) NOT NULL,
    `email` VARCHAR(45) NOT NULL,
    `logo` LONGBLOB,
    PRIMARY KEY (`universityID`)
)  ENGINE=INNODB AUTO_INCREMENT=1 DEFAULT CHARSET=UTF8MB4 COLLATE = UTF8MB4_0900_AI_CI;

CREATE TABLE `user_categories` (
    `categoryID` INT NOT NULL AUTO_INCREMENT,
    `name` VARCHAR(45) NOT NULL,
    PRIMARY KEY (`categoryID`)
)  ENGINE=INNODB AUTO_INCREMENT=1 DEFAULT CHARSET=UTF8MB4 COLLATE = UTF8MB4_0900_AI_CI;

CREATE TABLE `users` (
    `userID` INT NOT NULL AUTO_INCREMENT,
    `username` VARCHAR(45) NOT NULL,
    `email` VARCHAR(45) NOT NULL,
    `password` VARCHAR(45) NOT NULL,
    `categoryID` INT NOT NULL,
    `universityID` INT NOT NULL,
    PRIMARY KEY (`userID`),
    KEY `uniID_idx` (`universityID`),
    KEY `categoryID_idx` (`categoryID`),
    CONSTRAINT `categoryID` FOREIGN KEY (`categoryID`)
        REFERENCES `user_categories` (`categoryID`),
    CONSTRAINT `uniID` FOREIGN KEY (`universityID`)
        REFERENCES `university` (`universityID`)
)  ENGINE=INNODB AUTO_INCREMENT=1 DEFAULT CHARSET=UTF8MB4 COLLATE = UTF8MB4_0900_AI_CI;

CREATE TABLE `faculties` (
    `facultyID` INT NOT NULL AUTO_INCREMENT,
    `name` VARCHAR(45) NOT NULL,
    `universityID` INT NOT NULL,
    PRIMARY KEY (`facultyID`),
    KEY `universityID_idx` (`universityID`),
    CONSTRAINT `universityID` FOREIGN KEY (`universityID`)
        REFERENCES `university` (`universityID`)
)  ENGINE=INNODB AUTO_INCREMENT=1 DEFAULT CHARSET=UTF8MB4 COLLATE = UTF8MB4_0900_AI_CI;

CREATE TABLE `studyprograms` (
    `studyprogramID` INT NOT NULL AUTO_INCREMENT,
    `name` VARCHAR(45) NOT NULL,
    `duration` INT NOT NULL,
    `facultyID` INT NOT NULL,
    PRIMARY KEY (`studyprogramID`),
    KEY `facultyID_idx` (`facultyID`),
    CONSTRAINT `facultyID` FOREIGN KEY (`facultyID`)
        REFERENCES `faculties` (`facultyID`)
)  ENGINE=INNODB AUTO_INCREMENT=1 DEFAULT CHARSET=UTF8MB4 COLLATE = UTF8MB4_0900_AI_CI;

CREATE TABLE `students` (
    `studentID` INT NOT NULL AUTO_INCREMENT,
    `student` VARCHAR(45) NOT NULL,
    `grade` VARCHAR(45) NOT NULL,
    `barcode` VARCHAR(45) NOT NULL,
    `registration_date` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    `studyprogramID` INT NOT NULL,
    PRIMARY KEY (`studentID`),
    UNIQUE KEY `barcode_UNIQUE` (`barcode`),
    KEY `studyprogramID_idx` (`studyprogramID`),
    CONSTRAINT `studyprogramID` FOREIGN KEY (`studyprogramID`)
        REFERENCES `studyprograms` (`studyprogramID`)
)  ENGINE=INNODB AUTO_INCREMENT=1 DEFAULT CHARSET=UTF8MB4 COLLATE = UTF8MB4_0900_AI_CI;

CREATE TABLE `books` (
    `bookID` INT NOT NULL AUTO_INCREMENT,
    `title` VARCHAR(100) NOT NULL,
    `barcode` VARCHAR(45) NOT NULL,
    `category` VARCHAR(45) NOT NULL,
    `num_pages` INT NOT NULL,
    `language` VARCHAR(45) NOT NULL,
    `author` VARCHAR(45) NOT NULL,
    `publisher` VARCHAR(45) NOT NULL,
    `copies` INT NOT NULL,
    PRIMARY KEY (`bookID`)
)  ENGINE=INNODB AUTO_INCREMENT=1 DEFAULT CHARSET=UTF8MB4 COLLATE = UTF8MB4_0900_AI_CI;

CREATE TABLE `borrowed_books` (
    `borrowedID` INT NOT NULL AUTO_INCREMENT,
    `bookID` INT NOT NULL,
    `studentID` INT NOT NULL,
    `startDate` DATE NOT NULL,
    `dueDate` DATE NOT NULL,
    `status` VARCHAR(45) NOT NULL DEFAULT 'BORROWED',
    PRIMARY KEY (`borrowedID`),
    KEY `bookID_idx` (`bookID`),
    KEY `studentID_idx` (`studentID`),
    CONSTRAINT `bookID` FOREIGN KEY (`bookID`)
        REFERENCES `books` (`bookID`),
    CONSTRAINT `studentID` FOREIGN KEY (`studentID`)
        REFERENCES `students` (`studentID`)
)  ENGINE=INNODB AUTO_INCREMENT=1 DEFAULT CHARSET=UTF8MB4 COLLATE = UTF8MB4_0900_AI_CI;



