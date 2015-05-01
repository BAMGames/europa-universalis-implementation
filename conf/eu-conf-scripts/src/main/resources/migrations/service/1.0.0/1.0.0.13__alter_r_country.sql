ALTER TABLE R_COUNTRY
ADD COLUMN TYPE VARCHAR(255);

ALTER TABLE R_COUNTRY
ADD COLUMN RELIGION VARCHAR(255);

ALTER TABLE R_COUNTRY
ADD COLUMN RM INTEGER;

ALTER TABLE R_COUNTRY
ADD COLUMN SUB INTEGER;

ALTER TABLE R_COUNTRY
ADD COLUMN MA INTEGER;

ALTER TABLE R_COUNTRY
ADD COLUMN EC INTEGER;

ALTER TABLE R_COUNTRY
ADD COLUMN EW INTEGER;

ALTER TABLE R_COUNTRY
ADD COLUMN VA INTEGER;

ALTER TABLE R_COUNTRY
ADD COLUMN AN INTEGER;

ALTER TABLE R_COUNTRY
ADD COLUMN FIDELITY INTEGER;

ALTER TABLE R_COUNTRY
ADD COLUMN ARMY_CLASS VARCHAR(255);

ALTER TABLE R_COUNTRY
ADD COLUMN ELECTOR BIT;

ALTER TABLE R_COUNTRY
ADD COLUMN HRE BIT;

CREATE TABLE R_COUNTRY_PROVINCE_EU_CAPITALS (
  ID_R_COUNTRY     BIGINT NOT NULL,
  ID_R_PROVINCE_EU BIGINT NOT NULL,
  PRIMARY KEY (ID_R_COUNTRY, ID_R_PROVINCE_EU)
);

ALTER TABLE R_COUNTRY_PROVINCE_EU_CAPITALS
ADD INDEX FK_R_COUNTRY_PROVINCE_EU_CAPITALS_R_COUNTRY (ID_R_COUNTRY),
ADD CONSTRAINT FK_R_COUNTRY_PROVINCE_EU_CAPITALS_R_COUNTRY
FOREIGN KEY (ID_R_COUNTRY)
REFERENCES R_COUNTRY (ID);

ALTER TABLE R_COUNTRY_PROVINCE_EU_CAPITALS
ADD INDEX FK_R_COUNTRY_PROVINCE_EU_CAPITALS_R_PROVINCE_EU (ID_R_PROVINCE_EU),
ADD CONSTRAINT FK_R_COUNTRY_PROVINCE_EU_CAPITALS_R_PROVINCE_EU
FOREIGN KEY (ID_R_PROVINCE_EU)
REFERENCES R_PROVINCE_EU (ID);

CREATE TABLE R_COUNTRY_PROVINCE_EU (
  ID_R_COUNTRY     BIGINT NOT NULL,
  ID_R_PROVINCE_EU BIGINT NOT NULL,
  PRIMARY KEY (ID_R_COUNTRY, ID_R_PROVINCE_EU)
);

ALTER TABLE R_COUNTRY_PROVINCE_EU
ADD INDEX FK_R_COUNTRY_PROVINCE_EU_R_COUNTRY (ID_R_COUNTRY),
ADD CONSTRAINT FK_R_COUNTRY_PROVINCE_EU_R_COUNTRY
FOREIGN KEY (ID_R_COUNTRY)
REFERENCES R_COUNTRY (ID);

ALTER TABLE R_COUNTRY_PROVINCE_EU
ADD INDEX FK_R_COUNTRY_PROVINCE_EU_R_PROVINCE_EU (ID_R_PROVINCE_EU),
ADD CONSTRAINT FK_R_COUNTRY_PROVINCE_EU_R_PROVINCE_EU
FOREIGN KEY (ID_R_PROVINCE_EU)
REFERENCES R_PROVINCE_EU (ID);

