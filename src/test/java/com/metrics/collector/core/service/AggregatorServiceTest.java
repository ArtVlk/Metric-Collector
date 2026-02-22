package com.metrics.collector.core.service;

import com.metrics.collector.core.domain.MetricSnapshot;
import com.metrics.collector.core.domain.RawMetric;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class AggregatorServiceTest {

    private final AggregatorService aggregatorService = new AggregatorService();

    @Test
    @DisplayName("Должен корректно агрегировать одиночную метрику")
    void shouldAggregatedSingleMetric() {
        RawMetric metric = new RawMetric("cpu", 10.0, Instant.now());

        aggregatorService.ingest(metric);
        List<MetricSnapshot> result = aggregatorService.getAllMetrics();

        assertThat(result).hasSize(1);
        MetricSnapshot snapshot = result.getFirst();

        assertThat(snapshot.key()).isEqualTo("cpu");
        assertThat(snapshot.sum()).isEqualTo(10.0);
        assertThat(snapshot.count()).isEqualTo(1);
        assertThat(snapshot.min()).isEqualTo(10.0);
        assertThat(snapshot.max()).isEqualTo(10.0);
        assertThat(snapshot.average()).isEqualTo(10.0);
    }

    @Test
    @DisplayName("Должен корректно считать min, max, avg для нескольких значений")
    void shouldCalculateMinMaxAvg() {
        String key = "memory";
        aggregatorService.ingest(new RawMetric(key, 10.0, Instant.now()));
        aggregatorService.ingest(new RawMetric(key, 50.0, Instant.now()));
        aggregatorService.ingest(new RawMetric(key, 30.0, Instant.now()));

        MetricSnapshot snapshot = aggregatorService.getAllMetrics().getFirst();

        assertThat(snapshot.count()).isEqualTo(3);
        assertThat(snapshot.sum()).isEqualTo(90.0);
        assertThat(snapshot.min()).isEqualTo(10.0);
        assertThat(snapshot.max()).isEqualTo(50.0);
        assertThat(snapshot.average()).isEqualTo(30.0);
    }

    @Test
    @DisplayName("Должен разделять метрики по ключам")
    void shouldSeparateMetricsByKeys() {
        aggregatorService.ingest(new RawMetric("cpu", 10.0, Instant.now()));
        aggregatorService.ingest(new RawMetric("disk", 5.0, Instant.now()));

        List<MetricSnapshot> metrics = aggregatorService.getAllMetrics();

        assertThat(metrics).hasSize(2)
                .extracting(MetricSnapshot::key)
                .containsExactlyInAnyOrder("cpu", "disk");
    }
}
