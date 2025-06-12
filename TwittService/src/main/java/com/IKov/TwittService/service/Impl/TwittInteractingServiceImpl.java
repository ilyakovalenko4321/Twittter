package com.IKov.TwittService.service.Impl;

import com.IKov.TwittService.service.TwittInteractingService;
import com.IKov.TwittService.web.dto.TwittInteractingDto;
import jnr.ffi.annotations.In;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.sql.Timestamp;
import java.time.Duration;
import java.time.Instant;
import java.util.List;


@Service
@RequiredArgsConstructor
@Slf4j
public class TwittInteractingServiceImpl implements TwittInteractingService {

    @Value("${trend-recommended-days}")
    private Integer trendRecommendedDays;
    @Value("${configs.redis.trend-twitt-prefix}")
    private String trendTwittPrefix;

    private final RedisTemplate<String, Object> redisTemplate;

    @Override
    public boolean twittInteractingHandling(TwittInteractingDto twittInteractingDto) {
        //ToDo: завершить путь изменения актуальности.
        //ToDo: сделать ежедневное обновление актуальности в зависимости от времени
        //ToDo: Ежедневно пополнять и изменять список "актуальных постов"
        //ToDo: В Graph не забыть сделать список "актуальных людей" - постят с регулярностью больше 1 раза в неделю для поддержания развития новеньких
        //ToDo: Создать gRPC сервер "трендовых" постов
        // Завести scheduling обновление списка трендовых

        String key = trendTwittPrefix + twittInteractingDto.getId();

        List<Object> trendTwittList = redisTemplate.opsForList().range(key, 0, -1);

        if(trendTwittList != null && !trendTwittList.isEmpty()){
            String timestampStr = (String) trendTwittList.getFirst();
            Double score = (Double) trendTwittList.get(1);

            String[] parts = timestampStr.split("\\.");
            long seconds = Long.parseLong(parts[0]);
            int nanos = parts.length > 1 ? Integer.parseInt(parts[1]) : 0;

            Instant timestamp = Instant.ofEpochSecond(seconds, nanos);

            score = changeActualityScore(timestamp, score, twittInteractingDto.getInteractionWeight());

            redisTemplate.opsForList().rightPushAll(key, score, trendRecommendedDays - timePassed(timestamp));
            return true;
        }else {
            return false;
        }

    }

    private Double changeActualityScore(Instant publicationDate, Double actualScore, Double interactingWeight){

        long dayPassed = timePassed(publicationDate);

        if(dayPassed == 0){
            dayPassed = 1L;
        }

        //ToDO: Сделать нормальную логику добавления актуального веса события
        return actualScore+(interactingWeight*2/dayPassed);
    }

    private Long timePassed(Instant publicationDate){
        Instant currentDate = Instant.now();

        Duration duration = Duration.between(publicationDate, currentDate);
        return duration.toDays();
    }

}
