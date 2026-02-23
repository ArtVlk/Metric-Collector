CREATE TABLE IF NOT EXISTS metric_aggregated (
                                                 id BIGSERIAL PRIMARY KEY,
                                                 metric_key VARCHAR(255) NOT NULL,
    val_sum DOUBLE PRECISION,
    val_count BIGINT,
    val_min DOUBLE PRECISION,
    val_max DOUBLE PRECISION,
    val_avg DOUBLE PRECISION,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
    );

CREATE INDEX IF NOT EXISTS idx_metric_key_time ON metric_aggregated(metric_key, created_at);