package com.mkl.eu.service.service.socket;

import org.springframework.transaction.support.TransactionSynchronization;

/**
 * TransactionSynchronisation for after commit actions.
 *
 * @author MKL.
 */
public interface AfterCommitTransaction extends TransactionSynchronization {
    /** {@inheritDoc} */
    @Override
    void afterCommit();
}
