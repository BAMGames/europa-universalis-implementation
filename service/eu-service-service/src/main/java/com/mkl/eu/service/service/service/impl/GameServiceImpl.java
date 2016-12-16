package com.mkl.eu.service.service.service.impl;

import com.mkl.eu.client.common.exception.FunctionalException;
import com.mkl.eu.client.common.exception.IConstantsCommonException;
import com.mkl.eu.client.common.exception.TechnicalException;
import com.mkl.eu.client.common.util.CommonUtil;
import com.mkl.eu.client.common.vo.AuthentInfo;
import com.mkl.eu.client.common.vo.Request;
import com.mkl.eu.client.common.vo.SimpleRequest;
import com.mkl.eu.client.service.service.IGameService;
import com.mkl.eu.client.service.service.board.FindGamesRequest;
import com.mkl.eu.client.service.service.board.LoadGameRequest;
import com.mkl.eu.client.service.vo.Game;
import com.mkl.eu.client.service.vo.GameLight;
import com.mkl.eu.client.service.vo.chat.Chat;
import com.mkl.eu.client.service.vo.diff.Diff;
import com.mkl.eu.client.service.vo.diff.DiffResponse;
import com.mkl.eu.service.service.mapping.GameMapping;
import com.mkl.eu.service.service.persistence.oe.GameEntity;
import com.mkl.eu.service.service.persistence.oe.chat.ChatEntity;
import com.mkl.eu.service.service.persistence.oe.chat.MessageGlobalEntity;
import com.mkl.eu.service.service.persistence.oe.chat.RoomEntity;
import com.mkl.eu.service.service.persistence.oe.country.PlayableCountryEntity;
import com.mkl.eu.service.service.persistence.oe.diff.DiffEntity;
import com.mkl.eu.service.service.service.GameDiffsInfo;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Implementation of the Game Service.
 *
 * @author MKL.
 */
@Service
@Transactional(rollbackFor = {TechnicalException.class, FunctionalException.class})
public class GameServiceImpl extends AbstractService implements IGameService {
    /** Game mapping. */
    @Autowired
    private GameMapping gameMapping;

    /** {@inheritDoc} */
    @Override
    public List<GameLight> findGames(SimpleRequest<FindGamesRequest> request) throws FunctionalException, TechnicalException {
        failIfNull(new CheckForThrow<>().setTest(request).setCodeError(IConstantsCommonException.NULL_PARAMETER)
                .setMsgFormat(MSG_MISSING_PARAMETER).setName(PARAMETER_FIND_GAMES).setParams(METHOD_FIND_GAMES));

        // anonymous and logged users have the same level of information so no check on authent.

        List<GameEntity> gameEntities = gameDao.findGames(request.getRequest());

        List<GameLight> games = new ArrayList<>();
        for (GameEntity gameEntity : gameEntities) {
            boolean parsed = false;
            if (request.getRequest() != null
                    && !StringUtils.isEmpty(request.getRequest().getUsername())
                    && !StringUtils.equals(AuthentInfo.USERNAME_ANONYMOUS, request.getRequest().getUsername())) {
                List<PlayableCountryEntity> countries = gameEntity.getCountries().stream()
                        .filter(playableCountryEntity -> StringUtils.equals(playableCountryEntity.getUsername(), request.getRequest().getUsername()))
                        .collect(Collectors.toList());
                if (!countries.isEmpty()) {
                    for (PlayableCountryEntity country : countries) {
                        GameLight game = gameMapping.oeToVoLight(gameEntity);
                        games.add(game);
                        game.setIdCountry(country.getId());
                        game.setCountry(country.getName());
                        game.setUnreadMessages(chatDao.getUnreadMessagesNumber(country.getId()));
                        game.setActive(false);
                    }
                    parsed = true;
                }
            }

            if (!parsed) {
                GameLight game = gameMapping.oeToVoLight(gameEntity);
                games.add(game);
            }
        }

        return games;
    }

    /** {@inheritDoc} */
    @Override
    public Game loadGame(SimpleRequest<LoadGameRequest> request) throws FunctionalException {
        failIfNull(new CheckForThrow<>().setTest(request).setCodeError(IConstantsCommonException.NULL_PARAMETER)
                .setMsgFormat(MSG_MISSING_PARAMETER).setName(PARAMETER_LOAD_GAME).setParams(METHOD_LOAD_GAME));
        failIfNull(new CheckForThrow<>().setTest(request.getRequest()).setCodeError(IConstantsCommonException.NULL_PARAMETER)
                .setMsgFormat(MSG_MISSING_PARAMETER).setName(PARAMETER_LOAD_GAME, PARAMETER_REQUEST).setParams(METHOD_LOAD_GAME));
        failIfNull(new CheckForThrow<>().setTest(request.getRequest().getIdGame()).setCodeError(IConstantsCommonException.NULL_PARAMETER)
                .setMsgFormat(MSG_MISSING_PARAMETER).setName(PARAMETER_LOAD_GAME, PARAMETER_REQUEST, PARAMETER_ID_GAME).setParams(METHOD_LOAD_GAME));

        if (request.getAuthent() == null) {
            request.setAuthent(AuthentInfo.ANONYMOUS);
        }

        Long idGame = request.getRequest().getIdGame();
        Long idCountry = request.getRequest().getIdCountry();

        GameEntity game = gameDao.read(idGame);

        boolean isCountryOk = idCountry == null;
        if (!isCountryOk) {
            PlayableCountryEntity country = CommonUtil.findFirst(game.getCountries(), x -> x.getId().equals(idCountry));
            isCountryOk = country != null && StringUtils.equals(request.getAuthent().getUsername(), country.getUsername());
        }

        failIfFalse(new CheckForThrow<Boolean>().setTest(isCountryOk).setCodeError(IConstantsCommonException.INVALID_PARAMETER)
                .setMsgFormat("{1}: {0} ({2}) is not in game or is not played by {3}.").setName(PARAMETER_LOAD_GAME, PARAMETER_REQUEST, PARAMETER_ID_COUNTRY).setParams(METHOD_LOAD_GAME, idCountry, request.getAuthent().getUsername()));

        Game returnValue = gameMapping.oeToVo(game, idCountry);

        List<MessageGlobalEntity> globalMessages = chatDao.getGlobalMessages(idGame);
        List<ChatEntity> messages = null;
        List<RoomEntity> rooms = null;
        if (idCountry != null) {
            rooms = chatDao.getRooms(idGame, idCountry);
            messages = chatDao.getMessages(idCountry);
        }
        Chat chat = chatMapping.getChat(globalMessages, rooms, messages, idCountry);
        returnValue.setChat(chat);

        return returnValue;
    }

    /** {@inheritDoc} */
    @Override
    public DiffResponse updateGame(Request<Void> request) throws FunctionalException {
        failIfNull(new CheckForThrow<>().setTest(request).setCodeError(IConstantsCommonException.NULL_PARAMETER)
                .setMsgFormat(MSG_MISSING_PARAMETER).setName(PARAMETER_UPDATE_GAME).setParams(METHOD_UPDATE_GAME));

        GameDiffsInfo gameDiffs = checkGameAndGetDiffs(request.getGame(), METHOD_UPDATE_GAME, PARAMETER_UPDATE_GAME);

        List<DiffEntity> diffs = gameDiffs.getDiffs();
        List<Diff> diffVos = diffMapping.oesToVos(diffs);

        DiffResponse response = new DiffResponse();
        response.setDiffs(diffVos);
        response.setVersionGame(gameDiffs.getGame().getVersion());

        response.setMessages(getMessagesSince(request));

        return response;
    }
}
