package com.IKov.User_Service.entity.subscribition;

import lombok.Builder;
import lombok.Data;
import org.springframework.data.cassandra.core.cql.PrimaryKeyType;
import org.springframework.data.cassandra.core.mapping.PrimaryKeyClass;
import org.springframework.data.cassandra.core.mapping.PrimaryKeyColumn;

@PrimaryKeyClass
@Data
@Builder
public class SubscribitionPrimaryKey {

    @PrimaryKeyColumn(name = "person_tag", ordinal = 0, type = PrimaryKeyType.PARTITIONED)
    private String personTag;
    @PrimaryKeyColumn(name = "subscriber_tag", ordinal = 1, type = PrimaryKeyType.CLUSTERED)
    private String subscriberTag;

}
