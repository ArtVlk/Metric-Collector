package com.metrics.collector.infrastructure.in.tcp;

import com.metrics.collector.core.domain.RawMetric;
import org.springframework.stereotype.Component;

import java.time.Instant;

@Component
public class TextProtocolParser {

    public RawMetric parse(String line) {
        if (line == null || line.isBlank()) {
            throw new IllegalArgumentException("Empty line");
        }

        String[] parts = line.trim().split("\\s+");
        if (parts.length < 2) {
            throw new IllegalArgumentException("Invalid format. Expected: 'key value [timestamp]'");
        }

        String key = parts[0];
        double value;
        try {
            value = Double.parseDouble(parts[1]);
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("Value must be a number");
        }

        // Если таймстемп не передан, берем текущее время
        Instant timestamp = (parts.length > 2)
                ? Instant.ofEpochMilli(Long.parseLong(parts[2]))
                : Instant.now();

        return new RawMetric(key, value, timestamp);
    }
}
