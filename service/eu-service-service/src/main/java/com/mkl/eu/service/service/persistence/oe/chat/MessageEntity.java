package com.mkl.eu.service.service.persistence.oe.chat;

import javax.persistence.Entity;
import javax.persistence.Table;

/**
 * Message sent in a classic room (not global).
 *
 * @author MKL.
 */
@Entity
@Table(name = "C_MESSAGE")
public class MessageEntity extends AbstractMessageEntity {
}
