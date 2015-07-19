package com.mkl.eu.service.service.persistence.chat.impl;

import com.mkl.eu.service.service.persistence.chat.IChatDao;
import com.mkl.eu.service.service.persistence.impl.GenericDaoImpl;
import com.mkl.eu.service.service.persistence.oe.chat.ChatEntity;
import com.mkl.eu.service.service.persistence.oe.chat.MessageGlobalEntity;
import com.mkl.eu.service.service.persistence.oe.chat.RoomEntity;
import org.hibernate.Criteria;
import org.hibernate.criterion.Projections;
import org.hibernate.criterion.Restrictions;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Implementation of the Room DAO.
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
}
