package com.metrics.collector.infrastructure.in.tcp;

import com.metrics.collector.core.port.in.MetricIngestUseCase;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

@Component
public class TcpServer {

    private static final Logger log = LoggerFactory.getLogger(TcpServer.class);

    private final MetricIngestUseCase ingestUseCase;
    private final TextProtocolParser parser;

    private final ExecutorService executor = Executors.newVirtualThreadPerTaskExecutor();

    private volatile boolean running = false;
    private ServerSocket serverSocket;

    public TcpServer(MetricIngestUseCase ingestUseCase, TextProtocolParser parser) {
        this.ingestUseCase = ingestUseCase;
        this.parser = parser;
    }

    public void start(int port) {
        try {
            serverSocket = new ServerSocket(port);
            running = true;
            log.info("TCP Metrics Server started on port {}", port);
            log.info("Waiting for connections...");

            while (running) {
                try {
                    Socket client = serverSocket.accept();

                    ConnectionHandler handler = new ConnectionHandler(client, ingestUseCase, parser);
                    executor.submit(handler);

                } catch (IOException e) {
                    if (running) {
                        log.error("Error accepting connection", e);
                    }
                }
            }
        } catch (IOException e) {
            log.error("Could not start server on port " + port, e);
            throw new RuntimeException(e);
        }
    }

    public void stop() {
        running = false;
        try {
            if (serverSocket != null && !serverSocket.isClosed()) {
                serverSocket.close();
            }
            executor.shutdown();
            if (!executor.awaitTermination(5, TimeUnit.SECONDS)) {
                executor.shutdownNow();
            }
        } catch (Exception e) {
            log.error("Error stopping server", e);
        }
    }
}
