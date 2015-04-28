package com.mkl.eu.service.service.persistence;

import java.io.Serializable;
import java.util.List;

/**
 * GenericDao.
 *
 * @param <T>  type
 * @param <PK> cle primaire
 * @author MKL
 */
public interface IGenericDao<T, PK extends Serializable> {

    /**
     * create an instance.
     *
     * @param newInstance instance to create.
     * @return the new instance created.
     */
    T create(T newInstance);

    /**
     * create several instances.
     *
     * @param newInstances instances to create.
     * @return the instances created.
     */
    List<T> createAll(List<T> newInstances);

    /**
     * read.
     *
     * @param id primary key of the instance to read.
     * @return instance.
     */
    T read(PK id);

    /**
     * load.
     *
     * @param id primary key of the instance to load.
     * @return instance.
     */
    T load(PK id);

    /**
     * Multiple read.
     *
     * @param ids List of primary keys.
     * @return instances read.
     */
    List<T> read(List<PK> ids);

    /**
     * Read all the instances of this table.
     *
     * @return all the instances.
     */
    List<T> readAll();

    /**
     * update an instance.
     *
     * @param instance to update.
     * @param flush    to flush the session after the update.
     * @return instance updated.
     */
    T update(T instance, boolean flush);

    /**
     * Multiple update.
     *
     * @param instances to update.
     * @return instances updated.
     */
    List<T> updateAll(List<T> instances);

    /**
     * delete an instance.
     *
     * @param instance to delete.
     */
    void delete(T instance);

    /**
     * Multiple delete.
     *
     * @param instances to delete.
     */
    void deleteAll(List<T> instances);
}
