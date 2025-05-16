package com.IKov.TimelineService.entity;

import lombok.Data;
import org.springframework.data.annotation.Transient;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Data
public class TwittPost {

    @Transient
    private String userTag;
    private UUID twittId;
    private String twittText;
    private String twittHeader;
    private List<String> twittTags;
    private LocalDateTime createdAt;

}
