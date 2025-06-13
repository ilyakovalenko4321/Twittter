package com.IKov.TwittService.web.dto;

import jakarta.validation.constraints.Size;
import lombok.Data;

import java.util.List;

@Data
public class TwittCreateRequest {

    private String userTag;
    @Size(max = 60, message = "Header too long")
    private String twittHeader;
    @Size(max = 1800, message = "Text too long")
    private String twittText;
    @Size(max = 12, message = "Too many tags")
    private List<String> twittTags;

}
