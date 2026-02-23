package com.metrics.collector.core.service;

import com.metrics.collector.core.domain.MetricSnapshot;
import com.metrics.collector.core.domain.RawMetric;
import com.metrics.collector.core.port.in.MetricIngestUseCase;
import com.metrics.collector.core.port.in.MetricQueryUseCase;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

@Service
public class AggregatorService implements MetricIngestUseCase, MetricQueryUseCase {

    private final ConcurrentMap<String, MetricAccumulator> storage = new ConcurrentHashMap<>();

    @Override
    public void ingest(RawMetric metric) {
        storage.computeIfAbsent(metric.key(), MetricAccumulator::new)
                .add(metric.value());
    }

    @Override
    public List<MetricSnapshot> getAllMetrics() {
        return storage.values().stream()
                .map(MetricAccumulator::toSnapshot)
                .toList();
    }

    public List<MetricSnapshot> flushMetrics() {
        return storage.values().stream()
                .filter(acc -> acc.toSnapshot().count() > 0)
                .map(acc -> {
                    MetricSnapshot snapshot = acc.toSnapshot();
                    acc.reset();
                    return snapshot;
                })
                .filter(s -> s.count() > 0)
                .toList();
    }


}
