package com.metrics.collector.infrastructure.out;


import com.metrics.collector.core.domain.MetricSnapshot;
import com.metrics.collector.core.port.out.MetricRepository;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.sql.PreparedStatement;
import java.sql.Timestamp;
import java.time.Instant;
import java.util.List;

@Repository
public class JdbcMetricRepository implements MetricRepository {

    private final JdbcTemplate jdbcTemplate;

    public JdbcMetricRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    @Transactional
    public void saveAll(List<MetricSnapshot> metrics) {
        if (metrics.isEmpty()) return;

        String sql = """
            INSERT INTO metric_aggregated 
            (metric_key, val_sum, val_count, val_min, val_max, val_avg, created_at) 
            VALUES (?, ?, ?, ?, ?, ?, ?)
        """;

        jdbcTemplate.batchUpdate(sql, metrics, 100, (PreparedStatement ps, MetricSnapshot metric) -> {
            ps.setString(1, metric.key());
            ps.setDouble(2, metric.sum());
            ps.setLong(3, metric.count());
            ps.setDouble(4, metric.min());
            ps.setDouble(5, metric.max());
            ps.setDouble(6, metric.average());
            ps.setTimestamp(7, Timestamp.from(Instant.now()));
        });
    }
}