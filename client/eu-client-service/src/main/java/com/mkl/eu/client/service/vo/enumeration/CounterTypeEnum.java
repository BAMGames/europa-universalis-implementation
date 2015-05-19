package com.mkl.eu.client.service.vo.enumeration;

/**
 * Type of the face of a counter (in progress).
 *
 * @author MKL
 */
public enum CounterTypeEnum {
    /** Army counter. */
    ARMY(CounterFaceTypeEnum.ARMY_PLUS, CounterFaceTypeEnum.ARMY_MINUS),
    /** Army Timar counter (for Turkey). */
    ARMY_TIMAR(CounterFaceTypeEnum.ARMY_TIMAR_PLUS, CounterFaceTypeEnum.ARMY_TIMAR_MINUS),
    /** Fleet counter. */
    FLEET(CounterFaceTypeEnum.FLEET_PLUS, CounterFaceTypeEnum.FLEET_MINUS),
    /** Transport fleet counter. */
    FLEET_TRANSPORT(CounterFaceTypeEnum.FLEET_TRANSPORT_PLUS, CounterFaceTypeEnum.FLEET_TRANSPORT_MINUS),
    /** LD / ND counter. */
    LDND(CounterFaceTypeEnum.LAND_DETACHMENT, CounterFaceTypeEnum.NAVAL_DETACHMENT),
    /** LD Timar (turkish) / ND counter. */
    LDND_TIMAR(CounterFaceTypeEnum.LAND_DETACHMENT_TIMAR, CounterFaceTypeEnum.NAVAL_DETACHMENT),
    /** LD counter alone. */
    LD(CounterFaceTypeEnum.LAND_DETACHMENT, null),
    /** LD Timar (turkish) counter alone. */
    LD_TIMAR(CounterFaceTypeEnum.LAND_DETACHMENT_TIMAR, null),
    /** LD Kozak (russian) counter alone. */
    LD_KOZAK(CounterFaceTypeEnum.LAND_DETACHMENT_KOZAK, null),
    /** LDE / NDE counter. */
    LDENDE(CounterFaceTypeEnum.LAND_DETACHMENT_EXPLORATION, CounterFaceTypeEnum.NAVAL_DETACHMENT_EXPLORATION),
    /** LDE counter alone. */
    LDE(CounterFaceTypeEnum.LAND_DETACHMENT_EXPLORATION, null),
    /** LDE Kozak (russian) counter alone. */
    LDE_KOZAK(CounterFaceTypeEnum.LAND_DETACHMENT_EXPLORATION_KOZAK, null),
    /** NDE counter alone. */
    NDE(CounterFaceTypeEnum.NAVAL_DETACHMENT_EXPLORATION, null),
    /** NTD counter alone. */
    NTD(CounterFaceTypeEnum.NAVAL_TRANSPORT, null),
    /** Pirate or corsair counter. */
    PIRATE(CounterFaceTypeEnum.PIRATE_PLUS, CounterFaceTypeEnum.PIRATE_MINUS),
    /** Trading post counter. */
    TP(CounterFaceTypeEnum.TRADING_POST_PLUS, CounterFaceTypeEnum.TRADING_POST_MINUS),
    /** Colony counter. */
    COL(CounterFaceTypeEnum.COLONY_PLUS, CounterFaceTypeEnum.COLONY_MINUS),
    /** Fortress 1/2. */
    FORT12(CounterFaceTypeEnum.FORTRESS_1, CounterFaceTypeEnum.FORTRESS_2),
    /** Fortress 2/3. */
    FORT23(CounterFaceTypeEnum.FORTRESS_2, CounterFaceTypeEnum.FORTRESS_3),
    /** Fortress 3/4. */
    FORT34(CounterFaceTypeEnum.FORTRESS_3, CounterFaceTypeEnum.FORTRESS_4),
    /** Fortress 4/5. */
    FORT45(CounterFaceTypeEnum.FORTRESS_4, CounterFaceTypeEnum.FORTRESS_5),
    /** Fort. */
    FORT(CounterFaceTypeEnum.FORT, null),
    /** Arsenal 2/3. */
    ARS23(CounterFaceTypeEnum.ARSENAL_2, CounterFaceTypeEnum.ARSENAL_3),
    /** Arsenal 2/3 at Gibraltar (english player). */
    ARS23_GIBRALTAR(CounterFaceTypeEnum.ARSENAL_2_GIBRALTAR, CounterFaceTypeEnum.ARSENAL_3_GIBRALTAR),
    /** Arsenal 2/3 at Sebastopol (russian player). */
    ARS23_SEBASTOPOL(CounterFaceTypeEnum.ARSENAL_2_SEBASTOPOL, CounterFaceTypeEnum.ARSENAL_3_SEBASTOPOL),
    /** Arsenal 0/1 at St-Petersburg (russian player). */
    ARS01_ST_PETER(CounterFaceTypeEnum.ARSENAL_0_ST_PETER, CounterFaceTypeEnum.ARSENAL_1_ST_PETER),
    /** Arsenal 2/3 at St-Petersburg (russian player). */
    ARS23_ST_PETER(CounterFaceTypeEnum.ARSENAL_2_ST_PETER, CounterFaceTypeEnum.ARSENAL_3_ST_PETER),
    /** Arsenal 4/5 at St-Petersburg (russian player). */
    ARS45_ST_PETER(CounterFaceTypeEnum.ARSENAL_4_ST_PETER, CounterFaceTypeEnum.ARSENAL_5_ST_PETER),
    /** Arsenal 3/4. */
    ARS34(CounterFaceTypeEnum.ARSENAL_3, CounterFaceTypeEnum.ARSENAL_4),
    /** Missions. */
    MISSION(CounterFaceTypeEnum.MISSION, CounterFaceTypeEnum.MISSIONNARY),
    /** Sepoy. */
    SEPOY(CounterFaceTypeEnum.LAND_SEPOY, null),
    /** Sepoy exploration. */
    SEPOY_EXPLORATION(CounterFaceTypeEnum.LAND_SEPOY_EXPLORATION, null),
    /** Indian. */
    INDIAN(CounterFaceTypeEnum.LAND_INDIAN, null),
    /** Indian exploration. */
    INDIAN_EXPLORATION(CounterFaceTypeEnum.LAND_INDIAN_EXPLORATION, null),
    /** Trading fleet. */
    TF(CounterFaceTypeEnum.TRADING_FLEET_PLUS, CounterFaceTypeEnum.TRADING_FLEET_MINUS),
    /** Rotw diplomatic counter. */
    ROTW_DIPLO(CounterFaceTypeEnum.ROTW_ALLIANCE, CounterFaceTypeEnum.ROTW_RELATION),
    /** Manufacture of Art. */
    MNU_ART(CounterFaceTypeEnum.MNU_ART_PLUS, CounterFaceTypeEnum.MNU_ART_MINUS),
    /** Manufacture of Cereales. */
    MNU_CEREALS(CounterFaceTypeEnum.MNU_CEREALS_PLUS, CounterFaceTypeEnum.MNU_CEREALS_MINUS),
    /** Manufacture of Clothes. */
    MNU_CLOTHES(CounterFaceTypeEnum.MNU_CLOTHES_PLUS, CounterFaceTypeEnum.MNU_CLOTHES_MINUS),
    /** Manufacture of Fish. */
    MNU_FISH(CounterFaceTypeEnum.MNU_FISH_PLUS, CounterFaceTypeEnum.MNU_FISH_MINUS),
    /** Manufacture of Instruments. */
    MNU_INSTRUMENTS(CounterFaceTypeEnum.MNU_INSTRUMENTS_PLUS, CounterFaceTypeEnum.MNU_INSTRUMENTS_MINUS),
    /** Manufacture of Metal. */
    MNU_METAL(CounterFaceTypeEnum.MNU_METAL_PLUS, CounterFaceTypeEnum.MNU_METAL_MINUS),
    /** Manufacture of Metal (special for PRU). */
    MNU_METAL_SCHLESIEN(CounterFaceTypeEnum.MNU_METAL_SCHLESIEN_PLUS, CounterFaceTypeEnum.MNU_METAL_SCHLESIEN_MINUS),
    /** Manufacture of Salt. */
    MNU_SALT(CounterFaceTypeEnum.MNU_SALT_PLUS, CounterFaceTypeEnum.MNU_SALT_MINUS),
    /** Manufacture of Wine. */
    MNU_WINE(CounterFaceTypeEnum.MNU_WINE_PLUS, CounterFaceTypeEnum.MNU_WINE_MINUS),
    /** Manufacture of Wood. */
    MNU_WOOD(CounterFaceTypeEnum.MNU_WOOD_PLUS, CounterFaceTypeEnum.MNU_WOOD_MINUS);


    /** Type of face of recto. */
    private CounterFaceTypeEnum faceRecto;
    /** Type of face of verso. */
    private CounterFaceTypeEnum faceVerso;

    /**
     * Constructor.
     *
     * @param faceRecto type of face of recto.
     * @param faceVerso type of face of verso.
     */
    private CounterTypeEnum(CounterFaceTypeEnum faceRecto, CounterFaceTypeEnum faceVerso) {
        this.faceRecto = faceRecto;
        this.faceVerso = faceVerso;
    }

    /** @return the faceRecto. */
    public CounterFaceTypeEnum getFaceRecto() {
        return faceRecto;
    }

    /** @return the faceVerso. */
    public CounterFaceTypeEnum getFaceVerso() {
        return faceVerso;
    }
}
