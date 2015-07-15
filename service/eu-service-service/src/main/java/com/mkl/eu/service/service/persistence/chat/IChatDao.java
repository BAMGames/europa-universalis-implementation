package com.mkl.eu.service.service.persistence.chat;

import com.mkl.eu.service.service.persistence.IGenericDao;
import com.mkl.eu.service.service.persistence.oe.chat.ChatEntity;
import com.mkl.eu.service.service.persistence.oe.chat.MessageGlobalEntity;
import com.mkl.eu.service.service.persistence.oe.chat.RoomEntity;

import java.util.List;

/**
 * Interface of the Room DAO.
 *
 * @author MKL.
 */
public interface IChatDao extends IGenericDao<ChatEntity, Long> {
    /**
     * Retrieve the messages that can be seen by the country for a game.
     *
     * @param idGame    id of the game.
     * @param idCountry id of the country.
     * @return the messages.
     */
    List<ChatEntity> getMessages(Long idGame, Long idCountry);

    /**
     * Retrieve the global messages for a game.
     *
     * @param idGame id of the game.
     * @return the messages.
     */
    List<MessageGlobalEntity> getGlobalMessages(Long idGame);

    /**
     * Retrieve the rooms of the country.
     *
     * @param idGame    id of the game.
     * @param idCountry id of the country.
     * @return the list of rooms seen by the country.
     */
    List<RoomEntity> getRooms(Long idGame, Long idCountry);
}
