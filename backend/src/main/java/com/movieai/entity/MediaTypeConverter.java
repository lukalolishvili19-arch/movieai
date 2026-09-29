package com.movieai.entity;

import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

import com.movieai.dto.MediaType;

@Converter
public class MediaTypeConverter implements AttributeConverter<MediaType, String> {

    @Override
    public String convertToDatabaseColumn(MediaType attribute) {
        return attribute == null ? null : attribute.value();
    }

    @Override
    public MediaType convertToEntityAttribute(String dbData) {
        return dbData == null ? null : MediaType.fromValue(dbData);
    }
}
