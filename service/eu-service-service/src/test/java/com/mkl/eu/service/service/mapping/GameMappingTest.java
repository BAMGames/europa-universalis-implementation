package com.mkl.eu.service.service.mapping;

import com.mkl.eu.client.service.vo.Game;
import com.mkl.eu.client.service.vo.board.Counter;
import com.mkl.eu.client.service.vo.board.Stack;
import com.mkl.eu.client.service.vo.country.Country;
import com.mkl.eu.client.service.vo.enumeration.CounterTypeEnum;
import com.mkl.eu.client.service.vo.enumeration.GameStatusEnum;
import com.mkl.eu.client.service.vo.enumeration.RelationTypeEnum;
import com.mkl.eu.client.service.vo.enumeration.TerrainEnum;
import com.mkl.eu.client.service.vo.event.PoliticalEvent;
import com.mkl.eu.client.service.vo.player.Player;
import com.mkl.eu.client.service.vo.player.Relation;
import com.mkl.eu.service.service.persistence.oe.GameEntity;
import com.mkl.eu.service.service.persistence.oe.board.CounterEntity;
import com.mkl.eu.service.service.persistence.oe.board.EuropeanProvinceEntity;
import com.mkl.eu.service.service.persistence.oe.board.StackEntity;
import com.mkl.eu.service.service.persistence.oe.country.CountryEntity;
import com.mkl.eu.service.service.persistence.oe.event.PoliticalEventEntity;
import com.mkl.eu.service.service.persistence.oe.player.PlayerEntity;
import com.mkl.eu.service.service.persistence.oe.player.RelationEntity;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.unitils.reflectionassert.ReflectionAssert;

import java.util.ArrayList;
import java.util.List;

/**
 * Test of GameMapping.
 *
 * @author MKL.
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration({"/com/mkl/eu/service/service/eu-service-service-applicationContext-test.xml"})
public class GameMappingTest {
    private static final Country FRA_VO;
    private static final Country PRU_VO;
    private static final CountryEntity FRA_OE;
    private static final CountryEntity PRU_OE;
    private static final String PECS_VO = "Pecs";
    private static final String TYR_VO = "Tyr";
    private static final String IDF_VO = "IdF";
    private static final EuropeanProvinceEntity PECS_OE;
    private static final EuropeanProvinceEntity TYR_OE;
    private static final EuropeanProvinceEntity IDF_OE;
    @Autowired
    private GameMapping gameMapping;

    static {
        FRA_VO = new Country();
        FRA_VO.setId(1L);
        FRA_VO.setName("FRA");

        PRU_VO = new Country();
        PRU_VO.setId(2L);
        PRU_VO.setName("PRU");

        FRA_OE = new CountryEntity();
        FRA_OE.setId(1L);
        FRA_OE.setName("FRA");

        PRU_OE = new CountryEntity();
        PRU_OE.setId(2L);
        PRU_OE.setName("PRU");

        PECS_OE = new EuropeanProvinceEntity();
        PECS_OE.setId(1L);
        PECS_OE.setName("Pecs");
        PECS_OE.setDefaultOwner(FRA_OE);
        PECS_OE.setIncome(5);
        PECS_OE.setPort(true);
        PECS_OE.setPraesidiable(false);
        PECS_OE.setTerrain(TerrainEnum.DENSE_FOREST);

        TYR_OE = new EuropeanProvinceEntity();
        TYR_OE.setId(2L);
        TYR_OE.setName("Tyr");
        TYR_OE.setDefaultOwner(FRA_OE);
        TYR_OE.setIncome(2);
        TYR_OE.setPort(null);
        TYR_OE.setPraesidiable(true);
        TYR_OE.setTerrain(TerrainEnum.PLAIN);

        IDF_OE = new EuropeanProvinceEntity();
        IDF_OE.setId(3L);
        IDF_OE.setName("IdF");
        IDF_OE.setDefaultOwner(FRA_OE);
        IDF_OE.setIncome(18);
        IDF_OE.setPort(false);
        IDF_OE.setPraesidiable(null);
        IDF_OE.setTerrain(TerrainEnum.SEA);
    }

    @Test
    public void testVoidGameMapping() {
        Game vo = gameMapping.oeToVo(null);

        Assert.assertNull(vo);

        GameEntity entity = new GameEntity();

        vo = gameMapping.oeToVo(entity);

        ReflectionAssert.assertReflectionEquals(new Game(), vo);
    }

    @Test
    public void testFullGameMapping() {
        GameEntity entity = createGameEntity();

        Game vo = gameMapping.oeToVo(entity);

        ReflectionAssert.assertReflectionEquals(createGameVo(), vo);

    }

    private Game createGameVo() {
        Game object = new Game();

        object.setId(12L);
        object.setStatus(GameStatusEnum.ECONOMICAL_EVENT);
        object.setTurn(1);
        object.setVersion(15L);

        object.getCountries().add(FRA_VO);
        object.getCountries().add(PRU_VO);

        object.setEvents(createEventsVos());

        object.setPlayers(createPlayersVos());

        object.setRelations(createRelationsVos(object.getPlayers().get(0), object.getPlayers().get(0), object.getPlayers().get(0)));

        object.setStacks(createStacksVos());

        return object;
    }

    private List<PoliticalEvent> createEventsVos() {
        List<PoliticalEvent> objects = new ArrayList<>();

        PoliticalEvent object = new PoliticalEvent();
        object.setId(1L);
        object.setTurn(1);
        objects.add(object);
        object = new PoliticalEvent();
        object.setId(2L);
        object.setTurn(3);
        objects.add(object);
        object = new PoliticalEvent();
        object.setId(3L);
        object.setTurn(5);
        objects.add(object);

        return objects;
    }

    private List<Player> createPlayersVos() {
        List<Player> objects = new ArrayList<>();

        Player object = new Player();
        object.setId(1L);
        object.setCountry(FRA_VO);
        objects.add(object);
        object = new Player();
        object.setId(2L);
        objects.add(object);
        object = new Player();
        object.setId(3L);
        objects.add(object);

        return objects;
    }

    private List<Relation> createRelationsVos(Player first, Player second, Player third) {
        List<Relation> objects = new ArrayList<>();

        Relation object = new Relation();
        object.setId(1L);
        object.setType(RelationTypeEnum.ALLIANCE);
        object.setFirst(first);
        object.setSecond(second);
        objects.add(object);
        object = new Relation();
        object.setId(2L);
        object.setType(RelationTypeEnum.WAR);
        object.setFirst(second);
        object.setSecond(third);
        objects.add(object);

        return objects;
    }

    private List<Stack> createStacksVos() {
        List<Stack> objects = new ArrayList<>();

        Stack object = new Stack();
        object.setId(1L);
        object.setProvince(PECS_VO);
        List<Counter> subObjects = new ArrayList<>();
        Counter subObject = new Counter();
        subObject.setId(1L);
        subObject.setType(CounterTypeEnum.ARMY_MINUS);
        subObject.setCountry(FRA_VO);
        subObject.setOwner(object);
        subObjects.add(subObject);
        subObject = new Counter();
        subObject.setId(2L);
        subObject.setType(CounterTypeEnum.ARMY_PLUS);
        subObject.setCountry(PRU_VO);
        subObject.setOwner(object);
        subObjects.add(subObject);
        object.setCounters(subObjects);
        objects.add(object);
        object = new Stack();
        object.setId(2L);
        object.setProvince(TYR_VO);
        subObjects = new ArrayList<>();
        subObject = new Counter();
        subObject.setId(3L);
        subObject.setType(CounterTypeEnum.MNU_ART_MINUS);
        subObject.setCountry(FRA_VO);
        subObject.setOwner(object);
        subObjects.add(subObject);
        object.setCounters(subObjects);
        objects.add(object);
        object = new Stack();
        object.setId(3L);
        object.setProvince(IDF_VO);
        objects.add(object);

        return objects;
    }

    private GameEntity createGameEntity() {
        GameEntity object = new GameEntity();

        object.setId(12L);
        object.setStatus(GameStatusEnum.ECONOMICAL_EVENT);
        object.setTurn(1);
        object.setVersion(15L);

        object.getCountries().add(FRA_OE);
        object.getCountries().add(PRU_OE);

        object.setEvents(createEventsEntities());

        object.setPlayers(createPlayersEntities());

        object.setRelations(createRelationsEntities(object.getPlayers().get(0), object.getPlayers().get(0), object.getPlayers().get(0)));

        object.setStacks(createStacksEntities());

        return object;
    }

    private List<PoliticalEventEntity> createEventsEntities() {
        List<PoliticalEventEntity> objects = new ArrayList<>();

        PoliticalEventEntity object = new PoliticalEventEntity();
        object.setId(1L);
        object.setTurn(1);
        objects.add(object);
        object = new PoliticalEventEntity();
        object.setId(2L);
        object.setTurn(3);
        objects.add(object);
        object = new PoliticalEventEntity();
        object.setId(3L);
        object.setTurn(5);
        objects.add(object);

        return objects;
    }

    private List<PlayerEntity> createPlayersEntities() {
        List<PlayerEntity> objects = new ArrayList<>();

        PlayerEntity object = new PlayerEntity();
        object.setId(1L);
        object.setCountry(FRA_OE);
        objects.add(object);
        object = new PlayerEntity();
        object.setId(2L);
        objects.add(object);
        object = new PlayerEntity();
        object.setId(3L);
        objects.add(object);

        return objects;
    }

    private List<RelationEntity> createRelationsEntities(PlayerEntity first, PlayerEntity second, PlayerEntity third) {
        List<RelationEntity> objects = new ArrayList<>();

        RelationEntity object = new RelationEntity();
        object.setId(1L);
        object.setType(RelationTypeEnum.ALLIANCE);
        object.setFirst(first);
        object.setSecond(second);
        objects.add(object);
        object = new RelationEntity();
        object.setId(2L);
        object.setType(RelationTypeEnum.WAR);
        object.setFirst(second);
        object.setSecond(third);
        objects.add(object);

        return objects;
    }

    private List<StackEntity> createStacksEntities() {
        List<StackEntity> objects = new ArrayList<>();

        StackEntity object = new StackEntity();
        object.setId(1L);
        object.setProvince(PECS_OE);
        List<CounterEntity> subObjects = new ArrayList<>();
        CounterEntity subObject = new CounterEntity();
        subObject.setId(1L);
        subObject.setType(CounterTypeEnum.ARMY_MINUS);
        subObject.setCountry(FRA_OE);
        subObject.setOwner(object);
        subObjects.add(subObject);
        subObject = new CounterEntity();
        subObject.setId(2L);
        subObject.setType(CounterTypeEnum.ARMY_PLUS);
        subObject.setCountry(PRU_OE);
        subObject.setOwner(object);
        subObjects.add(subObject);
        object.setCounters(subObjects);
        objects.add(object);
        object = new StackEntity();
        object.setId(2L);
        object.setProvince(TYR_OE);
        subObjects = new ArrayList<>();
        subObject = new CounterEntity();
        subObject.setId(3L);
        subObject.setType(CounterTypeEnum.MNU_ART_MINUS);
        subObject.setCountry(FRA_OE);
        subObject.setOwner(object);
        subObjects.add(subObject);
        object.setCounters(subObjects);
        objects.add(object);
        object = new StackEntity();
        object.setId(3L);
        object.setProvince(IDF_OE);
        objects.add(object);

        return objects;
    }
}
