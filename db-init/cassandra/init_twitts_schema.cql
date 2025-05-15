CREATE KEYSPACE twitt_service
    WITH replication = {'class': 'SimpleStrategy', 'replication_factor': '1'};

USE twitt_service;

CREATE TABLE twitts (
                        created_at TIMESTAMP,
                        twitt_id UUID,
                        twitt_text TEXT,
                        twitt_tags LIST<TEXT>,
                        PRIMARY KEY (twitt_id)
);