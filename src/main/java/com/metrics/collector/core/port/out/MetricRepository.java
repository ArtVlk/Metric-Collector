package com.metrics.collector.core.port.out;

import com.metrics.collector.core.domain.MetricSnapshot;
import java.util.List;

public interface MetricRepository {
    void saveAll(List<MetricSnapshot> metrics);
}
