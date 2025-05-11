package com.IKov.TwittService.service;

import reactor.core.publisher.Mono;

import java.util.Map;

public interface KafkaSender {

    Mono<Void> send(String sendTo, Map<String, Object> value);

}
