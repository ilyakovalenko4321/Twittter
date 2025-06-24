USE twitt_service;

CREATE TABLE IF NOT EXISTS person_subscriber(
    person_tag TEXT,
    subscriber_tag TEXT,
    PRIMARY KEY ( person_tag, subscriber_tag )
);

CREATE TABLE person_subscriberNumber(
    person_tag TEXT,
    subscriber_number COUNTER,
    primary key ( person_tag )
);