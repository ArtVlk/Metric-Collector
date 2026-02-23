package com.metrics.collector.core.service;


import com.metrics.collector.core.domain.MetricSnapshot;
import com.metrics.collector.core.port.out.MetricRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class SchedulerService {

    private static final Logger log = LoggerFactory.getLogger(SchedulerService.class);

    private final AggregatorService aggregatorService;
    private final MetricRepository metricRepository;

    public SchedulerService(AggregatorService aggregatorService, MetricRepository metricRepository) {
        this.aggregatorService = aggregatorService;
        this.metricRepository = metricRepository;
    }

    @Scheduled(fixedRateString = "${app.scheduler.flush-rate-ms:5000}")
    public void flushMetricsToDatabase() {
        List<MetricSnapshot> snapshots = aggregatorService.flushMetrics();

        if (snapshots.isEmpty()) {
            return;
        }

        log.info("Flushing {} aggregated metrics to database...", snapshots.size());

        try {
            metricRepository.saveAll(snapshots);
            log.info("Successfully saved metrics.");
        } catch (Exception e) {
            log.error("Failed to save metrics to DB!", e);
        }
    }
}
