# User Profile Microservice

Кратко о входных/выходных данных и внутренней логике:

---

## 1. Эндпоинты

### 1.1. Профили пользователей (Public)

| Метод | Путь                           | Вход (JSON)                                | Выход         | Описание                        |
| ----- | ------------------------------ | ------------------------------------------ | ------------- | ------------------------------- |
| POST  | `/api/v1/users/public/create`  | `{ firstName, lastName, email, password }` | `201 Created` | Создать профиль и отправить код |
| POST  | `/api/v1/users/public/confirm` | `{ email, confirmationCode }`              | `200 OK`      | Подтвердить профиль по коду     |

### 1.2. Валидация Access Token (Internal)

| Метод | Путь                    | Параметры / Тело                               | Выход                           | Описание                                                                                                      |
| ----- | ----------------------- | ---------------------------------------------- | ------------------------------- | ------------------------------------------------------------------------------------------------------------- |
| POST  | `/api/v1/auth/validate` | `?token=<accessToken>`<br>`{ tag \| userTag }` | `200 OK` или `401 Unauthorized` | Проверяет JWT: подпись, subject, наличие в Redis и статус пользователя. В случае неуспеха — дизлогинит токен. |

### 1.3. JWT-аутентификация

| Метод  | Путь                           | Вход                                        | Выход                     | Описание                                               |
| ------ | ------------------------------ | ------------------------------------------- | ------------------------- | ------------------------------------------------------ |
| POST   | `/api/v1/tokens/public/login`  | `{ tag, password }`                         | `200 OK` + `JwtTokenPair` | Логин: проверяет пароль → выдаёт пару токенов          |
| POST   | `/api/v1/tokens/public/renew`  | `?token=<refreshToken>&tag=<tag>`           | `200 OK` + `JwtTokenPair` | Продление токенов: проверяет refreshToken → новая пара |
| DELETE | `/api/v1/tokens/public/logout` | `?accessToken=<token>&refreshToken=<token>` | `200 OK`                  | Логаут: удаляет оба токена из Redis                    |

---

## 2. Внутренняя логика

### 2.1. Создание профиля (`saveProfile`)

* Принимает DTO → мапит в `Profile`
* Хэширует пароль через `PasswordEncoder`
* Устанавливает статус `UNCONFIRMED`
* Сохраняет в БД (`ProfileRepository.save`)
* Генерирует 6‑значный код и сохраняет в Redis под ключом = `email`
* Отправляет код по e‑mail через `MailSender`

### 2.2. Подтверждение профиля (`confirmProfile`)

* Извлекает код из Redis по `email`
* Сравнивает с тем, что прислал клиент
* При совпадении → обновляет статус = `CONFIRMED` в БД
* Иначе → возвращает `false`

### 2.3. Логин (`login`)

* `POST /api/v1/tokens/public/login`
* `findByTag(tag)` → `passwordEncoder.matches()` → при успехе → `formJwtTokenPair(tag, profile)`

### 2.4. Валидация Access Token (`validate`)

* `POST /api/v1/auth/validate`
* `AuthService.validate(token, tag)`:

    * Парсит JWT, проверяет подпись и `expiration`
    * Сравнивает `subject` с `expectedTag`
    * Проверяет наличие токена в Redis и соответствие тегу
    * Проверяет статус пользователя (`status` в claim) на `CONFIRMED`
* Возвращает `true` → `200 OK`
* При `false` → вызывает `JwtService.logout(token, "")` → `401 Unauthorized`

### 2.5. Валидация Refresh Token (`validateRefreshToken`)

* Внутренняя проверка в `AuthService.validateRefreshToken(token, tag)`:

    * Парсит JWT и проверяет подпись
    * Сравнивает `subject` и наличие в Redis
    * Проверяет тип токена = `REFRESH` (`claim["type"]`)
* Возвращает булево

### 2.6. Продление токенов (`renew`)

* `POST /api/v1/tokens/public/renew`
* `JwtService.renew(token, tag)`:

    * Вызывает `validateRefreshToken(token, tag)`
    * При `true` удаляет старый refresh-токен из Redis
    * Вызывает `formJwtTokenPair(tag, profile)` → возвращает новую пару

### 2.7. Логаут (`logout`)

* `DELETE /api/v1/tokens/public/logout`
* `JwtService.logout(accessToken, refreshToken)`:

    * `redisTemplate.delete(accessToken)`
    * `redisTemplate.delete(refreshToken)`
    * Всегда возвращает `true`

---

## 3. Формирование токенов (`formJwtTokenPair`)

```java
private JwtTokenPair formJwtTokenPair(String tag, Profile profile) {
    JwtTokenPair pair = new JwtTokenPair();
    pair.setAccessToken(
        generateToken(tag, profile.getStatus(), issuer, accessExp, timeUnit, JwtTokenType.ACCESS)
    );
    pair.setRefreshToken(
        generateToken(tag, profile.getStatus(), issuer, refreshExp, timeUnit, JwtTokenType.REFRESH)
    );

    // Сохраняем в Redis с TTL
    redisTemplate.opsForValue().set(pair.getAccessToken(), tag);
    redisTemplate.expire(pair.getAccessToken(), accessExp, timeUnit);

    redisTemplate.opsForValue().set(pair.getRefreshToken(), tag);
    redisTemplate.expire(pair.getRefreshToken(), refreshExp, timeUnit);

    return pair;
}
```

Где `generateToken(...)` строит JWT с:

* `subject = tag`
* `claim("status") = profile.getStatus().name()`
* `claim("type") = JwtTokenType.{ACCESS|REFRESH}`
* `issuer`, `issuedAt`, `expiration`
* подпись чере
