CREATE TABLE profile (
                         id BIGSERIAL PRIMARY KEY,
                         name VARCHAR(255) NOT NULL,
                         email VARCHAR(255),
                         tag VARCHAR(255),
                         status VARCHAR(50), -- соответствует EnumType.STRING
                         password VARCHAR(255),
                         description TEXT
);

CREATE TABLE profile_twitt (
                               profile_id BIGINT NOT NULL,
                               twitt_id BIGINT,
                               CONSTRAINT fk_profile_twitt FOREIGN KEY (profile_id) REFERENCES profile(id)
);