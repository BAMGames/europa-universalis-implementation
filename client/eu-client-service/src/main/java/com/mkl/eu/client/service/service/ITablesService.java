package com.mkl.eu.client.service.service;

import com.mkl.eu.client.service.vo.ref.Referential;
import com.mkl.eu.client.service.vo.tables.Tables;

import javax.jws.WebResult;
import javax.jws.WebService;

/**
 * Interface for the table service.
 *
 * @author MKL.
 */
@WebService
public interface ITablesService extends INameConstants {
    /**
     * Retrieve the tables described in the appendix.
     *
     * @return the tables.
     */
    @WebResult(name = RESPONSE)
    Tables getTables();

    /**
     * Retrieve the referential described in the appendix.
     *
     * @return the referential.
     */
    @WebResult(name = RESPONSE)
    Referential getReferential();

    /**
     * Refresh the tables and the referentials.
     */
    void refresh();
}
