package com.IKov.User_Service.web.mapping;

import com.IKov.User_Service.entity.Profile.Profile;
import com.IKov.User_Service.entity.Profile.Status;
import com.IKov.User_Service.web.dto.ProfileDto;
import org.springframework.stereotype.Component;

/**
 * Mapper для преобразования между ProfileDto и Profile.
 */
@Component
public class ProfileMapping {

    /**
     * Переводит сущность Profile в DTO (для отправки клиенту).
     * Поля password и passwordConfirmation не попадают в DTO при отправке.
     */
    public ProfileDto toDto(Profile entity) {
        if (entity == null) {
            return null;
        }
        ProfileDto dto = new ProfileDto();
        dto.setName(entity.getName());
        dto.setEmail(entity.getEmail());
        dto.setTag(entity.getTag());
        dto.setDescription(entity.getDescription());
        return dto;
    }

    /**
     * Переводит DTO ProfileDto в сущность Profile (приёма от клиента).
     * Поле passwordConfirmation используется только для валидации и не копируется в сущность.
     */
    public Profile toEntity(ProfileDto dto) {
        if (dto == null) {
            return null;
        }
        Profile entity = new Profile();
        entity.setName(dto.getName());
        entity.setEmail(dto.getEmail());
        entity.setTag(dto.getTag());
        entity.setPassword(dto.getPassword());
        entity.setDescription(dto.getDescription());
        entity.setStatus(Status.UNCONFIRMED);
        return entity;
    }
}
