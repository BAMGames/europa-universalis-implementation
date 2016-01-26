package com.mkl.eu.service.service.service.impl;

import com.mkl.eu.client.common.exception.FunctionalException;
import com.mkl.eu.client.common.exception.TechnicalException;
import com.mkl.eu.client.service.service.ITablesService;
import com.mkl.eu.client.service.vo.tables.Tables;
import org.springframework.context.ApplicationListener;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Service for chat purpose.
 *
 * @author MKL.
 */
@Service
@Transactional(rollbackFor = {TechnicalException.class, FunctionalException.class})
public class TablesServiceImpl extends AbstractService implements ITablesService, ApplicationListener<ContextRefreshedEvent> {
    /** {@inheritDoc} */
    @Override
    public Tables getTables() {
        return super.getTables();
    }

    /** {@inheritDoc} */
    @Override
    public void onApplicationEvent(ContextRefreshedEvent contextRefreshedEvent) {
        cacheTables();
    }
}
