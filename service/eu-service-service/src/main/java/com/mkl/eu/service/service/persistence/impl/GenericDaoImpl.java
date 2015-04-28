package com.mkl.eu.service.service.persistence.impl;

import com.mkl.eu.client.common.exception.IConstantsCommonException;
import com.mkl.eu.client.common.exception.TechnicalException;
import com.mkl.eu.service.service.persistence.IGenericDao;
import org.hibernate.*;
import org.hibernate.criterion.Restrictions;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * GenericDaoImpl.
 *
 * @param <T>  type
 * @param <PK> Primary key.
 * @author MKL
 */
public abstract class GenericDaoImpl<T, PK extends Serializable> implements IGenericDao<T, PK> {

    /**
     * Logger.
     */
    private static final org.slf4j.Logger LOG = LoggerFactory.getLogger(GenericDaoImpl.class);
    /** Message : update. */
    private static final String MSG_UPDATE = "Error during update :";
    /** Message : error during update. */
    private static final String MSG_ERROR_UPDATE = "An error occured during the update";
    /** Message : delete. */
    private static final String MSG_DELETE = "Error during delete :";
    /**
     * sessionFactory.
     */
    @Autowired
    private transient SessionFactory sessionFactory;
    /**
     * Type of the persisted instance.
     */
    private Class<T> type;

    /**
     * Constructor.
     */
    public GenericDaoImpl() {
        super();
    }

    /**
     * Constructor.
     *
     * @param type type
     */
    public GenericDaoImpl(Class<T> type) {
        this.type = type;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public T create(T o) {
        try {
            getSession().save(o);
        } catch (HibernateException e) {
            LOG.error("Error during create :" + e.getMessage());
            throw new TechnicalException(IConstantsCommonException.ERROR_CREATION, "Une erreur est survenue durant la creation de l'objet en base", e, o);
        }
        return o;
    }

    /**
     * {@inheritDoc}
     */
    @SuppressWarnings("unchecked")
    @Override
    public List<T> createAll(List<T> persistentObjects) {
        List<T> res = new ArrayList<>();
        for (T o : persistentObjects) {
            try {
                res.add((T) getSession().save(o));
            } catch (HibernateException e) {
                LOG.error("Error during create all :" + e.getMessage());
                throw new TechnicalException(IConstantsCommonException.ERROR_CREATION, "Une erreur est survenue durant la creation de l'objet en base", e, o);
            }
        }
        return res;
    }

    /**
     * {@inheritDoc}
     */
    @SuppressWarnings("unchecked")
    @Override
    public T read(PK id) {
        T o;
        try {
            o = (T) getSession().get(type, id);
        } catch (HibernateException e) {
            LOG.error("Error during read :" + e.getMessage());
            throw new TechnicalException(IConstantsCommonException.ERROR_READ, "Une erreur est survenue durant la lecture de l'objet en base", e, id);
        }
        return o;

    }

    /**
     * {@inheritDoc}
     */
    @SuppressWarnings("unchecked")
    @Override
    public T load(PK id) {
        T o;
        try {
            o = (T) getSession().load(type, id);
        } catch (HibernateException e) {
            LOG.error("Error during load :" + e.getMessage());
            throw new TechnicalException(IConstantsCommonException.ERROR_READ, "Une erreur est survenue durant la lecture de l'objet en base", e, id);
        }
        return o;

    }

    /**
     * {@inheritDoc}
     */
    @SuppressWarnings("unchecked")
    @Override
    public List<T> read(List<PK> ids) {
        List<T> o;

        try {
            Criteria crit = getSession().createCriteria(type);
            crit.add(Restrictions.in("id", ids));
            o = crit.list();
        } catch (HibernateException e) {
            LOG.error("read :" + e.getMessage());
            throw new TechnicalException(IConstantsCommonException.ERROR_READ, "Une erreur est survenue durant la lecture d'objets en base", e, ids);
        }
        return o;

    }

    /**
     * {@inheritDoc}
     */
    @SuppressWarnings("unchecked")
    @Override
    public List<T> readAll() {
        List<T> o;

        try {
            Criteria crit = getSession().createCriteria(type);
            o = crit.list();
        } catch (HibernateException e) {
            LOG.error("readAll :" + e.getMessage());
            throw new TechnicalException(IConstantsCommonException.ERROR_READ, "Une erreur est survenue durant la lecture d'objets en base", e);
        }
        return o;

    }

    /**
     * {@inheritDoc}
     */
    @SuppressWarnings("unchecked")
    @Override
    public T update(T o, boolean flush) {
        T res;
        try {
            res = (T) getSession().merge(o);
            if (flush) {
                getSession().flush();
            }
        } catch (StaleObjectStateException e) {
            if (e.getMessage() != null && e.getMessage().startsWith("Row was updated or deleted by another transaction")) {
                LOG.warn("update: Mise à jour impossible en raison d'une modification concurrente");
                throw new TechnicalException(IConstantsCommonException.CONCURRENT_MODIFICATION, "Mise à jour impossible en raison d'une modification concurrente", e, o);
            } else {
                LOG.error(MSG_UPDATE + e.getMessage());
                throw new TechnicalException(IConstantsCommonException.ERROR_UPDATE, MSG_ERROR_UPDATE, e, o);
            }
        } catch (HibernateException e) {
            LOG.error(MSG_UPDATE + e.getMessage());
            throw new TechnicalException(IConstantsCommonException.ERROR_UPDATE, MSG_ERROR_UPDATE, e, o);
        }
        return res;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<T> updateAll(List<T> persistentObjects) {
        for (T o : persistentObjects) {
            try {
                getSession().merge(o);
            } catch (StaleObjectStateException e) {
                if (e.getMessage() != null && e.getMessage().startsWith("Row was updated or deleted by another transaction")) {
                    LOG.warn("update: Mise à jour impossible en raison d'une modification concurrente");
                    throw new TechnicalException(IConstantsCommonException.CONCURRENT_MODIFICATION, "Mise à jour impossible en raison d'une modification concurrente", e, o);
                } else {
                    LOG.error(MSG_UPDATE + e.getMessage());
                    throw new TechnicalException(IConstantsCommonException.ERROR_UPDATE, MSG_ERROR_UPDATE, e, o);
                }
            } catch (HibernateException e) {
                LOG.error(MSG_UPDATE + e.getMessage());
                throw new TechnicalException(IConstantsCommonException.ERROR_UPDATE, MSG_ERROR_UPDATE, e, o);
            }
        }

        return persistentObjects;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void delete(T o) {
        try {
            getSession().delete(o);
        } catch (HibernateException e) {
            LOG.error(MSG_DELETE + e.getMessage());
            throw new TechnicalException(IConstantsCommonException.ERROR_DELETE, "Une erreur est survenue durant la suppression", e, o);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void deleteAll(List<T> entities) {
        for (T o : entities) {
            try {
                getSession().delete(o);
            } catch (HibernateException e) {
                LOG.error(MSG_DELETE + e.getMessage());
                throw new TechnicalException(IConstantsCommonException.ERROR_DELETE, "Une erreur est survenue durant la suppression", e, o);
            }
        }
    }

    /**
     * Retourne une liste castée à partir du {@link Criteria} en paramètre.
     *
     * @param criteria le Criteria à lister
     * @return la liste typée du criteria
     */
    @SuppressWarnings("unchecked")
    protected List<T> listAndCast(Criteria criteria) {
        return criteria.list();
    }

    /**
     * Retourne une liste castée à partir de la {@link Query} en paramètre.
     *
     * @param query la Query à lister
     * @return la liste typée de la query
     */
    @SuppressWarnings("unchecked")
    protected List<T> listAndCast(Query query) {
        return query.list();
    }

    /** {@inheritDoc} */
    @Override
    public void flush() {
        getSession().flush();
    }

    /**
     * Retourne la session JPA.
     *
     * @return la session JPA.
     */
    public Session getSession() {
        return sessionFactory.getCurrentSession();
    }

    /**
     * Retourne le type d'entités associé au DAO.
     *
     * @return le type d'entités associé au DAO.
     */
    public Class<T> getType() {
        return type;
    }

    /**
     * Spécifie le type d'entités associé au DAO.
     *
     * @param type le type d'entités associé au DAO.
     */
    public void setType(Class<T> type) {
        this.type = type;
    }
}
