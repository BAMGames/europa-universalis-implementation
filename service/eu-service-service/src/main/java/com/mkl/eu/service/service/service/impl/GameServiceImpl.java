package com.mkl.eu.service.service.service.impl;

import com.mkl.eu.client.common.exception.FunctionalException;
import com.mkl.eu.client.common.exception.IConstantsCommonException;
import com.mkl.eu.client.common.exception.TechnicalException;
import com.mkl.eu.client.common.vo.AuthentRequest;
import com.mkl.eu.client.service.service.IGameService;
import com.mkl.eu.client.service.service.game.LoadGameRequest;
import com.mkl.eu.client.service.service.game.MoveCounterRequest;
import com.mkl.eu.client.service.service.game.MoveStackRequest;
import com.mkl.eu.client.service.service.game.UpdateGameRequest;
import com.mkl.eu.client.service.vo.Game;
import com.mkl.eu.client.service.vo.diff.Diff;
import com.mkl.eu.client.service.vo.diff.DiffResponse;
import com.mkl.eu.client.service.vo.enumeration.DiffAttributeTypeEnum;
import com.mkl.eu.client.service.vo.enumeration.DiffTypeEnum;
import com.mkl.eu.client.service.vo.enumeration.DiffTypeObjectEnum;
import com.mkl.eu.service.service.mapping.GameMapping;
import com.mkl.eu.service.service.mapping.diff.DiffMapping;
import com.mkl.eu.service.service.persistence.IGameDao;
import com.mkl.eu.service.service.persistence.board.ICounterDao;
import com.mkl.eu.service.service.persistence.board.IStackDao;
import com.mkl.eu.service.service.persistence.diff.IDiffDao;
import com.mkl.eu.service.service.persistence.oe.GameEntity;
import com.mkl.eu.service.service.persistence.oe.board.CounterEntity;
import com.mkl.eu.service.service.persistence.oe.board.StackEntity;
import com.mkl.eu.service.service.persistence.oe.diff.DiffAttributesEntity;
import com.mkl.eu.service.service.persistence.oe.diff.DiffEntity;
import com.mkl.eu.service.service.persistence.oe.ref.province.AbstractProvinceEntity;
import com.mkl.eu.service.service.persistence.ref.IProvinceDao;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

/**
 * Implementation of the Game Service.
 *
 * @author MKL.
 */
@Service
@Transactional(rollbackFor = {TechnicalException.class, FunctionalException.class})
public class GameServiceImpl extends AbstractService implements IGameService {
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
    /** Game mapping. */
    @Autowired
    private GameMapping gameMapping;
    /** Diff mapping. */
    @Autowired
    private DiffMapping diffMapping;

    /** {@inheritDoc} */
    @Override
    public Game loadGame(AuthentRequest<LoadGameRequest> loadGame) throws FunctionalException {
        failIfNull(new AbstractService.CheckForThrow<>().setTest(loadGame).setCodeError(IConstantsCommonException.NULL_PARAMETER)
                .setMsgFormat(MSG_MISSING_PARAMETER).setName(PARAMETER_LOAD_GAME).setParams(METHOD_LOAD_GAME));
        failIfNull(new AbstractService.CheckForThrow<>().setTest(loadGame.getRequest()).setCodeError(IConstantsCommonException.NULL_PARAMETER)
                .setMsgFormat(MSG_MISSING_PARAMETER).setName(PARAMETER_LOAD_GAME, PARAMETER_REQUEST).setParams(METHOD_LOAD_GAME));
        failIfNull(new AbstractService.CheckForThrow<>().setTest(loadGame.getRequest().getIdGame()).setCodeError(IConstantsCommonException.NULL_PARAMETER)
                .setMsgFormat(MSG_MISSING_PARAMETER).setName(PARAMETER_LOAD_GAME, PARAMETER_REQUEST, PARAMETER_ID_GAME).setParams(METHOD_LOAD_GAME));

        GameEntity game = gameDao.read(loadGame.getRequest().getIdGame());
        return gameMapping.oeToVo(game);
    }

    /** {@inheritDoc} */
    @Override
    public DiffResponse updateGame(AuthentRequest<UpdateGameRequest> updateGame) throws FunctionalException {
        failIfNull(new AbstractService.CheckForThrow<>().setTest(updateGame).setCodeError(IConstantsCommonException.NULL_PARAMETER)
                .setMsgFormat(MSG_MISSING_PARAMETER).setName(PARAMETER_UPDATE_GAME).setParams(METHOD_UPDATE_GAME));
        failIfNull(new AbstractService.CheckForThrow<>().setTest(updateGame.getRequest()).setCodeError(IConstantsCommonException.NULL_PARAMETER)
                .setMsgFormat(MSG_MISSING_PARAMETER).setName(PARAMETER_UPDATE_GAME, PARAMETER_REQUEST).setParams(METHOD_UPDATE_GAME));
        failIfNull(new AbstractService.CheckForThrow<>().setTest(updateGame.getRequest().getIdGame()).setCodeError(IConstantsCommonException.NULL_PARAMETER)
                .setMsgFormat(MSG_MISSING_PARAMETER).setName(PARAMETER_UPDATE_GAME, PARAMETER_REQUEST, PARAMETER_ID_GAME).setParams(METHOD_UPDATE_GAME));
        failIfNull(new CheckForThrow<>().setTest(updateGame.getRequest().getVersionGame()).setCodeError(IConstantsCommonException.NULL_PARAMETER)
                .setMsgFormat(MSG_MISSING_PARAMETER).setName(PARAMETER_UPDATE_GAME, PARAMETER_REQUEST, PARAMETER_VERSION_GAME).setParams(METHOD_UPDATE_GAME));

        List<DiffEntity> diffs = diffDao.getDiffsSince(updateGame.getRequest().getIdGame(), updateGame.getRequest().getVersionGame());
        List<Diff> diffVos = diffMapping.oesToVos(diffs);

        DiffResponse response = new DiffResponse();
        response.setDiffs(diffVos);
        if (!diffs.isEmpty()) {
            response.setVersionGame(diffs.stream().max((o1, o2) -> (int) (o1.getVersionGame() - o2.getVersionGame())).get().getVersionGame());
        } else {
            // if no diff, game is up to date and has the right version
            response.setVersionGame(updateGame.getRequest().getVersionGame());
        }

        return response;
    }

    /** {@inheritDoc} */
    @Override
    public DiffResponse moveStack(AuthentRequest<MoveStackRequest> moveStack) throws FunctionalException {
        failIfNull(new AbstractService.CheckForThrow<>().setTest(moveStack).setCodeError(IConstantsCommonException.NULL_PARAMETER)
                .setMsgFormat(MSG_MISSING_PARAMETER).setName(PARAMETER_MOVE_STACK).setParams(METHOD_UPDATE_GAME));
        failIfNull(new AbstractService.CheckForThrow<>().setTest(moveStack.getRequest()).setCodeError(IConstantsCommonException.NULL_PARAMETER)
                .setMsgFormat(MSG_MISSING_PARAMETER).setName(PARAMETER_MOVE_STACK, PARAMETER_REQUEST).setParams(METHOD_UPDATE_GAME));
        // TODO authorization

        Long idGame = moveStack.getRequest().getIdGame();
        Long versionGame = moveStack.getRequest().getVersionGame();
        Long idStack = moveStack.getRequest().getIdStack();
        String provinceTo = moveStack.getRequest().getProvinceTo();

        failIfNull(new CheckForThrow<>().setTest(idGame).setCodeError(IConstantsCommonException.NULL_PARAMETER)
                .setMsgFormat(MSG_MISSING_PARAMETER).setName(PARAMETER_MOVE_STACK, PARAMETER_REQUEST, PARAMETER_ID_GAME).setParams(METHOD_MOVE_STACK));
        failIfNull(new CheckForThrow<>().setTest(versionGame).setCodeError(IConstantsCommonException.NULL_PARAMETER)
                .setMsgFormat(MSG_MISSING_PARAMETER).setName(PARAMETER_MOVE_STACK, PARAMETER_REQUEST, PARAMETER_VERSION_GAME).setParams(METHOD_MOVE_STACK));
        failIfNull(new CheckForThrow<>().setTest(idStack).setCodeError(IConstantsCommonException.NULL_PARAMETER)
                .setMsgFormat(MSG_MISSING_PARAMETER).setName(PARAMETER_MOVE_STACK, PARAMETER_REQUEST, PARAMETER_ID_STACK).setParams(METHOD_MOVE_STACK));
        failIfEmpty(new CheckForThrow<String>().setTest(provinceTo).setCodeError(IConstantsCommonException.NULL_PARAMETER)
                .setMsgFormat(MSG_MISSING_PARAMETER).setName(PARAMETER_MOVE_STACK, PARAMETER_REQUEST, PARAMETER_PROVINCE_TO).setParams(METHOD_MOVE_STACK));

        GameEntity game = gameDao.lock(idGame);

        failIfNull(new CheckForThrow<>().setTest(game).setCodeError(IConstantsCommonException.INVALID_PARAMETER)
                .setMsgFormat(MSG_OBJECT_NOT_FOUNT).setName(PARAMETER_MOVE_STACK, PARAMETER_REQUEST, PARAMETER_ID_GAME).setParams(METHOD_MOVE_STACK, idGame));
        failIfFalse(new CheckForThrow<Boolean>().setTest(versionGame < game.getVersion()).setCodeError(IConstantsCommonException.INVALID_PARAMETER)
                .setMsgFormat(MSG_VERSION_INCORRECT).setName(PARAMETER_MOVE_STACK, PARAMETER_REQUEST, PARAMETER_VERSION_GAME).setParams(METHOD_MOVE_STACK, versionGame, game.getVersion()));

        List<DiffEntity> diffs = diffDao.getDiffsSince(idGame, versionGame);

        Optional<StackEntity> stackOpt = game.getStacks().stream().filter(x -> idStack.equals(x.getId())).findFirst();

        failIfFalse(new CheckForThrow<Boolean>().setTest(stackOpt.isPresent()).setCodeError(IConstantsCommonException.INVALID_PARAMETER)
                .setMsgFormat(MSG_OBJECT_NOT_FOUNT).setName(PARAMETER_MOVE_STACK, PARAMETER_REQUEST, PARAMETER_ID_STACK).setParams(METHOD_MOVE_STACK, idStack));

        StackEntity stack = stackOpt.get();

        AbstractProvinceEntity province = provinceDao.getProvinceByName(provinceTo);

        failIfNull(new CheckForThrow<>().setTest(province).setCodeError(IConstantsCommonException.INVALID_PARAMETER)
                .setMsgFormat(MSG_OBJECT_NOT_FOUNT).setName(PARAMETER_MOVE_STACK, PARAMETER_REQUEST, PARAMETER_PROVINCE_TO).setParams(METHOD_MOVE_STACK, provinceTo));

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

        diffs.add(diff);

        stack.setProvince(provinceTo);
        gameDao.update(game, false);

        DiffResponse response = new DiffResponse();
        response.setDiffs(diffMapping.oesToVos(diffs));
        response.setVersionGame(game.getVersion());

        return response;
    }

    /** {@inheritDoc} */
    @Override
    public DiffResponse moveCounter(AuthentRequest<MoveCounterRequest> moveCounter) throws FunctionalException, TechnicalException {
        failIfNull(new AbstractService.CheckForThrow<>().setTest(moveCounter).setCodeError(IConstantsCommonException.NULL_PARAMETER)
                .setMsgFormat(MSG_MISSING_PARAMETER).setName(PARAMETER_MOVE_COUNTER).setParams(METHOD_UPDATE_GAME));
        failIfNull(new AbstractService.CheckForThrow<>().setTest(moveCounter.getRequest()).setCodeError(IConstantsCommonException.NULL_PARAMETER)
                .setMsgFormat(MSG_MISSING_PARAMETER).setName(PARAMETER_MOVE_COUNTER, PARAMETER_REQUEST).setParams(METHOD_UPDATE_GAME));
        // TODO authorization

        Long idGame = moveCounter.getRequest().getIdGame();
        Long versionGame = moveCounter.getRequest().getVersionGame();
        Long idCounter = moveCounter.getRequest().getIdCounter();
        Long idStack = moveCounter.getRequest().getIdStack();

        failIfNull(new CheckForThrow<>().setTest(idGame).setCodeError(IConstantsCommonException.NULL_PARAMETER)
                .setMsgFormat(MSG_MISSING_PARAMETER).setName(PARAMETER_MOVE_COUNTER, PARAMETER_REQUEST, PARAMETER_ID_GAME).setParams(METHOD_MOVE_COUNTER));
        failIfNull(new CheckForThrow<>().setTest(versionGame).setCodeError(IConstantsCommonException.NULL_PARAMETER)
                .setMsgFormat(MSG_MISSING_PARAMETER).setName(PARAMETER_MOVE_COUNTER, PARAMETER_REQUEST, PARAMETER_VERSION_GAME).setParams(METHOD_MOVE_COUNTER));
        failIfNull(new CheckForThrow<>().setTest(idCounter).setCodeError(IConstantsCommonException.NULL_PARAMETER)
                .setMsgFormat(MSG_MISSING_PARAMETER).setName(PARAMETER_MOVE_COUNTER, PARAMETER_REQUEST, PARAMETER_ID_COUNTER).setParams(METHOD_MOVE_COUNTER));

        GameEntity game = gameDao.lock(idGame);

        failIfNull(new CheckForThrow<>().setTest(game).setCodeError(IConstantsCommonException.INVALID_PARAMETER)
                .setMsgFormat(MSG_OBJECT_NOT_FOUNT).setName(PARAMETER_MOVE_COUNTER, PARAMETER_REQUEST, PARAMETER_ID_GAME).setParams(METHOD_MOVE_COUNTER, idGame));
        failIfFalse(new CheckForThrow<Boolean>().setTest(versionGame < game.getVersion()).setCodeError(IConstantsCommonException.INVALID_PARAMETER)
                .setMsgFormat(MSG_VERSION_INCORRECT).setName(PARAMETER_MOVE_COUNTER, PARAMETER_REQUEST, PARAMETER_VERSION_GAME).setParams(METHOD_MOVE_COUNTER, versionGame, game.getVersion()));

        List<DiffEntity> diffs = diffDao.getDiffsSince(idGame, versionGame);

        CounterEntity counter = counterDao.getCounter(idCounter, idGame);

        failIfNull(new CheckForThrow<>().setTest(counter).setCodeError(IConstantsCommonException.INVALID_PARAMETER)
                .setMsgFormat(MSG_OBJECT_NOT_FOUNT).setName(PARAMETER_MOVE_COUNTER, PARAMETER_REQUEST, PARAMETER_ID_COUNTER).setParams(METHOD_MOVE_COUNTER, idGame));

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
