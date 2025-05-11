package com.IKov.TwittService.web.dto;

import lombok.Data;

@Data
public class TwittPostDto {

    private String userTag;
    private String twittHeader;
    private String twittText;
    private String[] twittTags;

}
