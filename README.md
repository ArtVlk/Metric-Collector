Проект реализован в рамках моей крайней учебной практики.

Выполнил: Волк Артём(МЕН-420202)

# High-Performance Multithreaded Metric Collector

Высокопроизводительный TCP-сервер для сбора, агрегации и хранения числовых метрик.
Проект реализован на **Java 25** с использованием **Virtual Threads** (Project Loom) для обработки тысяч одновременных подключений и **Spring Boot** для управления зависимостями.

## Основные возможности

*   **Высокая производительность:** Использует Java Virtual Threads для обработки каждого TCP-соединения (модель "thread-per-request" без накладных расходов OS threads).
*   **Агрегация в памяти:** Метрики не пишутся в БД сразу. Они накапливаются в памяти (Time-Window Aggregation) с использованием неблокирующих структур (`DoubleAdder`, `LongAdder`, CAS), что снижает нагрузку на диск.
*   **Пакетная запись (Batch Insert):** Данные сбрасываются в PostgreSQL пачками по расписанию, минимизируя IO-операции.
*   **Чистая архитектура:** Проект построен по принципам **Hexagonal Architecture (Ports and Adapters)**. Бизнес-логика изолирована от фреймворков и драйверов.
*   **CLI Интерфейс:** Управление запуском через **Picocli**.

---

## Технологический стек

*   **Language:** Java 25 (Preview features enabled for Virtual Threads)
*   **Framework:** Spring Boot 3.x
*   **Database:** PostgreSQL 15
*   **CLI:** Picocli
*   **Build Tool:** Maven
*   **Containerization:** Docker & Docker Compose

---


### Ключевые компоненты:
1.  **TcpServer:** Слушает порт и создает виртуальный поток для каждого клиента.
2.  **AggregatorService:** "Губка", впитывающая данные. Использует `ConcurrentHashMap` и атомики для lock-free агрегации (Sum, Count, Min, Max, Avg).
3.  **SchedulerService:** Периодически (по умолчанию раз в 5 сек) забирает агрегированные данные, очищает буфер и отправляет их в репозиторий.

---

## Настройка и Запуск

### 1. Предварительные требования
*   Java 21+ (желательно 25)
*   Maven
*   Docker & Docker Compose

### 2. Запуск Базы Данных
Используется Docker Compose для поднятия PostgreSQL.
**Внимание:** Порт базы данных изменен на `5433`, чтобы избежать конфликтов с локальными установками Postgres.

```bash
docker-compose up -d
```

### 3. Сборка проекта
```bash
mvn clean package -DskipTests
```
### 4. Запуск приложения
```bash
java -jar target/collector-0.0.1-SNAPSHOT.jar --port=8888
```

---

## Протокол передачи данных

Сервер принимает текстовые данные по TCP (строка за строкой).
Формат: `metric_name value [timestamp]`

Примеры:
```txt
cpu_load 45.5
memory_free 10248800
disk_write_ops 150 1678900000
```

---

## Тестирование

Отправка метрик через Telnet
```bash
telnet localhost 8888
> cpu 10
> cpu 20
> (Ctrl+]) -> quit
```

PowerShell скрипт для нагрузки
```bash
# Настройки
$server = "localhost"
$port = 8888

try {
    $client = New-Object System.Net.Sockets.TcpClient($server, $port)
    $stream = $client.GetStream()
    $writer = New-Object System.IO.StreamWriter($stream)
    $writer.AutoFlush = $true

    Write-Host "Подключено! Отправляем метрики..." -ForegroundColor Green

    # 1. Отправляем 100 замеров CPU (значение скачет от 10 до 50)
    1..100 | ForEach-Object {
        $val = Get-Random -Minimum 10 -Maximum 50
        $writer.WriteLine("cpu_usage $val")
    }

    # 2. Отправляем 100 замеров Памяти (значение стабильное 1024)
    1..100 | ForEach-Object {
        $writer.WriteLine("memory_free 1024")
    }
    
    # 3. Отправляем "горячий" диск (значения растут)
    1..50 | ForEach-Object {
        $writer.WriteLine("disk_write $_")
    }

    Write-Host "Готово! Отправлено 250 метрик." -ForegroundColor Yellow
} catch {
    Write-Error "Не удалось подключиться: $_"
} finally {
    if ($client) { $client.Close() }
}
```

## Структура Базы Данных

Таблица `metric_aggregated`:

| Column | Type | Description |
| :--- | :--- | :--- |
| id | BIGSERIAL | Primary Key |
| metric_key | VARCHAR | Имя метрики (например, "cpu") |
| val_sum | DOUBLE PRECISION | Сумма всех значений за период |
| val_count | BIGINT | Количество измерений за период |
| val_min | DOUBLE PRECISION | Минимальное значение |
| val_max | DOUBLE PRECISION | Максимальное значение |
| val_avg | DOUBLE PRECISION | Среднее значение |
| created_at | TIMESTAMP | Время агрегации |

---

## Конфигурация (application.yml)

Основные настройки находятся в `src/main/resources/application.yml:`

```yaml
app:
  scheduler:
    flush-rate-ms: 5000  # Как часто сбрасывать данные в БД (мс)

spring:
  datasource:
    url: jdbc:postgresql://localhost:5433/metrics_db
```