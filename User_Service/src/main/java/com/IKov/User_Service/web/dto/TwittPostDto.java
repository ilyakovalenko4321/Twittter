package com.IKov.User_Service.web.dto;

import lombok.Data;

import java.util.UUID;

@Data
public class TwittPostDto {

    private String userTag;
    private UUID twittId;
}
