package com.mkl.eu.client.service.vo.enumeration;

/**
 * Type of a counter (in progress).
 *
 * @author MKL
 */
public enum CounterTypeEnum {
    /** A-.*/
    ARMY_MINUS,
    /** A+. */
    ARMY_PLUS,
    /** LD. */
    LAND_DETACHMENT,
    /** LDe. */
    LAND_DETACHEMENT_EXPLORATION,
    /** F-. */
    FLEET_MINUS,
    /** F+. */
    FLEET_PLUS,
    /** NWD. */
    NAVAL_WARSHIP_DETACHMENT,
    /** NDe. */
    NAVAL_DETACHMENT_EXPLORATION,
    /** NG. */
    NAVAL_GALLEY,
    /** DTr. */
    NAVAL_TRANSPORT,
    /** P- (pirate and corsair). */
    PIRATE_MINUS,
    /** P+ (pirate and corsair). */
    PIRATE_PLUS,
    /** F0. */
    FORT,
    /** F1. */
    FORTRESS_1,
    /** F2. */
    FORTRESS_2,
    /** F3. */
    FORTRESS_3,
    /** F4. */
    FORTRESS_4,
    /** F5. */
    FORTRESS_5,
    /** Ars2. */
    ARSENAL_2,
    /** Ars3. */
    ARSENAL_3,
    /** Ars4. */
    ARSENAL_4,
    /** Missionnary. */
    MISSIONNARY,
    /** Mission. */
    MISSION,
    /** Col-. */
    COLONY_MINUS,
    /** Col+. */
    COLONY_PLUS,
    /** TP-. */
    TRADING_POST_MINUS,
    /** TP+.*/
    TRADING_POST_PLUS,
    /** Own.*/
    OWN,
    /** Control.*/
    CONTROL,
    /** Spanigh gold transport fleet of Atlantic.*/
    FLOTA_DE_ORO,
    /** Spanish gold transport fleet of Pacific.*/
    FLOTA_DEL_PERU,
    /** Gold transport fleet of Indian.*/
    EAST_INDIES,
    /** Turkish gold transport fleet of Asia.*/
    LEVANT,

    // MANUFACTURES //
    /** Manufacture of Art-.*/
    MNU_ART_MINUS,
    /** Manufacture of Art+.*/
    MNU_ART_PLUS,
    /** Manufacture of Wood-.*/
    MNU_WOOD_MINUS,
    /** Manufacture of Wood+.*/
    MNU_WOOD_PLUS,
    /** Manufacture of Cereals-.*/
    MNU_CEREALES_MINUS,
    /** Manufacture of Cereals+.*/
    MNU_CEREALES_PLUS,
    /** Manufacture of Instruments-.*/
    MNU_INSTRUMENTS_MINUS,
    /** Manufacture of Instruments+.*/
    MNU_INSTRUMENTS_PLUS,
    /** Manufacture of Metal-.*/
    MNU_METAL_MINUS,
    /** Manufacture of Metal+.*/
    MNU_METAL_PLUS,
    /** Manufacture of Metal- (special prussian manufacture on silesie).*/
    MNU_METAL_SCHLESIEN_MINUS,
    /** Manufacture of Metal+ (special prussian manufacture on silesie).*/
    MNU_METAL_SCHLESIEN_PLUS,
    /** Manufacture of Fish-.*/
    MNU_FISH_MINUS,
    /** Manufacture of Fish+.*/
    MNU_FISH_PLUS,
    /** Manufacture of Clothes-.*/
    MNU_CLOTHES_MINUS,
    /** Manufacture of Clothes+.*/
    MNU_CLOTHES_PLUS,
    /** Manufacture of Salt-.*/
    MNU_SALT_MINUS,
    /** Manufacture of Salt+.*/
    MNU_SALT_PLUS,
    /** Manufacture of Wine-.*/
    MNU_WINE_MINUS,
    /** Manufacture of Wine+.*/
    MNU_WINE_PLUS,

    // DIPLOMATIC COUNTERS //

    /** Diplomacy with ROTW relation.*/
    ROTW_RELATION,
    /** Diplomacy with ROTW alliance.*/
    ROTW_ALLIANCE,
    /** Diplomacy with European minor country.*/
    DIPLOMACY,

    // NEUTRAL COUNTERS //

    /** Revolt-.*/
    REVOLT_MINUS,
    /** Revolt+.*/
    REVOLT_PLUS,
    /** Gold mine.*/
    GOLD_MINE,
    /** Gold mine depleted.*/
    GOLD_DEPLETED,
    /** Pillage-.*/
    PILLAGE_MINUS,
    /** Pillage+.*/
    PILLAGE_PLUS,
    /** SiegeWork-.*/
    SIEGEWORK_MINUS,
    /** SiegeWork+.*/
    SIEGEWORK_PLUS,
    /** Dutch flood.*/
    FLOOD,
    /** Turn.*/
    TURN,
    /** Good weath (military rounds).*/
    GOOD_WEATHER,
    /** Bad weather (military rounds).*/
    BAD_WEATHER,
    /** Great orient trace center.*/
    TRADE_CENTER_GREAT_ORIENT,
    /** Mediterannean trade center.*/
    TRADE_CENTER_MEDITERANNEAN,
    /** Atlantic trade center.*/
    TRADE_CENTER_ATLANTIC,
    /** Indian trade center.*/
    TRADE_CENTER_INDIAN,

    // EXOTIC RESSOURCES //

    /** Exotic resource price of spice.*/
    SPICE_PRICE,
    /** Exotic resource production of spice.*/
    SPICE_PRODUCTION,
    /** Exotic resource price of sugar.*/
    SUGAR_PRICE,
    /** Exotic resource production of sugar.*/
    SUGAR_PRODUCTION,
    /** Exotic resource price of fish.*/
    FISH_PRICE,
    /** Exotic resource production of fish.*/
    FISH_PRODUCTION,
    /** Exotic resource price of products of america.*/
    PRODUCTS_OF_AMERICA_PRICE,
    /** Exotic resource production of products of america.*/
    PRODUCTS_OF_AMERICA_PRODUCTION,
    /** Exotic resource price of silk.*/
    SILK_PRICE,
    /** Exotic resource production of silk.*/
    SILK_PRODUCTION,
    /** Exotic resource price of salt.*/
    SALT_PRICE,
    /** Exotic resource production of salt.*/
    SALT_PRODUCTION,
    /** Exotic resource price of cotton.*/
    COTTON_PRICE,
    /** Exotic resource production of cotton.*/
    COTTON_PRODUCTION,
    /** Exotic resource price of furs.*/
    FURS_PRICE,
    /** Exotic resource production of furs.*/
    FURS_PRODUCTION,
    /** Exotic resource price of products of orient.*/
    PRODUCTS_OF_ORIENT_PRICE,
    /** Exotic resource production of products of orient.*/
    PRODUCTS_OF_ORIENT_PRODUCTION,
    /** Exotic resource price of slave.*/
    SLAVE_PRICE,
    /** Exotic resource production of slave.*/
    SLAVE_PRODUCTION,
    /** Inflation counter.*/
    INFLATION,
    /** Inflation counter with gold from America.*/
    INFLATION_GOLD,

    // TECHNOLOGY COUNTERS //

    /** Land technology Renaissance.*/
    TECH_RENAISSANCE,
    /** Land technology Tercios (special spanish tech).*/
    TECH_TERCIOS,
    /** Land technology Arquebus.*/
    TECH_ARQUEBUS,
    /** Land technology Musket.*/
    TECH_MUSKET,
    /** Land technology Baroque.*/
    TECH_BAROQUE,
    /** Land technology Manoeuvre.*/
    TECH_MANOEUVRE,
    /** Land technology Lace War.*/
    TECH_LACE_WAR,
    /** Naval technology New Galeon.*/
    TECH_NAE_GALEON,
    /** Naval technology Galleon fleet.*/
    TECH_GALLEON_FLUYT,
    /** Naval technology Galleas (special Venetian tech).*/
    TECH_GALLEASS,
    /** Naval technology Battery.*/
    TECH_BATTERY,
    /** Naval technology Vessek.*/
    TECH_VESSEL,
    /** Naval technology 3 Decker.*/
    TECH_THREE_DECKER,
    /** Naval technology 74.*/
    TECH_SEVENTY_FOUR,
    /** Land technology of a country.*/
    TECH_LAND,
    /** Naval technology of a country.*/
    TECH_NAVAL,
    /** Land technology of asia group.*/
    TECH_LAND_ASIA,
    /** Naval technology of asia group.*/
    TECH_NAVAL_ASIA,
    /** Land technology of islam group.*/
    TECH_LAND_ISLAM,
    /** Naval technology of islam group.*/
    TECH_NAVAL_ISLAM,
    /** Land technology of latin group.*/
    TECH_LAND_LATIN,
    /** Naval technology of latin group.*/
    TECH_NAVAL_LATIN,
    /** Land technology of orthodox group.*/
    TECH_LAND_ORTHODOX,
    /** Naval technology of orthodox group.*/
    TECH_NAVAL_ORTHODOX,


    // STABILITY COUNTERS //

    /** Stability.*/
    STABILITY,

    // ROTW COUNTERS



    // ORMUS_SILK, KARNATAKA_1, DOS_DE_FORT, PERSIA_AREA,, BISINAGAR_AREA,
    // GUZARATE_AREA, NO_RUSSIAN_CTZ, BANGLA_1, SUND_TAXES, REVOLUTIONNAIRES,
    // FREE_TRADE, BANGLIA_1, RUS_STZ, MOGOLIS_AREA, MOGOLIS_LOST_AREA, CHINA_AREA,
    //
}
