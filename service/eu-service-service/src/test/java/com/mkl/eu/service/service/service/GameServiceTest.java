package com.mkl.eu.service.service.service;

import com.mkl.eu.client.common.exception.FunctionalException;
import com.mkl.eu.client.common.exception.IConstantsCommonException;
import com.mkl.eu.client.common.vo.AuthentInfo;
import com.mkl.eu.client.common.vo.GameInfo;
import com.mkl.eu.client.common.vo.Request;
import com.mkl.eu.client.service.service.game.FindGamesRequest;
import com.mkl.eu.client.service.service.game.LoadGameRequest;
import com.mkl.eu.client.service.service.game.LoadTurnOrderRequest;
import com.mkl.eu.client.service.vo.Game;
import com.mkl.eu.client.service.vo.GameLight;
import com.mkl.eu.client.service.vo.chat.Chat;
import com.mkl.eu.client.service.vo.diff.Diff;
import com.mkl.eu.client.service.vo.diff.DiffResponse;
import com.mkl.eu.client.service.vo.diplo.CountryOrder;
import com.mkl.eu.client.service.vo.enumeration.GameStatusEnum;
import com.mkl.eu.service.service.mapping.GameMapping;
import com.mkl.eu.service.service.mapping.chat.ChatMapping;
import com.mkl.eu.service.service.mapping.diff.DiffMapping;
import com.mkl.eu.service.service.mapping.diplo.CountryOrderMapping;
import com.mkl.eu.service.service.persistence.IGameDao;
import com.mkl.eu.service.service.persistence.board.ICounterDao;
import com.mkl.eu.service.service.persistence.board.IStackDao;
import com.mkl.eu.service.service.persistence.chat.IChatDao;
import com.mkl.eu.service.service.persistence.diff.IDiffDao;
import com.mkl.eu.service.service.persistence.oe.GameEntity;
import com.mkl.eu.service.service.persistence.oe.chat.ChatEntity;
import com.mkl.eu.service.service.persistence.oe.chat.MessageGlobalEntity;
import com.mkl.eu.service.service.persistence.oe.chat.RoomEntity;
import com.mkl.eu.service.service.persistence.oe.country.PlayableCountryEntity;
import com.mkl.eu.service.service.persistence.oe.diff.DiffEntity;
import com.mkl.eu.service.service.persistence.oe.diplo.CountryOrderEntity;
import com.mkl.eu.service.service.persistence.ref.IProvinceDao;
import com.mkl.eu.service.service.service.impl.GameServiceImpl;
import com.mkl.eu.service.service.socket.SocketHandler;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InOrder;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import static org.mockito.Matchers.anyLong;
import static org.mockito.Matchers.anyObject;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.when;

/**
 * Test of GameService.
 *
 * @author MKL.
 */
@RunWith(MockitoJUnitRunner.class)
public class GameServiceTest {
    @InjectMocks
    private GameServiceImpl gameService;

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
    private CountryOrderMapping countryOrderMapping;

    @Mock
    private ChatMapping chatMapping;

    @Mock
    private DiffMapping diffMapping;

    @Mock
    private SocketHandler socketHandler;

    @Test
    public void testFindGamesSimple() throws Exception {
        try {
            gameService.findGames(null);
            Assert.fail("Should break because findGames is null");
        } catch (FunctionalException e) {
            Assert.assertEquals(IConstantsCommonException.NULL_PARAMETER, e.getCode());
            Assert.assertEquals("findGames", e.getParams()[0]);
        }

        Request<FindGamesRequest> request = new Request<>();

        List<GameEntity> games = new ArrayList<>();
        GameEntity game = new GameEntity();
        game.setId(1L);
        game.getCountries().add(new PlayableCountryEntity());
        game.getCountries().get(0).setId(11L);
        game.getCountries().get(0).setUsername("MKL");
        game.getCountries().get(0).setName("france");
        game.getCountries().add(new PlayableCountryEntity());
        game.getCountries().get(1).setId(12L);
        game.getCountries().get(1).setUsername("jym");
        game.getCountries().get(1).setName("espagne");
        game.getCountries().add(new PlayableCountryEntity());
        game.getCountries().get(1).setId(13L);
        game.getCountries().get(1).setUsername("MKL");
        game.getCountries().get(1).setName("russie");
        games.add(game);
        game = new GameEntity();
        game.setId(2L);
        game.getCountries().add(new PlayableCountryEntity());
        game.getCountries().get(0).setId(11L);
        game.getCountries().get(0).setUsername("MKL");
        game.getCountries().get(0).setName("france");
        game.getCountries().add(new PlayableCountryEntity());
        game.getCountries().get(1).setId(12L);
        game.getCountries().get(1).setUsername("jym");
        game.getCountries().get(1).setName("espagne");
        games.add(game);

        when(gameDao.findGames(null)).thenReturn(games);
        when(gameMapping.oeToVoLight(anyObject())).thenAnswer(invocationOnMock -> {
            GameLight light = new GameLight();

            light.setId(((GameEntity) invocationOnMock.getArguments()[0]).getId());

            return light;
        });

        List<GameLight> gameLights = gameService.findGames(request);

        InOrder inOrder = inOrder(gameDao, gameMapping);
        inOrder.verify(gameDao).findGames(null);
        inOrder.verify(gameMapping).oeToVoLight(games.get(0));
        inOrder.verify(gameMapping).oeToVoLight(games.get(1));

        Assert.assertEquals(2, gameLights.size());
        Assert.assertEquals(1L, gameLights.get(0).getId().longValue());
        Assert.assertEquals(null, gameLights.get(0).getCountry());
        Assert.assertEquals(null, gameLights.get(0).getIdCountry());
        Assert.assertEquals(0, gameLights.get(0).getUnreadMessages());
        Assert.assertEquals(2L, gameLights.get(1).getId().longValue());
        Assert.assertEquals(null, gameLights.get(1).getCountry());
        Assert.assertEquals(null, gameLights.get(1).getIdCountry());
        Assert.assertEquals(0, gameLights.get(1).getUnreadMessages());
    }

    @Test
    public void testFindGamesComplex() throws Exception {
        try {
            gameService.findGames(null);
            Assert.fail("Should break because findGames is null");
        } catch (FunctionalException e) {
            Assert.assertEquals(IConstantsCommonException.NULL_PARAMETER, e.getCode());
            Assert.assertEquals("findGames", e.getParams()[0]);
        }

        Request<FindGamesRequest> request = new Request<>();
        request.setRequest(new FindGamesRequest());
        request.getRequest().setUsername("MKL");

        List<GameEntity> games = new ArrayList<>();
        GameEntity game = new GameEntity();
        game.setId(1L);
        game.getCountries().add(new PlayableCountryEntity());
        game.getCountries().get(0).setId(11L);
        game.getCountries().get(0).setUsername("MKL");
        game.getCountries().get(0).setName("france");
        game.getCountries().add(new PlayableCountryEntity());
        game.getCountries().get(1).setId(12L);
        game.getCountries().get(1).setUsername("jym");
        game.getCountries().get(1).setName("espagne");
        game.getCountries().add(new PlayableCountryEntity());
        game.getCountries().get(1).setId(13L);
        game.getCountries().get(1).setUsername("MKL");
        game.getCountries().get(1).setName("russie");
        games.add(game);
        game = new GameEntity();
        game.setId(2L);
        game.getCountries().add(new PlayableCountryEntity());
        game.getCountries().get(0).setId(21L);
        game.getCountries().get(0).setUsername("MKL");
        game.getCountries().get(0).setName("angleterre");
        game.getCountries().add(new PlayableCountryEntity());
        game.getCountries().get(1).setId(22L);
        game.getCountries().get(1).setUsername("jym");
        game.getCountries().get(1).setName("pologne");
        games.add(game);

        when(gameDao.findGames(request.getRequest())).thenReturn(games);
        when(gameMapping.oeToVoLight(anyObject())).thenAnswer(invocationOnMock -> {
            GameLight light = new GameLight();

            light.setId(((GameEntity) invocationOnMock.getArguments()[0]).getId());

            return light;
        });
        when(chatDao.getUnreadMessagesNumber(anyLong())).thenAnswer(invocationOnMock -> invocationOnMock.getArguments()[0]);

        List<GameLight> gameLights = gameService.findGames(request);

        InOrder inOrder = inOrder(gameDao, chatDao, gameMapping);
        inOrder.verify(gameDao).findGames(request.getRequest());
        inOrder.verify(gameMapping).oeToVoLight(games.get(0));
        inOrder.verify(chatDao).getUnreadMessagesNumber(11L);
        inOrder.verify(gameMapping).oeToVoLight(games.get(0));
        inOrder.verify(chatDao).getUnreadMessagesNumber(13L);
        inOrder.verify(gameMapping).oeToVoLight(games.get(1));
        inOrder.verify(chatDao).getUnreadMessagesNumber(21L);

        Assert.assertEquals(3, gameLights.size());
        Assert.assertEquals(1L, gameLights.get(0).getId().longValue());
        Assert.assertEquals("france", gameLights.get(0).getCountry());
        Assert.assertEquals(11L, gameLights.get(0).getIdCountry().longValue());
        Assert.assertEquals(11L, gameLights.get(0).getUnreadMessages());
        Assert.assertEquals(1L, gameLights.get(1).getId().longValue());
        Assert.assertEquals("russie", gameLights.get(1).getCountry());
        Assert.assertEquals(13L, gameLights.get(1).getIdCountry().longValue());
        Assert.assertEquals(13L, gameLights.get(1).getUnreadMessages());
        Assert.assertEquals(2L, gameLights.get(2).getId().longValue());
        Assert.assertEquals("angleterre", gameLights.get(2).getCountry());
        Assert.assertEquals(21L, gameLights.get(2).getIdCountry().longValue());
        Assert.assertEquals(21L, gameLights.get(2).getUnreadMessages());
    }

    @Test
    public void testLoadGame() throws Exception {
        try {
            gameService.loadGame(null);
            Assert.fail("Should break because loadGame is null");
        } catch (FunctionalException e) {
            Assert.assertEquals(IConstantsCommonException.NULL_PARAMETER, e.getCode());
            Assert.assertEquals("loadGame", e.getParams()[0]);
        }

        Request<LoadGameRequest> request = new Request<>();

        try {
            gameService.loadGame(request);
            Assert.fail("Should break because loadGame.request is null");
        } catch (FunctionalException e) {
            Assert.assertEquals(IConstantsCommonException.NULL_PARAMETER, e.getCode());
            Assert.assertEquals("loadGame.request", e.getParams()[0]);
        }

        request.setRequest(new LoadGameRequest());

        try {
            gameService.loadGame(request);
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

        gameService.loadGame(request);

        InOrder inOrder = inOrder(gameDao, gameMapping, chatDao, chatMapping);
        inOrder.verify(gameDao).read(idGame);
        inOrder.verify(gameMapping).oeToVo(gameOe, null);
        inOrder.verify(chatDao).getGlobalMessages(idGame);
        inOrder.verify(chatMapping).getChat(globalMessages, null, null, null);

        request.getRequest().setIdCountry(idCountry);

        try {
            gameService.loadGame(request);
            Assert.fail("Should break because loadGame.request.idCountry is incorrect");
        } catch (FunctionalException e) {
            Assert.assertEquals(IConstantsCommonException.INVALID_PARAMETER, e.getCode());
            Assert.assertEquals("loadGame.request.idCountry", e.getParams()[0]);
        }

        request.setAuthent(new AuthentInfo("toto", null));

        gameOe.getCountries().add(new PlayableCountryEntity());
        gameOe.getCountries().get(0).setId(idCountry);
        gameOe.getCountries().get(0).setUsername("toto");

        gameService.loadGame(request);

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
            gameService.updateGame(null);
            Assert.fail("Should break because updateGame is null");
        } catch (FunctionalException e) {
            Assert.assertEquals(IConstantsCommonException.NULL_PARAMETER, e.getCode());
            Assert.assertEquals("updateGame", e.getParams()[0]);
        }

        Request<Void> request = new Request<>();

        try {
            gameService.updateGame(request);
            Assert.fail("Should break because updateGame.game is null");
        } catch (FunctionalException e) {
            Assert.assertEquals(IConstantsCommonException.NULL_PARAMETER, e.getCode());
            Assert.assertEquals("updateGame.game", e.getParams()[0]);
        }

        request.setGame(new GameInfo());

        try {
            gameService.updateGame(request);
            Assert.fail("Should break because updateGame.game.idGame is null");
        } catch (FunctionalException e) {
            Assert.assertEquals(IConstantsCommonException.NULL_PARAMETER, e.getCode());
            Assert.assertEquals("updateGame.game.idGame", e.getParams()[0]);
        }

        request.getGame().setIdGame(12L);

        try {
            gameService.updateGame(request);
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

        DiffResponse response = gameService.updateGame(request);

        InOrder inOrder = inOrder(diffDao, diffMapping);

        inOrder.verify(diffDao).getDiffsSince(12L, 1L);
        inOrder.verify(diffMapping).oesToVos(anyObject());

        Assert.assertEquals(5L, response.getVersionGame().longValue());
        Assert.assertEquals(diffVos, response.getDiffs());

        request.getGame().setIdGame(1L);
        request.getGame().setVersionGame(1L);

        response = gameService.updateGame(request);

        Assert.assertEquals(5L, response.getVersionGame().longValue());
    }

    @Test
    public void testLoadTurnOrder() {
        try {
            gameService.loadTurnOrder(null);
            Assert.fail("Should break because loadTurnOrder is null");
        } catch (FunctionalException e) {
            Assert.assertEquals(IConstantsCommonException.NULL_PARAMETER, e.getCode());
            Assert.assertEquals("loadTurnOrder", e.getParams()[0]);
        }

        Request<LoadTurnOrderRequest> request = new Request<>();

        try {
            gameService.loadTurnOrder(request);
            Assert.fail("Should break because loadTurnOrder.request is null");
        } catch (FunctionalException e) {
            Assert.assertEquals(IConstantsCommonException.NULL_PARAMETER, e.getCode());
            Assert.assertEquals("loadTurnOrder.request", e.getParams()[0]);
        }

        request.setRequest(new LoadTurnOrderRequest(1L, GameStatusEnum.MILITARY_MOVE));

        List<CountryOrderEntity> orderEntities = new ArrayList<>();
        orderEntities.add(new CountryOrderEntity());
        when(gameDao.findTurnOrder(1L, GameStatusEnum.MILITARY_MOVE)).thenReturn(orderEntities);
        List<CountryOrder> ordersVos = new ArrayList<>();
        ordersVos.add(new CountryOrder());
        ordersVos.add(new CountryOrder());
        when(countryOrderMapping.oesToVos(orderEntities, new HashMap<>())).thenReturn(ordersVos);

        try {
            List<CountryOrder> orders = gameService.loadTurnOrder(request);

            Assert.assertEquals(orders, ordersVos);
        } catch (FunctionalException e) {
            Assert.fail("Should not break " + e.getMessage());
        }
    }
}
