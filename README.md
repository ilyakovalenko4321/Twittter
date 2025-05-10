## User Profile Microservice

Кратко о входных/выходных данных и внутренней логике:

### Эндпоинты

| Метод | Путь                        | Вход (JSON)                                      | Выход            | Описание                            |
|-------|-----------------------------|--------------------------------------------------|------------------|-------------------------------------|
| POST  | `/api/v1/users/public/create`  | `{ firstName, lastName, email, password }`       | `201 Created`    | Создать профиль и отправить код     |
| POST  | `/api/v1/users/public/confirm` | `{ email, confirmationCode }`                    | `200 OK`         | Подтвердить профиль по коду         |

#### JWT-аутентификация
| Метод | Путь                                 | Вход                                           | Выход                 | Описание                                                            |
|-------|--------------------------------------|------------------------------------------------|-----------------------|---------------------------------------------------------------------|
| POST  | `/api/v1/tokens/public/login`        | `{ tag, password }`                            | `200 OK` + `JwtTokenPair` | Логин: проверяет пароль → выдаёт пару токенов                      |
| GET   | `/api/v1/tokens/renew`               | `refreshToken=<token>`                         | `200 OK` + `JwtTokenPair` | Продление токенов; доступен только межсервисно через NGINX proxy |
| DELETE| `/api/v1/tokens/logout`              | `accessToken=<token>&refreshToken=<token>`     | `200 OK`              | Логаут: отзывает оба токена в Redis                                 |


### Внутренняя логика

1. **Создание профиля (`saveProfile`)**
    - Принимает DTO → маппит в `Profile`
    - Хэширует пароль через `PasswordEncoder`
    - Устанавливает статус `UNCONFIRMED`
    - Сохраняет в БД (`ProfileRepository.save`)
    - Генерирует 6‑значный код и сохраняет в Redis под ключом = `email`
    - Отправляет код по e‑mail через `MailSender`

2. **Подтверждение профиля (`confirmProfile`)**
    - Извлекает «реальный» код из Redis по `email`
    - Сравнивает с тем, что прислал клиент
    - При совпадении: обновляет статус = `CONFIRMED` в БД
    - Иначе: возвращает `false` (код неверен)

3. **Логин**
   - **POST** `/api/v1/tokens/public/login`
   - **Вход:** `{ tag, password }`
   - **Выход:** `200 OK` + `{ accessToken, refreshToken }` или `401 Unauthorized`
   - **Логика:**
      1. `findByTag(tag)`
      2. `passwordEncoder.matches()`
      3. При успехе → `formJwtTokenPair()`

4. **Продление токенов**
   - **GET** `/api/v1/tokens/renew?refreshToken=<token>`
   - **Вход:** `refreshToken`
   - **Выход:** `200 OK` + новая пара или `401 Unauthorized`
   - **Логика (внутренняя):**
      1. Доступно через NGINX proxy
      2. Декодировать JWT (проверить подпись + expiration)
      3. Проверить наличие в Redis
      4. `formJwtTokenPair()`

5. **Логаут**
   - **DELETE** `/api/v1/tokens/logout?accessToken=<token>&refreshToken=<token>`
   - **Вход:** `accessToken`, `refreshToken`
   - **Выход:** `200 OK` или `401 Unauthorized`
   - **Логика (внутренняя):**
      1. `redisTemplate.delete(accessToken)`
      2. `redisTemplate.delete(refreshToken)`

### Формирование токенов

```java
private JwtTokenPair formJwtTokenPair(String tag, Profile profile) {
    JwtTokenPair p = new JwtTokenPair();
    p.setAccessToken(generateToken(..., ACCESS));
    p.setRefreshToken(generateToken(..., REFRESH));
    // сохранить + TTL
    redisTemplate.opsForValue().set(p.getAccessToken(), tag);
    redisTemplate.expire(p.getAccessToken(), accessExp, timeUnit);
    redisTemplate.opsForValue().set(p.getRefreshToken(), tag);
    redisTemplate.expire(p.getRefreshToken(), refreshExp, timeUnit);
    return p;
}
