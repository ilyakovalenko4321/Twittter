package com.IKov.User_Service.web.mapping;

import com.IKov.User_Service.entity.subscribition.Subscribition;
import com.IKov.User_Service.entity.subscribition.SubscribitionPrimaryKey;
import com.IKov.User_Service.web.dto.SubscribitionDto;
import lombok.experimental.UtilityClass;

@UtilityClass
public class SubscribitionMapper {

    public SubscribitionDto toDto(Subscribition subscribition) {
        if (subscribition == null) {
            return null;
        }

        SubscribitionDto dto = new SubscribitionDto();
        dto.setSubject(subscribition.getSubscribitionPrimaryKey().getPersonTag());
        dto.setObject(subscribition.getSubscribitionPrimaryKey().getSubscriberTag());
        return dto;
    }

    public Subscribition toEntity(SubscribitionDto dto) {
        if (dto == null) {
            return null;
        }

        Subscribition entity = new Subscribition();
        entity.setSubscribitionPrimaryKey(SubscribitionPrimaryKey.builder()
                .personTag(dto.getSubject())
                .subscriberTag(dto.getObject())
                .build());
        return entity;
    }
}

