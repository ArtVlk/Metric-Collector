package com.metrics.collector.core.port.in;

import com.metrics.collector.core.domain.MetricSnapshot;
import java.util.List;

public interface MetricQueryUseCase {
    List<MetricSnapshot> getAllMetrics();
}
