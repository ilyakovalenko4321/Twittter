package com.IKov.TwittService.entity;


import lombok.Data;
import org.springframework.data.annotation.Transient;
import org.springframework.data.cassandra.core.cql.Ordering;
import org.springframework.data.cassandra.core.cql.PrimaryKeyType;
import org.springframework.data.cassandra.core.mapping.Column;
import org.springframework.data.cassandra.core.mapping.PrimaryKeyColumn;
import org.springframework.data.cassandra.core.mapping.Table;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Data
@Table("twitts")
public class TwittPost {

    @Transient
    private String userTag;
    @PrimaryKeyColumn(name = "twitt_id", type = PrimaryKeyType.PARTITIONED)
    private UUID twittId;
    @Column("twitt_text")
    private String twittText;
    @Column("twitt_header")
    private String twittHeader;
    @Column("twitt_tags")
    private List<String> twittTags;
    @Column("created_at")
    private LocalDateTime createdAt;

}
