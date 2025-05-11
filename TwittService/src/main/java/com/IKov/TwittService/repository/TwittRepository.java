package com.IKov.TwittService.repository;

import com.IKov.TwittService.entity.TwittPost;
import org.springframework.data.cassandra.repository.CassandraRepository;

import java.util.UUID;

public interface TwittRepository extends CassandraRepository<TwittPost, UUID> {
}
