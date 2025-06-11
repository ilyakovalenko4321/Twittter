-- Создание типа ENUM для статуса профиля
CREATE TYPE profile_status AS ENUM ('UNCONFIRMED', 'CONFIRMED', 'BLOCKED');

-- Таблица profile
CREATE TABLE profile
(
    id          BIGSERIAL PRIMARY KEY,          -- Соответствует Long id в Java
    name        VARCHAR(255) NOT NULL,
    email       VARCHAR(255),
    tag         VARCHAR(255),
    status      VARCHAR(255),                 -- Используем ENUM вместо VARCHAR(50)
    password    VARCHAR(255),
    description TEXT
);

-- Индексы для таблицы profile
CREATE INDEX idx_profile_tag ON profile (tag);
CREATE INDEX idx_profile_name ON profile (name);

-- Таблица profile_twitt (список твитов профиля)
CREATE TABLE profile_twitt
(
    profile_id BIGINT NOT NULL,                 -- Ссылка на profile(id)
    twitt_id   BIGINT NOT NULL,                 -- Соответствует List<Long> в Java
    PRIMARY KEY (profile_id, twitt_id),
    CONSTRAINT fk_profile_twitt_profile
        FOREIGN KEY (profile_id)
            REFERENCES profile (id)
            ON DELETE CASCADE
);

-- Индекс для таблицы profile_twitt
CREATE INDEX idx_profile_twitt_profile_id ON profile_twitt (profile_id);

-- Таблица profile_subscribers (список подписчиков профиля)
CREATE TABLE profile_subscribers
(
    profile_id    BIGINT NOT NULL,              -- Ссылка на profile(id)
    subscriber_id VARCHAR(255) NOT NULL,        -- Соответствует List<String> в Java
    PRIMARY KEY (profile_id, subscriber_id),
    CONSTRAINT fk_profile_subscribers_profile
        FOREIGN KEY (profile_id)
            REFERENCES profile (id)
            ON DELETE CASCADE
);

-- Индекс для таблицы profile_subscribers
CREATE INDEX idx_profile_subscribers_profile_id ON profile_subscribers (profile_id);

-- Таблица subscription_profiles (список подписок профиля)
CREATE TABLE subscription_profiles              -- Исправлено название
(
    profile_id      BIGINT NOT NULL,            -- Ссылка на profile(id)
    subscription_id VARCHAR(255) NOT NULL,      -- Соответствует List<String> в Java
    PRIMARY KEY (profile_id, subscription_id),
    CONSTRAINT fk_subscription_profiles_profile
        FOREIGN KEY (profile_id)
            REFERENCES profile (id)
            ON DELETE CASCADE
);

-- Индекс для таблицы subscription_profiles
CREATE INDEX idx_subscription_profiles_profile_id ON subscription_profiles (profile_id);