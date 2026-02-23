package com.metrics.collector.core.domain;

import java.time.Instant;

public record RawMetric(
        String key,
        double value,
        Instant timestamp
) {}
