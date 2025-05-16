package com.IKov.TwittService.repository;

import com.IKov.TwittService.entity.TwittPost;
import org.springframework.data.cassandra.repository.CassandraRepository;
import org.springframework.data.cassandra.repository.Query;

import java.util.List;
import java.util.UUID;

public interface TwittRepository extends CassandraRepository<TwittPost, UUID> {
    @Query("SELECT * FROM twitts WHERE twitt_id IN ?0")
    List<TwittPost> findAllByTwittIdIn(List<UUID> ids);
}
