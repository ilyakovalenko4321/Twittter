CREATE KEYSPACE tweet_service
    WITH replication = {'class': 'SimpleStrategy', 'replication_factor': '1'};

USE tweet_service;

CREATE TABLE tweets (
                        user_id UUID,
                        created_at TIMESTAMP,
                        tweet_id UUID,
                        text TEXT,
                        media_urls LIST<TEXT>,
                        metadata MAP<TEXT, TEXT>,
                        PRIMARY KEY ((user_id), created_at)
) WITH default_time_to_live = 604800; -- 7 дней
