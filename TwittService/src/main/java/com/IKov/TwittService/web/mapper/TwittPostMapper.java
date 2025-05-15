package com.IKov.TwittService.web.mapper;

import com.IKov.TwittService.entity.TwittPost;
import com.IKov.TwittService.web.dto.TwittPostDto;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.UUID;
import java.util.stream.Collectors;

@Component
public class TwittPostMapper {

    public TwittPost toEntity(TwittPostDto dto) {
        TwittPost entity = new TwittPost();
        entity.setUserTag(dto.getUserTag());
        entity.setTwittId(UUID.randomUUID());
        entity.setTwittText(dto.getTwittText());
        entity.setTwittTags(dto.getTwittTags());
        entity.setCreatedAt(LocalDateTime.now());
        return entity;
    }

    public TwittPostDto toDto(TwittPost entity) {
        TwittPostDto dto = new TwittPostDto();
        dto.setTwittText(entity.getTwittText());
        dto.setTwittTags(entity.getTwittTags());
        return dto;
    }
}