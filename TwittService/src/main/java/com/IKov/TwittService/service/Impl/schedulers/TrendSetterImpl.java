package com.IKov.TwittService.service.Impl.schedulers;

import com.IKov.TwittService.service.TrendSetter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.Cursor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ScanOptions;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;
import java.util.*;

@Service
@RequiredArgsConstructor
@Slf4j
public class TrendSetterImpl implements TrendSetter {

    private final RedisTemplate<String, Object> redisTemplate;

    @Value("${configs.redis.trend-twitt-prefix}")
    private String trendTwittPrefix;

    @Value("${configs.redis.trend-key-set-prefix}")
    private String trendKeySetPrefix;

    @Value("${configs.local.trend_size}")
    private Integer trendLimit;

    @Override
    @Scheduled(cron = "* * 1 * * *") // каждый день в 01:00
    public void modifyTrendList() {
        List<Map.Entry<UUID, Double>> topEntries = scanAndCollectTopEntries(trendLimit);
        saveTopTrends(topEntries);
        log.info("Сформировано {} новых трендовых твитов", topEntries.size());
    }

    private List<Map.Entry<UUID, Double>> scanAndCollectTopEntries(int limit) {
        PriorityQueue<Map.Entry<UUID, Double>> queue = new PriorityQueue<>(limit,
                Comparator.comparingDouble(Map.Entry::getValue));

        ScanOptions options = ScanOptions.scanOptions()
                .match(trendTwittPrefix + "*")
                .count(1000)
                .build();

        try (Cursor<byte[]> cursor = Objects.requireNonNull(redisTemplate.getConnectionFactory())
                .getConnection()
                .keyCommands()
                .scan(options)) {

            while (cursor.hasNext()) {
                String key = new String(cursor.next(), StandardCharsets.UTF_8);
                UUID id = UUID.fromString(key.split("_", 2)[1]);

                List<Object> vals = redisTemplate.opsForList().range(key, 0, 1);
                if (vals == null || vals.size() < 2) continue;

                try {
                    double score = Double.parseDouble(vals.get(1).toString());
                    Map.Entry<UUID, Double> entry = Map.entry(id, score);
                    if (queue.size() < limit) {
                        queue.offer(entry);
                    } else if (queue.peek().getValue() < score) {
                        queue.poll();
                        queue.offer(entry);
                    }
                } catch (NumberFormatException ex) {
                    log.warn("Invalid score for key {}: {}", key, vals.get(1));
                }
            }
        } catch (Exception e) {
            log.error("Error scanning Redis for trend keys", e);
        }

        List<Map.Entry<UUID, Double>> list = new ArrayList<>(queue);
        list.sort(Map.Entry.<UUID, Double>comparingByValue().reversed());
        return list;
    }

    private void saveTopTrends(List<Map.Entry<UUID, Double>> entries) {
        for (Map.Entry<UUID, Double> e : entries) {
            redisTemplate.opsForSet().add(trendKeySetPrefix, e.getKey().toString());
        }
    }

}
