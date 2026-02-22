package com.metrics.collector.core.port.in;

import com.metrics.collector.core.domain.RawMetric;

public interface MetricIngestUseCase {
    void ingest(RawMetric metric);
}
