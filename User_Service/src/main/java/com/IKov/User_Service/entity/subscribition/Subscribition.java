package com.IKov.User_Service.entity.subscribition;

import lombok.Data;
import org.springframework.data.cassandra.core.mapping.PrimaryKey;
import org.springframework.data.cassandra.core.mapping.Table;

@Table("person_subscriber")
@Data
public class Subscribition {

    @PrimaryKey
    private SubscribitionPrimaryKey subscribitionPrimaryKey;

}
