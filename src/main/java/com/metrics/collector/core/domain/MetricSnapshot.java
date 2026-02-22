package com.metrics.collector.core.domain;

// Агрегированная метрика (снимок состояния)
public record MetricSnapshot(
        String key,
        double sum,
        long count,
        double min,
        double max,
        double average
) {}
