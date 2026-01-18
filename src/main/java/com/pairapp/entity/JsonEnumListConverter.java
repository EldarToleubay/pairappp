package com.pairapp.entity;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.pairapp.enums.MoodAvoid;
import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

@Converter
public class JsonEnumListConverter implements AttributeConverter<List<MoodAvoid>, String> {
    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

    @Override
    public String convertToDatabaseColumn(List<MoodAvoid> attribute) {
        if (attribute == null) {
            return null;
        }
        try {
            return OBJECT_MAPPER.writeValueAsString(attribute);
        } catch (JsonProcessingException e) {
            throw new IllegalArgumentException("Failed to serialize avoid list", e);
        }
    }

    @Override
    public List<MoodAvoid> convertToEntityAttribute(String dbData) {
        if (dbData == null || dbData.isBlank()) {
            return new ArrayList<>();
        }
        try {
            return OBJECT_MAPPER.readValue(dbData, OBJECT_MAPPER.getTypeFactory()
                    .constructCollectionType(List.class, MoodAvoid.class));
        } catch (IOException e) {
            throw new IllegalArgumentException("Failed to deserialize avoid list", e);
        }
    }
}
