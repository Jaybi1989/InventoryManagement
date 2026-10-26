INSERT INTO Brand (BrandID, Name) VALUES
                             (1,'Bosch'),
                             (2,'Makita'),
                             (3,'DeWalt');

INSERT INTO Category (CategoryID, Name) VALUES
                            (1,'Handwerkzeug'),
                            (2,'Messwerkzeug'),
                            (3,'Elektrowerkzeug'),
                            (4,'Maschinen');

INSERT INTO OperatingMode (OperatingModeID, Name) VALUES
                            (1,'Akku'),
                            (2,'220V'),
                            (3,'380V'),
                            (4,'Hand');

INSERT INTO Contact (ContactID, Lastname, Firstname, Postcode, City, Streetname, Housenumber, EmailAddress) VALUES
                            (1,'firma', 'Beispielhaft', '8000', 'Beispielhausen', 'Industriestrasse', '14', 'info@example.ch'),
                            (2,'Muster', 'Anna', '3302', 'Beispiel', 'Laengenhuebeltrasse', '23','anna@example.com'),
                            (3, 'Testperson', 'Danke', '8000', 'Beispielhausen', 'Kirchgasse', '1a', 'danke@example.ch');

INSERT INTO User (UserID, ContactID, Username, Password, Role) VALUES
                                                                   (1, 1, 'firma','$2a$12$KlDr.9Xa1eHhZTMUOC.WQeeBpYUFwxwhVFnnh0dNW1ttel1R3jtmq', 'ROLE_ADMIN '),
                                                                   (2, 3, 'Danke','$2a$12$Hh6ZDMlsHcgPjADsa4L6N.ek/rK4njZ.OQCjap92smVscFOsto/VO', 'ROLE_EMPLOYEE'),
                                                                   (3, 2, 'Jaybi','$2a$12$KlDr.9Xa1eHhZTMUOC.WQeeBpYUFwxwhVFnnh0dNW1ttel1R3jtmq', 'ROLE_ADMIN ');
