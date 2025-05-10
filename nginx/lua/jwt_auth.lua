local auth_header = ngx.var.http_Authorization

if not auth_header or auth_header == "" then
    ngx.status = ngx.HTTP_UNAUTHORIZED
    ngx.say("401 Unauthorized: no header Authorization present")
    return ngx.exit(ngx.HTTP_UNAUTHORIZED)
end

local res = ngx.location.capture("/internal_auth", {
    method = ngx.HTTP_GET,
    headers = {
        Authorization = auth_header,
        ["Content-Type"] = "application/json"
    }
})

if not res then
    ngx.log(ngx.ERR, "Auth subrequest failed")
    ngx.status = ngx.HTTP_INTERNAL_SERVER_ERROR
    ngx.say("500 Internal Server Error")
    return ngx.exit(ngx.HTTP_INTERNAL_SERVER_ERROR)
end

if res.status ~= 200 then
    ngx.status = ngx.HTTP_UNAUTHORIZED
    ngx.say("401 Unauthorized: токен не валиден или истёк")
    return ngx.exit(ngx.HTTP_UNAUTHORIZED)
end
