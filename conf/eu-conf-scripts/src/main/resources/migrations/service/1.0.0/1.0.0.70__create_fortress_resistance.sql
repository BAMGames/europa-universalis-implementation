CREATE TABLE T_FORTRESS_RESISTANCE (
  ID         BIGINT NOT NULL AUTO_INCREMENT,
  FORTRESS INTEGER,
  ROUND INTEGER,
  THIRD INTEGER,
  BREACH BIT,
  PRIMARY KEY (ID)
);

