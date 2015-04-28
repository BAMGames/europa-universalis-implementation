package com.mkl.eu.service.service.persistence.board.impl;

import com.mkl.eu.service.service.persistence.board.IStackDao;
import com.mkl.eu.service.service.persistence.impl.GenericDaoImpl;
import com.mkl.eu.service.service.persistence.oe.board.StackEntity;
import org.springframework.stereotype.Repository;

/**
 * Implementation of the Stack DAO.
 *
 * @author MKL.
 */
@Repository
public class StackDaoImpl extends GenericDaoImpl<StackEntity, Long> implements IStackDao {
    /**
     * Constructor.
     */
    public StackDaoImpl() {
        super(StackEntity.class);
    }
}
