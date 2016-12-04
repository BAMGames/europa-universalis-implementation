package com.mkl.eu.service.service.ws.impl;

import com.mkl.eu.client.service.service.ITablesService;
import com.mkl.eu.client.service.vo.ref.Referential;
import com.mkl.eu.client.service.vo.tables.Tables;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.web.context.support.SpringBeanAutowiringSupport;

import javax.jws.WebService;

/**
 * Separation from TableService because cxf can't handle @Transactional.
 *
 * @author MKL.
 */
@WebService(endpointInterface = "com.mkl.eu.client.service.service.ITablesService")
public class TablesWsServiceImpl extends SpringBeanAutowiringSupport implements ITablesService {
    /** Table Service. */
    @Autowired
    @Qualifier(value = "tablesServiceImpl")
    private ITablesService tableService;

    /** {@inheritDoc} */
    @Override
    public Tables getTables() {
        return tableService.getTables();
    }

    /** {@inheritDoc} */
    @Override
    public Referential getReferential() {
        return tableService.getReferential();
    }

    /** {@inheritDoc} */
    @Override
    public void refresh() {
        tableService.refresh();
    }
}
