package com.IKov.TwittService.entity;


import lombok.Data;
import org.springframework.data.annotation.Transient;
import org.springframework.data.cassandra.core.cql.Ordering;
import org.springframework.data.cassandra.core.cql.PrimaryKeyType;
import org.springframework.data.cassandra.core.mapping.PrimaryKeyColumn;
import org.springframework.data.cassandra.core.mapping.Table;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
@Table("tweets")
public class TwittPost {

    @Transient
    private String userTag;
    @PrimaryKeyColumn(name = "twitt_id", type = PrimaryKeyType.CLUSTERED)
    private UUID twittId;
    private String twittText;
    private String twittTags;
    @PrimaryKeyColumn(name = "created_at", type = PrimaryKeyType.CLUSTERED, ordering = Ordering.DESCENDING)
    private LocalDateTime createdAt;

}
