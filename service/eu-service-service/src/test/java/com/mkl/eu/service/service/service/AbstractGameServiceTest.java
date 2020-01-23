package com.mkl.eu.service.service.service;

import com.mkl.eu.client.common.exception.FunctionalException;
import com.mkl.eu.client.common.exception.IConstantsCommonException;
import com.mkl.eu.client.common.exception.TechnicalException;
import com.mkl.eu.client.common.vo.GameInfo;
import com.mkl.eu.client.common.vo.Request;
import com.mkl.eu.client.service.service.IConstantsServiceException;
import com.mkl.eu.client.service.util.CounterUtil;
import com.mkl.eu.client.service.vo.diff.Diff;
import com.mkl.eu.client.service.vo.enumeration.*;
import com.mkl.eu.client.service.vo.tables.BattleTech;
import com.mkl.eu.client.service.vo.tables.Leader;
import com.mkl.eu.client.service.vo.tables.Tables;
import com.mkl.eu.client.service.vo.tables.Tech;
import com.mkl.eu.service.service.mapping.diff.DiffMapping;
import com.mkl.eu.service.service.persistence.IGameDao;
import com.mkl.eu.service.service.persistence.diff.IDiffDao;
import com.mkl.eu.service.service.persistence.oe.GameEntity;
import com.mkl.eu.service.service.persistence.oe.board.CounterEntity;
import com.mkl.eu.service.service.persistence.oe.board.StackEntity;
import com.mkl.eu.service.service.persistence.oe.country.PlayableCountryEntity;
import com.mkl.eu.service.service.persistence.oe.diff.DiffAttributesEntity;
import com.mkl.eu.service.service.persistence.oe.diff.DiffEntity;
import com.mkl.eu.service.service.persistence.oe.diplo.CountryOrderEntity;
import com.mkl.eu.service.service.util.DiffUtil;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.Pair;
import org.junit.Assert;
import org.mockito.Mock;
import org.mockito.stubbing.Answer;
import org.springframework.transaction.support.TransactionSynchronizationManager;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static org.mockito.Matchers.*;
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
        game.setVersion(VERSION_SINCE);
        when(gameDao.lock(GAME_ID)).thenReturn(game);
        when(gameDao.load(GAME_ID)).thenReturn(game);

        return game;
    }

    protected GameEntity createGameUsingMocks(GameStatusEnum gameStatus, Long... idCountries) {
        GameEntity game = createGameUsingMocks();

        game.setStatus(gameStatus);
        for (Long idCountry : idCountries) {
            CountryOrderEntity order = new CountryOrderEntity();
            order.setCountry(new PlayableCountryEntity());
            order.getCountry().setId(idCountry);
            order.setActive(true);
            game.getOrders().add(order);
        }

        return game;
    }

    protected void simulateDiff() {
        if (!TransactionSynchronizationManager.isSynchronizationActive()) {
            TransactionSynchronizationManager.initSynchronization();
        }
        List<DiffEntity> diffBefore = new ArrayList<>();
        diffBefore.add(new DiffEntity());
        diffBefore.add(new DiffEntity());

        when(diffDao.getDiffsSince(eq(GAME_ID), anyLong(), eq(VERSION_SINCE))).thenReturn(diffBefore);

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
        when(gameDao.load(GAME_ID)).thenReturn(null);

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
        when(gameDao.load(GAME_ID)).thenReturn(game);

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

    protected <V> void testCheckStatus(GameEntity game, Request<V> request, IServiceWithCheckGame<V> service, String method, GameStatusEnum... statuses) {
        try {
            service.run(request);
            Assert.fail("Should break because game.status is invalid");
        } catch (FunctionalException e) {
            Assert.assertEquals(IConstantsServiceException.INVALID_STATUS, e.getCode());
            Assert.assertEquals(method + ".request", e.getParams()[0]);
        }

        GameStatusEnum wrongStatus = null;
        for (GameStatusEnum status : GameStatusEnum.values()) {
            if (!Arrays.stream(statuses).anyMatch(stat -> stat == status)) {
                wrongStatus = status;
                break;
            }
        }
        game.setStatus(wrongStatus);

        try {
            service.run(request);
            Assert.fail("Should break because game.status is invalid");
        } catch (FunctionalException e) {
            Assert.assertEquals(IConstantsServiceException.INVALID_STATUS, e.getCode());
            Assert.assertEquals(method + ".request", e.getParams()[0]);
        }

        for (GameStatusEnum status : statuses) {
            game.setStatus(status);
            if (!status.isSimultaneous() && request.getGame().getIdCountry() != null) {
                try {
                    service.run(request);
                    Assert.fail("Should break because game.status is invalid");
                } catch (FunctionalException e) {
                    Assert.assertEquals(IConstantsServiceException.INVALID_STATUS, e.getCode());
                    Assert.assertEquals(method + ".request", e.getParams()[0]);
                }

                CountryOrderEntity order = new CountryOrderEntity();
                order.setCountry(new PlayableCountryEntity());
                order.getCountry().setId(request.getGame().getIdCountry() * 2);
                order.setActive(true);
                game.getOrders().add(order);
                order = new CountryOrderEntity();
                order.setCountry(new PlayableCountryEntity());
                order.getCountry().setId(request.getGame().getIdCountry());
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
        bt = new BattleTech();
        bt.setTechnologyFor(Tech.MEDIEVAL);
        bt.setTechnologyAgainst(Tech.MEDIEVAL);
        bt.setLand(true);
        bt.setColumnShock("B");
        bt.setMoral(1);
        bt.setMoralBonusVeteran(true);
        tables.getBattleTechs().add(bt);
    }

    public static CounterEntity createCounter(Long id, String country, CounterFaceTypeEnum type, StackEntity owner) {
        CounterEntity counter = new CounterEntity();
        counter.setId(id);
        counter.setCountry(country);
        counter.setType(type);
        counter.setOwner(owner);
        return counter;
    }

    public static CounterEntity createCounter(Long id, String country, CounterFaceTypeEnum type, Long ownerId) {
        CounterEntity counter = new CounterEntity();
        counter.setId(id);
        counter.setCountry(country);
        counter.setType(type);
        if (ownerId != null) {
            counter.setOwner(new StackEntity());
            counter.getOwner().setId(ownerId);
        }
        return counter;
    }

    public static CounterEntity createCounter(Long id, String country, CounterFaceTypeEnum type) {
        return createCounter(id, country, type, (Long) null);
    }

    public static CounterEntity createLeader(LeaderBuilder leaderBuilder, Tables tables, StackEntity stack) {
        Leader leader = createLeaderTable(leaderBuilder, tables);
        CounterEntity counter = createCounter(leaderBuilder.id, leaderBuilder.country, CounterUtil.getLeaderType(leader), stack);
        counter.setCode(leaderBuilder.code);
        return counter;
    }

    public static Leader createLeaderTable(LeaderBuilder leaderBuilder, Tables tables) {
        Matcher m = Pattern.compile("([A-Z]) ?(\\d)(\\d)(\\d) ?\\-?(\\d)?").matcher(leaderBuilder.stats);
        Leader leader = new Leader();
        leader.setCode(leaderBuilder.code);
        leader.setCountry(leaderBuilder.country);
        leader.setType(leaderBuilder.type);
        leader.setBegin(leaderBuilder.begin);
        leader.setAnonymous(leaderBuilder.anonymous);
        if (m.matches()) {
            leader.setRank(m.group(1));
            leader.setManoeuvre(Integer.parseInt(m.group(2)));
            leader.setFire(Integer.parseInt(m.group(3)));
            leader.setShock(Integer.parseInt(m.group(4)));
            String siege = m.group(5);
            if (StringUtils.isNotEmpty(siege)) {
                leader.setSiege(Integer.parseInt(siege));
            }
        }
        tables.getLeaders().add(leader);
        return leader;
    }

    public static DiffAttributesEntity getAttributeFull(DiffEntity diff, DiffAttributeTypeEnum type) {
        return getAttributeFull(diff.getAttributes(), type);
    }

    public static DiffAttributesEntity getAttributeFull(List<DiffAttributesEntity> attributes, DiffAttributeTypeEnum type) {
        return attributes.stream()
                .filter(attribute -> attribute.getType() == type)
                .findAny()
                .orElse(null);
    }

    public static String getAttribute(DiffEntity diff, DiffAttributeTypeEnum type) {
        return getAttribute(diff.getAttributes(), type);
    }

    public static String getAttribute(List<DiffAttributesEntity> attributes, DiffAttributeTypeEnum type) {
        DiffAttributesEntity attr = attributes.stream()
                .filter(attribute -> attribute.getType() == type)
                .findAny()
                .orElse(null);
        return attr.getValue();
    }

    public static Answer<?> removeCounterAnswer() {
        return invocation -> {
            CounterEntity counter = invocation.getArgumentAt(0, CounterEntity.class);
            if (counter != null) {
                counter.getOwner().getCounters().remove(counter);
                return createDiff(counter.getOwner().getGame(), DiffTypeEnum.REMOVE, DiffTypeObjectEnum.COUNTER,
                        counter.getId());
            }
            return null;
        };
    }

    public static Answer<?> switchCounterAnswer() {
        return invocation -> {
            CounterEntity counter = invocation.getArgumentAt(0, CounterEntity.class);
            if (counter != null) {
                return createDiff(counter.getOwner().getGame(), DiffTypeEnum.MODIFY, DiffTypeObjectEnum.COUNTER,
                        counter.getId(),
                        DiffUtil.createDiffAttributes(DiffAttributeTypeEnum.TYPE, invocation.getArgumentAt(1, CounterFaceTypeEnum.class)));
            }
            return null;
        };
    }

    public static Answer<?> moveSpecialCounterAnswer() {
        return invocation -> createDiff(invocation.getArgumentAt(3, GameEntity.class), DiffTypeEnum.MOVE, DiffTypeObjectEnum.COUNTER,
                DiffUtil.createDiffAttributes(DiffAttributeTypeEnum.PROVINCE_TO, invocation.getArgumentAt(2, String.class)));
    }

    public static Answer<?> moveToSpecialBoxAnswer() {
        return invocation -> {
            CounterEntity counter = invocation.getArgumentAt(0, CounterEntity.class);
            if (counter != null) {
                return createDiff(invocation.getArgumentAt(2, GameEntity.class), DiffTypeEnum.MOVE, DiffTypeObjectEnum.COUNTER, counter.getId(),
                        DiffUtil.createDiffAttributes(DiffAttributeTypeEnum.PROVINCE_TO, invocation.getArgumentAt(1, String.class)));
            } else {
                return null;
            }
        };
    }

    public static Answer<?> createLeaderAnswer() {
        return invocation -> {
            GameEntity game = invocation.getArgumentAt(5, GameEntity.class);
            if (game != null) {
                return createDiff(game, DiffTypeEnum.ADD, DiffTypeObjectEnum.COUNTER,
                        DiffUtil.createDiffAttributes(DiffAttributeTypeEnum.CODE, invocation.getArgumentAt(1, String.class)),
                        DiffUtil.createDiffAttributes(DiffAttributeTypeEnum.COUNTRY, invocation.getArgumentAt(2, String.class)),
                        DiffUtil.createDiffAttributes(DiffAttributeTypeEnum.PROVINCE, invocation.getArgumentAt(4, String.class)));
            }
            return null;
        };
    }

    private static DiffEntity createDiff(GameEntity game, DiffTypeEnum type, DiffTypeObjectEnum typeObject, DiffAttributesEntity... attributes) {
        return createDiff(game, type, typeObject, null, attributes);
    }

    private static DiffEntity createDiff(GameEntity game, DiffTypeEnum type, DiffTypeObjectEnum typeObject, Long idObject, DiffAttributesEntity... attributes) {
        if (game != null) {
            return DiffUtil.createDiff(game, type, typeObject, idObject, attributes);
        }
        return null;
    }

    public static class LeaderBuilder {
        public Long id;
        public String code;
        public String country;
        public LeaderTypeEnum type;
        public Integer begin;
        public String stats;
        public boolean anonymous;

        public static LeaderBuilder create() {
            return new LeaderBuilder();
        }

        public LeaderBuilder id(Long id) {
            this.id = id;
            return this;
        }

        public LeaderBuilder code(String code) {
            this.code = code;
            return this;
        }

        public LeaderBuilder country(String country) {
            this.country = country;
            return this;
        }

        public LeaderBuilder type(LeaderTypeEnum type) {
            this.type = type;
            return this;
        }

        public LeaderBuilder begin(Integer begin) {
            this.begin = begin;
            return this;
        }

        public LeaderBuilder stats(String stats) {
            this.stats = stats;
            return this;
        }

        public LeaderBuilder anonymous() {
            this.anonymous = true;
            return this;
        }
    }
}
