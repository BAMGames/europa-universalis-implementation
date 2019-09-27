package com.mkl.eu.client.service.service;

import com.mkl.eu.client.common.vo.Request;
import com.mkl.eu.client.service.service.test.TestDiffRequest;
import com.mkl.eu.client.service.vo.diff.DiffResponse;

import javax.jws.WebParam;
import javax.jws.WebResult;
import javax.jws.WebService;

/**
 * Interface for the test service.
 *
 * @author MKL.
 */
@WebService
public interface ITestService extends INameConstants {
    /**
     * Will lead to the fake sending of diffs in order to test the client.
     *
     * @param request the diffs to send.
     * @return the diff sent.
     */
    @WebResult(name = RESPONSE)
    DiffResponse testDiff(@WebParam(name = "test") Request<TestDiffRequest> request);
}
