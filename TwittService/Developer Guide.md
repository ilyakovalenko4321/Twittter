# Документация сервиса управления твитами

Этот документ подробно описывает внутреннюю работу сервиса, отвечающего за:

* управление твитами,
* обработку взаимодействий с ними,
* формирование трендов,
* предоставление данных через интерфейсы.

Документ структурирован по функциональным блокам.

---

## 1. Репозитории данных

### 1.1. `CassandraTwittRepository`

Интерфейс, расширяющий `CassandraRepository` для взаимодействия с таблицей `twitts` в базе данных **Apache Cassandra**.

**Назначение:** Доступ к твитам, хранящимся в Cassandra.

**Методы:**

* `List<TwittEntity> findAllByTwittIdIn(List<UUID> ids)` — получение твитов по списку ID через CQL-запрос с `@Query`.

---

## 2. Обработка Kafka-событий

### 2.1. `KafkaTwittInteractingReceiver`

**Назначение:** Получение событий взаимодействий с твитами из Kafka.

**Механизм работы:**

* `@PostConstruct init()` — запускает прослушивание Kafka через `receiveTwittInteraction()`.
* `kafkaReceiver.receive()` — поток сообщений.
* `doOnNext(record)` —

    * извлечение `TwittInteractingDto`,
    * логирование ID и веса взаимодействия,
    * вызов `twittInteractingService.twittInteractingHandling(dto)`,
    * при `false` выбрасывает `RuntimeException`.
* `doOnError()` — логирование ошибок.
* `subscribe()` — активация потока.

**Зависимости:**

* `KafkaReceiver<String, Object>`
* `TwittInteractingService`

### 2.2. `KafkaTwittPublisher`

**Назначение:** Отправка сообщений в Kafka.

**Метод:**

* `Mono<Void> send(String sendTo, Map<String, Object> value)` — асинхронная отправка сообщения через `kafkaSender.send(...)`

**Зависимость:**

* `KafkaSender<String, Object>`

---

## 3. Сервисы обработки твитов

### 3.1. `TwittInteractingServiceImpl`

**Назначение:** Обработка взаимодействий и обновление "актуальности" твита в Redis.

**Методы:**

* `boolean twittInteractingHandling(TwittInteractingDto dto)` —

    * ключ в Redis: `trendTwittPrefix + twittId`
    * извлечение `timestamp` и `score` из Redis-списка,
    * вызов `changeActualityScore(...)`,
    * обновление Redis,
    * возвращает `true` или `false`

* `Double changeActualityScore(...)` — формула: `score + (weight * 2 / daysPassed)`

* `Long timePassed(Instant)` — количество дней между публикацией и сейчас

**Конфигурация:**

* `trendRecommendedDays`
* `trendTwittPrefix`

**Зависимость:** `RedisTemplate<String, Object>`

### 3.2. `TwittServiceImpl`

**Назначение:** Публикация, выборка случайных и трендовых твитов.

**Методы:**

* `boolean postTwitt(TwittEntity twittEntity)` —

    * `@Retryable`: 3 повтора, экспоненциальная задержка
    * сохранение в Cassandra
    * добавление в Redis-множество (random)
    * инициализация Redis-ключа (trend)
    * отправка в Kafka: `user-topic`, `index-topic`
    * `true` при успехе, иначе `RedisException`

* `boolean recover(Exception, TwittEntity)` — логирование ошибки, `false`

* `List<TwittEntity> formRandomTwittStack(Integer n)` —

    * ID из Redis (randomKeySetPrefix)
    * выборка из Cassandra

* `List<TwittEntity> formTrendTwittStack(Integer n)` — аналогично, но из `trendKeySetPrefix`

**Конфигурация:**

* Kafka: `userTopicName`, `indexTopicName`
* Redis: `trendRecommendedDays`, `trendTwittPrefix`, `randomKeySetPrefix`, `trendKeySetPrefix`

**Зависимости:**

* `CassandraTwittRepository`
* `TwittKafkaSender`
* `RedisTemplate`

---

## 4. Формирование трендов

### 4.1. `TrendSetterImpl`

**Назначение:** Ежедневное формирование топ-N трендов.

**Методы:**

* `@Scheduled(cron = "* * 1 * * *") modifyTrendList()` —

    * вызывает `scanAndCollectTopEntries(limit)`
    * вызывает `saveTopTrends(entries)`

* `List<Map.Entry<UUID, Double>> scanAndCollectTopEntries(int limit)` —

    * `PriorityQueue` на `limit` мест
    * сканирование Redis по шаблону ключей (`trendTwittPrefix + *`)
    * извлечение score из Redis List (второй элемент)
    * отбор top-N по актуальности

* `void saveTopTrends(entries)` —

    * сохраняет UUID в Redis Set (`trendKeySetPrefix`)

**Конфигурация:**

* `trendTwittPrefix`
* `trendKeySetPrefix`
* `trendLimit`

**Зависимость:** `RedisTemplate`

---

## 5. Контроллеры

### 5.1. `RandomTwittGrpcController`

**Назначение:** gRPC-интерфейс получения случайных и трендовых твитов.

**Методы:**

* `getRandomTwitts(request, responseObserver)` —

    * вызывает `randomTwittService.getRandomTwitts()`
    * возвращает `GetTwittRandomReply`

* `getTrendTwitts(request, responseObserver)` — аналогично выше

**Зависимость:** `RandomTwittService`

### 5.2. `TwittController`

**Назначение:** REST-интерфейс публикации твитов.

**Метод:**

* `@PostMapping("/post") create(@RequestBody TwittCreateRequest)` —

    * `twittDtoMapper.toEntity(...)`
    * `twittService.postTwitt(...)`
    * HTTP 200 OK или 500 Internal Server Error

**Зависимости:**

* `TwittService`
* `TwittDtoMapper`

---

## 6. Вспомогательные компоненты

### 6.1. `RandomTwittServiceImpl`

**Назначение:** Преобразование `TwittEntity` → gRPC-протобуф

**Методы:**

* `getRandomTwitts(Integer)` — вызывает `twittService.formRandomTwittStack(...)`, преобразует в `GetTwittRandomReply`
* `getTrendTwitts(Integer)` — аналогично, но для трендов
* `mapToProto(TwittEntity)` — маппинг полей, включая `createdAt` → `Timestamp`

**Зависимость:** `TwittService`

### 6.2. `TwittDtoMapper`

**Предполагаемый компонент** для преобразования DTO ↔ Entity (например, `TwittCreateRequest` → `TwittEntity`).

---
