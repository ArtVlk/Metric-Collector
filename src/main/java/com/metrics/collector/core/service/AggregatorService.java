package com.metrics.collector.core.service;

import com.metrics.collector.core.domain.MetricSnapshot;
import com.metrics.collector.core.domain.RawMetric;
import com.metrics.collector.core.port.in.MetricIngestUseCase;
import com.metrics.collector.core.port.in.MetricQueryUseCase;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

@Service
public class AggregatorService implements MetricIngestUseCase, MetricQueryUseCase {

    // ConcurrentMap обеспечивает потокобезопасность структуры карты (добавление новых ключей)
    // MetricAccumulator обеспечивает потокобезопасность изменения значений внутри ключа
    private final ConcurrentMap<String, MetricAccumulator> storage = new ConcurrentHashMap<>();

    @Override
    public void ingest(RawMetric metric) {
        // computeIfAbsent атомарна: создаст аккумулятор только если его нет
        storage.computeIfAbsent(metric.key(), MetricAccumulator::new)
                .add(metric.value());
    }

    @Override
    public List<MetricSnapshot> getAllMetrics() {
        return storage.values().stream()
                .map(MetricAccumulator::toSnapshot)
                .toList();
    }

    // Метод для шедулера (будет вызываться позже), который забирает данные и очищает
    // Пока оставим package-private или public, решим на этапе реализации сброса в БД
    public List<MetricSnapshot> flushMetrics() {
        // ВАЖНО: Тут есть тонкий момент с конкурентностью при сбросе.
        // Простейший вариант "Copy and Clear" (но можно потерять доли секунды метрик при очистке).
        // Более надежный: Double buffering или удаление ключей итератором.

        // Для MVP сделаем простой вариант: берем снепшоты и делаем reset.
        return storage.values().stream()
                .map(acc -> {
                    var snapshot = acc.toSnapshot();
                    acc.reset();
                    return snapshot;
                })
                .filter(s -> s.count() > 0) // Не сохраняем пустые
                .toList();
    }
}
