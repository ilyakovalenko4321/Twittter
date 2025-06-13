package com.IKov.TwittService.repository;

import com.IKov.TwittService.entity.twitt.TwittEntity;
import org.springframework.data.cassandra.repository.CassandraRepository;
import org.springframework.data.cassandra.repository.Query;

import java.util.List;
import java.util.UUID;

public interface CassandraTwittRepository extends CassandraRepository<TwittEntity, UUID> {
    @Query("SELECT * FROM twitts WHERE twitt_id IN ?0")
    List<TwittEntity> findAllByTwittIdIn(List<UUID> ids);
}
