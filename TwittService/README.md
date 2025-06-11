# 🐦 Twitt Microservice

![Java](https://img.shields.io/badge/Java-17+-blue) ![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.x-brightgreen) ![Cassandra](https://img.shields.io/badge/Cassandra-4.x-orange) ![Redis](https://img.shields.io/badge/Redis-6.x-red) ![Kafka](https://img.shields.io/badge/Kafka-3.x-yellow)

**Микросервис для обработки твитов: публикация, хранение и выборка случайных твитов.**

---

## 📚 Содержание

1. [Описание](#описание)
2. [Архитектура](#архитектура)
3. [Требования](#требования)
4. [Конфигурация](#конфигурация)
5. [Запуск](#запуск)
6. [REST API](#rest-api)
7. [gRPC Интерфейс](#grpc-интерфейс)
8. [Сценарии использования](#сценарии-использования)
9. [Внутренняя логика](#внутренняя-логика)
10. [Обработка ошибок и ретраи](#обработка-ошибок-и-ретраи)
11. [Известные проблемы и уязвимости](#известные-проблемы-и-уязвимости)

---

## 📝 Описание

Сервис обеспечивает:

* **Публикацию твитов** (REST).
* **Сохранение** в Cassandra.
* **Кеширование метаданных** в Redis для случайного отбора.
* **Асинхронную отправку** сообщений в Kafka (user и index топики).
* **Выборку случайных твитов** (gRPC).

---

## 🏗 Архитектура

```text
[Client] --> [REST Controller] --> [TwittService] --> {Cassandra, Redis, Kafka}
                                   ↳ [gRPC Service] --> [TwittService] --> Cassandra
```

* **REST Controller**: `POST /api/v1/twitt/post`
* **gRPC Service**: `GetTwitts` для получения N случайных твитов
* **TwittService**: бизнес-логика публикации и выборки
* **TwittRepository**: доступ к Cassandra
* **RedisTemplate**: хранение ключей для random selection
* **KafkaSender**: отправка событий

---

## 📋 Требования

* Java 17+
* Spring Boot 3.x
* Cassandra 4.x
* Redis 6.x
* Kafka 3.x
* Protobuf & gRPC

---

## ⚙️ Конфигурация

В `application.yml` или через переменные окружения:

```yaml
spring:
  kafka:
    user-topic: twitt-user-topic
    index-topic: twitt-index-topic
configs:
  redis:
    randomly-recommended-days: 7
    random-twitt-prefix: random_twitt
cassandra:
  contact-points: ...
  keyspace: twitt_keyspace
redis:
  host: localhost
  port: 6379
```

---

## ▶️ Запуск

1. Собрать проект:

   ```bash
   mvn clean package
   ```
2. Запустить зависимости (Cassandra, Redis, Kafka).
3. Запустить сервис:

   ```bash
   java -jar target/twitt-service.jar
   ```

---

## 🚀 REST API

### Публикация твита

`POST /api/v1/twitt/post`

* **Content-Type**: `application/json`

* **Тело запроса**:

  ```json
  {
    "userTag": "johndoe",
    "twittId": "550e8400-e29b-41d4-a716-446655440000",
    "twittText": "Hello, world!",
    "twittHeader": "Приветствие",
    "twittTags": ["intro", "hello"],
    "createdAt": "2025-06-11T12:00:00"
  }
  ```

* **Ответы**:

    * `200 OK` — успешно сохранено
    * `500 Internal Server Error` — ошибка при публикации

---

## 🛰 gRPC Интерфейс

```proto
service GetTwitts {
  rpc GetTwitts (GetTwittRequest) returns (GetTwittReply);
}

message GetTwittRequest {
  int32 twittsNumber = 1;
}

message GetTwittReply {
  repeated Twitt twitt = 1;
}

message Twitt {
  string userTag = 1;
  string twittText = 2;
  string twittHeader = 3;
  repeated string twittTags = 4;
  google.protobuf.Timestamp createdAt = 5;
}
```

* **Пример запроса**:

    * Вызов `GetTwitts` с `twittsNumber = 5` вернёт до 5 случайных твитов.

---

## 🎯 Сценарии использования

1. **Пользователь публикует твит** через REST.
2. **Сервис «Исследование»** получает случайный список твитов через gRPC.
3. **Модуль поиска** индексирует новые твиты, читая Kafka `index-topic`.
4. **Уведомления** подписчикам через Kafka `user-topic`.

---

## 🔍 Внутренняя логика

### Публикация твита

1. Запись в **Cassandra** (`twittRepository.save`).
2. Кеширование ключа в **Redis** — `prefix:twittId` с TTL.
3. Отправка сообщения в **Kafka**:

    * **user-topic**: `{ userTag → twittId }`
    * **index-topic**: `{ twittId, twittText, twittTags }`
4. **Ретраи** по исключениям (3 попытки, экспоненциальный бэкофф).

### Получение случайных твитов

1. **Scan Redis** по шаблону `prefix*`, собирая UUID.
2. **Набор до N ключей**.
3. **Запрос в Cassandra** `findAllByTwittIdIn`.
4. **Отправка** списка твитов клиенту.

---

## 🛠 Обработка ошибок и ретраи

| Компонент       | Поведение при ошибке                       | Ретрай                  |
| --------------- | ------------------------------------------ | ----------------------- |
| Cassandra Write | Исключение → лог → `@Retryable`            | 3 попытки, backoff 1s×2 |
| Redis Write     | Исключение → `RedisException` → 500 ответ  | Нет                     |
| Kafka Send      | Лог ошибки, без ретрая (fire-and-forget)   | Нет                     |
| gRPC Scan       | Исключение → `RedisException` (gRPC error) | Нет                     |

---

## ⚠️ Известные проблемы и уязвимости

1. **Фиктивный `userTag`** в gRPC  — используется "TEST\_TAG" вместо реального.
2. **Null-ключ в Kafka** — `ProducerRecord(key=null)`, плохой распределение.
3. **Отсутствие валидации** REST DTO (нет аннотаций `@Valid`).
4. **Blocking Scan** в Redis без постраничного обхода → подвисание на больших объёмах.
5. **TTL без учёта задержек** может привести к потере ключей до прочтения.
6. **Нет аутентификации/лимитирования** доступа к эндпоинтам.
7. **Избыточные catch-all** блоки раскрывают внутренние детали.
8. **Ретрай на все исключения** — ретраить client–errors бессмысленно.
9. **Отсутствие метрик**: нет мониторинга latency/fail rates.
10. **Потенциальный NPE** при `twittTags == null`.

---

*Готово к использованию! Проверьте и исправьте отмеченные проблемы для повышения стабильности и безопасности.*
