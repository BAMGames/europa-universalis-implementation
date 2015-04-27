ALTER TABLE border
DROP
FOREIGN KEY FK_j5iy3i8e1081cpv4ghxfsejok;

ALTER TABLE border
DROP
FOREIGN KEY FK_oq34c68ex4pt6oi87ehykv2dp;

ALTER TABLE counter
DROP
FOREIGN KEY FK_2tpbojw2cg4d68xdv47bhf72x;

ALTER TABLE counter
DROP
FOREIGN KEY FK_smkonbprv4ndcbcfxkwyo6eux;

ALTER TABLE country
DROP
FOREIGN KEY FK_8ls5po6k1qkhcmiyx0sndk270;

ALTER TABLE d_attribute
DROP
FOREIGN KEY FK_cjbi9udwnlny2ra2hygw1i8n3;

ALTER TABLE event_political
DROP
FOREIGN KEY FK_alogdp3y04u87cs425huk8vuw;

ALTER TABLE player
DROP
FOREIGN KEY FK_jbvdarqjorwgmufby0x0inrux;

ALTER TABLE player
DROP
FOREIGN KEY FK_p9t8s410xvde9xt7229p00s0c;

ALTER TABLE province_eu
DROP
FOREIGN KEY FK_evyi3s90cwo2p06tmjhh9s8m3;

ALTER TABLE province_eu
DROP
FOREIGN KEY FK_3g966lt1cs2lwqwlqicif16ar;

ALTER TABLE relation
DROP
FOREIGN KEY FK_lrr1j9po934l3ppmtc1omwpyj;

ALTER TABLE relation
DROP
FOREIGN KEY FK_bf8yqgg303mr7hf94qg2lr7an;

ALTER TABLE relation
DROP
FOREIGN KEY FK_l6dqqxrg3t685rsp8wjvo9u7m;

ALTER TABLE stack
DROP
FOREIGN KEY FK_f8vvyp6yje719q63ilynav2ah;

ALTER TABLE stack
DROP
FOREIGN KEY FK_oc35rtolciw2mwct2ctv62k0r;

DROP TABLE IF EXISTS border;

DROP TABLE IF EXISTS counter;

DROP TABLE IF EXISTS country;

DROP TABLE IF EXISTS d_attribute;

DROP TABLE IF EXISTS d_diff;

DROP TABLE IF EXISTS event_political;

DROP TABLE IF EXISTS game;

DROP TABLE IF EXISTS player;

DROP TABLE IF EXISTS province;

DROP TABLE IF EXISTS province_eu;

DROP TABLE IF EXISTS relation;

DROP TABLE IF EXISTS stack;

CREATE TABLE border (
    id               BIGINT NOT NULL AUTO_INCREMENT,
    type             VARCHAR(255),
    id_province_from BIGINT,
    id_province_to   BIGINT,
    PRIMARY KEY (id)
);

CREATE TABLE counter (
    id         BIGINT NOT NULL AUTO_INCREMENT,
    type       VARCHAR(255),
    id_country BIGINT,
    id_stack   BIGINT,
    PRIMARY KEY (id)
);

CREATE TABLE country (
    id      BIGINT NOT NULL AUTO_INCREMENT,
    name    VARCHAR(255),
    id_game BIGINT,
    PRIMARY KEY (id)
);

CREATE TABLE d_attribute (
    id      BIGINT NOT NULL AUTO_INCREMENT,
    type    VARCHAR(255),
    value   VARCHAR(255),
    id_diff BIGINT,
    PRIMARY KEY (id)
);

CREATE TABLE d_diff (
    id           BIGINT NOT NULL AUTO_INCREMENT,
    id_game      BIGINT,
    id_object    BIGINT,
    type         VARCHAR(255),
    type_object  VARCHAR(255),
    version_game BIGINT,
    PRIMARY KEY (id)
);

CREATE TABLE event_political (
    id      BIGINT NOT NULL AUTO_INCREMENT,
    turn    INTEGER,
    id_game BIGINT,
    PRIMARY KEY (id)
);

CREATE TABLE game (
    id      BIGINT NOT NULL AUTO_INCREMENT,
    status  VARCHAR(255),
    turn    INTEGER,
    version BIGINT NOT NULL,
    PRIMARY KEY (id)
);

CREATE TABLE player (
    id         BIGINT NOT NULL AUTO_INCREMENT,
    id_country BIGINT,
    id_game    BIGINT,
    PRIMARY KEY (id)
);

CREATE TABLE province (
    id      BIGINT NOT NULL AUTO_INCREMENT,
    name    VARCHAR(255),
    terrain VARCHAR(255),
    PRIMARY KEY (id)
);

CREATE TABLE province_eu (
    income       INTEGER,
    port         BIT,
    praesidiable BIT,
    id           BIGINT NOT NULL,
    id_country   BIGINT,
    PRIMARY KEY (id)
);

CREATE TABLE relation (
    id               BIGINT NOT NULL AUTO_INCREMENT,
    type             VARCHAR(255),
    id_player_first  BIGINT,
    id_game          BIGINT,
    id_player_second BIGINT,
    PRIMARY KEY (id)
);

CREATE TABLE stack (
    id          BIGINT NOT NULL AUTO_INCREMENT,
    id_game     BIGINT,
    id_province BIGINT,
    PRIMARY KEY (id)
);

ALTER TABLE province
ADD CONSTRAINT UK_ixpn85566lwxgajun8msnplik UNIQUE (name);

ALTER TABLE border
ADD CONSTRAINT FK_j5iy3i8e1081cpv4ghxfsejok
FOREIGN KEY (id_province_from)
REFERENCES province (id);

ALTER TABLE border
ADD CONSTRAINT FK_oq34c68ex4pt6oi87ehykv2dp
FOREIGN KEY (id_province_to)
REFERENCES province (id);

ALTER TABLE counter
ADD CONSTRAINT FK_2tpbojw2cg4d68xdv47bhf72x
FOREIGN KEY (id_country)
REFERENCES country (id);

ALTER TABLE counter
ADD CONSTRAINT FK_smkonbprv4ndcbcfxkwyo6eux
FOREIGN KEY (id_stack)
REFERENCES stack (id);

ALTER TABLE country
ADD CONSTRAINT FK_8ls5po6k1qkhcmiyx0sndk270
FOREIGN KEY (id_game)
REFERENCES game (id);

ALTER TABLE d_attribute
ADD CONSTRAINT FK_cjbi9udwnlny2ra2hygw1i8n3
FOREIGN KEY (id_diff)
REFERENCES d_diff (id);

ALTER TABLE event_political
ADD CONSTRAINT FK_alogdp3y04u87cs425huk8vuw
FOREIGN KEY (id_game)
REFERENCES game (id);

ALTER TABLE player
ADD CONSTRAINT FK_jbvdarqjorwgmufby0x0inrux
FOREIGN KEY (id_country)
REFERENCES country (id);

ALTER TABLE player
ADD CONSTRAINT FK_p9t8s410xvde9xt7229p00s0c
FOREIGN KEY (id_game)
REFERENCES game (id);

ALTER TABLE province_eu
ADD CONSTRAINT FK_evyi3s90cwo2p06tmjhh9s8m3
FOREIGN KEY (id_country)
REFERENCES country (id);

ALTER TABLE province_eu
ADD CONSTRAINT FK_3g966lt1cs2lwqwlqicif16ar
FOREIGN KEY (id)
REFERENCES province (id);

ALTER TABLE relation
ADD CONSTRAINT FK_lrr1j9po934l3ppmtc1omwpyj
FOREIGN KEY (id_player_first)
REFERENCES player (id);

ALTER TABLE relation
ADD CONSTRAINT FK_bf8yqgg303mr7hf94qg2lr7an
FOREIGN KEY (id_game)
REFERENCES game (id);

ALTER TABLE relation
ADD CONSTRAINT FK_l6dqqxrg3t685rsp8wjvo9u7m
FOREIGN KEY (id_player_second)
REFERENCES player (id);

ALTER TABLE stack
ADD CONSTRAINT FK_f8vvyp6yje719q63ilynav2ah
FOREIGN KEY (id_game)
REFERENCES game (id);

ALTER TABLE stack
ADD CONSTRAINT FK_oc35rtolciw2mwct2ctv62k0r
FOREIGN KEY (id_province)
REFERENCES province (id);
