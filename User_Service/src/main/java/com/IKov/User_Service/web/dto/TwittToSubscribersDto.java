package com.IKov.User_Service.web.dto;

import lombok.Builder;
import lombok.Data;

import java.util.List;
import java.util.UUID;

@Data
@Builder
public class TwittToSubscribersDto {
    private UUID twittUUID;
    private List<String> subscribersList;
}
