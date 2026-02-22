package com.metrics.collector.core.service;

import com.metrics.collector.core.domain.MetricSnapshot;

import java.util.concurrent.atomic.AtomicReference;
import java.util.concurrent.atomic.DoubleAdder;
import java.util.concurrent.atomic.LongAdder;

/**
 * Класс-аккумулятор для одной метрики.
 * Использует DoubleAdder для высокой производительности записи (Sum/Count).
 * Для Min/Max используем CAS (Compare-And-Swap) оптимизацию.
 */
class MetricAccumulator {
    private final String key;
    private final DoubleAdder sum = new DoubleAdder();
    private final LongAdder count = new LongAdder();

    // Для Min/Max нам нужны атомики, так как Adder'ы не поддерживают min/max операции
    private final AtomicReference<Double> min = new AtomicReference<>(Double.MAX_VALUE);
    private final AtomicReference<Double> max = new AtomicReference<>(Double.MIN_VALUE);

    public MetricAccumulator(String key) {
        this.key = key;
    }

    public void add(double value) {
        sum.add(value);
        count.increment();
        accumulateMin(value);
        accumulateMax(value);
    }

    // Оптимистичная блокировка (CAS) для обновления Min
    private void accumulateMin(double value) {
        min.updateAndGet(current -> Math.min(current, value));
    }

    // Оптимистичная блокировка (CAS) для обновления Max
    private void accumulateMax(double value) {
        max.updateAndGet(current -> Math.max(current, value));
    }

    public MetricSnapshot toSnapshot() {
        double currentSum = sum.sum();
        long currentCount = count.sum();
        // Защита от деления на ноль, если снимок запросили до первой записи
        if (currentCount == 0) {
            return new MetricSnapshot(key, 0, 0, 0, 0, 0);
        }
        return new MetricSnapshot(
                key,
                currentSum,
                currentCount,
                min.get(),
                max.get(),
                currentSum / currentCount // Average
        );
    }

    // Метод для очистки после сброса в БД (если мы хотим сбрасывать состояние)
    public void reset() {
        sum.reset();
        count.reset();
        min.set(Double.MAX_VALUE);
        max.set(Double.MIN_VALUE);
    }
}
