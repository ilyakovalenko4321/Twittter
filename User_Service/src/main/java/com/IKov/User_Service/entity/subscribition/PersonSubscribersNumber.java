package com.IKov.User_Service.entity.subscribition;

import lombok.Data;
import org.springframework.data.cassandra.core.mapping.PrimaryKey;
import org.springframework.data.cassandra.core.mapping.Table;

@Table("person_subscriberNumber")
@Data
public class PersonSubscribersNumber {

    @PrimaryKey
    private String personTag;
    private Long subscriberNumber;

}
