package ru.gkpromtech.exhibition.utils;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;

import java.io.IOException;
import java.util.Date;

public class JsonDateDeserializer extends JsonDeserializer<Date> {
    @Override
    public Date deserialize(JsonParser jp, DeserializationContext ctx) throws IOException {
        // "2015-02-27T10:22:38.000Z"
        try {
            return new Date(Long.parseLong(jp.getText()) * 1000);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }
}