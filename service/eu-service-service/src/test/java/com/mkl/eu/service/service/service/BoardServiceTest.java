package com.mkl.eu.service.service.service;

import com.mkl.eu.client.common.exception.FunctionalException;
import com.mkl.eu.client.common.exception.IConstantsCommonException;
import com.mkl.eu.client.common.vo.AuthentInfo;
import com.mkl.eu.client.common.vo.GameInfo;
import com.mkl.eu.client.common.vo.Request;
import com.mkl.eu.client.service.service.board.LoadGameRequest;
import com.mkl.eu.client.service.service.board.MoveCounterRequest;
import com.mkl.eu.client.service.service.board.MoveStackRequest;
import com.mkl.eu.client.service.vo.Game;
import com.mkl.eu.client.service.vo.chat.Chat;
import com.mkl.eu.client.service.vo.diff.Diff;
import com.mkl.eu.client.service.vo.diff.DiffResponse;
import com.mkl.eu.client.service.vo.enumeration.DiffAttributeTypeEnum;
import com.mkl.eu.client.service.vo.enumeration.DiffTypeEnum;
import com.mkl.eu.client.service.vo.enumeration.DiffTypeObjectEnum;
import com.mkl.eu.service.service.mapping.GameMapping;
import com.mkl.eu.service.service.mapping.chat.ChatMapping;
import com.mkl.eu.service.service.mapping.diff.DiffMapping;
import com.mkl.eu.service.service.persistence.IGameDao;
import com.mkl.eu.service.service.persistence.board.ICounterDao;
import com.mkl.eu.service.service.persistence.board.IStackDao;
import com.mkl.eu.service.service.persistence.chat.IChatDao;
import com.mkl.eu.service.service.persistence.diff.IDiffDao;
import com.mkl.eu.service.service.persistence.oe.GameEntity;
import com.mkl.eu.service.service.persistence.oe.board.CounterEntity;
import com.mkl.eu.service.service.persistence.oe.board.StackEntity;
import com.mkl.eu.service.service.persistence.oe.chat.ChatEntity;
import com.mkl.eu.service.service.persistence.oe.chat.MessageGlobalEntity;
import com.mkl.eu.service.service.persistence.oe.chat.RoomEntity;
import com.mkl.eu.service.service.persistence.oe.country.PlayableCountryEntity;
import com.mkl.eu.service.service.persistence.oe.diff.DiffEntity;
import com.mkl.eu.service.service.persistence.oe.ref.province.BorderEntity;
import com.mkl.eu.service.service.persistence.oe.ref.province.EuropeanProvinceEntity;
import com.mkl.eu.service.service.persistence.ref.IProvinceDao;
import com.mkl.eu.service.service.service.impl.BoardServiceImpl;
import com.mkl.eu.service.service.socket.SocketHandler;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InOrder;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import java.util.ArrayList;
import java.util.List;

import static org.mockito.Matchers.anyLong;
import static org.mockito.Matchers.anyObject;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.when;

/**
 * Test of BoardService.
 *
 * @author MKL.
 */
@RunWith(MockitoJUnitRunner.class)
public class BoardServiceTest {
    @InjectMocks
    private BoardServiceImpl boardService;

    @Mock
    private IGameDao gameDao;

    @Mock
    private IProvinceDao provinceDao;

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
    public void testLoadGame() throws Exception {
        try {
            boardService.loadGame(null);
            Assert.fail("Should break because loadGame is null");
        } catch (FunctionalException e) {
            Assert.assertEquals(IConstantsCommonException.NULL_PARAMETER, e.getCode());
            Assert.assertEquals("loadGame", e.getParams()[0]);
        }

        Request<LoadGameRequest> request = new Request<>();

        try {
            boardService.loadGame(request);
            Assert.fail("Should break because loadGame.request is null");
        } catch (FunctionalException e) {
            Assert.assertEquals(IConstantsCommonException.NULL_PARAMETER, e.getCode());
            Assert.assertEquals("loadGame.request", e.getParams()[0]);
        }

        request.setRequest(new LoadGameRequest());

        try {
            boardService.loadGame(request);
            Assert.fail("Should break because loadGame.request.idGame is null");
        } catch (FunctionalException e) {
            Assert.assertEquals(IConstantsCommonException.NULL_PARAMETER, e.getCode());
            Assert.assertEquals("loadGame.request.idGame", e.getParams()[0]);
        }

        Long idGame = 12L;
        Long idCountry = 13L;

        request.getRequest().setIdGame(idGame);

        GameEntity gameOe = new GameEntity();
        Game gameVo = new Game();
        List<MessageGlobalEntity> globalMessages = new ArrayList<>();
        List<RoomEntity> rooms = new ArrayList<>();
        List<ChatEntity> messages = new ArrayList<>();

        when(gameDao.read(idGame)).thenReturn(gameOe);
        when(gameMapping.oeToVo(gameOe, null)).thenReturn(gameVo);
        when(gameMapping.oeToVo(gameOe, idCountry)).thenReturn(gameVo);
        when(chatDao.getGlobalMessages(idGame)).thenReturn(globalMessages);
        when(chatDao.getRooms(idGame, idCountry)).thenReturn(rooms);
        when(chatDao.getMessages(idCountry)).thenReturn(messages);
        when(chatMapping.getChat(globalMessages, rooms, messages, idCountry)).thenReturn(new Chat());

        boardService.loadGame(request);

        InOrder inOrder = inOrder(gameDao, gameMapping, chatDao, chatMapping);
        inOrder.verify(gameDao).read(idGame);
        inOrder.verify(gameMapping).oeToVo(gameOe, null);
        inOrder.verify(chatDao).getGlobalMessages(idGame);
        inOrder.verify(chatMapping).getChat(globalMessages, null, null, null);

        request.getRequest().setIdCountry(idCountry);

        try {
            boardService.loadGame(request);
            Assert.fail("Should break because loadGame.request.idCountry is incorrect");
        } catch (FunctionalException e) {
            Assert.assertEquals(IConstantsCommonException.INVALID_PARAMETER, e.getCode());
            Assert.assertEquals("loadGame.request.idCountry", e.getParams()[0]);
        }

        request.setAuthent(new AuthentInfo("toto", null));

        gameOe.getCountries().add(new PlayableCountryEntity());
        gameOe.getCountries().get(0).setId(idCountry);
        gameOe.getCountries().get(0).setUsername("toto");

        boardService.loadGame(request);

        inOrder = inOrder(gameDao, gameMapping, chatDao, chatMapping);
        inOrder.verify(gameDao).read(idGame);
        inOrder.verify(gameMapping).oeToVo(gameOe, idCountry);
        inOrder.verify(chatDao).getGlobalMessages(idGame);
        inOrder.verify(chatDao).getRooms(idGame, idCountry);
        inOrder.verify(chatDao).getMessages(idCountry);
        inOrder.verify(chatMapping).getChat(globalMessages, rooms, messages, idCountry);
    }

    @Test
    public void testUpdateGame() throws Exception {

        try {
            boardService.updateGame(null);
            Assert.fail("Should break because updateGame is null");
        } catch (FunctionalException e) {
            Assert.assertEquals(IConstantsCommonException.NULL_PARAMETER, e.getCode());
            Assert.assertEquals("updateGame", e.getParams()[0]);
        }

        Request<Void> request = new Request<>();

        try {
            boardService.updateGame(request);
            Assert.fail("Should break because updateGame.game is null");
        } catch (FunctionalException e) {
            Assert.assertEquals(IConstantsCommonException.NULL_PARAMETER, e.getCode());
            Assert.assertEquals("updateGame.game", e.getParams()[0]);
        }

        request.setGame(new GameInfo());

        try {
            boardService.updateGame(request);
            Assert.fail("Should break because updateGame.game.idGame is null");
        } catch (FunctionalException e) {
            Assert.assertEquals(IConstantsCommonException.NULL_PARAMETER, e.getCode());
            Assert.assertEquals("updateGame.game.idGame", e.getParams()[0]);
        }

        request.getGame().setIdGame(12L);

        try {
            boardService.updateGame(request);
            Assert.fail("Should break because updateGame.game.versionGame is null");
        } catch (FunctionalException e) {
            Assert.assertEquals(IConstantsCommonException.NULL_PARAMETER, e.getCode());
            Assert.assertEquals("updateGame.game.versionGame", e.getParams()[0]);
        }

        request.getGame().setVersionGame(1L);

        List<DiffEntity> diffs = new ArrayList<>();
        DiffEntity diff1 = new DiffEntity();
        diff1.setVersionGame(1L);
        diffs.add(diff1);
        DiffEntity diff2 = new DiffEntity();
        diff2.setVersionGame(2L);
        diffs.add(diff2);
        DiffEntity diff3 = new DiffEntity();
        diff3.setVersionGame(3L);
        diffs.add(diff3);
        DiffEntity diff4 = new DiffEntity();
        diff4.setVersionGame(4L);
        diffs.add(diff4);
        DiffEntity diff5 = new DiffEntity();
        diff5.setVersionGame(5L);
        diffs.add(diff5);

        GameEntity game = new GameEntity();
        game.setId(12L);
        game.setVersion(5L);

        when(gameDao.lock(anyLong())).thenReturn(game);

        when(diffDao.getDiffsSince(12L, 1L)).thenReturn(diffs);

        List<Diff> diffVos = new ArrayList<>();
        diffVos.add(new Diff());
        diffVos.add(new Diff());

        when(diffMapping.oesToVos(anyObject())).thenReturn(diffVos);

        DiffResponse response = boardService.updateGame(request);

        InOrder inOrder = inOrder(diffDao, diffMapping);

        inOrder.verify(diffDao).getDiffsSince(12L, 1L);
        inOrder.verify(diffMapping).oesToVos(anyObject());

        Assert.assertEquals(5L, response.getVersionGame().longValue());
        Assert.assertEquals(diffVos, response.getDiffs());

        request.getGame().setIdGame(1L);
        request.getGame().setVersionGame(1L);

        response = boardService.updateGame(request);

        Assert.assertEquals(5L, response.getVersionGame().longValue());
    }

    @Test
    public void testMoveStackFailSimple() {
        try {
            boardService.moveStack(null);
            Assert.fail("Should break because moveStack is null");
        } catch (FunctionalException e) {
            Assert.assertEquals(IConstantsCommonException.NULL_PARAMETER, e.getCode());
            Assert.assertEquals("moveStack", e.getParams()[0]);
        }

        Request<MoveStackRequest> request = new Request<>();

        try {
            boardService.moveStack(request);
            Assert.fail("Should break because moveStack.game is null");
        } catch (FunctionalException e) {
            Assert.assertEquals(IConstantsCommonException.NULL_PARAMETER, e.getCode());
            Assert.assertEquals("moveStack.game", e.getParams()[0]);
        }

        request.setGame(new GameInfo());

        try {
            boardService.moveStack(request);
            Assert.fail("Should break because idGame is null");
        } catch (FunctionalException e) {
            Assert.assertEquals(IConstantsCommonException.NULL_PARAMETER, e.getCode());
            Assert.assertEquals("moveStack.game.idGame", e.getParams()[0]);
        }

        request.getGame().setIdGame(12L);

        try {
            boardService.moveStack(request);
            Assert.fail("Should break because versionGame is null");
        } catch (FunctionalException e) {
            Assert.assertEquals(IConstantsCommonException.NULL_PARAMETER, e.getCode());
            Assert.assertEquals("moveStack.game.versionGame", e.getParams()[0]);
        }

        request.getGame().setVersionGame(1L);

        try {
            boardService.moveStack(request);
            Assert.fail("Should break because game does not exist");
        } catch (FunctionalException e) {
            Assert.assertEquals(IConstantsCommonException.INVALID_PARAMETER, e.getCode());
            Assert.assertEquals("moveStack.game.idGame", e.getParams()[0]);
        }

        GameEntity game = new GameEntity();
        game.setId(12L);

        when(gameDao.lock(12L)).thenReturn(game);

        try {
            boardService.moveStack(request);
            Assert.fail("Should break because versions does not match");
        } catch (FunctionalException e) {
            Assert.assertEquals(IConstantsCommonException.INVALID_PARAMETER, e.getCode());
            Assert.assertEquals("moveStack.game.versionGame", e.getParams()[0]);
        }

        game.setVersion(5L);

        try {
            boardService.moveStack(request);
            Assert.fail("Should break because moveStack.request is null");
        } catch (FunctionalException e) {
            Assert.assertEquals(IConstantsCommonException.NULL_PARAMETER, e.getCode());
            Assert.assertEquals("moveStack.request", e.getParams()[0]);
        }

        request.setRequest(new MoveStackRequest());

        try {
            boardService.moveStack(request);
            Assert.fail("Should break because idStack is null");
        } catch (FunctionalException e) {
            Assert.assertEquals(IConstantsCommonException.NULL_PARAMETER, e.getCode());
            Assert.assertEquals("moveStack.request.idStack", e.getParams()[0]);
        }

        request.getRequest().setIdStack(4L);

        try {
            boardService.moveStack(request);
            Assert.fail("Should break because provinceTo is null");
        } catch (FunctionalException e) {
            Assert.assertEquals(IConstantsCommonException.NULL_PARAMETER, e.getCode());
            Assert.assertEquals("moveStack.request.provinceTo", e.getParams()[0]);
        }

        request.getRequest().setProvinceTo("IdF");
    }

    @Test
    public void testMoveStackFailComplex() {
        Request<MoveStackRequest> request = new Request<>();
        request.setRequest(new MoveStackRequest());
        request.setAuthent(new AuthentInfo());
        request.setGame(new GameInfo());
        request.getGame().setIdGame(12L);
        request.getGame().setVersionGame(1L);
        request.getRequest().setIdStack(13L);
        request.getRequest().setProvinceTo("IdF");

        EuropeanProvinceEntity idf = new EuropeanProvinceEntity();
        idf.setId(257L);
        idf.setName("IdF");

        EuropeanProvinceEntity pecs = new EuropeanProvinceEntity();
        pecs.setId(256L);
        pecs.setName("pecs");

        GameEntity game = new GameEntity();
        game.setId(12L);
        game.setVersion(5L);

        when(gameDao.lock(12L)).thenReturn(game);

        try {
            boardService.moveStack(request);
            Assert.fail("Should break because stack does not exist");
        } catch (FunctionalException e) {
            Assert.assertEquals(IConstantsCommonException.INVALID_PARAMETER, e.getCode());
            Assert.assertEquals("moveStack.request.idStack", e.getParams()[0]);
        }

        StackEntity stack = new StackEntity();
        stack.setProvince("pecs");
        stack.setId(14L);
        game.getStacks().add(stack);

        try {
            boardService.moveStack(request);
            Assert.fail("Should break because stack does not exist");
        } catch (FunctionalException e) {
            Assert.assertEquals(IConstantsCommonException.INVALID_PARAMETER, e.getCode());
            Assert.assertEquals("moveStack.request.idStack", e.getParams()[0]);
        }

        stack.setId(13L);

        try {
            boardService.moveStack(request);
            Assert.fail("Should break because province does not exist");
        } catch (FunctionalException e) {
            Assert.assertEquals(IConstantsCommonException.INVALID_PARAMETER, e.getCode());
            Assert.assertEquals("moveStack.request.provinceTo", e.getParams()[0]);
        }

        when(provinceDao.getProvinceByName("pecs")).thenReturn(pecs);
        when(provinceDao.getProvinceByName("IdF")).thenReturn(idf);

        try {
            boardService.moveStack(request);
            Assert.fail("Should break because province is not close to former one");
        } catch (FunctionalException e) {
            Assert.assertEquals(IConstantsCommonException.INVALID_PARAMETER, e.getCode());
            Assert.assertEquals("moveStack.request.provinceTo", e.getParams()[0]);
        }
    }

    @Test
    public void testMoveStackSuccess() throws Exception {
        Request<MoveStackRequest> request = new Request<>();
        request.setRequest(new MoveStackRequest());
        request.setAuthent(new AuthentInfo());
        request.setGame(new GameInfo());
        request.getGame().setIdGame(12L);
        request.getGame().setVersionGame(1L);
        request.getRequest().setIdStack(13L);
        request.getRequest().setProvinceTo("IdF");

        EuropeanProvinceEntity idf = new EuropeanProvinceEntity();
        idf.setId(257L);
        idf.setName("IdF");

        EuropeanProvinceEntity pecs = new EuropeanProvinceEntity();
        pecs.setId(256L);
        pecs.setName("pecs");

        GameEntity game = new GameEntity();
        game.setId(12L);
        game.setVersion(5L);
        StackEntity stack = new StackEntity();
        stack.setProvince("pecs");
        stack.setId(13L);
        BorderEntity border = new BorderEntity();
        border.setProvinceFrom(pecs);
        border.setProvinceTo(idf);
        pecs.getBorders().add(border);
        game.getStacks().add(stack);

        when(gameDao.lock(12L)).thenReturn(game);

        when(provinceDao.getProvinceByName("pecs")).thenReturn(pecs);
        when(provinceDao.getProvinceByName("IdF")).thenReturn(idf);

        List<DiffEntity> diffBefore = new ArrayList<>();
        diffBefore.add(new DiffEntity());
        diffBefore.add(new DiffEntity());

        when(diffDao.getDiffsSince(12L, 1L)).thenReturn(diffBefore);

        when(diffDao.create(anyObject())).thenAnswer(invocation -> {
            diffEntity = (DiffEntity) invocation.getArguments()[0];
            return diffEntity;
        });

        List<Diff> diffAfter = new ArrayList<>();
        diffAfter.add(new Diff());
        diffAfter.add(new Diff());

        when(diffMapping.oesToVos(anyObject())).thenReturn(diffAfter);

        DiffResponse response = boardService.moveStack(request);

        InOrder inOrder = inOrder(gameDao, provinceDao, diffDao, socketHandler, diffMapping);

        inOrder.verify(gameDao).lock(12L);
        inOrder.verify(diffDao).getDiffsSince(12L, 1L);
        inOrder.verify(provinceDao).getProvinceByName("IdF");
        inOrder.verify(provinceDao).getProvinceByName("pecs");
        inOrder.verify(diffDao).create(anyObject());
        inOrder.verify(socketHandler).push(anyObject(), anyObject(), anyObject());
        inOrder.verify(diffMapping).oesToVos(anyObject());

        Assert.assertEquals(13L, diffEntity.getIdObject().longValue());
        Assert.assertEquals(game.getVersion(), diffEntity.getVersionGame().longValue());
        Assert.assertEquals(DiffTypeEnum.MOVE, diffEntity.getType());
        Assert.assertEquals(DiffTypeObjectEnum.STACK, diffEntity.getTypeObject());
        Assert.assertEquals(12L, diffEntity.getIdGame().longValue());
        Assert.assertEquals(game.getVersion(), diffEntity.getVersionGame().longValue());
        Assert.assertEquals(2, diffEntity.getAttributes().size());
        Assert.assertEquals(DiffAttributeTypeEnum.PROVINCE_FROM, diffEntity.getAttributes().get(0).getType());
        Assert.assertEquals(pecs.getName(), diffEntity.getAttributes().get(0).getValue());
        Assert.assertEquals(DiffAttributeTypeEnum.PROVINCE_TO, diffEntity.getAttributes().get(1).getType());
        Assert.assertEquals(idf.getName(), diffEntity.getAttributes().get(1).getValue());

        Assert.assertEquals(game.getVersion(), response.getVersionGame().longValue());
        Assert.assertEquals(diffAfter, response.getDiffs());
    }

    @Test
    public void testMoveCounterFailSimple() {
        try {
            boardService.moveCounter(null);
            Assert.fail("Should break because moveCounter is null");
        } catch (FunctionalException e) {
            Assert.assertEquals(IConstantsCommonException.NULL_PARAMETER, e.getCode());
            Assert.assertEquals("moveCounter", e.getParams()[0]);
        }

        Request<MoveCounterRequest> request = new Request<>();

        try {
            boardService.moveCounter(request);
            Assert.fail("Should break because moveCounter.authent is null");
        } catch (FunctionalException e) {
            Assert.assertEquals(IConstantsCommonException.NULL_PARAMETER, e.getCode());
            Assert.assertEquals("moveCounter.authent", e.getParams()[0]);
        }

        request.setAuthent(new AuthentInfo());

        try {
            boardService.moveCounter(request);
            Assert.fail("Should break because moveCounter.game is null");
        } catch (FunctionalException e) {
            Assert.assertEquals(IConstantsCommonException.NULL_PARAMETER, e.getCode());
            Assert.assertEquals("moveCounter.game", e.getParams()[0]);
        }

        request.setGame(new GameInfo());

        try {
            boardService.moveCounter(request);
            Assert.fail("Should break because idGame is null");
        } catch (FunctionalException e) {
            Assert.assertEquals(IConstantsCommonException.NULL_PARAMETER, e.getCode());
            Assert.assertEquals("moveCounter.game.idGame", e.getParams()[0]);
        }

        request.getGame().setIdGame(12L);

        try {
            boardService.moveCounter(request);
            Assert.fail("Should break because versionGame is null");
        } catch (FunctionalException e) {
            Assert.assertEquals(IConstantsCommonException.NULL_PARAMETER, e.getCode());
            Assert.assertEquals("moveCounter.game.versionGame", e.getParams()[0]);
        }

        request.getGame().setVersionGame(1L);

        try {
            boardService.moveCounter(request);
            Assert.fail("Should break because game does not exist");
        } catch (FunctionalException e) {
            Assert.assertEquals(IConstantsCommonException.INVALID_PARAMETER, e.getCode());
            Assert.assertEquals("moveCounter.game.idGame", e.getParams()[0]);
        }

        GameEntity game = new GameEntity();
        game.setId(12L);

        when(gameDao.lock(12L)).thenReturn(game);

        try {
            boardService.moveCounter(request);
            Assert.fail("Should break because versions does not match");
        } catch (FunctionalException e) {
            Assert.assertEquals(IConstantsCommonException.INVALID_PARAMETER, e.getCode());
            Assert.assertEquals("moveCounter.game.versionGame", e.getParams()[0]);
        }

        game.setVersion(5L);

        try {
            boardService.moveCounter(request);
            Assert.fail("Should break because moveCounter.request is null");
        } catch (FunctionalException e) {
            Assert.assertEquals(IConstantsCommonException.NULL_PARAMETER, e.getCode());
            Assert.assertEquals("moveCounter.request", e.getParams()[0]);
        }

        request.setRequest(new MoveCounterRequest());

        try {
            boardService.moveCounter(request);
            Assert.fail("Should break because idCounter is null");
        } catch (FunctionalException e) {
            Assert.assertEquals(IConstantsCommonException.NULL_PARAMETER, e.getCode());
            Assert.assertEquals("moveCounter.request.idCounter", e.getParams()[0]);
        }
    }

    @Test
    public void testMoveCounterFailComplex() {
        Request<MoveCounterRequest> request = new Request<>();
        request.setRequest(new MoveCounterRequest());
        request.setAuthent(new AuthentInfo());
        request.setGame(new GameInfo());
        request.getGame().setIdGame(12L);
        request.getGame().setVersionGame(1L);
        request.getRequest().setIdCounter(13L);
        request.getRequest().setIdStack(8L);

        EuropeanProvinceEntity idf = new EuropeanProvinceEntity();
        idf.setId(257L);
        idf.setName("IdF");

        EuropeanProvinceEntity pecs = new EuropeanProvinceEntity();
        pecs.setId(256L);
        pecs.setName("pecs");

        GameEntity game = new GameEntity();
        game.setId(12L);
        game.setVersion(5L);

        StackEntity stack = new StackEntity();
        stack.setProvince("IdF");
        stack.setId(8L);
        game.getStacks().add(stack);

        stack = new StackEntity();
        stack.setProvince("Pecs");
        stack.setId(9L);
        CounterEntity counter = new CounterEntity();
        counter.setId(13L);
        counter.setOwner(stack);
        counter.setCountry("france");
        game.getStacks().add(stack);
        PlayableCountryEntity country = new PlayableCountryEntity();
        country.setName("france");
        country.setUsername("toto");
        game.getCountries().add(country);

        when(gameDao.lock(12L)).thenReturn(game);

        try {
            boardService.moveCounter(request);
            Assert.fail("Should break because counter does not exist");
        } catch (FunctionalException e) {
            Assert.assertEquals(IConstantsCommonException.INVALID_PARAMETER, e.getCode());
            Assert.assertEquals("moveCounter.request.idCounter", e.getParams()[0]);
        }

        when(counterDao.getCounter(13L, 12L)).thenReturn(counter);

        try {
            boardService.moveCounter(request);
            Assert.fail("Should break because username has not the right to move this counter");
        } catch (FunctionalException e) {
            Assert.assertEquals(IConstantsCommonException.ACCESS_RIGHT, e.getCode());
            Assert.assertEquals("moveCounter.authent.username", e.getParams()[0]);
        }

        counter.setCountry("genes");

        List<String> patrons = new ArrayList<>();
        patrons.add("france");
        when(counterDao.getPatrons("genes", 12L)).thenReturn(patrons);

        try {
            boardService.moveCounter(request);
            Assert.fail("Should break because username has not the right to move this counter");
        } catch (FunctionalException e) {
            Assert.assertEquals(IConstantsCommonException.ACCESS_RIGHT, e.getCode());
            Assert.assertEquals("username", e.getParams()[0]);
        }

        request.getAuthent().setUsername("toto");

        try {
            boardService.moveCounter(request);
            Assert.fail("Should break because trying to move the counter in another province");
        } catch (FunctionalException e) {
            Assert.assertEquals(IConstantsCommonException.INVALID_PARAMETER, e.getCode());
            Assert.assertEquals("moveCounter.request.idStack", e.getParams()[0]);
        }

        request.getRequest().setIdStack(9L);

        try {
            boardService.moveCounter(request);
            Assert.fail("Should break because trying to move the counter in the same stack");
        } catch (FunctionalException e) {
            Assert.assertEquals(IConstantsCommonException.INVALID_PARAMETER, e.getCode());
            Assert.assertEquals("moveCounter.request.idStack", e.getParams()[0]);
        }
    }

    @Test
    public void testMoveCounterSuccess() throws Exception {
        Request<MoveCounterRequest> request = new Request<>();
        request.setRequest(new MoveCounterRequest());
        request.setAuthent(new AuthentInfo());
        request.getAuthent().setUsername("toto");
        request.setGame(new GameInfo());
        request.getGame().setIdGame(12L);
        request.getGame().setVersionGame(1L);
        request.getRequest().setIdCounter(13L);
        request.getRequest().setIdStack(8L);

        GameEntity game = new GameEntity();
        game.setId(12L);
        game.setVersion(5L);

        StackEntity stack = new StackEntity();
        stack.setProvince("IdF");
        stack.setId(8L);
        game.getStacks().add(stack);

        stack = new StackEntity();
        stack.setProvince("IdF");
        stack.setId(9L);
        CounterEntity counter = new CounterEntity();
        counter.setId(13L);
        counter.setOwner(stack);
        counter.setCountry("genes");
        stack.getCounters().add(counter);
        stack.getCounters().add(new CounterEntity());
        game.getStacks().add(stack);
        PlayableCountryEntity country = new PlayableCountryEntity();
        country.setName("france");
        country.setUsername("toto");
        game.getCountries().add(country);

        List<String> patrons = new ArrayList<>();
        patrons.add("france");

        when(gameDao.lock(12L)).thenReturn(game);

        List<DiffEntity> diffBefore = new ArrayList<>();
        diffBefore.add(new DiffEntity());
        diffBefore.add(new DiffEntity());

        when(diffDao.getDiffsSince(12L, 1L)).thenReturn(diffBefore);

        when(counterDao.getCounter(13L, 12L)).thenReturn(counter);

        when(counterDao.getPatrons("genes", 12L)).thenReturn(patrons);

        when(diffDao.create(anyObject())).thenAnswer(invocation -> {
            diffEntity = (DiffEntity) invocation.getArguments()[0];
            return diffEntity;
        });

        List<Diff> diffAfter = new ArrayList<>();
        diffAfter.add(new Diff());
        diffAfter.add(new Diff());

        when(diffMapping.oesToVos(anyObject())).thenReturn(diffAfter);

        DiffResponse response = boardService.moveCounter(request);

        InOrder inOrder = inOrder(gameDao, diffDao, socketHandler, counterDao, stackDao, diffMapping);

        inOrder.verify(gameDao).lock(12L);
        inOrder.verify(diffDao).getDiffsSince(12L, 1L);
        inOrder.verify(counterDao).getCounter(13L, 12L);
        inOrder.verify(counterDao).getPatrons("genes", 12L);
        inOrder.verify(diffDao).create(anyObject());
        inOrder.verify(socketHandler).push(anyObject(), anyObject(), anyObject());
        inOrder.verify(gameDao).update(game, false);
        inOrder.verify(diffMapping).oesToVos(anyObject());

        Assert.assertEquals(13L, diffEntity.getIdObject().longValue());
        Assert.assertEquals(game.getVersion(), diffEntity.getVersionGame().longValue());
        Assert.assertEquals(DiffTypeEnum.MOVE, diffEntity.getType());
        Assert.assertEquals(DiffTypeObjectEnum.COUNTER, diffEntity.getTypeObject());
        Assert.assertEquals(12L, diffEntity.getIdGame().longValue());
        Assert.assertEquals(game.getVersion(), diffEntity.getVersionGame().longValue());
        Assert.assertEquals(3, diffEntity.getAttributes().size());
        Assert.assertEquals(DiffAttributeTypeEnum.STACK_FROM, diffEntity.getAttributes().get(0).getType());
        Assert.assertEquals("9", diffEntity.getAttributes().get(0).getValue());
        Assert.assertEquals(DiffAttributeTypeEnum.STACK_TO, diffEntity.getAttributes().get(1).getType());
        Assert.assertEquals("8", diffEntity.getAttributes().get(1).getValue());
        Assert.assertEquals(DiffAttributeTypeEnum.PROVINCE, diffEntity.getAttributes().get(2).getType());
        Assert.assertEquals("IdF", diffEntity.getAttributes().get(2).getValue());

        Assert.assertEquals(game.getVersion(), response.getVersionGame().longValue());
        Assert.assertEquals(diffAfter, response.getDiffs());
    }

    @Test
    public void testMoveCounterInNewStackSuccess() throws Exception {
        Request<MoveCounterRequest> request = new Request<>();
        request.setRequest(new MoveCounterRequest());
        request.setAuthent(new AuthentInfo());
        request.getAuthent().setUsername("toto");
        request.setGame(new GameInfo());
        request.getGame().setIdGame(12L);
        request.getGame().setVersionGame(1L);
        request.getRequest().setIdCounter(13L);

        GameEntity game = new GameEntity();
        game.setId(12L);
        game.setVersion(5L);

        StackEntity stack = new StackEntity();
        stack.setProvince("IdF");
        stack.setId(9L);
        CounterEntity counter = new CounterEntity();
        counter.setId(13L);
        counter.setOwner(stack);
        stack.getCounters().add(counter);
        game.getStacks().add(stack);

        when(gameDao.lock(12L)).thenReturn(game);

        List<DiffEntity> diffBefore = new ArrayList<>();
        diffBefore.add(new DiffEntity());
        diffBefore.add(new DiffEntity());

        when(diffDao.getDiffsSince(12L, 1L)).thenReturn(diffBefore);

        when(counterDao.getCounter(13L, 12L)).thenReturn(counter);

        ArgumentCaptor<StackEntity> arg = ArgumentCaptor.forClass(StackEntity.class);
        when(stackDao.create(arg.capture())).thenAnswer(invocationOnMock -> {
            StackEntity entity = (StackEntity) invocationOnMock.getArguments()[0];
            entity.setId(25L);
            return entity;
        });

        when(diffDao.create(anyObject())).thenAnswer(invocation -> {
            diffEntity = (DiffEntity) invocation.getArguments()[0];
            return diffEntity;
        });

        List<Diff> diffAfter = new ArrayList<>();
        diffAfter.add(new Diff());
        diffAfter.add(new Diff());

        when(diffMapping.oesToVos(anyObject())).thenReturn(diffAfter);

        DiffResponse response = boardService.moveCounter(request);

        InOrder inOrder = inOrder(gameDao, diffDao, counterDao, stackDao, diffMapping);

        inOrder.verify(gameDao).lock(12L);
        inOrder.verify(diffDao).getDiffsSince(12L, 1L);
        inOrder.verify(counterDao).getCounter(13L, 12L);
        inOrder.verify(stackDao).create(arg.capture());
        inOrder.verify(diffDao).create(anyObject());
        inOrder.verify(gameDao).update(game, false);
        inOrder.verify(diffMapping).oesToVos(anyObject());

        Assert.assertEquals(13L, diffEntity.getIdObject().longValue());
        Assert.assertEquals(game.getVersion(), diffEntity.getVersionGame().longValue());
        Assert.assertEquals(DiffTypeEnum.MOVE, diffEntity.getType());
        Assert.assertEquals(DiffTypeObjectEnum.COUNTER, diffEntity.getTypeObject());
        Assert.assertEquals(12L, diffEntity.getIdGame().longValue());
        Assert.assertEquals(game.getVersion(), diffEntity.getVersionGame().longValue());
        Assert.assertEquals(4, diffEntity.getAttributes().size());
        Assert.assertEquals(DiffAttributeTypeEnum.STACK_FROM, diffEntity.getAttributes().get(0).getType());
        Assert.assertEquals("9", diffEntity.getAttributes().get(0).getValue());
        Assert.assertEquals(DiffAttributeTypeEnum.STACK_TO, diffEntity.getAttributes().get(1).getType());
        Assert.assertEquals("25", diffEntity.getAttributes().get(1).getValue());
        Assert.assertEquals(DiffAttributeTypeEnum.PROVINCE, diffEntity.getAttributes().get(2).getType());
        Assert.assertEquals("IdF", diffEntity.getAttributes().get(2).getValue());
        Assert.assertEquals(DiffAttributeTypeEnum.STACK_DEL, diffEntity.getAttributes().get(3).getType());
        Assert.assertEquals("9", diffEntity.getAttributes().get(3).getValue());

        Assert.assertEquals(game.getVersion(), response.getVersionGame().longValue());
        Assert.assertEquals(diffAfter, response.getDiffs());
    }
}
