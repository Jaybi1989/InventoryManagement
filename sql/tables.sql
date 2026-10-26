CREATE TABLE Brand (
                        BrandID INT AUTO_INCREMENT PRIMARY KEY,
                        Name VARCHAR(255)
);

CREATE TABLE Category (
                        CategoryID INT AUTO_INCREMENT PRIMARY KEY,
                        Name VARCHAR(255)
);

CREATE TABLE OperatingMode (
                        OperatingModeID INT AUTO_INCREMENT PRIMARY KEY,
                        Name VARCHAR(255)
);

CREATE TABLE Contact (
                        ContactID INT AUTO_INCREMENT PRIMARY KEY,
                        Lastname VARCHAR(255),
                        Firstname VARCHAR(255),
                        Postcode VARCHAR(10),
                        City VARCHAR(30),
                        Streetname VARCHAR(255),
                        Housenumber VARCHAR(20),
                        EmailAddress TEXT

);

CREATE TABLE User (
                        UserID INT AUTO_INCREMENT PRIMARY KEY,
                        ContactID int not null,
                        Username VARCHAR(255),
                        Password VARCHAR(255),
                        Role VARCHAR(255),
                        FOREIGN KEY (ContactID) REFERENCES Contact(ContactID)

);

CREATE TABLE Article (
                         ArticleID BIGINT AUTO_INCREMENT PRIMARY KEY,
                         BrandID INT,
                         CategoryID INT,
                         OperatingModeID INT,
                         Articlename VARCHAR(255),
                         PurchaseDate DATE,
                         WarrantyDuration INT,
                         IsDeleted BOOLEAN,
                         DeletionDate DATETIME,
                         FOREIGN KEY (BrandID) REFERENCES Brand(BrandID),
                         FOREIGN KEY (CategoryID) REFERENCES Category(CategoryID),
                         FOREIGN KEY (OperatingModeID) REFERENCES OperatingMode(OperatingModeID)
);

CREATE TABLE Location (
                        LocationID INT AUTO_INCREMENT PRIMARY KEY,
                        ContactID INT,
                        ArticleID BIGINT,
                        LoanDate DATETIME,
                        FOREIGN KEY (ContactID) REFERENCES Contact(ContactID),
                        FOREIGN KEY (ArticleID) REFERENCES Article(ArticleID)
);

