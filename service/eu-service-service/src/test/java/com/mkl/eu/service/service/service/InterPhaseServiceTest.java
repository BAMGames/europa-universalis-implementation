package com.mkl.eu.service.service.service;

import com.mkl.eu.client.common.exception.FunctionalException;
import com.mkl.eu.client.common.exception.IConstantsCommonException;
import com.mkl.eu.client.common.vo.AuthentInfo;
import com.mkl.eu.client.common.vo.Request;
import com.mkl.eu.client.service.service.IConstantsServiceException;
import com.mkl.eu.client.service.service.IInterPhaseService;
import com.mkl.eu.client.service.service.common.ValidateRequest;
import com.mkl.eu.client.service.service.eco.ExchequerRepartitionRequest;
import com.mkl.eu.client.service.service.eco.ImproveStabilityRequest;
import com.mkl.eu.client.service.service.military.LandLootingRequest;
import com.mkl.eu.client.service.service.military.LandRedeployRequest;
import com.mkl.eu.client.service.util.GameUtil;
import com.mkl.eu.client.service.vo.enumeration.*;
import com.mkl.eu.service.service.domain.ICounterDomain;
import com.mkl.eu.service.service.domain.IStatusWorkflowDomain;
import com.mkl.eu.service.service.persistence.board.ICounterDao;
import com.mkl.eu.service.service.persistence.oe.GameEntity;
import com.mkl.eu.service.service.persistence.oe.board.CounterEntity;
import com.mkl.eu.service.service.persistence.oe.board.StackEntity;
import com.mkl.eu.service.service.persistence.oe.country.PlayableCountryEntity;
import com.mkl.eu.service.service.persistence.oe.diff.DiffEntity;
import com.mkl.eu.service.service.persistence.oe.diplo.CountryOrderEntity;
import com.mkl.eu.service.service.persistence.oe.eco.EconomicalSheetEntity;
import com.mkl.eu.service.service.persistence.oe.military.SiegeEntity;
import com.mkl.eu.service.service.persistence.oe.ref.province.AbstractProvinceEntity;
import com.mkl.eu.service.service.persistence.oe.ref.province.EuropeanProvinceEntity;
import com.mkl.eu.service.service.persistence.oe.ref.province.RegionEntity;
import com.mkl.eu.service.service.persistence.oe.ref.province.RotwProvinceEntity;
import com.mkl.eu.service.service.persistence.ref.IProvinceDao;
import com.mkl.eu.service.service.service.impl.InterPhaseServiceImpl;
import com.mkl.eu.service.service.util.DiffUtil;
import com.mkl.eu.service.service.util.IOEUtil;
import org.apache.commons.lang3.tuple.Pair;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import java.util.*;

import static org.mockito.Matchers.*;
import static org.mockito.Mockito.when;

/**
 * Test of BoardService.
 *
 * @author MKL.
 */
@RunWith(MockitoJUnitRunner.class)
public class InterPhaseServiceTest extends AbstractGameServiceTest {
    @InjectMocks
    private InterPhaseServiceImpl interPhaseService;

    @Mock
    private ICounterDomain counterDomain;

    @Mock
    private IStatusWorkflowDomain statusWorkflowDomain;

    @Mock
    private IProvinceDao provinceDao;

    @Mock
    private ICounterDao counterDao;

    @Mock
    private IOEUtil oeUtil;

    @Test
    public void testLandLootingFail() {
        Pair<Request<LandLootingRequest>, GameEntity> pair = testCheckGame(interPhaseService::landLooting, "landLooting");
        Request<LandLootingRequest> request = pair.getLeft();
        GameEntity game = pair.getRight();
        PlayableCountryEntity country = new PlayableCountryEntity();
        country.setName("france");
        country.setId(26L);
        game.getCountries().add(country);
        request.getGame().setIdCountry(26L);
        testCheckStatus(pair.getRight(), request, interPhaseService::landLooting, "landLooting", GameStatusEnum.REDEPLOYMENT);

        try {
            interPhaseService.landLooting(request);
            Assert.fail("Should break because landLooting.request is null");
        } catch (FunctionalException e) {
            Assert.assertEquals(IConstantsCommonException.NULL_PARAMETER, e.getCode());
            Assert.assertEquals("landLooting.request", e.getParams()[0]);
        }

        request.setRequest(new LandLootingRequest());

        try {
            interPhaseService.landLooting(request);
            Assert.fail("Should break because type is null");
        } catch (FunctionalException e) {
            Assert.assertEquals(IConstantsCommonException.NULL_PARAMETER, e.getCode());
            Assert.assertEquals("landLooting.request.type", e.getParams()[0]);
        }

        request.getRequest().setType(LandLootTypeEnum.PILLAGE);

        try {
            interPhaseService.landLooting(request);
            Assert.fail("Should break because idStack is null");
        } catch (FunctionalException e) {
            Assert.assertEquals(IConstantsCommonException.NULL_PARAMETER, e.getCode());
            Assert.assertEquals("landLooting.request.idStack", e.getParams()[0]);
        }

        request.getRequest().setIdStack(4L);

        try {
            interPhaseService.landLooting(request);
            Assert.fail("Should break because stack does not exist");
        } catch (FunctionalException e) {
            Assert.assertEquals(IConstantsCommonException.INVALID_PARAMETER, e.getCode());
            Assert.assertEquals("landLooting.request.idStack", e.getParams()[0]);
        }

        StackEntity stack = new StackEntity();
        stack.setId(4L);
        stack.setCountry("france");
        stack.setProvince("pecs");
        stack.setMovePhase(MovePhaseEnum.LOOTING);
        game.getStacks().add(stack);
        EuropeanProvinceEntity pecs = new EuropeanProvinceEntity();
        pecs.setName("pecs");
        when(provinceDao.getProvinceByName("pecs")).thenReturn(pecs);

        try {
            interPhaseService.landLooting(request);
            Assert.fail("Should break because stack is invalid");
        } catch (FunctionalException e) {
            Assert.assertEquals(IConstantsServiceException.LAND_LOOTING_INVALID_STACK, e.getCode());
            Assert.assertEquals("landLooting.request.idStack", e.getParams()[0]);
        }

        when(oeUtil.isMobile(stack)).thenReturn(true);

        try {
            interPhaseService.landLooting(request);
            Assert.fail("Should break because stack has already looted");
        } catch (FunctionalException e) {
            Assert.assertEquals(IConstantsServiceException.LAND_LOOTING_TWICE, e.getCode());
            Assert.assertEquals("landLooting.request.idStack", e.getParams()[0]);
        }

        stack.setMovePhase(MovePhaseEnum.LOOTING_BESIEGING);

        try {
            interPhaseService.landLooting(request);
            Assert.fail("Should break because stack has already looted");
        } catch (FunctionalException e) {
            Assert.assertEquals(IConstantsServiceException.LAND_LOOTING_TWICE, e.getCode());
            Assert.assertEquals("landLooting.request.idStack", e.getParams()[0]);
        }

        stack.setMovePhase(null);

        try {
            interPhaseService.landLooting(request);
            Assert.fail("Should break because stack does not belong to country");
        } catch (FunctionalException e) {
            Assert.assertEquals(IConstantsCommonException.ACCESS_RIGHT, e.getCode());
            Assert.assertEquals("landLooting.request.idStack", e.getParams()[0]);
        }

        when(counterDao.getPatrons(stack.getCountry(), game.getId())).thenReturn(Collections.singletonList(country.getName()));

        try {
            interPhaseService.landLooting(request);
            Assert.fail("Should break because province does not belong to an enemy");
        } catch (FunctionalException e) {
            Assert.assertEquals(IConstantsServiceException.LAND_LOOTING_NOT_ENEMY, e.getCode());
            Assert.assertEquals("landLooting.request.idStack", e.getParams()[0]);
        }

        when(oeUtil.getEnemies(country, game)).thenReturn(Collections.singletonList("spain"));
        when(oeUtil.getOwner(pecs, game)).thenReturn("spain");
        when(oeUtil.getFortressLevel(pecs, game)).thenReturn(5);

        try {
            interPhaseService.landLooting(request);
            Assert.fail("Should break because province cannot be looted because of forces");
        } catch (FunctionalException e) {
            Assert.assertEquals(IConstantsServiceException.LAND_LOOTING_INSUFFICIENT_FORCES, e.getCode());
            Assert.assertEquals("landLooting.request.idStack", e.getParams()[0]);
        }

        request.getRequest().setType(LandLootTypeEnum.BURN_TP);
        when(oeUtil.getFortressLevel(pecs, game)).thenReturn(0);

        try {
            interPhaseService.landLooting(request);
            Assert.fail("Should break because there is no TP to burn");
        } catch (FunctionalException e) {
            Assert.assertEquals(IConstantsServiceException.LAND_LOOTING_BURN_TP_NO_TP, e.getCode());
            Assert.assertEquals("landLooting.request.idStack", e.getParams()[0]);
        }

        stack = new StackEntity();
        stack.setProvince("pecs");
        stack.getCounters().add(createCounter(12L, "turkey", CounterFaceTypeEnum.TRADING_POST_MINUS));
        game.getStacks().add(stack);

        try {
            interPhaseService.landLooting(request);
            Assert.fail("Should break because there is no enemy TP to burn");
        } catch (FunctionalException e) {
            Assert.assertEquals(IConstantsServiceException.LAND_LOOTING_BURN_TP_NO_TP, e.getCode());
            Assert.assertEquals("landLooting.request.idStack", e.getParams()[0]);
        }

        when(oeUtil.getEnemies(country, game)).thenReturn(Arrays.asList("spain", "turkey"));

        try {
            interPhaseService.landLooting(request);
            Assert.fail("Should break because province must be controlled to burn the TP");
        } catch (FunctionalException e) {
            Assert.assertEquals(IConstantsServiceException.LAND_LOOTING_BURN_TP_NO_CONTROL, e.getCode());
            Assert.assertEquals("landLooting.request.idStack", e.getParams()[0]);
        }
    }

    @Test
    public void testLandLootingEurope() throws FunctionalException {
        LandLootingBuilder.create().type(LandLootTypeEnum.PILLAGE).income(10)
                .whenLandLooting(interPhaseService, this)
                .thenExpect(LandLootingResultBuilder.create().income(10).addPillage());
        LandLootingBuilder.create().type(LandLootTypeEnum.PILLAGE).income(10).addPillage(CounterFaceTypeEnum.PILLAGE_MINUS)
                .whenLandLooting(interPhaseService, this)
                .thenExpect(LandLootingResultBuilder.create().addPillage());
        LandLootingBuilder.create().type(LandLootTypeEnum.PILLAGE).income(10).addPillage(CounterFaceTypeEnum.PILLAGE_PLUS).addPillage(CounterFaceTypeEnum.PILLAGE_MINUS)
                .whenLandLooting(interPhaseService, this)
                .thenExpect(LandLootingResultBuilder.create().switchPillage());
        LandLootingBuilder.create().type(LandLootTypeEnum.PILLAGE).income(10).addPillage(CounterFaceTypeEnum.PILLAGE_PLUS).addPillage(CounterFaceTypeEnum.PILLAGE_PLUS)
                .whenLandLooting(interPhaseService, this)
                .thenExpect(LandLootingResultBuilder.create());
    }

    @Test
    public void testLandLootingRotw() throws FunctionalException {
        LandLootingBuilder.create().type(LandLootTypeEnum.PILLAGE).income(10).rotw()
                .whenLandLooting(interPhaseService, this)
                .thenExpect(LandLootingResultBuilder.create().income(5).addPillage());
        LandLootingBuilder.create().type(LandLootTypeEnum.PILLAGE).income(10).rotw().army()
                .whenLandLooting(interPhaseService, this)
                .thenExpect(LandLootingResultBuilder.create().income(10).addPillage());
        LandLootingBuilder.create().type(LandLootTypeEnum.PILLAGE).income(10).rotw().addPillage(CounterFaceTypeEnum.PILLAGE_MINUS)
                .whenLandLooting(interPhaseService, this)
                .thenExpect(LandLootingResultBuilder.create().addPillage());
        LandLootingBuilder.create().type(LandLootTypeEnum.PILLAGE).income(10).rotw().addPillage(CounterFaceTypeEnum.PILLAGE_PLUS).addPillage(CounterFaceTypeEnum.PILLAGE_MINUS)
                .whenLandLooting(interPhaseService, this)
                .thenExpect(LandLootingResultBuilder.create().switchPillage());
        LandLootingBuilder.create().type(LandLootTypeEnum.PILLAGE).income(10).rotw().addPillage(CounterFaceTypeEnum.PILLAGE_PLUS).addPillage(CounterFaceTypeEnum.PILLAGE_PLUS)
                .whenLandLooting(interPhaseService, this)
                .thenExpect(LandLootingResultBuilder.create());
    }

    @Test
    public void testLandLootingMinor() throws FunctionalException {
        LandLootingBuilder.create().type(LandLootTypeEnum.PILLAGE).income(10).minor()
                .whenLandLooting(interPhaseService, this)
                .thenExpect(LandLootingResultBuilder.create().addPillage());
        LandLootingBuilder.create().type(LandLootTypeEnum.PILLAGE).income(10).minor().rotw()
                .whenLandLooting(interPhaseService, this)
                .thenExpect(LandLootingResultBuilder.create().addPillage());
    }

    @Test
    public void testLandLootingBurnTp() throws FunctionalException {
        LandLootingBuilder.create().type(LandLootTypeEnum.BURN_TP).income(10).rotw()
                .whenLandLooting(interPhaseService, this)
                .thenExpect(LandLootingResultBuilder.create().removeTp());
        LandLootingBuilder.create().type(LandLootTypeEnum.BURN_TP).income(10).rotw().addPillage(CounterFaceTypeEnum.PILLAGE_PLUS)
                .whenLandLooting(interPhaseService, this)
                .thenExpect(LandLootingResultBuilder.create().removeTp());
    }

    @Test
    public void testLandLootingBesieging() throws FunctionalException {
        LandLootingBuilder.create().type(LandLootTypeEnum.PILLAGE).income(10).besieging()
                .whenLandLooting(interPhaseService, this)
                .thenExpect(LandLootingResultBuilder.create().income(10).addPillage().stillBesieging());
        LandLootingBuilder.create().type(LandLootTypeEnum.PILLAGE).income(10).rotw().besieging()
                .whenLandLooting(interPhaseService, this)
                .thenExpect(LandLootingResultBuilder.create().income(5).addPillage().stillBesieging());
        LandLootingBuilder.create().type(LandLootTypeEnum.PILLAGE).income(10).minor().besieging()
                .whenLandLooting(interPhaseService, this)
                .thenExpect(LandLootingResultBuilder.create().addPillage().stillBesieging());
    }

    static class LandLootingBuilder {
        LandLootTypeEnum type;
        boolean rotw;
        boolean army;
        boolean minor;
        boolean besieging;
        int income;
        List<CounterFaceTypeEnum> pillages = new ArrayList<>();
        List<DiffEntity> diffs;

        static LandLootingBuilder create() {
            return new LandLootingBuilder();
        }

        LandLootingBuilder type(LandLootTypeEnum type) {
            this.type = type;
            return this;
        }

        LandLootingBuilder rotw() {
            this.rotw = true;
            return this;
        }

        LandLootingBuilder army() {
            this.army = true;
            return this;
        }

        LandLootingBuilder minor() {
            this.minor = true;
            return this;
        }

        LandLootingBuilder besieging() {
            this.besieging = true;
            return this;
        }

        LandLootingBuilder income(int income) {
            this.income = income;
            return this;
        }

        LandLootingBuilder addPillage(CounterFaceTypeEnum pillage) {
            this.pillages.add(pillage);
            return this;
        }

        LandLootingBuilder whenLandLooting(InterPhaseServiceImpl interPhaseService, InterPhaseServiceTest testClass) throws FunctionalException {
            Pair<Request<LandLootingRequest>, GameEntity> pair = testClass.testCheckGame(interPhaseService::landLooting, "landLooting");
            Request<LandLootingRequest> request = pair.getLeft();
            request.getGame().setIdCountry(26L);
            request.setRequest(new LandLootingRequest());
            request.getRequest().setType(type);
            request.getRequest().setIdStack(4L);
            GameEntity game = pair.getRight();
            game.setTurn(6);
            PlayableCountryEntity country = new PlayableCountryEntity();
            country.setName("france");
            country.setId(26L);
            EconomicalSheetEntity sheet = new EconomicalSheetEntity();
            sheet.setTurn(game.getTurn());
            sheet.setPillages(50);
            country.getEconomicalSheets().add(sheet);
            game.getCountries().add(country);
            testClass.testCheckStatus(pair.getRight(), request, interPhaseService::landLooting, "landLooting", GameStatusEnum.REDEPLOYMENT);

            StackEntity lootingStack = new StackEntity();
            lootingStack.setId(4L);
            String countryName = minor ? "sabaudia" : "france";
            lootingStack.setCountry(countryName);
            lootingStack.setProvince("pecs");
            lootingStack.getCounters().add(createCounter(11L, countryName, army ? CounterFaceTypeEnum.ARMY_MINUS : CounterFaceTypeEnum.LAND_DETACHMENT));
            game.getStacks().add(lootingStack);
            if (type == LandLootTypeEnum.BURN_TP) {
                StackEntity stack = new StackEntity();
                stack.setProvince("pecs");
                stack.getCounters().add(createCounter(12L, "spain", CounterFaceTypeEnum.TRADING_POST_MINUS));
                game.getStacks().add(stack);
            }
            if (!pillages.isEmpty()) {
                StackEntity stack = new StackEntity();
                stack.setId(15L);
                stack.setProvince("pecs");
                for (CounterFaceTypeEnum pillage : pillages) {
                    CounterEntity counter = createCounter(666L, null, pillage);
                    counter.setOwner(stack);
                    stack.getCounters().add(counter);
                }
                game.getStacks().add(stack);
            }
            AbstractProvinceEntity pecs;
            if (rotw) {
                pecs = new RotwProvinceEntity();
                ((RotwProvinceEntity) pecs).setRegion("region");
                RegionEntity region = new RegionEntity();
                region.setIncome(income);
                when(testClass.provinceDao.getRegionByName("region")).thenReturn(region);
            } else {
                pecs = new EuropeanProvinceEntity();
                ((EuropeanProvinceEntity) pecs).setIncome(income);
            }
            pecs.setName("pecs");

            when(testClass.provinceDao.getProvinceByName("pecs")).thenReturn(pecs);
            when(testClass.oeUtil.isMobile(lootingStack)).thenReturn(true);
            when(testClass.counterDao.getPatrons(lootingStack.getCountry(), game.getId())).thenReturn(Collections.singletonList(country.getName()));
            when(testClass.oeUtil.getAllies(country, game)).thenReturn(Collections.singletonList("france"));
            when(testClass.oeUtil.getEnemies(country, game)).thenReturn(Collections.singletonList("spain"));
            when(testClass.oeUtil.getOwner(pecs, game)).thenReturn("spain");
            if (besieging) {
                when(testClass.oeUtil.getController(pecs, game)).thenReturn("spain");
            } else {
                when(testClass.oeUtil.getController(pecs, game)).thenReturn("france");
            }
            StackEntity stack = new StackEntity();
            stack.setId(16L);
            stack.setProvince("pecs");
            when(testClass.counterDomain.createStack("pecs", null, game)).thenReturn(stack);
            when(testClass.counterDomain.createCounter(CounterFaceTypeEnum.PILLAGE_PLUS, null, pillages.isEmpty() ? 16L : 15L, game))
                    .thenReturn(DiffUtil.createDiff(game, DiffTypeEnum.ADD, DiffTypeObjectEnum.COUNTER));
            when(testClass.counterDomain.switchCounter(anyLong(), any(), anyInt(), any()))
                    .thenReturn(DiffUtil.createDiff(game, DiffTypeEnum.MODIFY, DiffTypeObjectEnum.COUNTER));
            when(testClass.counterDomain.removeCounter(12L, game))
                    .thenReturn(DiffUtil.createDiff(game, DiffTypeEnum.REMOVE, DiffTypeObjectEnum.COUNTER, 12L));

            testClass.simulateDiff();

            interPhaseService.landLooting(request);

            diffs = testClass.retrieveDiffsCreated();

            return this;
        }

        LandLootingBuilder thenExpect(LandLootingResultBuilder result) {
            int nbDiffs = 1;

            DiffEntity diff = diffs.stream()
                    .filter(d -> d.getType() == DiffTypeEnum.MODIFY && d.getTypeObject() == DiffTypeObjectEnum.STACK)
                    .findAny()
                    .orElse(null);
            Assert.assertNotNull("The stack should have been modified but was not.", diff);
            Assert.assertEquals("The wrong stack was modified.", 4L, diff.getIdObject().longValue());
            Assert.assertEquals("The stack was wrongly modified.", result.movePhase.name(), getAttribute(diff, DiffAttributeTypeEnum.MOVE_PHASE));

            diff = diffs.stream()
                    .filter(d -> d.getType() == DiffTypeEnum.MODIFY && d.getTypeObject() == DiffTypeObjectEnum.ECO_SHEET)
                    .findAny()
                    .orElse(null);
            if (result.income > 0) {
                Assert.assertNotNull("Eco sheet should be updated but was not.", diff);
                Assert.assertEquals("Eco sheet was updated with wrong new pillage value.", 50 + result.income + "", getAttribute(diff, DiffAttributeTypeEnum.PILLAGE));
                nbDiffs++;
            } else {
                Assert.assertNull("Eco sheet should not be updated but was.", diff);
            }
            diff = diffs.stream()
                    .filter(d -> d.getType() == DiffTypeEnum.ADD && d.getTypeObject() == DiffTypeObjectEnum.COUNTER)
                    .findAny()
                    .orElse(null);
            if (result.addPillage) {
                Assert.assertNotNull("A pillage should be added but was not.", diff);
                nbDiffs++;
            } else {
                Assert.assertNull("A pillage should not be added but was.", diff);
            }
            diff = diffs.stream()
                    .filter(d -> d.getType() == DiffTypeEnum.MODIFY && d.getTypeObject() == DiffTypeObjectEnum.COUNTER)
                    .findAny()
                    .orElse(null);
            if (result.switchPillage) {
                Assert.assertNotNull("A pillage should be switched but was not.", diff);
                nbDiffs++;
            } else {
                Assert.assertNull("A pillage should not be switched but was.", diff);
            }
            diff = diffs.stream()
                    .filter(d -> d.getType() == DiffTypeEnum.REMOVE && d.getTypeObject() == DiffTypeObjectEnum.COUNTER && Objects.equals(12L, d.getIdObject()))
                    .findAny()
                    .orElse(null);
            if (result.removeTp) {
                Assert.assertNotNull("A tp should be removed but was not.", diff);
                nbDiffs++;
            } else {
                Assert.assertNull("A tp should not be removed but was.", diff);
            }

            Assert.assertEquals("Number of diffs received mismatch.", nbDiffs, diffs.size());

            return this;
        }
    }

    static class LandLootingResultBuilder {
        MovePhaseEnum movePhase = MovePhaseEnum.LOOTING;
        int income;
        boolean addPillage;
        boolean switchPillage;
        boolean removeTp;

        static LandLootingResultBuilder create() {
            return new LandLootingResultBuilder();
        }

        LandLootingResultBuilder stillBesieging() {
            this.movePhase = MovePhaseEnum.LOOTING_BESIEGING;
            return this;
        }

        LandLootingResultBuilder income(int income) {
            this.income = income;
            return this;
        }

        LandLootingResultBuilder addPillage() {
            this.addPillage = true;
            return this;
        }

        LandLootingResultBuilder switchPillage() {
            this.switchPillage = true;
            return this;
        }

        LandLootingResultBuilder removeTp() {
            this.removeTp = true;
            return this;
        }
    }

    @Test
    public void testLandRedeployFail() {
        Pair<Request<LandRedeployRequest>, GameEntity> pair = testCheckGame(interPhaseService::landRedeploy, "landRedeploy");
        Request<LandRedeployRequest> request = pair.getLeft();
        GameEntity game = pair.getRight();
        PlayableCountryEntity country = new PlayableCountryEntity();
        country.setName("france");
        country.setId(26L);
        game.getCountries().add(country);
        request.getGame().setIdCountry(26L);
        testCheckStatus(pair.getRight(), request, interPhaseService::landRedeploy, "landRedeploy", GameStatusEnum.REDEPLOYMENT);

        try {
            interPhaseService.landRedeploy(request);
            Assert.fail("Should break because landRedeploy.request is null");
        } catch (FunctionalException e) {
            Assert.assertEquals(IConstantsCommonException.NULL_PARAMETER, e.getCode());
            Assert.assertEquals("landRedeploy.request", e.getParams()[0]);
        }

        request.setRequest(new LandRedeployRequest());

        try {
            interPhaseService.landRedeploy(request);
            Assert.fail("Should break because province is null");
        } catch (FunctionalException e) {
            Assert.assertEquals(IConstantsCommonException.NULL_PARAMETER, e.getCode());
            Assert.assertEquals("landRedeploy.request.province", e.getParams()[0]);
        }

        request.getRequest().setProvince("idf");

        try {
            interPhaseService.landRedeploy(request);
            Assert.fail("Should break because idStack is null");
        } catch (FunctionalException e) {
            Assert.assertEquals(IConstantsCommonException.NULL_PARAMETER, e.getCode());
            Assert.assertEquals("landRedeploy.request.idStack", e.getParams()[0]);
        }

        request.getRequest().setIdStack(4L);

        try {
            interPhaseService.landRedeploy(request);
            Assert.fail("Should break because stack does not exist");
        } catch (FunctionalException e) {
            Assert.assertEquals(IConstantsCommonException.INVALID_PARAMETER, e.getCode());
            Assert.assertEquals("landRedeploy.request.idStack", e.getParams()[0]);
        }

        StackEntity stack = new StackEntity();
        stack.setId(4L);
        stack.setCountry("france");
        stack.setProvince("pecs");
        stack.setMovePhase(MovePhaseEnum.LOOTING);
        game.getStacks().add(stack);
        EuropeanProvinceEntity pecs = new EuropeanProvinceEntity();
        pecs.setName("pecs");
        when(provinceDao.getProvinceByName("pecs")).thenReturn(pecs);

        try {
            interPhaseService.landRedeploy(request);
            Assert.fail("Should break because stack is invalid");
        } catch (FunctionalException e) {
            Assert.assertEquals(IConstantsServiceException.STACK_NOT_MOBILE, e.getCode());
            Assert.assertEquals("landRedeploy.request.idStack", e.getParams()[0]);
        }

        when(oeUtil.isMobile(stack)).thenReturn(true);

        try {
            interPhaseService.landRedeploy(request);
            Assert.fail("Should break because stack does not belong to country");
        } catch (FunctionalException e) {
            Assert.assertEquals(IConstantsCommonException.ACCESS_RIGHT, e.getCode());
            Assert.assertEquals("landRedeploy.request.idStack", e.getParams()[0]);
        }

        when(counterDao.getPatrons(stack.getCountry(), game.getId())).thenReturn(Collections.singletonList(country.getName()));

        try {
            interPhaseService.landRedeploy(request);
            Assert.fail("Should break because province does not belong to an enemy");
        } catch (FunctionalException e) {
            Assert.assertEquals(IConstantsServiceException.LAND_REDEPLOY_NOT_ENEMY, e.getCode());
            Assert.assertEquals("landRedeploy.request.idStack", e.getParams()[0]);
        }

        when(oeUtil.getEnemies(country, game)).thenReturn(Collections.singletonList("spain"));
        when(oeUtil.getController(pecs, game)).thenReturn("spain");

        try {
            interPhaseService.landRedeploy(request);
            Assert.fail("Should break because province does not exist");
        } catch (FunctionalException e) {
            Assert.assertEquals(IConstantsCommonException.INVALID_PARAMETER, e.getCode());
            Assert.assertEquals("landRedeploy.request.province", e.getParams()[0]);
        }

        EuropeanProvinceEntity idf = new EuropeanProvinceEntity();
        idf.setName("idf");
        when(provinceDao.getProvinceByName("idf")).thenReturn(idf);

        try {
            interPhaseService.landRedeploy(request);
            Assert.fail("Should break because units cant redeploy in province");
        } catch (FunctionalException e) {
            Assert.assertEquals(IConstantsServiceException.UNIT_CANT_REDEPLOY_PROVINCE, e.getCode());
            Assert.assertEquals("landRedeploy.request.province", e.getParams()[0]);
        }
    }

    @Test
    public void testLandRedeploySuccessNoSiegeWork() throws FunctionalException {
        testLandRedeploySuccess();
    }

    @Test
    public void testLandRedeploySuccessWithSiegeWork() throws FunctionalException {
        testLandRedeploySuccess(CounterFaceTypeEnum.SIEGEWORK_MINUS, CounterFaceTypeEnum.SIEGEWORK_PLUS);
    }

    @Test
    public void testLandRedeploySuccessBordel() throws FunctionalException {
        testLandRedeploySuccess(CounterFaceTypeEnum.SIEGEWORK_MINUS, CounterFaceTypeEnum.REVOLT_PLUS);
    }

    private void testLandRedeploySuccess(CounterFaceTypeEnum... siegeworks) throws FunctionalException {
        Pair<Request<LandRedeployRequest>, GameEntity> pair = testCheckGame(interPhaseService::landRedeploy, "landRedeploy");
        Request<LandRedeployRequest> request = pair.getLeft();
        request.setRequest(new LandRedeployRequest());
        request.getRequest().setProvince("idf");
        request.getRequest().setIdStack(4L);
        GameEntity game = pair.getRight();
        StackEntity stack = new StackEntity();
        stack.setId(4L);
        stack.setCountry("france");
        stack.setProvince("pecs");
        stack.setMovePhase(MovePhaseEnum.LOOTING);
        game.getStacks().add(stack);
        StackEntity stackSiegeworks = new StackEntity();
        stackSiegeworks.setId(3L);
        stackSiegeworks.setProvince("milano");
        stackSiegeworks.getCounters().add(createCounter(1L, null, CounterFaceTypeEnum.SIEGEWORK_MINUS, stackSiegeworks));
        game.getStacks().add(stackSiegeworks);
        if (siegeworks != null && siegeworks.length > 0) {
            stackSiegeworks = new StackEntity();
            stackSiegeworks.setId(5L);
            stackSiegeworks.setProvince("pecs");
            for (CounterFaceTypeEnum siegework : siegeworks) {
                stackSiegeworks.getCounters().add(createCounter(1L, null, siegework, stackSiegeworks));
            }
            game.getStacks().add(stackSiegeworks);
        }
        PlayableCountryEntity country = new PlayableCountryEntity();
        country.setName("france");
        country.setId(26L);
        game.getCountries().add(country);
        request.getGame().setIdCountry(26L);
        testCheckStatus(pair.getRight(), request, interPhaseService::landRedeploy, "landRedeploy", GameStatusEnum.REDEPLOYMENT);

        EuropeanProvinceEntity pecs = new EuropeanProvinceEntity();
        pecs.setName("pecs");
        when(provinceDao.getProvinceByName("pecs")).thenReturn(pecs);
        EuropeanProvinceEntity idf = new EuropeanProvinceEntity();
        idf.setName("idf");
        when(provinceDao.getProvinceByName("idf")).thenReturn(idf);
        when(oeUtil.isMobile(stack)).thenReturn(true);
        when(counterDao.getPatrons(stack.getCountry(), game.getId())).thenReturn(Collections.singletonList(country.getName()));
        when(oeUtil.getEnemies(country, game)).thenReturn(Collections.singletonList("spain"));
        when(oeUtil.getController(pecs, game)).thenReturn("spain");
        when(oeUtil.canRetreat(idf, false, 0d, country, game)).thenReturn(true);
        when(counterDomain.removeCounter(anyLong(), any())).thenAnswer(removeCounterAnswer());

        simulateDiff();

        interPhaseService.landRedeploy(request);

        List<DiffEntity> diffs = retrieveDiffsCreated();
        DiffEntity diff = diffs.stream()
                .filter(d -> d.getType() == DiffTypeEnum.MOVE && d.getTypeObject() == DiffTypeObjectEnum.STACK)
                .findAny()
                .orElse(null);
        Assert.assertNotNull(diff);
        Assert.assertEquals(4L, diff.getIdObject().longValue());
        Assert.assertEquals("pecs", getAttribute(diff, DiffAttributeTypeEnum.PROVINCE_FROM));
        Assert.assertEquals("idf", getAttribute(diff, DiffAttributeTypeEnum.PROVINCE_TO));
        Assert.assertEquals(MovePhaseEnum.MOVED.name(), getAttribute(diff, DiffAttributeTypeEnum.MOVE_PHASE));
        long siegeworksRemoved = siegeworks == null ? 0 : Arrays.stream(siegeworks)
                .filter(type -> type == CounterFaceTypeEnum.SIEGEWORK_MINUS || type == CounterFaceTypeEnum.SIEGEWORK_PLUS)
                .count();
        long diffsRemoved = diffs.stream()
                .filter(d -> d.getType() == DiffTypeEnum.REMOVE && d.getTypeObject() == DiffTypeObjectEnum.COUNTER && Objects.equals(1L, d.getIdObject()))
                .count();
        Assert.assertEquals(diffsRemoved, siegeworksRemoved);
    }

    @Test
    public void testValidateMilRoundFail() {
        Pair<Request<ValidateRequest>, GameEntity> pair = testCheckGame(interPhaseService::validateRedeploy, "validateRedeploy");
        Request<ValidateRequest> request = pair.getLeft();
        request.getGame().setIdCountry(26L);
        GameEntity game = pair.getRight();
        game.setTurn(10);

        testCheckStatus(game, request, interPhaseService::validateRedeploy, "validateRedeploy", GameStatusEnum.REDEPLOYMENT);

        game.setTurn(22);
        PlayableCountryEntity country = new PlayableCountryEntity();
        country.setId(13L);
        country.setName("france");
        country.setUsername("MKL");
        game.getCountries().add(country);
        request.setAuthent(new AuthentInfo());
        request.getAuthent().setUsername("toto");

        try {
            interPhaseService.validateRedeploy(request);
            Assert.fail("Should break because request.request is null");
        } catch (FunctionalException e) {
            Assert.assertEquals(IConstantsCommonException.NULL_PARAMETER, e.getCode());
            Assert.assertEquals("validateRedeploy.request", e.getParams()[0]);
        }

        request.setRequest(new ValidateRequest());

        try {
            interPhaseService.validateRedeploy(request);
            Assert.fail("Should break because request.request.idCountry is invalid");
        } catch (FunctionalException e) {
            Assert.assertEquals(IConstantsCommonException.INVALID_PARAMETER, e.getCode());
            Assert.assertEquals("validateRedeploy.game.idCountry", e.getParams()[0]);
        }

        game.getCountries().get(0).setId(26L);

        try {
            interPhaseService.validateRedeploy(request);
            Assert.fail("Should break because request.authent can't do this action");
        } catch (FunctionalException e) {
            Assert.assertEquals(IConstantsCommonException.ACCESS_RIGHT, e.getCode());
            Assert.assertEquals("validateRedeploy.authent.username", e.getParams()[0]);
        }

        request.getAuthent().setUsername("MKL");

        when(oeUtil.getAllies(country, game)).thenReturn(Arrays.asList("france", "turkey", "sabaudia", "genua"));
        when(counterDao.getMinors(country.getName(), game.getId())).thenReturn(Arrays.asList("sabaudia", "corsica"));
        when(oeUtil.getFortressLevel(any(), any())).thenReturn(3);
        when(provinceDao.getProvinceByName(anyString())).thenReturn(new EuropeanProvinceEntity());

        StackEntity stack = new StackEntity();
        stack.setProvince("pecs");
        stack.setCountry("france");
        stack.setMovePhase(MovePhaseEnum.BESIEGING);
        game.getStacks().add(stack);
        stack = new StackEntity();
        stack.setProvince("pecs");
        stack.getCounters().add(createCounter(1L, null, CounterFaceTypeEnum.SIEGEWORK_PLUS, stack));
        game.getStacks().add(stack);
        stack = new StackEntity();
        stack.setProvince("milano");
        stack.setCountry("genua");
        stack.setMovePhase(MovePhaseEnum.BESIEGING);
        game.getStacks().add(stack);
        stack = new StackEntity();
        stack.setProvince("napoli");
        stack.setCountry("sabaudia");
        stack.setMovePhase(MovePhaseEnum.BESIEGING);
        game.getStacks().add(stack);
        stack = new StackEntity();
        stack.setProvince("andalucia");
        stack.setCountry("corsica");
        stack.setMovePhase(MovePhaseEnum.BESIEGING);
        game.getStacks().add(stack);

        stack = new StackEntity();
        stack.setProvince("ulm");
        stack.setCountry("france");
        stack.setMovePhase(MovePhaseEnum.BESIEGING);
        stack.getCounters().add(createCounter(1L, "france", CounterFaceTypeEnum.ARMY_PLUS, stack));
        game.getStacks().add(stack);
        stack = new StackEntity();
        stack.setProvince("ulm");
        stack.getCounters().add(createCounter(1L, null, CounterFaceTypeEnum.SIEGEWORK_MINUS, stack));
        game.getStacks().add(stack);
        SiegeEntity siege = new SiegeEntity();
        siege.setProvince("ulm");
        siege.setTurn(game.getTurn() + 1);
        siege.setBreach(true);
        game.getSieges().add(siege);

        stack = new StackEntity();
        stack.setProvince("brandebourg");
        stack.setCountry("france");
        stack.setMovePhase(MovePhaseEnum.BESIEGING);
        stack.getCounters().add(createCounter(1L, "france", CounterFaceTypeEnum.ARMY_PLUS, stack));
        game.getStacks().add(stack);
        stack = new StackEntity();
        stack.setProvince("brandebourg");
        stack.getCounters().add(createCounter(1L, null, CounterFaceTypeEnum.SIEGEWORK_PLUS, stack));
        game.getStacks().add(stack);

        stack = new StackEntity();
        stack.setProvince("silesie");
        stack.setCountry("france");
        stack.setMovePhase(MovePhaseEnum.BESIEGING);
        stack.getCounters().add(createCounter(1L, "france", CounterFaceTypeEnum.ARMY_PLUS, stack));
        game.getStacks().add(stack);
        stack = new StackEntity();
        stack.setProvince("silesie");
        stack.getCounters().add(createCounter(1L, null, CounterFaceTypeEnum.SIEGEWORK_MINUS, stack));
        game.getStacks().add(stack);
        siege = new SiegeEntity();
        siege.setProvince("silesie");
        siege.setTurn(game.getTurn());
        siege.setBreach(true);
        game.getSieges().add(siege);

        try {
            interPhaseService.validateRedeploy(request);
            Assert.fail("Should break because there are still stack that must redeploy");
        } catch (FunctionalException e) {
            Assert.assertEquals(IConstantsServiceException.STACK_MUST_REDEPLOY, e.getCode());
            Assert.assertEquals("validateRedeploy.request.validate", e.getParams()[0]);
            Assert.assertEquals("pecs,napoli,ulm", e.getParams()[2]);
        }
    }

    @Test
    public void testValidateMilRoundSuccessSimple() throws FunctionalException {
        Request<ValidateRequest> request = new Request<>();
        request.setAuthent(new AuthentInfo());
        request.getAuthent().setUsername("MKL");
        request.setGame(createGameInfo());
        request.setRequest(new ValidateRequest());
        request.getGame().setIdCountry(13L);

        GameEntity game = createGameUsingMocks(GameStatusEnum.MILITARY_MOVE, 13L);
        game.setStatus(GameStatusEnum.REDEPLOYMENT);
        PlayableCountryEntity france = new PlayableCountryEntity();
        game.getCountries().add(france);
        france.setId(13L);
        france.setName("france");
        france.setUsername("MKL");
        france.setReady(false);

        StackEntity stack = new StackEntity();
        stack.setProvince("pecs");
        stack.setCountry("france");
        stack.setMovePhase(MovePhaseEnum.BESIEGING);
        stack.getCounters().add(createCounter(1L, "france", CounterFaceTypeEnum.ARMY_MINUS, stack));
        stack.getCounters().add(createCounter(2L, null, CounterFaceTypeEnum.SIEGEWORK_PLUS, stack));
        game.getStacks().add(stack);

        when(oeUtil.getAllies(france, game)).thenReturn(Collections.singletonList("france"));
        EuropeanProvinceEntity pecs = new EuropeanProvinceEntity();
        pecs.setName("pecs");
        when(provinceDao.getProvinceByName("pecs")).thenReturn(pecs);
        when(oeUtil.getFortressLevel(pecs, game)).thenReturn(2);

        simulateDiff();

        interPhaseService.validateRedeploy(request);

        List<DiffEntity> diffEntities = retrieveDiffsCreated();

        Assert.assertEquals(0, diffEntities.size());
    }

    @Test
    public void testValidateMilRoundSuccessSimple2() throws FunctionalException {
        Request<ValidateRequest> request = new Request<>();
        request.setAuthent(new AuthentInfo());
        request.getAuthent().setUsername("MKL");
        request.setGame(createGameInfo());
        request.setRequest(new ValidateRequest());
        request.getGame().setIdCountry(13L);

        GameEntity game = createGameUsingMocks(GameStatusEnum.MILITARY_MOVE, 13L);
        game.getOrders().get(0).setReady(true);
        game.setStatus(GameStatusEnum.REDEPLOYMENT);
        PlayableCountryEntity france = new PlayableCountryEntity();
        game.getCountries().add(france);
        france.setId(13L);
        france.setName("france");
        france.setUsername("MKL");
        france.setReady(false);

        StackEntity stack = new StackEntity();
        stack.setProvince("pecs");
        stack.setCountry("france");
        stack.setMovePhase(MovePhaseEnum.BESIEGING);
        stack.getCounters().add(createCounter(1L, "france", CounterFaceTypeEnum.ARMY_MINUS, stack));
        game.getStacks().add(stack);

        when(oeUtil.getAllies(france, game)).thenReturn(Collections.singletonList("france"));
        EuropeanProvinceEntity pecs = new EuropeanProvinceEntity();
        pecs.setName("pecs");
        when(provinceDao.getProvinceByName("pecs")).thenReturn(pecs);
        when(oeUtil.getController(pecs, game)).thenReturn("france");
        when(oeUtil.getFortressLevel(pecs, game)).thenReturn(4);

        simulateDiff();

        interPhaseService.validateRedeploy(request);

        List<DiffEntity> diffEntities = retrieveDiffsCreated();

        Assert.assertEquals(1, diffEntities.size());
        DiffEntity diff = diffEntities.stream()
                .filter(d -> d.getType() == DiffTypeEnum.INVALIDATE && d.getTypeObject() == DiffTypeObjectEnum.TURN_ORDER)
                .findAny()
                .orElse(null);
        Assert.assertNotNull(diff);
        Assert.assertEquals("13", getAttribute(diff, DiffAttributeTypeEnum.ID_COUNTRY));
    }

    @Test
    public void testValidateMilRoundSuccessSimple3() throws FunctionalException {
        Request<ValidateRequest> request = new Request<>();
        request.setAuthent(new AuthentInfo());
        request.getAuthent().setUsername("MKL");
        request.setGame(createGameInfo());
        request.setRequest(new ValidateRequest());
        request.getRequest().setValidate(true);
        request.getGame().setIdCountry(13L);

        GameEntity game = createGameUsingMocks(GameStatusEnum.MILITARY_MOVE, 13L);
        CountryOrderEntity order = new CountryOrderEntity();
        order.setActive(true);
        order.setCountry(new PlayableCountryEntity());
        order.getCountry().setId(14L);
        game.getOrders().add(order);
        game.setStatus(GameStatusEnum.REDEPLOYMENT);
        game.getCountries().add(new PlayableCountryEntity());
        game.getCountries().get(0).setId(13L);
        game.getCountries().get(0).setName("france");
        game.getCountries().get(0).setUsername("MKL");
        game.getCountries().get(0).setReady(false);

        simulateDiff();

        interPhaseService.validateRedeploy(request);

        List<DiffEntity> diffEntities = retrieveDiffsCreated();

        Assert.assertEquals(1, diffEntities.size());
        DiffEntity diff = diffEntities.stream()
                .filter(d -> d.getType() == DiffTypeEnum.VALIDATE && d.getTypeObject() == DiffTypeObjectEnum.TURN_ORDER)
                .findAny()
                .orElse(null);
        Assert.assertNotNull(diff);
        Assert.assertEquals("13", getAttribute(diff, DiffAttributeTypeEnum.ID_COUNTRY));
    }

    @Test
    public void testValidateMilRoundSuccessComplexNextMove() throws FunctionalException {
        Request<ValidateRequest> request = new Request<>();
        request.setAuthent(new AuthentInfo());
        request.getAuthent().setUsername("MKL");
        request.setGame(createGameInfo());
        request.setRequest(new ValidateRequest());
        request.getRequest().setValidate(true);
        request.getGame().setIdCountry(13L);

        GameEntity game = createGameUsingMocks(GameStatusEnum.REDEPLOYMENT, 13L);
        game.getOrders().clear();
        CountryOrderEntity order = new CountryOrderEntity();
        order.setActive(true);
        order.setCountry(new PlayableCountryEntity());
        order.setPosition(3);
        order.getCountry().setId(13L);
        game.getOrders().add(order);
        order = new CountryOrderEntity();
        order.setActive(false);
        order.setCountry(new PlayableCountryEntity());
        order.setPosition(2);
        order.getCountry().setId(9L);
        game.getOrders().add(order);
        order = new CountryOrderEntity();
        order.setActive(false);
        order.setCountry(new PlayableCountryEntity());
        order.setPosition(4);
        order.getCountry().setId(21L);
        game.getOrders().add(order);
        order = new CountryOrderEntity();
        order.setActive(false);
        order.setCountry(new PlayableCountryEntity());
        order.setPosition(4);
        order.getCountry().setId(22L);
        game.getOrders().add(order);
        order = new CountryOrderEntity();
        order.setActive(false);
        order.setCountry(new PlayableCountryEntity());
        order.setPosition(4);
        order.getCountry().setId(21L);
        game.getOrders().add(order);

        game.setStatus(GameStatusEnum.REDEPLOYMENT);
        game.getCountries().add(new PlayableCountryEntity());
        game.getCountries().get(0).setId(13L);
        game.getCountries().get(0).setName("france");
        game.getCountries().get(0).setUsername("MKL");
        game.getCountries().get(0).setReady(false);
        DiffEntity endRedeploymentPhase = DiffUtil.createDiff(game, DiffTypeEnum.MODIFY, DiffTypeObjectEnum.GAME);
        when(statusWorkflowDomain.endRedeploymentPhase(game)).thenReturn(Collections.singletonList(endRedeploymentPhase));

        simulateDiff();

        interPhaseService.validateRedeploy(request);

        List<DiffEntity> diffEntities = retrieveDiffsCreated();

        Assert.assertEquals(1, diffEntities.size());
        Assert.assertEquals(endRedeploymentPhase, diffEntities.get(0));
    }

    @Test
    public void testExchequerRepartitionFail() {
        Pair<Request<ExchequerRepartitionRequest>, GameEntity> pair = testCheckGame(interPhaseService::exchequerRepartition, "exchequerRepartition");
        Request<ExchequerRepartitionRequest> request = pair.getLeft();
        GameEntity game = pair.getRight();
        game.setTurn(10);

        testCheckStatus(game, request, interPhaseService::exchequerRepartition, "exchequerRepartition", GameStatusEnum.EXCHEQUER);

        request.getGame().setIdCountry(26L);
        PlayableCountryEntity country = new PlayableCountryEntity();
        country.setId(26L);
        country.setName("france");
        game.getCountries().add(country);

        try {
            interPhaseService.exchequerRepartition(request);
            Assert.fail("Should break because request.request is null");
        } catch (FunctionalException e) {
            Assert.assertEquals(IConstantsCommonException.NULL_PARAMETER, e.getCode());
            Assert.assertEquals("exchequerRepartition.request", e.getParams()[0]);
        }

        request.setRequest(new ExchequerRepartitionRequest());

        try {
            interPhaseService.exchequerRepartition(request);
            Assert.fail("Should break because request.game.idCountry is invalid");
        } catch (FunctionalException e) {
            Assert.assertEquals(IConstantsCommonException.INVALID_PARAMETER, e.getCode());
            Assert.assertEquals("exchequerRepartition.request.game.idCountry", e.getParams()[0]);
        }

        EconomicalSheetEntity sheet = new EconomicalSheetEntity();
        country.getEconomicalSheets().add(sheet);

        try {
            interPhaseService.exchequerRepartition(request);
            Assert.fail("Should break because request.request.idCountry is invalid");
        } catch (FunctionalException e) {
            Assert.assertEquals(IConstantsCommonException.INVALID_PARAMETER, e.getCode());
            Assert.assertEquals("exchequerRepartition.request.game.idCountry", e.getParams()[0]);
        }

        sheet.setTurn(game.getTurn());
        request.getRequest().setPrestige(1);

        try {
            interPhaseService.exchequerRepartition(request);
            Assert.fail("Should break because prestige spent is too high");
        } catch (FunctionalException e) {
            Assert.assertEquals(IConstantsServiceException.PRESTIGE_TOO_HIGH, e.getCode());
            Assert.assertEquals("exchequerRepartition.request.request.prestige", e.getParams()[0]);
        }

        sheet.setPrestigeIncome(100);
        request.getRequest().setPrestige(100);
        request.getRequest().setPrestige(101);

        try {
            interPhaseService.exchequerRepartition(request);
            Assert.fail("Should break because prestige spent is too high");
        } catch (FunctionalException e) {
            Assert.assertEquals(IConstantsServiceException.PRESTIGE_TOO_HIGH, e.getCode());
            Assert.assertEquals("exchequerRepartition.request.request.prestige", e.getParams()[0]);
        }

        sheet.setPrestigeIncome(-1);
        request.getRequest().setPrestige(-1);
        request.getRequest().setPrestige(-1);

        try {
            interPhaseService.exchequerRepartition(request);
            Assert.fail("Should break because prestige spent is too high");
        } catch (FunctionalException e) {
            Assert.assertEquals(IConstantsServiceException.PRESTIGE_TOO_HIGH, e.getCode());
            Assert.assertEquals("exchequerRepartition.request.request.prestige", e.getParams()[0]);
        }

        sheet.setPrestigeIncome(100);
        sheet.setRemainingExpenses(60);
        request.getRequest().setPrestige(80);

        try {
            interPhaseService.exchequerRepartition(request);
            Assert.fail("Should break because prestige spent is too high");
        } catch (FunctionalException e) {
            Assert.assertEquals(IConstantsServiceException.PRESTIGE_TOO_HIGH, e.getCode());
            Assert.assertEquals("exchequerRepartition.request.request.prestige", e.getParams()[0]);
        }

        sheet.setPrestigeIncome(60);
        sheet.setRemainingExpenses(100);
        request.getRequest().setPrestige(80);

        try {
            interPhaseService.exchequerRepartition(request);
            Assert.fail("Should break because prestige spent is too high");
        } catch (FunctionalException e) {
            Assert.assertEquals(IConstantsServiceException.PRESTIGE_TOO_HIGH, e.getCode());
            Assert.assertEquals("exchequerRepartition.request.request.prestige", e.getParams()[0]);
        }

        sheet.setPrestigeIncome(60);
        sheet.setRemainingExpenses(60);
        request.getRequest().setPrestige(80);

        try {
            interPhaseService.exchequerRepartition(request);
            Assert.fail("Should break because prestige spent is too high");
        } catch (FunctionalException e) {
            Assert.assertEquals(IConstantsServiceException.PRESTIGE_TOO_HIGH, e.getCode());
            Assert.assertEquals("exchequerRepartition.request.request.prestige", e.getParams()[0]);
        }
    }

    @Test
    public void testExchequerRepartitionSuccess() throws FunctionalException {
        ExchequerRepartitionBuilder.create()
                .whenRepartition(this, interPhaseService)
                .thenExpect(true);
        ExchequerRepartitionBuilder.create()
                .prestigeSpentBefore(0)
                .whenRepartition(this, interPhaseService)
                .thenExpect(false);
        ExchequerRepartitionBuilder.create()
                .prestigeIncome(100).prestigeSpent(50)
                .whenRepartition(this, interPhaseService)
                .thenExpect(true);
        ExchequerRepartitionBuilder.create()
                .prestigeIncome(100).prestigeSpentBefore(50).prestigeSpent(50)
                .whenRepartition(this, interPhaseService)
                .thenExpect(false);
        ExchequerRepartitionBuilder.create()
                .prestigeIncome(100).prestigeSpentBefore(80).prestigeSpent(50)
                .whenRepartition(this, interPhaseService)
                .thenExpect(true);
    }

    static class ExchequerRepartitionBuilder {
        Integer prestigeSpentBefore;
        Integer prestigeIncome;
        int prestigeSpent;
        EconomicalSheetEntity sheet;
        List<DiffEntity> diffs;

        static ExchequerRepartitionBuilder create() {
            return new ExchequerRepartitionBuilder();
        }

        ExchequerRepartitionBuilder prestigeSpentBefore(Integer prestigeSpentBefore) {
            this.prestigeSpentBefore = prestigeSpentBefore;
            return this;
        }

        ExchequerRepartitionBuilder prestigeIncome(Integer prestigeIncome) {
            this.prestigeIncome = prestigeIncome;
            return this;
        }

        ExchequerRepartitionBuilder prestigeSpent(int prestigeSpent) {
            this.prestigeSpent = prestigeSpent;
            return this;
        }

        ExchequerRepartitionBuilder whenRepartition(InterPhaseServiceTest testClass, IInterPhaseService interPhaseService) throws FunctionalException {
            Pair<Request<ExchequerRepartitionRequest>, GameEntity> pair = testClass.testCheckGame(interPhaseService::exchequerRepartition, "exchequerRepartition");
            Request<ExchequerRepartitionRequest> request = pair.getLeft();
            request.setRequest(new ExchequerRepartitionRequest());
            request.getRequest().setPrestige(prestigeSpent);
            GameEntity game = pair.getRight();
            game.setTurn(6);
            PlayableCountryEntity country = new PlayableCountryEntity();
            country.setName("france");
            country.setId(26L);
            sheet = new EconomicalSheetEntity();
            sheet.setId(17L);
            sheet.setTurn(game.getTurn());
            sheet.setPrestigeSpent(prestigeSpentBefore);
            sheet.setPrestigeIncome(prestigeIncome);
            sheet.setRemainingExpenses(prestigeIncome);
            country.getEconomicalSheets().add(sheet);
            game.getCountries().add(country);
            testClass.testCheckStatus(pair.getRight(), request, interPhaseService::exchequerRepartition, "exchequerRepartition", GameStatusEnum.EXCHEQUER);
            request.getGame().setIdCountry(26L);

            testClass.simulateDiff();

            interPhaseService.exchequerRepartition(request);

            diffs = testClass.retrieveDiffsCreated();

            return this;
        }

        ExchequerRepartitionBuilder thenExpect(boolean ceateDiff) {
            if (ceateDiff) {
                Assert.assertEquals("If old and new prestige spent are not the same, a diff should be created.", 1, diffs.size());

                DiffEntity diff = diffs.stream()
                        .filter(d -> d.getType() == DiffTypeEnum.MODIFY && d.getTypeObject() == DiffTypeObjectEnum.ECO_SHEET
                                && Objects.equals(17L, d.getIdObject()))
                        .findAny()
                        .orElse(null);

                Assert.assertNotNull("The modify eco sheet diff was not created while it should.", diff);
                Assert.assertEquals("The id country in the modify eco sheet is wrong.", "26", getAttribute(diff, DiffAttributeTypeEnum.ID_COUNTRY));
                Assert.assertEquals("The prestige spent attribute in the modify eco sheet diff is wrong.", prestigeSpent + "", getAttribute(diff, DiffAttributeTypeEnum.EXCHEQUER_PRESTIGE_SPENT));
                Assert.assertEquals("The prestige spent in the eco sheet is wrong.", prestigeSpent, sheet.getPrestigeSpent().intValue());
            } else {
                Assert.assertEquals("If old and new prestige spent are the same, no diff should be created.", 0, diffs.size());
                Assert.assertEquals("The prestige spent in the eco sheet is wrong.", prestigeSpentBefore, sheet.getPrestigeSpent());
            }

            return this;
        }
    }

    @Test
    public void testvalidateExchequerFail() {
        Pair<Request<ValidateRequest>, GameEntity> pair = testCheckGame(interPhaseService::validateExchequer, "validateExchequer");
        Request<ValidateRequest> request = pair.getLeft();
        GameEntity game = pair.getRight();

        testCheckStatus(game, request, interPhaseService::validateExchequer, "validateExchequer", GameStatusEnum.EXCHEQUER);

        game.setTurn(22);
        game.getCountries().add(new PlayableCountryEntity());
        game.getCountries().get(0).setId(13L);
        game.getCountries().get(0).setName("france");
        game.getCountries().get(0).setUsername("MKL");

        try {
            interPhaseService.validateExchequer(request);
            Assert.fail("Should break because request.authent is null");
        } catch (FunctionalException e) {
            Assert.assertEquals(IConstantsCommonException.NULL_PARAMETER, e.getCode());
            Assert.assertEquals("validateExchequer.authent", e.getParams()[0]);
        }

        request.setAuthent(new AuthentInfo());
        request.getAuthent().setUsername("toto");

        try {
            interPhaseService.validateExchequer(request);
            Assert.fail("Should break because request.request is null");
        } catch (FunctionalException e) {
            Assert.assertEquals(IConstantsCommonException.NULL_PARAMETER, e.getCode());
            Assert.assertEquals("validateExchequer.request", e.getParams()[0]);
        }

        request.setRequest(new ValidateRequest());

        try {
            interPhaseService.validateExchequer(request);
            Assert.fail("Should break because request.request.idCountry is null");
        } catch (FunctionalException e) {
            Assert.assertEquals(IConstantsCommonException.NULL_PARAMETER, e.getCode());
            Assert.assertEquals("validateExchequer.game.idCountry", e.getParams()[0]);
        }

        request.getGame().setIdCountry(666L);

        try {
            interPhaseService.validateExchequer(request);
            Assert.fail("Should break because request.request.idCountry is invalid");
        } catch (FunctionalException e) {
            Assert.assertEquals(IConstantsCommonException.INVALID_PARAMETER, e.getCode());
            Assert.assertEquals("validateExchequer.idCountry", e.getParams()[0]);
        }

        request.getGame().setIdCountry(13L);

        try {
            interPhaseService.validateExchequer(request);
            Assert.fail("Should break because request.authent can't do this action");
        } catch (FunctionalException e) {
            Assert.assertEquals(IConstantsCommonException.ACCESS_RIGHT, e.getCode());
            Assert.assertEquals("validateExchequer.authent.username", e.getParams()[0]);
        }
    }

    @Test
    public void testValidateAdmActSuccessSimple() throws FunctionalException {
        Request<ValidateRequest> request = new Request<>();
        request.setAuthent(new AuthentInfo());
        request.getAuthent().setUsername("MKL");
        request.setGame(createGameInfo());
        request.setRequest(new ValidateRequest());
        request.getGame().setIdCountry(13L);

        GameEntity game = createGameUsingMocks();
        game.setTurn(22);
        game.setStatus(GameStatusEnum.EXCHEQUER);
        game.getCountries().add(new PlayableCountryEntity());
        game.getCountries().get(0).setId(13L);
        game.getCountries().get(0).setName("france");
        game.getCountries().get(0).setUsername("MKL");
        game.getCountries().get(0).setReady(false);

        simulateDiff();

        interPhaseService.validateExchequer(request);

        List<DiffEntity> diffEntities = retrieveDiffsCreated();

        Assert.assertEquals(0, diffEntities.size());
    }

    @Test
    public void testValidateAdmActSuccessMedium() throws FunctionalException {
        Request<ValidateRequest> request = new Request<>();
        request.setAuthent(new AuthentInfo());
        request.getAuthent().setUsername("MKL");
        request.setGame(createGameInfo());
        request.setRequest(new ValidateRequest());
        request.getGame().setIdCountry(13L);

        GameEntity game = createGameUsingMocks();
        game.setTurn(22);
        game.setStatus(GameStatusEnum.EXCHEQUER);
        game.getCountries().add(new PlayableCountryEntity());
        game.getCountries().get(0).setId(13L);
        game.getCountries().get(0).setName("france");
        game.getCountries().get(0).setUsername("MKL");
        game.getCountries().get(0).setReady(true);

        simulateDiff();

        interPhaseService.validateExchequer(request);

        List<DiffEntity> diffs = retrieveDiffsCreated();

        Assert.assertEquals(1, diffs.size());
        DiffEntity diff = diffs.stream()
                .filter(d -> d.getType() == DiffTypeEnum.INVALIDATE && d.getTypeObject() == DiffTypeObjectEnum.STATUS)
                .findAny()
                .orElse(null);
        Assert.assertNotNull(diff);
        Assert.assertEquals("13", getAttribute(diff, DiffAttributeTypeEnum.ID_COUNTRY));
    }

    @Test
    public void testValidateAdmActSuccessComplex() throws FunctionalException {
        Request<ValidateRequest> request = new Request<>();
        request.setAuthent(new AuthentInfo());
        request.getAuthent().setUsername("MKL");
        request.setGame(createGameInfo());
        request.setRequest(new ValidateRequest());
        request.getGame().setIdCountry(13L);
        request.getRequest().setValidate(true);

        GameEntity game = createGameUsingMocks();
        game.setTurn(22);
        game.setStatus(GameStatusEnum.EXCHEQUER);
        game.getCountries().add(new PlayableCountryEntity());
        game.getCountries().get(0).setId(13L);
        game.getCountries().get(0).setName("france");
        game.getCountries().get(0).setUsername("MKL");
        game.getCountries().get(0).setReady(false);
        game.getCountries().add(new PlayableCountryEntity());
        game.getCountries().get(1).setId(14L);
        game.getCountries().get(1).setName("angleterre");
        game.getCountries().get(1).setReady(false);

        List<DiffEntity> statusDiffs = new ArrayList<>();
        DiffEntity statusDiff = DiffUtil.createDiff(game, DiffTypeEnum.MODIFY, DiffTypeObjectEnum.GAME);
        statusDiffs.add(statusDiff);
        when(statusWorkflowDomain.endExchequerPhase(game)).thenReturn(statusDiffs);

        simulateDiff();

        interPhaseService.validateExchequer(request);

        List<DiffEntity> diffEntities = retrieveDiffsCreated();

        Assert.assertEquals(1, diffEntities.size());
        Assert.assertEquals(statusDiff, diffEntities.get(0));
    }

    @Test
    public void testImproveStability() {
        Pair<Request<ImproveStabilityRequest>, GameEntity> pair = testCheckGame(interPhaseService::improveStability, "improveStability");
        Request<ImproveStabilityRequest> request = pair.getLeft();
        GameEntity game = pair.getRight();

        testCheckStatus(game, request, interPhaseService::improveStability, "improveStability", GameStatusEnum.STABILITY);
        request.getGame().setIdCountry(13L);

        game.setTurn(22);
        PlayableCountryEntity country = new PlayableCountryEntity();
        country.setId(13L);
        country.setName("france");
        game.getCountries().add(country);
        country.setReady(true);
        when(oeUtil.getStability(game, country.getName())).thenReturn(3);

        try {
            interPhaseService.improveStability(request);
            Assert.fail("Should break because request.request is null");
        } catch (FunctionalException e) {
            Assert.assertEquals(IConstantsCommonException.NULL_PARAMETER, e.getCode());
            Assert.assertEquals("improveStability.request", e.getParams()[0]);
        }

        request.setRequest(new ImproveStabilityRequest());

        try {
            interPhaseService.improveStability(request);
            Assert.fail("Should break because improve stability has already been done.");
        } catch (FunctionalException e) {
            Assert.assertEquals(IConstantsServiceException.ACTION_ALREADY_DONE, e.getCode());
            Assert.assertEquals("improveStability.request", e.getParams()[0]);
        }

        country.setReady(false);
        request.getRequest().setInvestment(InvestmentEnum.S);

        try {
            interPhaseService.improveStability(request);
            Assert.fail("Should break because stability is already at max.");
        } catch (FunctionalException e) {
            Assert.assertEquals(IConstantsServiceException.STABILITY_MAX, e.getCode());
            Assert.assertEquals("improveStability.request.investment", e.getParams()[0]);
        }
    }

    @Test
    public void testImproveStabilitySuccess() throws FunctionalException {
        // No try to improve stab
        ImproveStabilityBuilder.create()
                .whenImproveStability(this, interPhaseService)
                .thenExpect(ImproveStabilityResultBuilder.create());
        ImproveStabilityBuilder.create().lastCountry()
                .whenImproveStability(this, interPhaseService)
                .thenExpect(ImproveStabilityResultBuilder.create());

        // 5 + 2 no stability change
        ImproveStabilityBuilder.create()
                .stability(0).investment(InvestmentEnum.S).stabilityModifier(2).stabilityDieRoll(5)
                .whenImproveStability(this, interPhaseService)
                .thenExpect(ImproveStabilityResultBuilder.create()
                        .stabilityCost(30));

        // 3 + 2 stability drops by 1
        ImproveStabilityBuilder.create()
                .stability(0).investment(InvestmentEnum.S).stabilityModifier(2).stabilityDieRoll(3)
                .whenImproveStability(this, interPhaseService)
                .thenExpect(ImproveStabilityResultBuilder.create()
                        .stabilityCost(30).stabilityMoved().newStability(-1));

        // 3 - 15 stability drops by 1
        ImproveStabilityBuilder.create()
                .stability(0).investment(InvestmentEnum.S).stabilityModifier(-15).stabilityDieRoll(3)
                .whenImproveStability(this, interPhaseService)
                .thenExpect(ImproveStabilityResultBuilder.create()
                        .stabilityCost(30).stabilityMoved().newStability(-1));

        // 3 + 2 stability should drop by 1 but it is already at -3
        ImproveStabilityBuilder.create()
                .stability(-3).investment(InvestmentEnum.S).stabilityModifier(2).stabilityDieRoll(3)
                .whenImproveStability(this, interPhaseService)
                .thenExpect(ImproveStabilityResultBuilder.create()
                        .stabilityCost(30));

        // 5 + 8 stability gains 1
        ImproveStabilityBuilder.create()
                .stability(0).investment(InvestmentEnum.S).stabilityModifier(8).stabilityDieRoll(5)
                .whenImproveStability(this, interPhaseService)
                .thenExpect(ImproveStabilityResultBuilder.create()
                        .stabilityCost(30).stabilityMoved().newStability(1));

        // 5 + 8 + 2 (Medium investment) stability gains 2
        ImproveStabilityBuilder.create()
                .stability(0).investment(InvestmentEnum.M).stabilityModifier(8).stabilityDieRoll(5)
                .whenImproveStability(this, interPhaseService)
                .thenExpect(ImproveStabilityResultBuilder.create()
                        .stabilityCost(50).stabilityMoved().newStability(2));

        // 5 + 8 + 5 (High investment) stability gains 3
        ImproveStabilityBuilder.create()
                .stability(0).investment(InvestmentEnum.L).stabilityModifier(8).stabilityDieRoll(5)
                .whenImproveStability(this, interPhaseService)
                .thenExpect(ImproveStabilityResultBuilder.create()
                        .stabilityCost(100).stabilityMoved().newStability(3));

        // 5 + 8 + 5 (High investment) stability should gain 3 but gains only 2 because capped at 3.
        ImproveStabilityBuilder.create()
                .stability(1).investment(InvestmentEnum.L).stabilityModifier(8).stabilityDieRoll(5)
                .whenImproveStability(this, interPhaseService)
                .thenExpect(ImproveStabilityResultBuilder.create()
                        .stabilityCost(100).stabilityMoved().newStability(3));
    }

    static class ImproveStabilityBuilder {
        int stability;
        InvestmentEnum investment;
        Integer stabilityModifier;
        int stabilityDieRoll;
        boolean lastCountry;
        List<DiffEntity> diffs;
        EconomicalSheetEntity sheet;

        static ImproveStabilityBuilder create() {
            return new ImproveStabilityBuilder();
        }

        ImproveStabilityBuilder stability(int stability) {
            this.stability = stability;
            return this;
        }

        ImproveStabilityBuilder investment(InvestmentEnum investment) {
            this.investment = investment;
            return this;
        }

        ImproveStabilityBuilder stabilityModifier(Integer stabilityModifier) {
            this.stabilityModifier = stabilityModifier;
            return this;
        }

        ImproveStabilityBuilder stabilityDieRoll(int stabilityDieRoll) {
            this.stabilityDieRoll = stabilityDieRoll;
            return this;
        }

        ImproveStabilityBuilder lastCountry() {
            this.lastCountry = true;
            return this;
        }

        ImproveStabilityBuilder whenImproveStability(InterPhaseServiceTest testClass, IInterPhaseService interPhaseService) throws FunctionalException {
            Pair<Request<ImproveStabilityRequest>, GameEntity> pair = testClass.testCheckGame(interPhaseService::improveStability, "improveStability");
            Request<ImproveStabilityRequest> request = pair.getLeft();
            request.setRequest(new ImproveStabilityRequest());
            request.getRequest().setInvestment(investment);
            GameEntity game = pair.getRight();
            game.setTurn(6);
            PlayableCountryEntity country = new PlayableCountryEntity();
            country.setName("france");
            country.setId(26L);
            sheet = new EconomicalSheetEntity();
            sheet.setId(17L);
            sheet.setTurn(game.getTurn());
            sheet.setStabModifier(stabilityModifier);
            country.getEconomicalSheets().add(sheet);
            game.getCountries().add(country);
            if (!lastCountry) {
                PlayableCountryEntity otherCountry = new PlayableCountryEntity();
                otherCountry.setName("espagne");
                otherCountry.setId(27L);
                otherCountry.setUsername("toto");
                game.getCountries().add(otherCountry);
            }
            testClass.testCheckStatus(pair.getRight(), request, interPhaseService::improveStability, "improveStability", GameStatusEnum.STABILITY);
            request.getGame().setIdCountry(26L);

            when(testClass.oeUtil.getStability(game, country.getName())).thenReturn(stability);
            when(testClass.oeUtil.rollDie(game, country)).thenReturn(stabilityDieRoll);
            when(testClass.statusWorkflowDomain.endStabilityPhase(game)).thenReturn(Collections.singletonList(DiffUtil.createDiff(game, DiffTypeEnum.MODIFY, DiffTypeObjectEnum.GAME)));
            when(testClass.counterDomain.moveSpecialCounter(any(), any(), any(), any())).thenAnswer(moveSpecialCounterAnswer());

            testClass.simulateDiff();

            interPhaseService.improveStability(request);

            diffs = testClass.retrieveDiffsCreated();

            return this;
        }

        ImproveStabilityBuilder thenExpect(ImproveStabilityResultBuilder result) {
            DiffEntity stabDiff = diffs.stream()
                    .filter(diff -> diff.getType() == DiffTypeEnum.MOVE && diff.getTypeObject() == DiffTypeObjectEnum.COUNTER)
                    .findAny()
                    .orElse(null);
            if (result.stabilityMoved) {
                Assert.assertNotNull("The stability counter move diff is missing.", stabDiff);
                if (result.newStability != null) {
                    String box = GameUtil.getStabilityBox(result.newStability);
                    Assert.assertEquals("The stability counter has moved to the wrong box province.", box, getAttribute(stabDiff, DiffAttributeTypeEnum.PROVINCE_TO));
                }
            } else {
                Assert.assertNull("The stability counter move diff is present while it should not.", stabDiff);
            }

            DiffEntity ecoSheetDiff = diffs.stream()
                    .filter(diff -> diff.getType() == DiffTypeEnum.MODIFY && diff.getTypeObject() == DiffTypeObjectEnum.ECO_SHEET
                            && Objects.equals(sheet.getId(), diff.getIdObject()))
                    .findAny()
                    .orElse(null);
            if (investment != null) {
                Assert.assertNotNull("The modify eco sheet diff is missing.", ecoSheetDiff);
                Assert.assertEquals("The country in the diff is wrong.", "26", getAttribute(ecoSheetDiff, DiffAttributeTypeEnum.ID_COUNTRY));
                Assert.assertEquals("The stability improvement cost in the diff is wrong.", result.stabilityCost + "", getAttribute(ecoSheetDiff, DiffAttributeTypeEnum.STAB));
                Assert.assertEquals("The stability improvement cost is wrong.", result.stabilityCost, sheet.getStab());
                Assert.assertEquals("The stability die roll in the diff is wrong.", stabilityDieRoll + "", getAttribute(ecoSheetDiff, DiffAttributeTypeEnum.STAB_DIE));
                Assert.assertEquals("The stability die roll is wrong.", stabilityDieRoll, sheet.getStabDie().intValue());
                if (result.newStability != null) {
                    String box = GameUtil.getStabilityBox(result.newStability);
                    Assert.assertEquals("The stability counter has moved to the wrong box province.", box, getAttribute(stabDiff, DiffAttributeTypeEnum.PROVINCE_TO));
                }
            } else {
                Assert.assertNull("The modify eco sheet diff is present while it should not.", ecoSheetDiff);
            }

            DiffEntity nextPhaseDiff = diffs.stream()
                    .filter(diff -> diff.getType() == DiffTypeEnum.MODIFY && diff.getTypeObject() == DiffTypeObjectEnum.GAME)
                    .findAny()
                    .orElse(null);
            DiffEntity nextPlayerDiff = diffs.stream()
                    .filter(diff -> diff.getType() == DiffTypeEnum.VALIDATE && diff.getTypeObject() == DiffTypeObjectEnum.STATUS)
                    .findAny()
                    .orElse(null);
            if (lastCountry) {
                Assert.assertNotNull("The next phase diff is missing.", nextPhaseDiff);
                Assert.assertNull("The next player diff is present while it should not.", nextPlayerDiff);
            } else {
                Assert.assertNull("The next phase diff is present while it should not.", nextPhaseDiff);
                Assert.assertNotNull("The next player diff is missing.", nextPlayerDiff);
            }

            return this;
        }
    }

    static class ImproveStabilityResultBuilder {
        boolean stabilityMoved;
        Integer newStability;
        Integer stabilityCost;

        static ImproveStabilityResultBuilder create() {
            return new ImproveStabilityResultBuilder();
        }

        ImproveStabilityResultBuilder stabilityMoved() {
            this.stabilityMoved = true;
            return this;
        }

        ImproveStabilityResultBuilder newStability(Integer newStability) {
            this.newStability = newStability;
            return this;
        }

        ImproveStabilityResultBuilder stabilityCost(Integer stabilityCost) {
            this.stabilityCost = stabilityCost;
            return this;
        }
    }
}
