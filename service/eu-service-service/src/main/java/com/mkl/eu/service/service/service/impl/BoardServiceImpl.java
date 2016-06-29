package com.mkl.eu.service.service.service.impl;

import com.mkl.eu.client.common.exception.FunctionalException;
import com.mkl.eu.client.common.exception.IConstantsCommonException;
import com.mkl.eu.client.common.exception.TechnicalException;
import com.mkl.eu.client.common.util.CommonUtil;
import com.mkl.eu.client.common.vo.AuthentInfo;
import com.mkl.eu.client.common.vo.Request;
import com.mkl.eu.client.common.vo.SimpleRequest;
import com.mkl.eu.client.service.service.IBoardService;
import com.mkl.eu.client.service.service.board.FindGamesRequest;
import com.mkl.eu.client.service.service.board.LoadGameRequest;
import com.mkl.eu.client.service.service.board.MoveCounterRequest;
import com.mkl.eu.client.service.service.board.MoveStackRequest;
import com.mkl.eu.client.service.vo.Game;
import com.mkl.eu.client.service.vo.GameLight;
import com.mkl.eu.client.service.vo.chat.Chat;
import com.mkl.eu.client.service.vo.diff.Diff;
import com.mkl.eu.client.service.vo.diff.DiffResponse;
import com.mkl.eu.client.service.vo.enumeration.DiffAttributeTypeEnum;
import com.mkl.eu.client.service.vo.enumeration.DiffTypeEnum;
import com.mkl.eu.client.service.vo.enumeration.DiffTypeObjectEnum;
import com.mkl.eu.service.service.mapping.GameMapping;
import com.mkl.eu.service.service.mapping.chat.ChatMapping;
import com.mkl.eu.service.service.persistence.board.ICounterDao;
import com.mkl.eu.service.service.persistence.board.IStackDao;
import com.mkl.eu.service.service.persistence.chat.IChatDao;
import com.mkl.eu.service.service.persistence.oe.GameEntity;
import com.mkl.eu.service.service.persistence.oe.board.CounterEntity;
import com.mkl.eu.service.service.persistence.oe.board.StackEntity;
import com.mkl.eu.service.service.persistence.oe.chat.ChatEntity;
import com.mkl.eu.service.service.persistence.oe.chat.MessageGlobalEntity;
import com.mkl.eu.service.service.persistence.oe.chat.RoomEntity;
import com.mkl.eu.service.service.persistence.oe.country.PlayableCountryEntity;
import com.mkl.eu.service.service.persistence.oe.diff.DiffAttributesEntity;
import com.mkl.eu.service.service.persistence.oe.diff.DiffEntity;
import com.mkl.eu.service.service.persistence.oe.ref.province.AbstractProvinceEntity;
import com.mkl.eu.service.service.persistence.ref.IProvinceDao;
import com.mkl.eu.service.service.service.GameDiffsInfo;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Implementation of the Board Service.
 *
 * @author MKL.
 */
@Service
@Transactional(rollbackFor = {TechnicalException.class, FunctionalException.class})
public class BoardServiceImpl extends AbstractService implements IBoardService {
    /** Province DAO. */
    @Autowired
    private IProvinceDao provinceDao;
    /** Stack DAO. */
    @Autowired
    private IStackDao stackDao;
    /** Counter DAO. */
    @Autowired
    private ICounterDao counterDao;
    /** Chat DAO. */
    @Autowired
    private IChatDao chatDao;
    /** Game mapping. */
    @Autowired
    private GameMapping gameMapping;
    /** Chat mapping. */
    @Autowired
    private ChatMapping chatMapping;

    /** {@inheritDoc} */
    @Override
    public List<GameLight> findGames(SimpleRequest<FindGamesRequest> request) throws FunctionalException, TechnicalException {
        failIfNull(new AbstractService.CheckForThrow<>().setTest(request).setCodeError(IConstantsCommonException.NULL_PARAMETER)
                .setMsgFormat(MSG_MISSING_PARAMETER).setName(PARAMETER_FIND_GAMES).setParams(METHOD_FIND_GAMES));

        // anonymous and logged users have the same level of information so no check on authent.

        List<GameEntity> gameEntities = gameDao.findGames(request.getRequest());

        List<GameLight> games = new ArrayList<>();
        for (GameEntity gameEntity : gameEntities) {
            boolean parsed = false;
            if (request.getRequest() != null
                    && !StringUtils.isEmpty(request.getRequest().getUsername())
                    && !StringUtils.equals(AuthentInfo.USERNAME_ANONYMOUS, request.getRequest().getUsername())) {
                List<PlayableCountryEntity> countries = gameEntity.getCountries().stream().filter(playableCountryEntity -> StringUtils.equals(playableCountryEntity.getUsername(), request.getRequest().getUsername())).collect(Collectors.toList());
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
        failIfNull(new AbstractService.CheckForThrow<>().setTest(request).setCodeError(IConstantsCommonException.NULL_PARAMETER)
                .setMsgFormat(MSG_MISSING_PARAMETER).setName(PARAMETER_LOAD_GAME).setParams(METHOD_LOAD_GAME));
        failIfNull(new AbstractService.CheckForThrow<>().setTest(request.getRequest()).setCodeError(IConstantsCommonException.NULL_PARAMETER)
                .setMsgFormat(MSG_MISSING_PARAMETER).setName(PARAMETER_LOAD_GAME, PARAMETER_REQUEST).setParams(METHOD_LOAD_GAME));
        failIfNull(new AbstractService.CheckForThrow<>().setTest(request.getRequest().getIdGame()).setCodeError(IConstantsCommonException.NULL_PARAMETER)
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

        failIfFalse(new AbstractService.CheckForThrow<Boolean>().setTest(isCountryOk).setCodeError(IConstantsCommonException.INVALID_PARAMETER)
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
        failIfNull(new AbstractService.CheckForThrow<>().setTest(request).setCodeError(IConstantsCommonException.NULL_PARAMETER)
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

    /** {@inheritDoc} */
    @Override
    public DiffResponse moveStack(Request<MoveStackRequest> request) throws FunctionalException {
        failIfNull(new AbstractService.CheckForThrow<>().setTest(request).setCodeError(IConstantsCommonException.NULL_PARAMETER)
                .setMsgFormat(MSG_MISSING_PARAMETER).setName(PARAMETER_MOVE_STACK).setParams(METHOD_MOVE_STACK));

        GameDiffsInfo gameDiffs = checkGameAndGetDiffs(request.getGame(), METHOD_MOVE_STACK, PARAMETER_MOVE_STACK);
        GameEntity game = gameDiffs.getGame();

        failIfNull(new AbstractService.CheckForThrow<>().setTest(request.getRequest()).setCodeError(IConstantsCommonException.NULL_PARAMETER)
                .setMsgFormat(MSG_MISSING_PARAMETER).setName(PARAMETER_MOVE_STACK, PARAMETER_REQUEST).setParams(METHOD_MOVE_STACK));
        // TODO authorization

        Long idStack = request.getRequest().getIdStack();
        String provinceTo = request.getRequest().getProvinceTo();

        failIfNull(new CheckForThrow<>().setTest(idStack).setCodeError(IConstantsCommonException.NULL_PARAMETER)
                .setMsgFormat(MSG_MISSING_PARAMETER).setName(PARAMETER_MOVE_STACK, PARAMETER_REQUEST, PARAMETER_ID_STACK).setParams(METHOD_MOVE_STACK));
        failIfEmpty(new CheckForThrow<String>().setTest(provinceTo).setCodeError(IConstantsCommonException.NULL_PARAMETER)
                .setMsgFormat(MSG_MISSING_PARAMETER).setName(PARAMETER_MOVE_STACK, PARAMETER_REQUEST, PARAMETER_PROVINCE_TO).setParams(METHOD_MOVE_STACK));

        Optional<StackEntity> stackOpt = game.getStacks().stream().filter(x -> idStack.equals(x.getId())).findFirst();

        failIfFalse(new CheckForThrow<Boolean>().setTest(stackOpt.isPresent()).setCodeError(IConstantsCommonException.INVALID_PARAMETER)
                .setMsgFormat(MSG_OBJECT_NOT_FOUND).setName(PARAMETER_MOVE_STACK, PARAMETER_REQUEST, PARAMETER_ID_STACK).setParams(METHOD_MOVE_STACK, idStack));

        StackEntity stack = stackOpt.get();

        AbstractProvinceEntity province = provinceDao.getProvinceByName(provinceTo);

        failIfNull(new CheckForThrow<>().setTest(province).setCodeError(IConstantsCommonException.INVALID_PARAMETER)
                .setMsgFormat(MSG_OBJECT_NOT_FOUND).setName(PARAMETER_MOVE_STACK, PARAMETER_REQUEST, PARAMETER_PROVINCE_TO).setParams(METHOD_MOVE_STACK, provinceTo));

        AbstractProvinceEntity provinceStack = provinceDao.getProvinceByName(stack.getProvince());
        boolean isNear = false;
        if (provinceStack != null) {
            isNear = provinceStack.getBorders().stream().filter(x -> province.getId().equals(x.getProvinceTo().getId())).findFirst().isPresent();
        }

        failIfFalse(new CheckForThrow<Boolean>().setTest(isNear).setCodeError(IConstantsCommonException.INVALID_PARAMETER)
                .setMsgFormat(MSG_NOT_NEIGHBOR).setName(PARAMETER_MOVE_STACK, PARAMETER_REQUEST, PARAMETER_PROVINCE_TO).setParams(METHOD_MOVE_STACK, provinceTo, stack.getProvince()));

        DiffEntity diff = new DiffEntity();
        diff.setIdGame(game.getId());
        diff.setVersionGame(game.getVersion());
        diff.setType(DiffTypeEnum.MOVE);
        diff.setTypeObject(DiffTypeObjectEnum.STACK);
        diff.setIdObject(idStack);
        DiffAttributesEntity diffAttributes = new DiffAttributesEntity();
        diffAttributes.setType(DiffAttributeTypeEnum.PROVINCE_FROM);
        diffAttributes.setValue(stack.getProvince());
        diffAttributes.setDiff(diff);
        diff.getAttributes().add(diffAttributes);
        diffAttributes = new DiffAttributesEntity();
        diffAttributes.setType(DiffAttributeTypeEnum.PROVINCE_TO);
        diffAttributes.setValue(provinceTo);
        diffAttributes.setDiff(diff);
        diff.getAttributes().add(diffAttributes);

        createDiff(diff);

        List<DiffEntity> diffs = gameDiffs.getDiffs();
        diffs.add(diff);

        stack.setProvince(provinceTo);
        gameDao.update(game, false);

        DiffResponse response = new DiffResponse();
        response.setDiffs(diffMapping.oesToVos(diffs));
        response.setVersionGame(game.getVersion());

        response.setMessages(getMessagesSince(request));

        return response;
    }

    /** {@inheritDoc} */
    @Override
    public DiffResponse moveCounter(Request<MoveCounterRequest> request) throws FunctionalException, TechnicalException {
        failIfNull(new AbstractService.CheckForThrow<>().setTest(request).setCodeError(IConstantsCommonException.NULL_PARAMETER)
                .setMsgFormat(MSG_MISSING_PARAMETER).setName(PARAMETER_MOVE_COUNTER).setParams(METHOD_MOVE_COUNTER));
        failIfNull(new AbstractService.CheckForThrow<>().setTest(request.getAuthent()).setCodeError(IConstantsCommonException.NULL_PARAMETER)
                .setMsgFormat(MSG_MISSING_PARAMETER).setName(PARAMETER_MOVE_COUNTER, PARAMETER_AUTHENT).setParams(METHOD_MOVE_COUNTER));

        GameDiffsInfo gameDiffs = checkGameAndGetDiffs(request.getGame(), METHOD_MOVE_COUNTER, PARAMETER_MOVE_COUNTER);
        GameEntity game = gameDiffs.getGame();

        failIfNull(new AbstractService.CheckForThrow<>().setTest(request.getRequest()).setCodeError(IConstantsCommonException.NULL_PARAMETER)
                .setMsgFormat(MSG_MISSING_PARAMETER).setName(PARAMETER_MOVE_COUNTER, PARAMETER_REQUEST).setParams(METHOD_MOVE_COUNTER));

        Long idCounter = request.getRequest().getIdCounter();
        Long idStack = request.getRequest().getIdStack();

        failIfNull(new CheckForThrow<>().setTest(idCounter).setCodeError(IConstantsCommonException.NULL_PARAMETER)
                .setMsgFormat(MSG_MISSING_PARAMETER).setName(PARAMETER_MOVE_COUNTER, PARAMETER_REQUEST, PARAMETER_ID_COUNTER).setParams(METHOD_MOVE_COUNTER));

        CounterEntity counter = counterDao.getCounter(idCounter, game.getId());

        failIfNull(new CheckForThrow<>().setTest(counter).setCodeError(IConstantsCommonException.INVALID_PARAMETER)
                .setMsgFormat(MSG_OBJECT_NOT_FOUND).setName(PARAMETER_MOVE_COUNTER, PARAMETER_REQUEST, PARAMETER_ID_COUNTER).setParams(METHOD_MOVE_COUNTER, game.getId()));

        Optional<PlayableCountryEntity> country = game.getCountries().stream().filter(x -> StringUtils.equals(counter.getCountry(), x.getName())).findFirst();
        if (country.isPresent()) {
            failIfFalse(new CheckForThrow<Boolean>().setTest(StringUtils.equals(request.getAuthent().getUsername(), country.get().getUsername()))
                    .setCodeError(IConstantsCommonException.ACCESS_RIGHT)
                    .setMsgFormat(MSG_ACCESS_RIGHT).setName(PARAMETER_MOVE_COUNTER, PARAMETER_AUTHENT, PARAMETER_USERNAME).setParams(METHOD_MOVE_COUNTER, request.getAuthent().getUsername(), country.get().getUsername()));

        } else {
            List<String> patrons = counterDao.getPatrons(counter.getCountry(), game.getId());
            if (patrons.size() == 1) {
                country = game.getCountries().stream().filter(x -> StringUtils.equals(patrons.get(0), x.getName())).findFirst();
                if (country.isPresent()) {
                    failIfFalse(new CheckForThrow<Boolean>().setTest(StringUtils.equals(request.getAuthent().getUsername(), country.get().getUsername()))
                            .setCodeError(IConstantsCommonException.ACCESS_RIGHT)
                            .setMsgFormat(MSG_ACCESS_RIGHT).setName(PARAMETER_USERNAME).setParams(METHOD_MOVE_COUNTER, request.getAuthent().getUsername(), country.get().getUsername()));

                }
            } else {
                // TODO manage minor countries in war with no or multiple patrons
            }
        }

        Optional<StackEntity> stackOpt = null;
        if (idStack != null) {
            stackOpt = game.getStacks().stream().filter(x -> idStack.equals(x.getId())).findFirst();
        }


        StackEntity stack;
        if (stackOpt != null && stackOpt.isPresent()) {
            stack = stackOpt.get();
        } else {
            stack = new StackEntity();
            stack.setProvince(counter.getOwner().getProvince());
            stack.setGame(game);

        /*
         Thanks Hibernate to have 7 years old bugs.
         https://hibernate.atlassian.net/browse/HHH-6776
         https://hibernate.atlassian.net/browse/HHH-7404
          */

            stackDao.create(stack);

            game.getStacks().add(stack);
        }

        failIfFalse(new CheckForThrow<Boolean>().setTest(StringUtils.equals(counter.getOwner().getProvince(), stack.getProvince())).setCodeError(IConstantsCommonException.INVALID_PARAMETER)
                .setMsgFormat(MSG_NOT_SAME_PROVINCE).setName(PARAMETER_MOVE_COUNTER, PARAMETER_REQUEST, PARAMETER_ID_STACK).setParams(METHOD_MOVE_COUNTER, counter.getOwner().getProvince(), stack.getProvince()));

        failIfFalse(new CheckForThrow<Boolean>().setTest(counter.getOwner() != stack).setCodeError(IConstantsCommonException.INVALID_PARAMETER)
                .setMsgFormat(MSG_NOT_SAME_STACK).setName(PARAMETER_MOVE_COUNTER, PARAMETER_REQUEST, PARAMETER_ID_STACK).setParams(METHOD_MOVE_COUNTER));

        DiffEntity diff = new DiffEntity();
        diff.setIdGame(game.getId());
        diff.setVersionGame(game.getVersion());
        diff.setType(DiffTypeEnum.MOVE);
        diff.setTypeObject(DiffTypeObjectEnum.COUNTER);
        diff.setIdObject(idCounter);
        DiffAttributesEntity diffAttributes = new DiffAttributesEntity();
        diffAttributes.setType(DiffAttributeTypeEnum.STACK_FROM);
        diffAttributes.setValue(counter.getOwner().getId().toString());
        diffAttributes.setDiff(diff);
        diff.getAttributes().add(diffAttributes);
        diffAttributes = new DiffAttributesEntity();
        diffAttributes.setType(DiffAttributeTypeEnum.STACK_TO);
        diffAttributes.setValue(stack.getId().toString());
        diffAttributes.setDiff(diff);
        diff.getAttributes().add(diffAttributes);
        diffAttributes = new DiffAttributesEntity();
        diffAttributes.setType(DiffAttributeTypeEnum.PROVINCE);
        diffAttributes.setValue(counter.getOwner().getProvince());
        diffAttributes.setDiff(diff);
        diff.getAttributes().add(diffAttributes);
        if (counter.getOwner().getCounters().size() == 1) {
            diffAttributes = new DiffAttributesEntity();
            diffAttributes.setType(DiffAttributeTypeEnum.STACK_DEL);
            diffAttributes.setValue(counter.getOwner().getId().toString());
            diffAttributes.setDiff(diff);
            diff.getAttributes().add(diffAttributes);
        }

        createDiff(diff);

        List<DiffEntity> diffs = gameDiffs.getDiffs();
        diffs.add(diff);

        StackEntity oldStack = counter.getOwner();
        counter.setOwner(stack);
        oldStack.getCounters().remove(counter);
        stack.getCounters().add(counter);
        if (oldStack.getCounters().isEmpty()) {
            oldStack.setGame(null);
            game.getStacks().remove(oldStack);
        }

        gameDao.update(game, false);

        DiffResponse response = new DiffResponse();
        response.setDiffs(diffMapping.oesToVos(diffs));
        response.setVersionGame(game.getVersion());

        response.setMessages(getMessagesSince(request));

        return response;
    }
}
