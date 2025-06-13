package com.IKov.TwittService.web.mapper;

import com.IKov.TwittService.entity.twitt.TwittEntity;
import com.IKov.TwittService.web.dto.TwittCreateRequest;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.UUID;

@Component
public class TwittDtoMapper {

    public TwittEntity toEntity(TwittCreateRequest dto) {
        TwittEntity entity = new TwittEntity();
        entity.setUserTag(dto.getUserTag());
        entity.setTwittId(UUID.randomUUID());
        entity.setTwittHeader(dto.getTwittHeader());
        entity.setTwittText(dto.getTwittText());
        entity.setTwittTags(dto.getTwittTags());
        entity.setCreatedAt(LocalDateTime.now());
        return entity;
    }

    public TwittCreateRequest toDto(TwittEntity entity) {
        TwittCreateRequest dto = new TwittCreateRequest();
        dto.setTwittText(entity.getTwittText());
        dto.setTwittTags(entity.getTwittTags());
        return dto;
    }
}