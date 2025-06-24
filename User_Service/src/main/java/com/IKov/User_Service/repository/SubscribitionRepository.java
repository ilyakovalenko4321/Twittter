package com.IKov.User_Service.repository;

import com.IKov.User_Service.entity.subscribition.Subscribition;
import com.IKov.User_Service.entity.subscribition.SubscribitionPrimaryKey;
import org.springframework.data.cassandra.repository.CassandraRepository;
import org.springframework.data.cassandra.repository.Query;

import java.util.List;

public interface SubscribitionRepository extends CassandraRepository<Subscribition, SubscribitionPrimaryKey> {

    @Query("""
            SELECT * FROM person_subscriber
            WHERE person_tag = ?0""")
    List<Subscribition> findAllByPersonTag(String userTag);

}
