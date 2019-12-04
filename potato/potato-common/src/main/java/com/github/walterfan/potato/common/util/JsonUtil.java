package com.github.walterfan.potato.common.util;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonParser;

import com.fasterxml.jackson.core.Version;
import com.fasterxml.jackson.databind.DeserializationContext;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.deser.std.StdScalarDeserializer;
import com.fasterxml.jackson.databind.module.SimpleModule;
import com.fasterxml.jackson.databind.ser.std.StdSerializer;
import com.fasterxml.jackson.datatype.jdk8.Jdk8Module;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.fasterxml.jackson.module.paramnames.ParameterNamesModule;
import org.joda.time.format.DateTimeFormatter;
import org.joda.time.format.ISODateTimeFormat;

import java.io.IOException;
import java.time.Instant;


/**
 * @Author: Walter Fan
 **/
public final class JsonUtil {
    public static final DateTimeFormatter isoDateTimeFormatter = ISODateTimeFormat.dateTime().withZoneUTC();
    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

    static {
        OBJECT_MAPPER.setSerializationInclusion(JsonInclude.Include.NON_EMPTY);
        OBJECT_MAPPER.disable(SerializationFeature.FAIL_ON_EMPTY_BEANS);

        OBJECT_MAPPER.registerModule(new ParameterNamesModule());
        OBJECT_MAPPER.registerModule(new Jdk8Module());
        OBJECT_MAPPER.registerModule(new JavaTimeModule()); // new module, NOT JSR310Module

        SimpleModule module = new SimpleModule("potato", new Version(1, 0, 0, null, null, null));
        module.addSerializer(new StdSerializer<Instant>(Instant.class) {
            @Override
            public void serialize(Instant date, JsonGenerator jg, SerializerProvider sp) throws IOException {
                jg.writeString(isoDateTimeFormatter.print(date.toEpochMilli()));
            }
        });

        module.addDeserializer(Instant.class, new StdScalarDeserializer<Instant>(Instant.class) {
            @Override
            public Instant deserialize(JsonParser jp, DeserializationContext dc) throws IOException {
                try {
                    if (jp.getText() == null || jp.getText().trim().isEmpty()) {
                        return null;
                    } else {
                        return Instant.parse(jp.getText());
                    }
                } catch (IllegalArgumentException e) {
                    throw dc.mappingException("Unable to parse date: " + e.getMessage());
                }
            }
        });
        OBJECT_MAPPER.registerModule(module);


    }

    private JsonUtil() {
        //no instance
    }

    public static ObjectMapper getObjectMapper() {
        return OBJECT_MAPPER;
    }

    public static String toJson(Object object) {
        try {
            return OBJECT_MAPPER.writeValueAsString(object);
        } catch (IOException e) {
            throw new IllegalArgumentException("Unable to serialize object to json", e);
        }
    }



    public static <T> T fromJson(String json, Class<T> type) {
        try {
            return OBJECT_MAPPER.readValue(json, type);
        } catch (IOException var4) {
            throw new IllegalArgumentException("Unable to de-serialize json: " + json, var4);
        }
    }



}