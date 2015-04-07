package com.mkl.eu.service.service.mapping;

import com.mkl.eu.client.service.vo.EuObject;
import com.mkl.eu.service.service.persistence.oe.IEntity;

import java.util.HashMap;
import java.util.Map;

/**
 * Abstract class for mapping purpose.
 *
 * @author MKL.
 */
public abstract class AbstractMapping {

    /**
     * If the VO was already created, retrieve it. If not, create it then store it.
     *
     * @param classKey       class where to store the VO.
     * @param source         entity that has to be mapped to a VO.
     * @param objectsCreated Map of already created VOs.
     * @param function       Function to call if the VO was not already created.
     * @param <T>            Type of the VO.
     * @param <U>            Type of the Entity.
     * @param <V>            Class of the VO.
     * @return the VO mapped from the source.
     */
    @SuppressWarnings("unchecked")
    protected <T extends EuObject, U extends IEntity, V extends Class<T>> T storeVo(V classKey, U source, Map<Class<?>, Map<Long, Object>> objectsCreated, ITransformation<U, T> function) {
        T target;
        if (source != null && objectsCreated.get(classKey) != null && objectsCreated.get(classKey).containsKey(source.getId())) {
            target = (T) objectsCreated.get(classKey).get(source.getId());
        } else {
            target = function.transform(source);
            if (target != null && source != null) {
                if (!objectsCreated.containsKey(classKey)) {
                    objectsCreated.put(classKey, new HashMap<Long, Object>());
                }
                objectsCreated.get(classKey).put(source.getId(), target);
            }
        }
        return target;
    }

    /**
     * Transform a class to another.
     *
     * @param <T> Type of the source class.
     * @param <U> Type of the target class.
     */
    public interface ITransformation<T, U> {
        U transform(T source);
    }
}
