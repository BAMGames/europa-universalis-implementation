package com.mkl.eu.service.service.service.impl;

import com.mkl.eu.client.common.exception.FunctionalException;
import com.mkl.eu.client.common.exception.IConstantsCommonException;
import com.mkl.eu.client.common.exception.TechnicalException;
import com.mkl.eu.client.common.util.CommonUtil;
import com.mkl.eu.client.common.vo.AuthentInfo;
import com.mkl.eu.client.common.vo.GameInfo;
import com.mkl.eu.client.common.vo.Request;
import com.mkl.eu.client.common.vo.SimpleRequest;
import com.mkl.eu.client.service.service.IBoardService;
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
import com.mkl.eu.service.service.persistence.oe.diff.DiffAttributesEntity;
import com.mkl.eu.service.service.persistence.oe.diff.DiffEntity;
import com.mkl.eu.service.service.persistence.oe.ref.province.AbstractProvinceEntity;
import com.mkl.eu.service.service.persistence.ref.IProvinceDao;
import com.mkl.eu.service.service.service.GameDiffsInfo;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

/**
 * Implementation of the Board Service.
 *
 * @author MKL.
 */
@Service
@Transactional(rollbackFor = {TechnicalException.class, FunctionalException.class})
public class BoardServiceImpl extends AbstractService implements IBoardService {
    /** Game DAO. */
    @Autowired
    private IGameDao gameDao;
    /** Province DAO. */
    @Autowired
    private IProvinceDao provinceDao;
    /** Stack DAO. */
    @Autowired
    private IStackDao stackDao;
    /** Counter DAO. */
    @Autowired
    private ICounterDao counterDao;
    /** Diff DAO. */
    @Autowired
    private IDiffDao diffDao;
    /** Chat DAO. */
    @Autowired
    private IChatDao chatDao;
    /** Game mapping. */
    @Autowired
    private GameMapping gameMapping;
    /** Chat mapping. */
    @Autowired
    private ChatMapping chatMapping;
    /** Diff mapping. */
    @Autowired
    private DiffMapping diffMapping;

    /** {@inheritDoc} */
    @Override
    public Game loadGame(SimpleRequest<LoadGameRequest> loadGame) throws FunctionalException {
        failIfNull(new AbstractService.CheckForThrow<>().setTest(loadGame).setCodeError(IConstantsCommonException.NULL_PARAMETER)
                .setMsgFormat(MSG_MISSING_PARAMETER).setName(PARAMETER_LOAD_GAME).setParams(METHOD_LOAD_GAME));
        failIfNull(new AbstractService.CheckForThrow<>().setTest(loadGame.getRequest()).setCodeError(IConstantsCommonException.NULL_PARAMETER)
                .setMsgFormat(MSG_MISSING_PARAMETER).setName(PARAMETER_LOAD_GAME, PARAMETER_REQUEST).setParams(METHOD_LOAD_GAME));
        failIfNull(new AbstractService.CheckForThrow<>().setTest(loadGame.getRequest().getIdGame()).setCodeError(IConstantsCommonException.NULL_PARAMETER)
                .setMsgFormat(MSG_MISSING_PARAMETER).setName(PARAMETER_LOAD_GAME, PARAMETER_REQUEST, PARAMETER_ID_GAME).setParams(METHOD_LOAD_GAME));

        if (loadGame.getAuthent() == null) {
            loadGame.setAuthent(AuthentInfo.ANONYMOUS);
        }

        Long idGame = loadGame.getRequest().getIdGame();
        Long idCountry = loadGame.getRequest().getIdCountry();

        GameEntity game = gameDao.read(idGame);

        boolean isCountryOk = idCountry == null;
        if (!isCountryOk) {
            PlayableCountryEntity country = CommonUtil.findFirst(game.getCountries(), x -> x.getId().equals(idCountry));
            isCountryOk = country != null && StringUtils.equals(loadGame.getAuthent().getUsername(), country.getUsername());
        }

        failIfFalse(new AbstractService.CheckForThrow<Boolean>().setTest(isCountryOk).setCodeError(IConstantsCommonException.INVALID_PARAMETER)
                .setMsgFormat("{1}: {0} ({2}) is not in game or is not played by {3}.").setName(PARAMETER_LOAD_GAME, PARAMETER_REQUEST, PARAMETER_ID_COUNTRY).setParams(METHOD_LOAD_GAME, idCountry, loadGame.getAuthent().getUsername()));

        Game returnValue = gameMapping.oeToVo(game);

        List<MessageGlobalEntity> globalMessages = chatDao.getGlobalMessages(idGame);
        List<ChatEntity> messages = null;
        List<RoomEntity> rooms = null;
        if (idCountry != null) {
            rooms = chatDao.getRooms(idGame, idCountry);
            messages = chatDao.getMessages(idGame, idCountry);
        }
        Chat chat = chatMapping.getChat(globalMessages, rooms, messages, idCountry);
        returnValue.setChat(chat);

        return returnValue;
    }

    /** {@inheritDoc} */
    @Override
    public DiffResponse updateGame(Request<Void> updateGame) throws FunctionalException {
        failIfNull(new AbstractService.CheckForThrow<>().setTest(updateGame).setCodeError(IConstantsCommonException.NULL_PARAMETER)
                .setMsgFormat(MSG_MISSING_PARAMETER).setName(PARAMETER_UPDATE_GAME).setParams(METHOD_UPDATE_GAME));

        GameDiffsInfo gameDiffs = checkGameAndGetDiffs(updateGame.getGame(), METHOD_UPDATE_GAME, PARAMETER_UPDATE_GAME);

        List<DiffEntity> diffs = gameDiffs.getDiffs();
        List<Diff> diffVos = diffMapping.oesToVos(diffs);

        DiffResponse response = new DiffResponse();
        response.setDiffs(diffVos);
        response.setVersionGame(gameDiffs.getGame().getVersion());

        return response;
    }

    /** {@inheritDoc} */
    @Override
    public DiffResponse moveStack(Request<MoveStackRequest> moveStack) throws FunctionalException {
        failIfNull(new AbstractService.CheckForThrow<>().setTest(moveStack).setCodeError(IConstantsCommonException.NULL_PARAMETER)
                .setMsgFormat(MSG_MISSING_PARAMETER).setName(PARAMETER_MOVE_STACK).setParams(METHOD_MOVE_STACK));

        GameDiffsInfo gameDiffs = checkGameAndGetDiffs(moveStack.getGame(), METHOD_MOVE_STACK, PARAMETER_MOVE_STACK);
        GameEntity game = gameDiffs.getGame();

        failIfNull(new AbstractService.CheckForThrow<>().setTest(moveStack.getRequest()).setCodeError(IConstantsCommonException.NULL_PARAMETER)
                .setMsgFormat(MSG_MISSING_PARAMETER).setName(PARAMETER_MOVE_STACK, PARAMETER_REQUEST).setParams(METHOD_MOVE_STACK));
        // TODO authorization

        Long idStack = moveStack.getRequest().getIdStack();
        String provinceTo = moveStack.getRequest().getProvinceTo();

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

        diffDao.create(diff);

        List<DiffEntity> diffs = gameDiffs.getDiffs();
        diffs.add(diff);

        stack.setProvince(provinceTo);
        gameDao.update(game, false);

        DiffResponse response = new DiffResponse();
        response.setDiffs(diffMapping.oesToVos(diffs));
        response.setVersionGame(game.getVersion());

        return response;
    }

    /**
     * Check that game info are properly assigned, retrieve the game and its diffs and return it.
     *
     * @param gameInfo to check.
     * @param method   calling this. For logging purpose.
     * @param param    name of the param holding the gameInfo. For logging purpose.
     * @return the game and its diffs.
     * @throws FunctionalException functional exception.
     */
    protected GameDiffsInfo checkGameAndGetDiffs(GameInfo gameInfo, String method, String param) throws FunctionalException {
        failIfNull(new AbstractService.CheckForThrow<>().setTest(gameInfo).setCodeError(IConstantsCommonException.NULL_PARAMETER)
                .setMsgFormat(MSG_MISSING_PARAMETER).setName(param, PARAMETER_GAME).setParams(method));

        Long idGame = gameInfo.getIdGame();
        Long versionGame = gameInfo.getVersionGame();

        failIfNull(new CheckForThrow<>().setTest(idGame).setCodeError(IConstantsCommonException.NULL_PARAMETER)
                .setMsgFormat(MSG_MISSING_PARAMETER).setName(param, PARAMETER_GAME, PARAMETER_ID_GAME).setParams(method));
        failIfNull(new CheckForThrow<>().setTest(versionGame).setCodeError(IConstantsCommonException.NULL_PARAMETER)
                .setMsgFormat(MSG_MISSING_PARAMETER).setName(param, PARAMETER_GAME, PARAMETER_VERSION_GAME).setParams(method));

        GameEntity game = gameDao.lock(idGame);

        failIfNull(new CheckForThrow<>().setTest(game).setCodeError(IConstantsCommonException.INVALID_PARAMETER)
                .setMsgFormat(MSG_OBJECT_NOT_FOUND).setName(param, PARAMETER_GAME, PARAMETER_ID_GAME).setParams(method, idGame));
        failIfFalse(new CheckForThrow<Boolean>().setTest(versionGame < game.getVersion()).setCodeError(IConstantsCommonException.INVALID_PARAMETER)
                .setMsgFormat(MSG_VERSION_INCORRECT).setName(param, PARAMETER_GAME, PARAMETER_VERSION_GAME).setParams(method, versionGame, game.getVersion()));

        List<DiffEntity> diffs = diffDao.getDiffsSince(idGame, versionGame);

        return new GameDiffsInfo(game, diffs);
    }

    /** {@inheritDoc} */
    @Override
    public DiffResponse moveCounter(Request<MoveCounterRequest> moveCounter) throws FunctionalException, TechnicalException {
        failIfNull(new AbstractService.CheckForThrow<>().setTest(moveCounter).setCodeError(IConstantsCommonException.NULL_PARAMETER)
                .setMsgFormat(MSG_MISSING_PARAMETER).setName(PARAMETER_MOVE_COUNTER).setParams(METHOD_MOVE_COUNTER));
        failIfNull(new AbstractService.CheckForThrow<>().setTest(moveCounter.getAuthent()).setCodeError(IConstantsCommonException.NULL_PARAMETER)
                .setMsgFormat(MSG_MISSING_PARAMETER).setName(PARAMETER_MOVE_COUNTER, PARAMETER_AUTHENT).setParams(METHOD_MOVE_COUNTER));

        GameDiffsInfo gameDiffs = checkGameAndGetDiffs(moveCounter.getGame(), METHOD_MOVE_COUNTER, PARAMETER_MOVE_COUNTER);
        GameEntity game = gameDiffs.getGame();

        failIfNull(new AbstractService.CheckForThrow<>().setTest(moveCounter.getRequest()).setCodeError(IConstantsCommonException.NULL_PARAMETER)
                .setMsgFormat(MSG_MISSING_PARAMETER).setName(PARAMETER_MOVE_COUNTER, PARAMETER_REQUEST).setParams(METHOD_MOVE_COUNTER));

        Long idCounter = moveCounter.getRequest().getIdCounter();
        Long idStack = moveCounter.getRequest().getIdStack();

        failIfNull(new CheckForThrow<>().setTest(idCounter).setCodeError(IConstantsCommonException.NULL_PARAMETER)
                .setMsgFormat(MSG_MISSING_PARAMETER).setName(PARAMETER_MOVE_COUNTER, PARAMETER_REQUEST, PARAMETER_ID_COUNTER).setParams(METHOD_MOVE_COUNTER));

        CounterEntity counter = counterDao.getCounter(idCounter, game.getId());

        failIfNull(new CheckForThrow<>().setTest(counter).setCodeError(IConstantsCommonException.INVALID_PARAMETER)
                .setMsgFormat(MSG_OBJECT_NOT_FOUND).setName(PARAMETER_MOVE_COUNTER, PARAMETER_REQUEST, PARAMETER_ID_COUNTER).setParams(METHOD_MOVE_COUNTER, game.getId()));

        Optional<PlayableCountryEntity> country = game.getCountries().stream().filter(x -> StringUtils.equals(counter.getCountry(), x.getName())).findFirst();
        if (country.isPresent()) {
            failIfFalse(new CheckForThrow<Boolean>().setTest(StringUtils.equals(moveCounter.getAuthent().getUsername(), country.get().getUsername()))
                    .setCodeError(IConstantsCommonException.ACCESS_RIGHT)
                    .setMsgFormat(MSG_ACCESS_RIGHT).setName(PARAMETER_USERNAME).setParams(METHOD_MOVE_COUNTER, moveCounter.getAuthent().getUsername(), country.get().getUsername()));

        } else {
            List<String> patrons = counterDao.getPatrons(counter.getCountry(), game.getId());
            if (patrons.size() == 1) {
                country = game.getCountries().stream().filter(x -> StringUtils.equals(patrons.get(0), x.getName())).findFirst();
                if (country.isPresent()) {
                    failIfFalse(new CheckForThrow<Boolean>().setTest(StringUtils.equals(moveCounter.getAuthent().getUsername(), country.get().getUsername()))
                            .setCodeError(IConstantsCommonException.ACCESS_RIGHT)
                            .setMsgFormat(MSG_ACCESS_RIGHT).setName(PARAMETER_USERNAME).setParams(METHOD_MOVE_COUNTER, moveCounter.getAuthent().getUsername(), country.get().getUsername()));

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

        diffDao.create(diff);

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

        return response;
    }
}
