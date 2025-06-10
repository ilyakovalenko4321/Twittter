-- 1. Получаем заголовок Authorization
local auth_header = ngx.var.http_Authorization
if not auth_header or auth_header == "" then
    ngx.status = ngx.HTTP_UNAUTHORIZED
    ngx.say("401 Unauthorized: no Authorization header present")
    return ngx.exit(ngx.HTTP_UNAUTHORIZED)
end

-- 2. Читаем тело оригинального запроса (JSON)
ngx.req.read_body()
local orig_body = ngx.req.get_body_data() or ""

-- 3. Формируем target URL с query‑параметром token
--    Экранируем токен, чтобы избежать проблем с URL‑спецсимволами
local token_escaped = ngx.escape_uri(auth_header)
local internal_path = "/internal_auth?token=" .. token_escaped

-- 4. Запускаем subrequest: POST на internal_path, передаём тело и заголовок Content-Type
local res = ngx.location.capture(internal_path, {
    method  = ngx.HTTP_POST,
    body    = orig_body,
    headers = {
        ["Content-Type"] = "application/json",
    },
})

-- 5. Обрабатываем возможный провал subrequest’а
if not res then
    ngx.log(ngx.ERR, "Auth subrequest failed")
    ngx.status = ngx.HTTP_INTERNAL_SERVER_ERROR
    ngx.say("500 Internal Server Error")
    return ngx.exit(ngx.HTTP_INTERNAL_SERVER_ERROR)
end

-- 6. Проверяем статус валидации
if res.status ~= ngx.HTTP_OK then
    ngx.status = ngx.HTTP_UNAUTHORIZED
    ngx.say("401 Unauthorized: токен не валиден или истёк")
    return ngx.exit(ngx.HTTP_UNAUTHORIZED)
end

