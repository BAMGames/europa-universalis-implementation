@XmlJavaTypeAdapters({
        @XmlJavaTypeAdapter(type = ZonedDateTime.class,
                value = JaxbZonedDateTimeAdapter.class)
}) package com.mkl.eu.client.service.vo.chat;

import com.mkl.eu.client.common.adapter.JaxbZonedDateTimeAdapter;

import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapters;
import java.time.ZonedDateTime;