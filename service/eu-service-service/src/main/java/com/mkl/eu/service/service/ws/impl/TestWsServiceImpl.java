package com.mkl.eu.service.service.ws.impl;

import com.mkl.eu.client.common.vo.Request;
import com.mkl.eu.client.service.service.ITestService;
import com.mkl.eu.client.service.service.test.TestDiffRequest;
import com.mkl.eu.client.service.vo.diff.DiffResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.web.context.support.SpringBeanAutowiringSupport;

import javax.jws.WebService;

/**
 * Separation from TestService because cxf can't handle @Transactional.
 *
 * @author MKL.
 */
@WebService(endpointInterface = "com.mkl.eu.client.service.service.ITestService")
public class TestWsServiceImpl extends SpringBeanAutowiringSupport implements ITestService {
    /** Test Service. */
    @Autowired
    @Qualifier(value = "testServiceImpl")
    private ITestService testService;

    /** {@inheritDoc} */
    @Override
    public DiffResponse testDiff(Request<TestDiffRequest> request) {
        return testService.testDiff(request);
    }
}
