package com.metrics.collector.core.domain;

import java.time.Instant;

// Входящая "сырая" метрика
public record RawMetric(
        String key,
        double value,
        Instant timestamp
) {}
