package com.mkl.eu.service.service.service;

import com.mkl.eu.client.common.exception.FunctionalException;
import com.mkl.eu.client.common.exception.IConstantsCommonException;
import com.mkl.eu.client.common.vo.AuthentInfo;
import com.mkl.eu.client.common.vo.GameInfo;
import com.mkl.eu.client.common.vo.Request;
import com.mkl.eu.client.service.service.eco.AddAdminActionRequest;
import com.mkl.eu.client.service.service.eco.RemoveAdminActionRequest;
import com.mkl.eu.client.service.vo.diff.Diff;
import com.mkl.eu.client.service.vo.diff.DiffResponse;
import com.mkl.eu.client.service.vo.enumeration.*;
import com.mkl.eu.service.service.mapping.GameMapping;
import com.mkl.eu.service.service.mapping.chat.ChatMapping;
import com.mkl.eu.service.service.mapping.diff.DiffMapping;
import com.mkl.eu.service.service.persistence.IGameDao;
import com.mkl.eu.service.service.persistence.board.ICounterDao;
import com.mkl.eu.service.service.persistence.board.IStackDao;
import com.mkl.eu.service.service.persistence.chat.IChatDao;
import com.mkl.eu.service.service.persistence.diff.IDiffDao;
import com.mkl.eu.service.service.persistence.eco.IAdminActionDao;
import com.mkl.eu.service.service.persistence.oe.GameEntity;
import com.mkl.eu.service.service.persistence.oe.board.CounterEntity;
import com.mkl.eu.service.service.persistence.oe.board.StackEntity;
import com.mkl.eu.service.service.persistence.oe.country.PlayableCountryEntity;
import com.mkl.eu.service.service.persistence.oe.diff.DiffEntity;
import com.mkl.eu.service.service.persistence.oe.eco.AdministrativeActionEntity;
import com.mkl.eu.service.service.service.impl.EconomicServiceImpl;
import com.mkl.eu.service.service.socket.SocketHandler;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InOrder;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import java.util.ArrayList;
import java.util.List;

import static org.mockito.Matchers.anyObject;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.when;

/**
 * Test of BoardService.
 *
 * @author MKL.
 */
@RunWith(MockitoJUnitRunner.class)
public class EcoServiceTest {
    @InjectMocks
    private EconomicServiceImpl economicService;

    @Mock
    private IGameDao gameDao;

    @Mock
    private IAdminActionDao adminActionDao;

    @Mock
    private ICounterDao counterDao;

    @Mock
    private IStackDao stackDao;

    @Mock
    private IChatDao chatDao;

    @Mock
    private IDiffDao diffDao;

    @Mock
    private GameMapping gameMapping;

    @Mock
    private ChatMapping chatMapping;

    @Mock
    private DiffMapping diffMapping;

    @Mock
    private SocketHandler socketHandler;

    /** Variable used to store something coming from a mock. */
    private DiffEntity diffEntity;

    @Test
    public void testAddAdmActFailSimple() {
        try {
            economicService.addAdminAction(null);
            Assert.fail("Should break because addAdminAction is null");
        } catch (FunctionalException e) {
            Assert.assertEquals(IConstantsCommonException.NULL_PARAMETER, e.getCode());
            Assert.assertEquals("addAdminAction", e.getParams()[0]);
        }

        Request<AddAdminActionRequest> request = new Request<>();

        try {
            economicService.addAdminAction(request);
            Assert.fail("Should break because addAdminAction.authent is null");
        } catch (FunctionalException e) {
            Assert.assertEquals(IConstantsCommonException.NULL_PARAMETER, e.getCode());
            Assert.assertEquals("addAdminAction.authent", e.getParams()[0]);
        }

        request.setAuthent(new AuthentInfo());

        try {
            economicService.addAdminAction(request);
            Assert.fail("Should break because addAdminAction.game is null");
        } catch (FunctionalException e) {
            Assert.assertEquals(IConstantsCommonException.NULL_PARAMETER, e.getCode());
            Assert.assertEquals("addAdminAction.game", e.getParams()[0]);
        }

        request.setGame(new GameInfo());

        try {
            economicService.addAdminAction(request);
            Assert.fail("Should break because idGame is null");
        } catch (FunctionalException e) {
            Assert.assertEquals(IConstantsCommonException.NULL_PARAMETER, e.getCode());
            Assert.assertEquals("addAdminAction.game.idGame", e.getParams()[0]);
        }

        request.getGame().setIdGame(12L);

        try {
            economicService.addAdminAction(request);
            Assert.fail("Should break because versionGame is null");
        } catch (FunctionalException e) {
            Assert.assertEquals(IConstantsCommonException.NULL_PARAMETER, e.getCode());
            Assert.assertEquals("addAdminAction.game.versionGame", e.getParams()[0]);
        }

        request.getGame().setVersionGame(1L);

        try {
            economicService.addAdminAction(request);
            Assert.fail("Should break because game does not exist");
        } catch (FunctionalException e) {
            Assert.assertEquals(IConstantsCommonException.INVALID_PARAMETER, e.getCode());
            Assert.assertEquals("addAdminAction.game.idGame", e.getParams()[0]);
        }

        GameEntity game = new GameEntity();
        game.setId(12L);
        game.setTurn(1);
        game.getCountries().add(new PlayableCountryEntity());
        game.getCountries().get(0).setId(12L);
        game.getCountries().get(0).setName("france");
        game.getStacks().add(new StackEntity());
        game.getStacks().get(0).getCounters().add(new CounterEntity());
        game.getStacks().get(0).getCounters().get(0).setId(2L);
        game.getStacks().get(0).getCounters().get(0).setCountry("angleterre");
        game.getStacks().add(new StackEntity());
        game.getStacks().get(1).getCounters().add(new CounterEntity());
        game.getStacks().get(1).getCounters().get(0).setId(3L);
        game.getStacks().get(1).getCounters().get(0).setCountry("france");
        game.getStacks().get(1).getCounters().get(0).setType(CounterFaceTypeEnum.MNU_ART_MINUS);
        game.getStacks().get(1).getCounters().add(new CounterEntity());
        game.getStacks().get(1).getCounters().get(1).setId(4L);
        game.getStacks().get(1).getCounters().get(1).setCountry("france");
        game.getStacks().get(1).getCounters().get(1).setType(CounterFaceTypeEnum.FLEET_MINUS);

        when(gameDao.lock(12L)).thenReturn(game);

        try {
            economicService.addAdminAction(request);
            Assert.fail("Should break because versions does not match");
        } catch (FunctionalException e) {
            Assert.assertEquals(IConstantsCommonException.INVALID_PARAMETER, e.getCode());
            Assert.assertEquals("addAdminAction.game.versionGame", e.getParams()[0]);
        }

        game.setVersion(5L);

        try {
            economicService.addAdminAction(request);
            Assert.fail("Should break because addAdminAction.request is null");
        } catch (FunctionalException e) {
            Assert.assertEquals(IConstantsCommonException.NULL_PARAMETER, e.getCode());
            Assert.assertEquals("addAdminAction.request", e.getParams()[0]);
        }

        request.setRequest(new AddAdminActionRequest());

        try {
            economicService.addAdminAction(request);
            Assert.fail("Should break because idCountry is null");
        } catch (FunctionalException e) {
            Assert.assertEquals(IConstantsCommonException.NULL_PARAMETER, e.getCode());
            Assert.assertEquals("addAdminAction.request.idCountry", e.getParams()[0]);
        }

        request.getRequest().setIdCountry(11L);

        try {
            economicService.addAdminAction(request);
            Assert.fail("Should break because idCountry is not found");
        } catch (FunctionalException e) {
            Assert.assertEquals(IConstantsCommonException.INVALID_PARAMETER, e.getCode());
            Assert.assertEquals("addAdminAction.request.idCountry", e.getParams()[0]);
        }

        request.getRequest().setIdCountry(12L);

        try {
            economicService.addAdminAction(request);
            Assert.fail("Should break because type is null");
        } catch (FunctionalException e) {
            Assert.assertEquals(IConstantsCommonException.NULL_PARAMETER, e.getCode());
            Assert.assertEquals("addAdminAction.request.type", e.getParams()[0]);
        }

        request.getRequest().setType(AdminActionTypeEnum.DIS);

        try {
            economicService.addAdminAction(request);
            Assert.fail("Should break because idObject is null");
        } catch (FunctionalException e) {
            Assert.assertEquals(IConstantsCommonException.NULL_PARAMETER, e.getCode());
            Assert.assertEquals("addAdminAction.request.idObject", e.getParams()[0]);
        }

        request.getRequest().setIdObject(1L);

        try {
            economicService.addAdminAction(request);
            Assert.fail("Should break because idObject is invalid");
        } catch (FunctionalException e) {
            Assert.assertEquals(IConstantsCommonException.INVALID_PARAMETER, e.getCode());
            Assert.assertEquals("addAdminAction.request.idObject", e.getParams()[0]);
        }

        request.getRequest().setIdObject(2L);

        try {
            economicService.addAdminAction(request);
            Assert.fail("Should break because idObject is invalid");
        } catch (FunctionalException e) {
            Assert.assertEquals(IConstantsCommonException.INVALID_PARAMETER, e.getCode());
            Assert.assertEquals("addAdminAction.request.idObject", e.getParams()[0]);
        }

        request.getRequest().setIdObject(3L);

        try {
            economicService.addAdminAction(request);
            Assert.fail("Should break because idObject is invalid");
        } catch (FunctionalException e) {
            Assert.assertEquals(IConstantsCommonException.INVALID_PARAMETER, e.getCode());
            Assert.assertEquals("addAdminAction.request.idObject", e.getParams()[0]);
        }

        request.getRequest().setIdObject(4L);
        request.getRequest().setType(AdminActionTypeEnum.LM);

        try {
            economicService.addAdminAction(request);
            Assert.fail("Should break because idObject is invalid");
        } catch (FunctionalException e) {
            Assert.assertEquals(IConstantsCommonException.INVALID_PARAMETER, e.getCode());
            Assert.assertEquals("addAdminAction.request.idObject", e.getParams()[0]);
        }

        request.getRequest().setType(AdminActionTypeEnum.DIS);

        List<AdministrativeActionEntity> actions = new ArrayList<>();
        actions.add(new AdministrativeActionEntity());
        when(adminActionDao.findAdminActions(12L, 1, 4L, AdminActionTypeEnum.LM, AdminActionTypeEnum.DIS)).thenReturn(actions);

        try {
            economicService.addAdminAction(request);
            Assert.fail("Should break because idObject is invalid");
        } catch (FunctionalException e) {
            Assert.assertEquals(IConstantsCommonException.INVALID_PARAMETER, e.getCode());
            Assert.assertEquals("addAdminAction.request.idObject", e.getParams()[0]);
        }
    }

    @Test
    public void testAddAdmActLMSuccess() throws Exception {
        Request<AddAdminActionRequest> request = new Request<>();
        request.setRequest(new AddAdminActionRequest());
        request.setAuthent(new AuthentInfo());
        request.setGame(new GameInfo());
        request.getGame().setIdGame(12L);
        request.getGame().setVersionGame(1L);
        request.getRequest().setIdCountry(14L);
        request.getRequest().setType(AdminActionTypeEnum.LM);
        request.getRequest().setIdObject(4L);

        GameEntity game = new GameEntity();
        game.setId(12L);
        game.setVersion(5L);
        game.setTurn(2);
        game.getCountries().add(new PlayableCountryEntity());
        game.getCountries().get(0).setId(14L);
        game.getCountries().get(0).setName("france");
        game.getStacks().add(new StackEntity());
        game.getStacks().get(0).getCounters().add(new CounterEntity());
        game.getStacks().get(0).getCounters().get(0).setId(2L);
        game.getStacks().get(0).getCounters().get(0).setCountry("angleterre");
        game.getStacks().add(new StackEntity());
        game.getStacks().get(1).getCounters().add(new CounterEntity());
        game.getStacks().get(1).getCounters().get(0).setId(3L);
        game.getStacks().get(1).getCounters().get(0).setCountry("france");
        game.getStacks().get(1).getCounters().get(0).setType(CounterFaceTypeEnum.MNU_ART_MINUS);
        game.getStacks().get(1).getCounters().add(new CounterEntity());
        game.getStacks().get(1).getCounters().get(1).setId(4L);
        game.getStacks().get(1).getCounters().get(1).setCountry("france");
        game.getStacks().get(1).getCounters().get(1).setType(CounterFaceTypeEnum.ARMY_MINUS);

        when(gameDao.lock(12L)).thenReturn(game);

        List<DiffEntity> diffBefore = new ArrayList<>();
        diffBefore.add(new DiffEntity());
        diffBefore.add(new DiffEntity());

        when(diffDao.getDiffsSince(12L, 1L)).thenReturn(diffBefore);

        when(adminActionDao.create(anyObject())).thenAnswer(invocation -> {
            AdministrativeActionEntity action = (AdministrativeActionEntity) invocation.getArguments()[0];
            action.setId(13L);
            return action;
        });

        List<Diff> diffAfter = new ArrayList<>();
        diffAfter.add(new Diff());
        diffAfter.add(new Diff());

        when(diffMapping.oesToVos(anyObject())).thenAnswer(invocation -> {
            diffEntity = ((List<DiffEntity>) invocation.getArguments()[0]).get(2);
            return diffAfter;
        });

        DiffResponse response = economicService.addAdminAction(request);

        InOrder inOrder = inOrder(gameDao, adminActionDao, diffDao, diffMapping);

        inOrder.verify(gameDao).lock(12L);
        inOrder.verify(diffDao).getDiffsSince(12L, 1L);
        inOrder.verify(adminActionDao).findAdminActions(request.getRequest().getIdCountry(), game.getTurn(), request.getRequest().getIdObject(), AdminActionTypeEnum.LM, AdminActionTypeEnum.DIS);
        inOrder.verify(adminActionDao).create(anyObject());
        inOrder.verify(diffMapping).oesToVos(anyObject());

        Assert.assertEquals(13L, diffEntity.getIdObject().longValue());
        Assert.assertEquals(game.getVersion(), diffEntity.getVersionGame().longValue());
        Assert.assertEquals(DiffTypeEnum.ADD, diffEntity.getType());
        Assert.assertEquals(DiffTypeObjectEnum.ADM_ACT, diffEntity.getTypeObject());
        Assert.assertEquals(12L, diffEntity.getIdGame().longValue());
        Assert.assertEquals(game.getVersion(), diffEntity.getVersionGame().longValue());
        Assert.assertEquals(4, diffEntity.getAttributes().size());
        Assert.assertEquals(DiffAttributeTypeEnum.ID_COUNTRY, diffEntity.getAttributes().get(0).getType());
        Assert.assertEquals(request.getRequest().getIdCountry().toString(), diffEntity.getAttributes().get(0).getValue());
        Assert.assertEquals(DiffAttributeTypeEnum.TURN, diffEntity.getAttributes().get(1).getType());
        Assert.assertEquals(game.getTurn().toString(), diffEntity.getAttributes().get(1).getValue());
        Assert.assertEquals(DiffAttributeTypeEnum.TYPE, diffEntity.getAttributes().get(2).getType());
        Assert.assertEquals(request.getRequest().getType().name(), diffEntity.getAttributes().get(2).getValue());
        Assert.assertEquals(DiffAttributeTypeEnum.ID_OBJECT, diffEntity.getAttributes().get(3).getType());
        Assert.assertEquals(request.getRequest().getIdObject().toString(), diffEntity.getAttributes().get(3).getValue());

        Assert.assertEquals(game.getVersion(), response.getVersionGame().longValue());
        Assert.assertEquals(diffAfter, response.getDiffs());
    }

    @Test
    public void testAddAdmActDISSuccess() throws Exception {
        Request<AddAdminActionRequest> request = new Request<>();
        request.setRequest(new AddAdminActionRequest());
        request.setAuthent(new AuthentInfo());
        request.setGame(new GameInfo());
        request.getGame().setIdGame(12L);
        request.getGame().setVersionGame(1L);
        request.getRequest().setIdCountry(14L);
        request.getRequest().setType(AdminActionTypeEnum.DIS);
        request.getRequest().setIdObject(4L);

        GameEntity game = new GameEntity();
        game.setId(12L);
        game.setVersion(5L);
        game.setTurn(2);
        game.getCountries().add(new PlayableCountryEntity());
        game.getCountries().get(0).setId(14L);
        game.getCountries().get(0).setName("france");
        game.getStacks().add(new StackEntity());
        game.getStacks().get(0).getCounters().add(new CounterEntity());
        game.getStacks().get(0).getCounters().get(0).setId(2L);
        game.getStacks().get(0).getCounters().get(0).setCountry("angleterre");
        game.getStacks().add(new StackEntity());
        game.getStacks().get(1).getCounters().add(new CounterEntity());
        game.getStacks().get(1).getCounters().get(0).setId(3L);
        game.getStacks().get(1).getCounters().get(0).setCountry("france");
        game.getStacks().get(1).getCounters().get(0).setType(CounterFaceTypeEnum.MNU_ART_MINUS);
        game.getStacks().get(1).getCounters().add(new CounterEntity());
        game.getStacks().get(1).getCounters().get(1).setId(4L);
        game.getStacks().get(1).getCounters().get(1).setCountry("france");
        game.getStacks().get(1).getCounters().get(1).setType(CounterFaceTypeEnum.ARMY_MINUS);

        when(gameDao.lock(12L)).thenReturn(game);

        List<DiffEntity> diffBefore = new ArrayList<>();
        diffBefore.add(new DiffEntity());
        diffBefore.add(new DiffEntity());

        when(diffDao.getDiffsSince(12L, 1L)).thenReturn(diffBefore);

        when(adminActionDao.create(anyObject())).thenAnswer(invocation -> {
            AdministrativeActionEntity action = (AdministrativeActionEntity) invocation.getArguments()[0];
            action.setId(13L);
            return action;
        });

        List<Diff> diffAfter = new ArrayList<>();
        diffAfter.add(new Diff());
        diffAfter.add(new Diff());

        when(diffMapping.oesToVos(anyObject())).thenAnswer(invocation -> {
            diffEntity = ((List<DiffEntity>) invocation.getArguments()[0]).get(2);
            return diffAfter;
        });

        DiffResponse response = economicService.addAdminAction(request);

        InOrder inOrder = inOrder(gameDao, adminActionDao, diffDao, diffMapping);

        inOrder.verify(gameDao).lock(12L);
        inOrder.verify(diffDao).getDiffsSince(12L, 1L);
        inOrder.verify(adminActionDao).findAdminActions(request.getRequest().getIdCountry(), game.getTurn(), request.getRequest().getIdObject(), AdminActionTypeEnum.LM, AdminActionTypeEnum.DIS);
        inOrder.verify(adminActionDao).create(anyObject());
        inOrder.verify(diffMapping).oesToVos(anyObject());

        Assert.assertEquals(13L, diffEntity.getIdObject().longValue());
        Assert.assertEquals(game.getVersion(), diffEntity.getVersionGame().longValue());
        Assert.assertEquals(DiffTypeEnum.ADD, diffEntity.getType());
        Assert.assertEquals(DiffTypeObjectEnum.ADM_ACT, diffEntity.getTypeObject());
        Assert.assertEquals(12L, diffEntity.getIdGame().longValue());
        Assert.assertEquals(game.getVersion(), diffEntity.getVersionGame().longValue());
        Assert.assertEquals(4, diffEntity.getAttributes().size());
        Assert.assertEquals(DiffAttributeTypeEnum.ID_COUNTRY, diffEntity.getAttributes().get(0).getType());
        Assert.assertEquals(request.getRequest().getIdCountry().toString(), diffEntity.getAttributes().get(0).getValue());
        Assert.assertEquals(DiffAttributeTypeEnum.TURN, diffEntity.getAttributes().get(1).getType());
        Assert.assertEquals(game.getTurn().toString(), diffEntity.getAttributes().get(1).getValue());
        Assert.assertEquals(DiffAttributeTypeEnum.TYPE, diffEntity.getAttributes().get(2).getType());
        Assert.assertEquals(request.getRequest().getType().name(), diffEntity.getAttributes().get(2).getValue());
        Assert.assertEquals(DiffAttributeTypeEnum.ID_OBJECT, diffEntity.getAttributes().get(3).getType());
        Assert.assertEquals(request.getRequest().getIdObject().toString(), diffEntity.getAttributes().get(3).getValue());

        Assert.assertEquals(game.getVersion(), response.getVersionGame().longValue());
        Assert.assertEquals(diffAfter, response.getDiffs());
    }

    @Test
    public void testRemoveAdmActFailSimple() {
        try {
            economicService.removeAdminAction(null);
            Assert.fail("Should break because removeAdminAction is null");
        } catch (FunctionalException e) {
            Assert.assertEquals(IConstantsCommonException.NULL_PARAMETER, e.getCode());
            Assert.assertEquals("removeAdminAction", e.getParams()[0]);
        }

        Request<RemoveAdminActionRequest> request = new Request<>();

        try {
            economicService.removeAdminAction(request);
            Assert.fail("Should break because removeAdminAction.authent is null");
        } catch (FunctionalException e) {
            Assert.assertEquals(IConstantsCommonException.NULL_PARAMETER, e.getCode());
            Assert.assertEquals("removeAdminAction.authent", e.getParams()[0]);
        }

        request.setAuthent(new AuthentInfo());
        request.getAuthent().setUsername("MKL");

        try {
            economicService.removeAdminAction(request);
            Assert.fail("Should break because removeAdminAction.game is null");
        } catch (FunctionalException e) {
            Assert.assertEquals(IConstantsCommonException.NULL_PARAMETER, e.getCode());
            Assert.assertEquals("removeAdminAction.game", e.getParams()[0]);
        }

        request.setGame(new GameInfo());

        try {
            economicService.removeAdminAction(request);
            Assert.fail("Should break because idGame is null");
        } catch (FunctionalException e) {
            Assert.assertEquals(IConstantsCommonException.NULL_PARAMETER, e.getCode());
            Assert.assertEquals("removeAdminAction.game.idGame", e.getParams()[0]);
        }

        request.getGame().setIdGame(12L);

        try {
            economicService.removeAdminAction(request);
            Assert.fail("Should break because versionGame is null");
        } catch (FunctionalException e) {
            Assert.assertEquals(IConstantsCommonException.NULL_PARAMETER, e.getCode());
            Assert.assertEquals("removeAdminAction.game.versionGame", e.getParams()[0]);
        }

        request.getGame().setVersionGame(1L);

        try {
            economicService.removeAdminAction(request);
            Assert.fail("Should break because game does not exist");
        } catch (FunctionalException e) {
            Assert.assertEquals(IConstantsCommonException.INVALID_PARAMETER, e.getCode());
            Assert.assertEquals("removeAdminAction.game.idGame", e.getParams()[0]);
        }

        GameEntity game = new GameEntity();
        game.setId(12L);

        when(gameDao.lock(12L)).thenReturn(game);

        try {
            economicService.removeAdminAction(request);
            Assert.fail("Should break because versions does not match");
        } catch (FunctionalException e) {
            Assert.assertEquals(IConstantsCommonException.INVALID_PARAMETER, e.getCode());
            Assert.assertEquals("removeAdminAction.game.versionGame", e.getParams()[0]);
        }

        game.setVersion(5L);

        try {
            economicService.removeAdminAction(request);
            Assert.fail("Should break because removeAdminAction.request is null");
        } catch (FunctionalException e) {
            Assert.assertEquals(IConstantsCommonException.NULL_PARAMETER, e.getCode());
            Assert.assertEquals("removeAdminAction.request", e.getParams()[0]);
        }

        request.setRequest(new RemoveAdminActionRequest());

        try {
            economicService.removeAdminAction(request);
            Assert.fail("Should break because idAdmAct is null");
        } catch (FunctionalException e) {
            Assert.assertEquals(IConstantsCommonException.NULL_PARAMETER, e.getCode());
            Assert.assertEquals("removeAdminAction.request.idAdmAct", e.getParams()[0]);
        }

        request.getRequest().setIdAdmAct(11L);

        try {
            economicService.removeAdminAction(request);
            Assert.fail("Should break because idAdmAct is not found");
        } catch (FunctionalException e) {
            Assert.assertEquals(IConstantsCommonException.INVALID_PARAMETER, e.getCode());
            Assert.assertEquals("removeAdminAction.request.idAdmAct", e.getParams()[0]);
        }

        AdministrativeActionEntity action12 = new AdministrativeActionEntity();
        action12.setId(12L);
        action12.setCountry(new PlayableCountryEntity());
        action12.getCountry().setUsername("Jym");
        when(adminActionDao.load(12L)).thenReturn(action12);

        request.getRequest().setIdAdmAct(12L);

        try {
            economicService.removeAdminAction(request);
            Assert.fail("Should break because idAdmAct is not owned by the player");
        } catch (FunctionalException e) {
            Assert.assertEquals(IConstantsCommonException.ACCESS_RIGHT, e.getCode());
            Assert.assertEquals("removeAdminAction.authent.username", e.getParams()[0]);
        }

        AdministrativeActionEntity action13 = new AdministrativeActionEntity();
        action13.setId(13L);
        action13.setCountry(new PlayableCountryEntity());
        action13.getCountry().setUsername("MKL");
        when(adminActionDao.load(13L)).thenReturn(action13);

        request.getRequest().setIdAdmAct(13L);

        try {
            economicService.removeAdminAction(request);
            Assert.fail("Should break because idAdmAct is not planned");
        } catch (FunctionalException e) {
            Assert.assertEquals(IConstantsCommonException.INVALID_PARAMETER, e.getCode());
            Assert.assertEquals("removeAdminAction.request.idAdmAct", e.getParams()[0]);
        }
    }

    @Test
    public void testRemoveAdmActSuccess() throws FunctionalException {
        Request<RemoveAdminActionRequest> request = new Request<>();
        request.setAuthent(new AuthentInfo());
        request.getAuthent().setUsername("MKL");
        request.setGame(new GameInfo());
        request.getGame().setIdGame(12L);
        request.getGame().setVersionGame(1L);

        request.setRequest(new RemoveAdminActionRequest());

        request.getRequest().setIdAdmAct(13L);

        GameEntity game = new GameEntity();
        game.setId(12L);
        game.setVersion(5L);

        when(gameDao.lock(12L)).thenReturn(game);

        AdministrativeActionEntity action13 = new AdministrativeActionEntity();
        action13.setId(13L);
        action13.setCountry(new PlayableCountryEntity());
        action13.getCountry().setId(666L);
        action13.getCountry().setUsername("MKL");
        action13.setType(AdminActionTypeEnum.DIS);
        action13.setStatus(AdminActionStatusEnum.PLANNED);

        when(adminActionDao.load(13L)).thenReturn(action13);

        List<DiffEntity> diffBefore = new ArrayList<>();
        diffBefore.add(new DiffEntity());
        diffBefore.add(new DiffEntity());

        when(diffDao.getDiffsSince(12L, 1L)).thenReturn(diffBefore);

        List<Diff> diffAfter = new ArrayList<>();
        diffAfter.add(new Diff());
        diffAfter.add(new Diff());

        when(diffMapping.oesToVos(anyObject())).thenAnswer(invocation -> {
            diffEntity = ((List<DiffEntity>) invocation.getArguments()[0]).get(2);
            return diffAfter;
        });

        DiffResponse response = economicService.removeAdminAction(request);

        InOrder inOrder = inOrder(gameDao, adminActionDao, diffDao, diffMapping);

        inOrder.verify(gameDao).lock(12L);
        inOrder.verify(diffDao).getDiffsSince(12L, 1L);
        inOrder.verify(adminActionDao).load(request.getRequest().getIdAdmAct());
        inOrder.verify(adminActionDao).delete(action13);
        inOrder.verify(diffMapping).oesToVos(anyObject());

        Assert.assertEquals(13L, diffEntity.getIdObject().longValue());
        Assert.assertEquals(game.getVersion(), diffEntity.getVersionGame().longValue());
        Assert.assertEquals(DiffTypeEnum.REMOVE, diffEntity.getType());
        Assert.assertEquals(DiffTypeObjectEnum.ADM_ACT, diffEntity.getTypeObject());
        Assert.assertEquals(12L, diffEntity.getIdGame().longValue());
        Assert.assertEquals(game.getVersion(), diffEntity.getVersionGame().longValue());
        Assert.assertEquals(2, diffEntity.getAttributes().size());
        Assert.assertEquals(DiffAttributeTypeEnum.ID_COUNTRY, diffEntity.getAttributes().get(0).getType());
        Assert.assertEquals(action13.getCountry().getId().toString(), diffEntity.getAttributes().get(0).getValue());
        Assert.assertEquals(DiffAttributeTypeEnum.TYPE, diffEntity.getAttributes().get(1).getType());
        Assert.assertEquals(action13.getType().name(), diffEntity.getAttributes().get(1).getValue());

        Assert.assertEquals(game.getVersion(), response.getVersionGame().longValue());
        Assert.assertEquals(diffAfter, response.getDiffs());
    }
}
