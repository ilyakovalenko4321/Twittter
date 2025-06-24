package com.IKov.User_Service.repository;

import com.IKov.User_Service.entity.subscribition.PersonSubscribersNumber;
import org.springframework.data.cassandra.repository.CassandraRepository;
import org.springframework.data.cassandra.repository.Query;

public interface SubscribitionNumberRepository extends CassandraRepository<PersonSubscribersNumber, String> {

    @Query("""
            UPDATE person_subscriberNumber
                SET subscriber_number = subscriber_number + 1
                WHERE person_tag = ?0""")
    void increment(String personTag);

    @Query("""
            UPDATE person_subscriberNumber
                SET subscriber_number = subscriber_number - 1
                WHERE person_tag = ?0""")
    void decrement(String personTag);

}
