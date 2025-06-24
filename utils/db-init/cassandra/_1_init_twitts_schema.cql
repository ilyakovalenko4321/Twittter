CREATE KEYSPACE IF NOT EXISTS twitt_service
    WITH replication = {'class': 'SimpleStrategy', 'replication_factor': '1'};

USE twitt_service;

CREATE TABLE IF NOT EXISTS twitts (
                        user_tag TEXT,
                        created_at TIMESTAMP,
                        twitt_header TEXT,
                        twitt_id UUID,
                        twitt_text TEXT,
                        twitt_tags LIST<TEXT>,
                        PRIMARY KEY (twitt_id)
);