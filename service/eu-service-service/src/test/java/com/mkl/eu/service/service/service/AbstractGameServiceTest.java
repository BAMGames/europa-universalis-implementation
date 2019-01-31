package com.mkl.eu.service.service.service;

import com.mkl.eu.client.common.exception.FunctionalException;
import com.mkl.eu.client.common.exception.IConstantsCommonException;
import com.mkl.eu.client.common.exception.TechnicalException;
import com.mkl.eu.client.common.vo.GameInfo;
import com.mkl.eu.client.common.vo.Request;
import com.mkl.eu.client.service.service.IConstantsServiceException;
import com.mkl.eu.client.service.vo.diff.Diff;
import com.mkl.eu.client.service.vo.enumeration.CounterFaceTypeEnum;
import com.mkl.eu.client.service.vo.enumeration.DiffAttributeTypeEnum;
import com.mkl.eu.client.service.vo.enumeration.GameStatusEnum;
import com.mkl.eu.client.service.vo.tables.BattleTech;
import com.mkl.eu.client.service.vo.tables.Tables;
import com.mkl.eu.client.service.vo.tables.Tech;
import com.mkl.eu.service.service.mapping.diff.DiffMapping;
import com.mkl.eu.service.service.persistence.IGameDao;
import com.mkl.eu.service.service.persistence.diff.IDiffDao;
import com.mkl.eu.service.service.persistence.oe.GameEntity;
import com.mkl.eu.service.service.persistence.oe.board.CounterEntity;
import com.mkl.eu.service.service.persistence.oe.country.PlayableCountryEntity;
import com.mkl.eu.service.service.persistence.oe.diff.DiffAttributesEntity;
import com.mkl.eu.service.service.persistence.oe.diff.DiffEntity;
import com.mkl.eu.service.service.persistence.oe.diplo.CountryOrderEntity;
import com.mkl.eu.service.service.socket.SocketHandler;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.Pair;
import org.junit.Assert;
import org.mockito.Mock;

import java.util.ArrayList;
import java.util.List;

import static org.mockito.Matchers.anyObject;
import static org.mockito.Mockito.when;

/**
 * Utility methods for testing code in AbstractService.
 *
 * @author MKL.
 */
public abstract class AbstractGameServiceTest {
    protected static final Long GAME_ID = 12L;
    protected static final Long VERSION_SINCE = 1L;

    @Mock
    protected IGameDao gameDao;

    @Mock
    protected IDiffDao diffDao;

    @Mock
    protected DiffMapping diffMapping;

    @Mock
    protected SocketHandler socketHandler;

    /** Variable used to store something coming from a mock. */
    private List<DiffEntity> diffEntities;

    private static List<Diff> diffAfter;

    static {
        diffAfter = new ArrayList<>();
        diffAfter.add(new Diff());
        diffAfter.add(new Diff());
    }

    protected GameInfo createGameInfo() {
        GameInfo gameInfo = new GameInfo();
        gameInfo.setIdGame(GAME_ID);
        gameInfo.setVersionGame(VERSION_SINCE);

        return gameInfo;
    }

    protected GameEntity createGameUsingMocks() {
        GameEntity game = new GameEntity();
        game.setId(GAME_ID);
        game.setVersion(5L);
        when(gameDao.lock(GAME_ID)).thenReturn(game);

        return game;
    }

    protected GameEntity createGameUsingMocks(GameStatusEnum gameStatus, Long... idCountries) {
        GameEntity game = createGameUsingMocks();

        game.setStatus(gameStatus);
        for (Long idCountry : idCountries) {
            CountryOrderEntity order = new CountryOrderEntity();
            order.setGameStatus(gameStatus);
            order.setCountry(new PlayableCountryEntity());
            order.getCountry().setId(idCountry);
            order.setActive(true);
            game.getOrders().add(order);
        }

        return game;
    }

    protected void simulateDiff() {
        List<DiffEntity> diffBefore = new ArrayList<>();
        diffBefore.add(new DiffEntity());
        diffBefore.add(new DiffEntity());

        when(diffDao.getDiffsSince(GAME_ID, VERSION_SINCE)).thenReturn(diffBefore);

        when(diffMapping.oesToVos(anyObject())).thenAnswer(invocation -> {
            List<DiffEntity> diffs = invocation.getArgumentAt(0, List.class);
            if (diffs == null) {
                return null;
            }
            diffEntities = diffs.subList(2, diffs.size());
            return diffAfter;
        });
    }

    protected List<DiffEntity> retrieveDiffsCreated() {
        return diffEntities;
    }

    protected DiffEntity retrieveDiffCreated() {
        return !diffEntities.isEmpty() ? diffEntities.get(0) : null;
    }

    protected List<Diff> getDiffAfter() {
        return diffAfter;
    }

    public <V> Pair<Request<V>, GameEntity> testCheckGame(IServiceWithCheckGame<V> service, String method) {
        when(gameDao.lock(GAME_ID)).thenReturn(null);

        try {
            service.run(null);
            Assert.fail("Should break because " + method + " is null");
        } catch (FunctionalException e) {
            Assert.assertEquals(IConstantsCommonException.NULL_PARAMETER, e.getCode());
            Assert.assertEquals(method, e.getParams()[0]);
        }

        Request<V> request = new Request<>();

        try {
            service.run(request);
            Assert.fail("Should break because " + method + ".game is null");
        } catch (FunctionalException e) {
            Assert.assertEquals(IConstantsCommonException.NULL_PARAMETER, e.getCode());
            Assert.assertEquals(method + ".game", e.getParams()[0]);
        }

        request.setGame(new GameInfo());

        try {
            service.run(request);
            Assert.fail("Should break because " + method + " is null");
            Assert.fail("Should break because idGame is null");
        } catch (FunctionalException e) {
            Assert.assertEquals(IConstantsCommonException.NULL_PARAMETER, e.getCode());
            Assert.assertEquals(method + ".game.idGame", e.getParams()[0]);
        }

        request.getGame().setIdGame(GAME_ID);

        try {
            service.run(request);
            Assert.fail("Should break because versionGame is null");
        } catch (FunctionalException e) {
            Assert.assertEquals(IConstantsCommonException.NULL_PARAMETER, e.getCode());
            Assert.assertEquals(method + ".game.versionGame", e.getParams()[0]);
        }

        request.getGame().setVersionGame(1L);

        try {
            service.run(request);
            Assert.fail("Should break because game does not exist");
        } catch (FunctionalException e) {
            Assert.assertEquals(IConstantsCommonException.INVALID_PARAMETER, e.getCode());
            Assert.assertEquals(method + ".game.idGame", e.getParams()[0]);
        }

        GameEntity game = new GameEntity();
        game.setId(GAME_ID);

        when(gameDao.lock(GAME_ID)).thenReturn(game);

        try {
            service.run(request);
            Assert.fail("Should break because versions does not match");
        } catch (FunctionalException e) {
            Assert.assertEquals(IConstantsCommonException.INVALID_PARAMETER, e.getCode());
            Assert.assertEquals(method + ".game.versionGame", e.getParams()[0]);
        }

        game.setVersion(5L);

        return new ImmutablePair<>(request, game);
    }

    protected <V> void testCheckStatus(GameEntity game, Request<V> request, IServiceWithCheckGame<V> service, String method, GameStatusEnum status) {
        try {
            service.run(request);
            Assert.fail("Should break because game.status is invalid");
        } catch (FunctionalException e) {
            Assert.assertEquals(IConstantsServiceException.INVALID_STATUS, e.getCode());
            Assert.assertEquals(method + ".request", e.getParams()[0]);
        }

        GameStatusEnum wrongStatus = GameStatusEnum.MILITARY_HIERARCHY;
        if (status == GameStatusEnum.MILITARY_HIERARCHY) {
            wrongStatus = GameStatusEnum.ADMINISTRATIVE_ACTIONS_CHOICE;
        }
        game.setStatus(wrongStatus);

        try {
            service.run(request);
            Assert.fail("Should break because game.status is invalid");
        } catch (FunctionalException e) {
            Assert.assertEquals(IConstantsServiceException.INVALID_STATUS, e.getCode());
            Assert.assertEquals(method + ".request", e.getParams()[0]);
        }

        game.setStatus(status);
        if (request.getIdCountry() != null) {
            try {
                service.run(request);
                Assert.fail("Should break because game.status is invalid");
            } catch (FunctionalException e) {
                Assert.assertEquals(IConstantsServiceException.INVALID_STATUS, e.getCode());
                Assert.assertEquals(method + ".request", e.getParams()[0]);
            }

            CountryOrderEntity order = new CountryOrderEntity();
            order.setGameStatus(wrongStatus);
            order.setCountry(new PlayableCountryEntity());
            order.getCountry().setId(request.getIdCountry());
            order.setActive(true);
            game.getOrders().add(order);
            order = new CountryOrderEntity();
            order.setGameStatus(getOrderStatus(status));
            order.setCountry(new PlayableCountryEntity());
            order.getCountry().setId(request.getIdCountry() * 2);
            order.setActive(true);
            game.getOrders().add(order);
            order = new CountryOrderEntity();
            order.setGameStatus(getOrderStatus(status));
            order.setCountry(new PlayableCountryEntity());
            order.getCountry().setId(request.getIdCountry());
            order.setActive(false);
            game.getOrders().add(order);

            try {
                service.run(request);
                Assert.fail("Should break because game.status is invalid");
            } catch (FunctionalException e) {
                Assert.assertEquals(IConstantsServiceException.INVALID_STATUS, e.getCode());
                Assert.assertEquals(method + ".request", e.getParams()[0]);
            }

            order.setActive(true);
            game.setStatus(wrongStatus);

            try {
                service.run(request);
                Assert.fail("Should break because game.status is invalid");
            } catch (FunctionalException e) {
                Assert.assertEquals(IConstantsServiceException.INVALID_STATUS, e.getCode());
                Assert.assertEquals(method + ".request", e.getParams()[0]);
            }

            game.setStatus(status);
        }
    }

    private GameStatusEnum getOrderStatus(GameStatusEnum status) {
        switch (status) {
            case MILITARY_CAMPAIGN:
            case MILITARY_SUPPLY:
            case MILITARY_MOVE:
            case MILITARY_BATTLES:
            case MILITARY_SIEGES:
            case MILITARY_NEUTRALS:
                return GameStatusEnum.MILITARY_MOVE;
            default:
                return status;
        }
    }

    protected interface IServiceWithCheckGame<V> {
        void run(Request<V> request) throws TechnicalException, FunctionalException;
    }

    protected void fillBatleTechTables(Tables tables) {
        BattleTech battleTech = new BattleTech();
        battleTech.setTechnologyFor(Tech.ARQUEBUS);
        battleTech.setTechnologyAgainst(Tech.RENAISSANCE);
        battleTech.setColumnFire("C");
        battleTech.setColumnShock("A");
        battleTech.setLand(true);
        battleTech.setMoral(2);
        battleTech.setMoralBonusVeteran(true);
        tables.getBattleTechs().add(battleTech);
        battleTech = new BattleTech();
        battleTech.setTechnologyFor(Tech.RENAISSANCE);
        battleTech.setTechnologyAgainst(Tech.ARQUEBUS);
        battleTech.setColumnFire("C");
        battleTech.setColumnShock("B");
        battleTech.setLand(true);
        battleTech.setMoral(2);
        battleTech.setMoralBonusVeteran(true);
        tables.getBattleTechs().add(battleTech);

        BattleTech bt = new BattleTech();
        bt.setTechnologyFor(Tech.RENAISSANCE);
        bt.setTechnologyAgainst(Tech.RENAISSANCE);
        bt.setLand(true);
        bt.setColumnFire("C");
        bt.setColumnShock("A");
        bt.setMoral(2);
        bt.setMoralBonusVeteran(true);
        tables.getBattleTechs().add(bt);
        bt = new BattleTech();
        bt.setTechnologyFor(Tech.RENAISSANCE);
        bt.setTechnologyAgainst(Tech.MEDIEVAL);
        bt.setLand(true);
        bt.setColumnFire("C");
        bt.setColumnShock("A");
        bt.setMoral(1);
        bt.setMoralBonusVeteran(true);
        tables.getBattleTechs().add(bt);
        bt = new BattleTech();
        bt.setTechnologyFor(Tech.MEDIEVAL);
        bt.setTechnologyAgainst(Tech.RENAISSANCE);
        bt.setLand(true);
        bt.setColumnShock("B");
        bt.setMoral(1);
        bt.setMoralBonusVeteran(true);
        tables.getBattleTechs().add(bt);
        bt = new BattleTech();
        bt.setTechnologyFor(Tech.ARQUEBUS);
        bt.setTechnologyAgainst(Tech.ARQUEBUS);
        bt.setLand(true);
        bt.setColumnFire("C");
        bt.setColumnShock("B");
        bt.setMoral(2);
        bt.setMoralBonusVeteran(true);
        tables.getBattleTechs().add(bt);
        bt = new BattleTech();
        bt.setTechnologyFor(Tech.MUSKET);
        bt.setTechnologyAgainst(Tech.MUSKET);
        bt.setLand(true);
        bt.setColumnFire("C");
        bt.setColumnShock("B");
        bt.setMoral(3);
        bt.setMoralBonusVeteran(true);
        tables.getBattleTechs().add(bt);
        bt = new BattleTech();
        bt.setTechnologyFor(Tech.ARQUEBUS);
        bt.setTechnologyAgainst(Tech.MUSKET);
        bt.setLand(true);
        bt.setColumnFire("C");
        bt.setColumnShock("B");
        bt.setMoral(2);
        bt.setMoralBonusVeteran(true);
        tables.getBattleTechs().add(bt);
        bt = new BattleTech();
        bt.setTechnologyFor(Tech.MUSKET);
        bt.setTechnologyAgainst(Tech.ARQUEBUS);
        bt.setLand(true);
        bt.setColumnFire("B");
        bt.setColumnShock("B");
        bt.setMoral(3);
        bt.setMoralBonusVeteran(true);
        tables.getBattleTechs().add(bt);
    }

    public static CounterEntity createCounter(Long id, String country, CounterFaceTypeEnum type) {
        CounterEntity counter = new CounterEntity();
        counter.setId(id);
        counter.setCountry(country);
        counter.setType(type);
        return counter;
    }

    public static String getAttribute(DiffEntity diff, DiffAttributeTypeEnum type) {
        return diff.getAttributes().stream()
                .filter(attribute -> attribute.getType() == type)
                .map(DiffAttributesEntity::getValue)
                .findAny()
                .orElse(null);
    }
}
