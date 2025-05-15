package com.IKov.TwittService.web.dto;

import lombok.Data;

import java.util.List;

@Data
public class TwittPostDto {

    private String userTag;
    private String twittHeader;
    private String twittText;
    private List<String> twittTags;

}
