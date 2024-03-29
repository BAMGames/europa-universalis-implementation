package com.mkl.eu.service.service.persistence.chat;

import com.mkl.eu.service.service.persistence.IGenericDao;
import com.mkl.eu.service.service.persistence.oe.chat.*;

import java.util.List;

/**
 * Interface of the Chat DAO.
 *
 * @author MKL.
 */
public interface IChatDao extends IGenericDao<RoomEntity, Long> {
    /**
     * Retrieve the messages that can be seen by the country.
     *
     * @param idCountry id of the country.
     * @return the messages.
     */
    List<ChatEntity> getMessages(Long idCountry);

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

    /**
     * Returns the number of unread message of the country.
     * The global messages are not concerned by this method.
     *
     * @param idCountry id of the country.
     * @return the number of unread message of the country.
     */
    long getUnreadMessagesNumber(Long idCountry);

    /**
     * Returns the global room of a game.
     *
     * @param idGame id of the game.
     * @return the global room of a game.
     */
    RoomGlobalEntity getRoomGlobal(Long idGame);

    /**
     * Returns the room of a game by its id.
     *
     * @param idGame id of the game.
     * @param idRoom id of the room.
     * @return the room of a game by its id.
     */
    RoomEntity getRoom(Long idGame, Long idRoom);

    /**
     * Returns the room of a game by its name.
     *
     * @param idGame id of the game.
     * @param name   name of the room.
     * @return the room of a game by its name.
     */
    RoomEntity getRoom(Long idGame, String name);

    /**
     * Inserts a message global.
     *
     * @param message message to insert.
     */
    void createMessage(MessageGlobalEntity message);

    /**
     * Inserts a list of messages.
     *
     * @param messages messages to insert.
     */
    void createMessage(List<ChatEntity> messages);

    /**
     * Retrieves all the non global messages for a given game and a given country whose ID are greater than lastId.
     *
     * @param idGame    id of the game.
     * @param idCountry id of the country.
     * @param lastId    id of the last message received, <code>null</code> to receive all messages.
     * @return all the non global messages for a given game and a given country whose ID are greater than lastId.
     */
    List<ChatEntity> getMessagesSince(Long idGame, Long idCountry, Long lastId);


    /**
     * Retrieves all the global messages for a given game whose ID are greater than lastId.
     *
     * @param idGame id of the game.
     * @param lastId id of the last message received, <code>null</code> to receive all messages.
     * @return all the global messages for a given game whose ID are greater than lastId.
     */
    List<MessageGlobalEntity> getMessagesGlobalSince(Long idGame, Long lastId);

    /**
     * Inserts a presentEntity (invite in a room).
     *
     * @param present present to insert.
     */
    void createPresent(PresentEntity present);

    /**
     * Read the messages in the room.
     *
     * @param idRoom    id of the room.
     * @param idCountry id of the player to mark messages.
     * @param maxId     maximum of messages id to mark as read.
     * @return the number of messages read.
     */
    int readMessagesInRoom(Long idRoom, Long idCountry, Long maxId);
}
