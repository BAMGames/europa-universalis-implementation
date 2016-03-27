package com.mkl.eu.service.service.mapping.chat;

import com.mkl.eu.client.common.util.CommonUtil;
import com.mkl.eu.client.service.vo.chat.Chat;
import com.mkl.eu.client.service.vo.chat.Message;
import com.mkl.eu.client.service.vo.chat.MessageDiff;
import com.mkl.eu.client.service.vo.chat.Room;
import com.mkl.eu.client.service.vo.country.PlayableCountry;
import com.mkl.eu.service.service.mapping.AbstractMapping;
import com.mkl.eu.service.service.mapping.country.PlayableCountryMapping;
import com.mkl.eu.service.service.persistence.oe.chat.*;
import com.mkl.eu.service.service.persistence.oe.country.PlayableCountryEntity;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Description of file.
 *
 * @author MKL.
 */
@Component
public class ChatMapping extends AbstractMapping {
    /** Mapping for a country. */
    @Autowired
    private PlayableCountryMapping playableCountryMapping;

    /**
     * Creates a Chat from the messages in the global room and the other messages.
     *
     * @param globalMessages messages in the global room.
     * @param rooms          rooms where the country is.
     * @param messages       messages in other rooms.
     * @param idCountry      id of the country retrieving his messages.
     * @return a Chat.
     */
    public Chat getChat(List<MessageGlobalEntity> globalMessages, List<RoomEntity> rooms, List<ChatEntity> messages, Long idCountry) {
        Chat chat = new Chat();

        Map<Class<?>, Map<Long, Object>> objectsCreated = new HashMap<>();

        if (globalMessages != null) {
            chat.setGlobalMessages(oesToVosMessage(globalMessages, objectsCreated));
        }

        if (messages != null && idCountry != null) {
            for (RoomEntity roomEntity : rooms) {
                Room room = oeToVo(roomEntity, objectsCreated, idCountry);
                chat.getRooms().add(room);
            }

            Map<RoomEntity, List<ChatEntity>> messagesByRoom = messages.stream().collect(Collectors.groupingBy(ChatEntity::getRoom));

            for (RoomEntity roomEntity : messagesByRoom.keySet()) {
                Room room = CommonUtil.findFirst(chat.getRooms(), r -> r.getId().equals(roomEntity.getId()));
                if (room == null) {
                    room = oeToVo(roomEntity, objectsCreated, idCountry);
                    chat.getRooms().add(room);
                }
                room.setMessages(oesToVosChat(messagesByRoom.get(roomEntity), objectsCreated));
            }
        }

        return chat;
    }

    /**
     * OEs to VOs.
     *
     * @param sources        object source.
     * @param objectsCreated Objects created by the mappings (sort of caching).
     * @return object mapped.
     */
    public List<Message> oesToVosChat(List<ChatEntity> sources, final Map<Class<?>, Map<Long, Object>> objectsCreated) {
        if (sources == null) {
            return null;
        }

        List<Message> targets = new ArrayList<>();

        for (ChatEntity source : sources) {
            Message target = storeVo(Message.class, source.getMessage(), objectsCreated, this::oeToVo);
            if (target != null) {
                target.setDateRead(source.getDateRead());
                targets.add(target);
            }
        }

        return targets;
    }

    /**
     * OEs to VOs.
     *
     * @param sources        object source.
     * @param objectsCreated Objects created by the mappings (sort of caching).
     * @return object mapped.
     */
    public List<Message> oesToVosMessage(List<? extends AbstractMessageEntity> sources, final Map<Class<?>, Map<Long, Object>> objectsCreated) {
        if (sources == null) {
            return null;
        }

        List<Message> targets = new ArrayList<>();

        for (AbstractMessageEntity source : sources) {
            Message target = storeVo(InnerMessage.class, source, objectsCreated, this::oeToVo);
            if (target != null) {
                targets.add(target);
            }
        }

        return targets;
    }

    /**
     * OE to VO.
     *
     * @param source         object source.
     * @param objectsCreated Objects created by the mappings (sort of caching).
     * @return object mapped.
     */
    public Message oeToVo(AbstractMessageEntity source, final Map<Class<?>, Map<Long, Object>> objectsCreated) {
        if (source == null) {
            return null;
        }

        Message target = new Message();

        target.setId(source.getId());
        target.setDateSent(source.getDateSent());
        target.setMessage(source.getMessage());
        target.setSender(storeVo(PlayableCountry.class, source.getSender(), objectsCreated, playableCountryMapping::oeToVo));

        return target;
    }

    /**
     * OE to VO.
     *
     * @param source         object source.
     * @param objectsCreated Objects created by the mappings (sort of caching).
     * @return object mapped.
     */
    public Room oeToVo(RoomEntity source, final Map<Class<?>, Map<Long, Object>> objectsCreated, Long idCountry) {
        if (source == null) {
            return null;
        }

        Room target = new Room();

        target.setId(source.getId());
        target.setName(source.getName());
        List<PlayableCountryEntity> countries = source.getPresents().stream().filter(PresentEntity::isPresent).map(PresentEntity::getCountry).collect(Collectors.toList());
        target.setCountries(playableCountryMapping.oesToVos(countries, objectsCreated));
        target.setOwner(storeVo(PlayableCountry.class, source.getOwner(), objectsCreated, playableCountryMapping::oeToVo));
        PresentEntity present = CommonUtil.findFirst(source.getPresents(), presentEntity -> presentEntity.getCountry().getId().equals(idCountry));
        if (present != null) {
            target.setVisible(present.isVisible());
            target.setPresent(present.isPresent());
        }

        return target;
    }

    /**
     * OEs to VOs.
     *
     * @param sources        object source.
     * @param objectsCreated Objects created by the mappings (sort of caching).
     * @return object mapped.
     */
    public List<MessageDiff> oesToVosChatSince(List<ChatEntity> sources, final Map<Class<?>, Map<Long, Object>> objectsCreated) {
        if (sources == null) {
            return null;
        }

        List<MessageDiff> targets = new ArrayList<>();

        for (ChatEntity source : sources) {
            MessageDiff target = storeVo(InnerMessageDiff.class, source.getMessage(), objectsCreated, this::oeToVoSince);
            if (target != null) {
                target.setDateRead(source.getDateRead());
                target.setIdRoom(source.getRoom().getId());
                targets.add(target);
            }
        }

        return targets;
    }

    /**
     * OEs to VOs.
     *
     * @param sources        object source.
     * @param objectsCreated Objects created by the mappings (sort of caching).
     * @return object mapped.
     */
    public List<MessageDiff> oesToVosMessageSince(List<? extends AbstractMessageEntity> sources, final Map<Class<?>, Map<Long, Object>> objectsCreated) {
        if (sources == null) {
            return null;
        }

        List<MessageDiff> targets = new ArrayList<>();

        for (AbstractMessageEntity source : sources) {
            MessageDiff target = storeVo(InnerMessageDiff.class, source, objectsCreated, this::oeToVoSince);
            if (target != null) {
                targets.add(target);
            }
        }

        return targets;
    }

    /**
     * OE to VO.
     *
     * @param source object source.
     * @return object mapped.
     */
    public MessageDiff oeToVoSince(AbstractMessageEntity source) {
        if (source == null) {
            return null;
        }

        MessageDiff target = new MessageDiff();

        target.setId(source.getId());
        target.setDateSent(source.getDateSent());
        target.setMessage(source.getMessage());
        target.setIdSender(source.getSender().getId());

        return target;
    }

    /**
     * Class extending Message but different of message to avoid collision between normal messages and global messages.
     */
    private static class InnerMessage extends Message {

    }

    /**
     * Class extending Message but different of message to avoid collision between normal messages and global messages.
     */
    private static class InnerMessageDiff extends MessageDiff {

    }
}
