package com.mkl.eu.service.service.persistence.chat.impl;

import com.mkl.eu.client.common.exception.IConstantsCommonException;
import com.mkl.eu.client.common.exception.TechnicalException;
import com.mkl.eu.service.service.persistence.chat.IChatDao;
import com.mkl.eu.service.service.persistence.impl.GenericDaoImpl;
import com.mkl.eu.service.service.persistence.oe.chat.ChatEntity;
import com.mkl.eu.service.service.persistence.oe.chat.MessageGlobalEntity;
import com.mkl.eu.service.service.persistence.oe.chat.RoomEntity;
import com.mkl.eu.service.service.persistence.oe.chat.RoomGlobalEntity;
import org.hibernate.Criteria;
import org.hibernate.HibernateException;
import org.hibernate.criterion.Projections;
import org.hibernate.criterion.Restrictions;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Implementation of the Chat DAO.
 *
 * @author MKL.
 */
@Repository
public class ChatDaoImpl extends GenericDaoImpl<ChatEntity, Long> implements IChatDao {
    /**
     * Constructor.
     */
    public ChatDaoImpl() {
        super(ChatEntity.class);
    }

    /** {@inheritDoc} */
    @Override
    public List<ChatEntity> getMessages(Long idGame, Long idCountry) {
        Criteria criteria = getSession().createCriteria(ChatEntity.class);

        criteria.add(Restrictions.eq("receiver.id", idCountry));

        Criteria criteriaRoom = criteria.createCriteria("room", "room");

        criteriaRoom.add(Restrictions.eq("game.id", idGame));

        //noinspection unchecked
        return criteria.list();
    }

    /** {@inheritDoc} */
    @Override
    public List<MessageGlobalEntity> getGlobalMessages(Long idGame) {
        Criteria criteria = getSession().createCriteria(MessageGlobalEntity.class);

        Criteria criteriaRoom = criteria.createCriteria("room", "room");

        criteriaRoom.add(Restrictions.eq("game.id", idGame));

        //noinspection unchecked
        return criteria.list();
    }

    /** {@inheritDoc} */
    @Override
    public List<RoomEntity> getRooms(Long idGame, Long idCountry) {
        Criteria criteria = getSession().createCriteria(RoomEntity.class);

        criteria.createAlias("presents", "presents");

        criteria.add(Restrictions.eq("game.id", idGame));
        criteria.add(Restrictions.eq("presents.country.id", idCountry));

        //noinspection unchecked
        return criteria.list();
    }

    /** {@inheritDoc} */
    @Override
    public long getUnreadMessagesNumber(Long idGame, Long idCountry) {
        Criteria criteria = getSession().createCriteria(ChatEntity.class);

        criteria.add(Restrictions.eq("receiver.id", idCountry));
        criteria.add(Restrictions.isNull("dateRead"));

        Criteria criteriaRoom = criteria.createCriteria("room", "room");

        criteriaRoom.add(Restrictions.eq("game.id", idGame));

        criteria.setProjection(Projections.rowCount());

        //noinspection unchecked
        return (long) criteria.uniqueResult();
    }

    /** {@inheritDoc} */
    @Override
    public RoomGlobalEntity getRoomGlobal(Long idGame) {
        Criteria criteria = getSession().createCriteria(RoomGlobalEntity.class);

        criteria.add(Restrictions.eq("game.id", idGame));

        return (RoomGlobalEntity) criteria.uniqueResult();
    }

    /** {@inheritDoc} */
    @Override
    public RoomEntity getRoom(Long idGame, Long idRoom) {
        Criteria criteria = getSession().createCriteria(RoomEntity.class);

        criteria.add(Restrictions.eq("id", idRoom));
        criteria.add(Restrictions.eq("game.id", idGame));

        return (RoomEntity) criteria.uniqueResult();
    }

    /** {@inheritDoc} */
    @Override
    public void createMessage(MessageGlobalEntity message) {
        try {
            getSession().persist(message);
        } catch (HibernateException e) {
            LOG.error("Error during create :" + e.getMessage());
            throw new TechnicalException(IConstantsCommonException.ERROR_CREATION, "An error occurred during the insertion in database", e, message.getId());
        }
    }

    /** {@inheritDoc} */
    @Override
    public void createMessage(List<ChatEntity> messages) {
        for (ChatEntity o : messages) {
            try {
                getSession().save(o);
            } catch (HibernateException e) {
                LOG.error("Error during create all :" + e.getMessage());
                throw new TechnicalException(IConstantsCommonException.ERROR_CREATION, "An error occurred during the insertion in database", e, o.getId());
            }
        }
    }

    /** {@inheritDoc} */
    @Override
    public List<ChatEntity> getMessagesSince(Long idGame, Long idCountry, Long lastId) {
        Criteria criteria = getSession().createCriteria(ChatEntity.class);

        criteria.createAlias("room", "room");

        criteria.add(Restrictions.eq("room.game.id", idGame));
        criteria.add(Restrictions.eq("receiver.id", idCountry));
        criteria.add(Restrictions.gt("id", lastId));

        //noinspection unchecked
        return criteria.list();
    }

    /** {@inheritDoc} */
    @Override
    public List<MessageGlobalEntity> getMessagesGlobalSince(Long idGame, Long lastId) {
        Criteria criteria = getSession().createCriteria(MessageGlobalEntity.class);

        criteria.createAlias("room", "room");

        criteria.add(Restrictions.eq("room.game.id", idGame));
        criteria.add(Restrictions.gt("id", lastId));

        //noinspection unchecked
        return criteria.list();
    }
}
