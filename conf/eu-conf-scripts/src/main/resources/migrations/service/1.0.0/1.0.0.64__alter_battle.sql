ALTER TABLE BATTLE
ADD COLUMN PHASING_FIRSTDAY_FIRE INTEGER DEFAULT 0,
ADD COLUMN PHASING_FIRSTDAY_SHOCK INTEGER DEFAULT 0,
ADD COLUMN PHASING_FIRSTDAY_PURSUIT INTEGER DEFAULT 0,
ADD COLUMN PHASING_SECONDDAY_FIRE INTEGER DEFAULT 0,
ADD COLUMN PHASING_SECONDDAY_SHOCK INTEGER DEFAULT 0,
ADD COLUMN PHASING_SECONDDAY_PURSUIT INTEGER DEFAULT 0,
ADD COLUMN NONPHASING_FIRSTDAY_FIRE INTEGER DEFAULT 0,
ADD COLUMN NONPHASING_FIRSTDAY_SHOCK INTEGER DEFAULT 0,
ADD COLUMN NONPHASING_FIRSTDAY_PURSUIT INTEGER DEFAULT 0,
ADD COLUMN NONPHASING_SECONDDAY_FIRE INTEGER DEFAULT 0,
ADD COLUMN NONPHASING_SECONDDAY_SHOCK INTEGER DEFAULT 0,
ADD COLUMN NONPHASING_SECONDDAY_PURSUIT INTEGER DEFAULT 0;
