package com.metrics.collector.core.service;

import com.metrics.collector.core.domain.MetricSnapshot;
import com.metrics.collector.core.domain.RawMetric;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.stream.IntStream;

import static org.assertj.core.api.Assertions.assertThat;

class AggregatorServiceConcurrencyTest {

    @Test
    @DisplayName("Должен выдерживать высокую нагрузку на один ключ без потери данных")
    void shouldHandleHighConcurrencyOnSingleKey() throws InterruptedException {
        AggregatorService service = new AggregatorService();
        int numberOfThreads = 10_000;
        String key = "concurrent_hit";

        try (ExecutorService executor = Executors.newVirtualThreadPerTaskExecutor()) {

            IntStream.range(0, numberOfThreads).forEach(i -> {
                executor.submit(() -> {
                    service.ingest(new RawMetric(key, 1.0, Instant.now()));
                });
            });

        }

        MetricSnapshot snapshot = service.getAllMetrics().getFirst();

        assertThat(snapshot.key()).isEqualTo(key);
        assertThat(snapshot.count()).isEqualTo(numberOfThreads);
        assertThat(snapshot.sum()).isEqualTo((double) numberOfThreads);
        assertThat(snapshot.min()).isEqualTo(1.0);
        assertThat(snapshot.max()).isEqualTo(1.0);
        assertThat(snapshot.average()).isEqualTo(1.0);
    }

    @Test
    @DisplayName("Должен корректно обновлять Min/Max в многопоточной среде")
    void shouldUpdateMinMaxConcurrently() {
        AggregatorService service = new AggregatorService();
        String key = "min_max_race";
        int iterations = 1000;

        try (ExecutorService executor = Executors.newVirtualThreadPerTaskExecutor()) {
            executor.submit(() -> {
                for (int i = 0; i < iterations; i++) service.ingest(new RawMetric(key, -100.0, Instant.now()));
            });

            executor.submit(() -> {
                for (int i = 0; i < iterations; i++) service.ingest(new RawMetric(key, 100.0, Instant.now()));
            });

            executor.submit(() -> {
                for (int i = 0; i < iterations; i++) service.ingest(new RawMetric(key, 0.0, Instant.now()));
            });
        }

        MetricSnapshot snapshot = service.getAllMetrics().getFirst();

        assertThat(snapshot.min()).isEqualTo(-100.0);
        assertThat(snapshot.max()).isEqualTo(100.0);
        assertThat(snapshot.count()).isEqualTo(iterations * 3);
    }
}
