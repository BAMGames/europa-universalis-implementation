package com.mkl.eu.service.service.service;

import com.mkl.eu.client.common.exception.FunctionalException;
import com.mkl.eu.client.common.exception.IConstantsCommonException;
import com.mkl.eu.client.common.vo.Request;
import com.mkl.eu.client.service.service.IConstantsServiceException;
import com.mkl.eu.client.service.service.military.LandLootingRequest;
import com.mkl.eu.client.service.vo.enumeration.*;
import com.mkl.eu.service.service.domain.ICounterDomain;
import com.mkl.eu.service.service.domain.IStatusWorkflowDomain;
import com.mkl.eu.service.service.persistence.board.ICounterDao;
import com.mkl.eu.service.service.persistence.oe.GameEntity;
import com.mkl.eu.service.service.persistence.oe.board.CounterEntity;
import com.mkl.eu.service.service.persistence.oe.board.StackEntity;
import com.mkl.eu.service.service.persistence.oe.country.PlayableCountryEntity;
import com.mkl.eu.service.service.persistence.oe.diff.DiffEntity;
import com.mkl.eu.service.service.persistence.oe.eco.EconomicalSheetEntity;
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
            when(testClass.counterDomain.createCounter(CounterFaceTypeEnum.PILLAGE_PLUS, null, pillages.isEmpty() ? null : 15L, game))
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
}
