package com.mkl.eu.service.service.service.impl;

import com.mkl.eu.client.common.exception.FunctionalException;
import com.mkl.eu.client.common.exception.TechnicalException;
import com.mkl.eu.client.common.vo.Request;
import com.mkl.eu.client.service.service.ITestService;
import com.mkl.eu.client.service.service.test.TestDiffRequest;
import com.mkl.eu.client.service.vo.diff.DiffResponse;
import com.mkl.eu.service.service.socket.WebSocketServer;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Service for chat purpose.
 *
 * @author MKL.
 */
@Service
@Transactional(rollbackFor = {TechnicalException.class, FunctionalException.class})
public class TestServiceImpl extends AbstractService implements ITestService {

    /** {@inheritDoc} */
    @Override
    public DiffResponse testDiff(Request<TestDiffRequest> request) {
        DiffResponse response = new DiffResponse();
        response.setDiffs(request.getRequest().getDiffs());
        response.setVersionGame(request.getGame().getVersionGame());
        WebSocketServer.push(request.getGame().getIdGame(), response, null);
        return response;
    }
}
