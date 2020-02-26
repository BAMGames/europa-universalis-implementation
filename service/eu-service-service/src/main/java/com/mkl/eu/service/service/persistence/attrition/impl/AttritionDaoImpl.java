package com.mkl.eu.service.service.persistence.attrition.impl;

import com.mkl.eu.service.service.persistence.attrition.IAttritionDao;
import com.mkl.eu.service.service.persistence.impl.GenericDaoImpl;
import com.mkl.eu.service.service.persistence.oe.attrition.AttritionEntity;
import org.springframework.stereotype.Repository;

/**
 * Implementation of the Attrition DAO.
 *
 * @author MKL.
 */
@Repository
public class AttritionDaoImpl extends GenericDaoImpl<AttritionEntity, Long> implements IAttritionDao {
    /**
     * Constructor.
     */
    public AttritionDaoImpl() {
        super(AttritionEntity.class);
    }
}
