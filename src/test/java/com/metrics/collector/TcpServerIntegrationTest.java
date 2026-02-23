package com.metrics.collector;


import com.metrics.collector.core.port.in.MetricQueryUseCase;
import com.metrics.collector.infrastructure.in.tcp.TcpServer;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles; // Добавили импорт

import java.io.PrintWriter;
import java.net.Socket;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import static org.assertj.core.api.Assertions.assertThat;
import static org.awaitility.Awaitility.await;
import static java.util.concurrent.TimeUnit.SECONDS;

@SpringBootTest
@ActiveProfiles("test")
class TcpServerIntegrationTest {

    @Autowired
    private TcpServer tcpServer;

    @Autowired
    private MetricQueryUseCase queryUseCase;

    private final ExecutorService executor = Executors.newSingleThreadExecutor();

    @Test
    void shouldAcceptMetricsViaTcp() {
        int port = 9099;

        executor.submit(() -> tcpServer.start(port));

        await().atMost(2, SECONDS).ignoreExceptions().until(() -> {
            try (Socket s = new Socket("localhost", port)) {
                return true;
            }
        });

        try (Socket socket = new Socket("localhost", port);
             PrintWriter out = new PrintWriter(socket.getOutputStream(), true)) {
            out.println("temp 55.5");
            out.println("temp 60.5");
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

        await().atMost(5, SECONDS).untilAsserted(() -> {
            var metrics = queryUseCase.getAllMetrics();
            assertThat(metrics).isNotEmpty();
            var snapshot = metrics.getFirst();
            assertThat(snapshot.key()).isEqualTo("temp");
            assertThat(snapshot.count()).isEqualTo(2);
        });
    }

    @AfterEach
    void tearDown() {
        tcpServer.stop();
        executor.shutdownNow();
    }
}
