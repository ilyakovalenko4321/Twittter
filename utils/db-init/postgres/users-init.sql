CREATE TYPE profile_status AS ENUM ('UNCONFIRMED', 'CONFIRMED', 'BLOCKED')

CREATE TABLE profile
(
    id          BIGSERIAL PRIMARY KEY,
    name        VARCHAR(255) NOT NULL,
    email       VARCHAR(255),
    tag         VARCHAR(255),
    status      VARCHAR(50),
    password    VARCHAR(255),
    description TEXT
);

CREATE INDEX idx_profile_tag ON profile (tag);
CREATE INDEX idx_profile_name ON profile (name);

CREATE TABLE profile_twitt
(
    profile_id BIGINT NOT NULL,
    twitt_id   BIGINT,
    PRIMARY KEY (profile_id, subscriber_id),
    CONSTRAINT fk_profile_subscribers_profile
        FOREIGN KEY (profile_id)
            REFERENCES profile (id)
            ON DELETE CASCADE
);

CREATE INDEX idx_profile_twitt_profile_id ON profile_twitt (profile_id)

CREATE TABLE profile_subscribers
(
    profile_id     BIGINT NOT NULL,
    subscribers_id BIGINT,
    PRIMARY KEY (profile_id, subscribers_id),
    CONSTRAINT fk_profile_subscribers_profile
        FOREIGN KEY (profile_id)
            REFERENCES profile (id)
            ON DELETE CASCADE
);

CREATE INDEX idx_profile_subscribers_profile_id ON profile_subscribers (profile_id);

CREATE TABLE subscribition_profiles
(
    subscribition_id BIGINT NOT NULL,
    profile_id   BIGINT NOT NULL,
    PRIMARY KEY (subscribition_id, profile_id),
    CONSTRAINT fk_subscribition_profiles_profile
        FOREIGN KEY (subscribition_id)
            REFERENCES profile(subscribition_id)
            ON DELETE CASCADE
)

CREATE INDEX idx_subscribition_profiles_subscribition_id ON profile_subscribers(profile_id);