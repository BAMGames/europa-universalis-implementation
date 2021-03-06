ALTER TABLE PROVINCE_EU
ADD COLUMN R_COUNTRY VARCHAR(255);

UPDATE PROVINCE_EU
SET R_COUNTRY = (SELECT NAME
                 FROM COUNTRY
                 WHERE ID = ID_COUNTRY);

ALTER TABLE PROVINCE_EU
DROP FOREIGN KEY FK_PROVINCE_EU_COUNTRY;

ALTER TABLE PROVINCE_EU
DROP INDEX FK_PROVINCE_EU_COUNTRY;

ALTER TABLE PROVINCE_EU
DROP COLUMN ID_COUNTRY;

ALTER TABLE COUNTER
ADD COLUMN R_COUNTRY VARCHAR(255);

UPDATE COUNTER
SET R_COUNTRY = (SELECT NAME
                 FROM COUNTRY
                 WHERE ID = ID_COUNTRY);

ALTER TABLE COUNTER
DROP FOREIGN KEY FK_COUNTER_COUNTRY;

ALTER TABLE COUNTER
DROP INDEX FK_COUNTER_COUNTRY;

ALTER TABLE COUNTER
DROP COLUMN ID_COUNTRY;