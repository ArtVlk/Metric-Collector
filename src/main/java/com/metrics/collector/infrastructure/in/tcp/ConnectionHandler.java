package com.metrics.collector.infrastructure.in.tcp;

import com.metrics.collector.core.port.in.MetricIngestUseCase;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.Socket;

/**
 * Runnable задача, которая выполняется в отдельном виртуальном потоке для каждого клиента.
 */
public class ConnectionHandler implements Runnable {

    private static final Logger log = LoggerFactory.getLogger(ConnectionHandler.class);

    private final Socket clientSocket;
    private final MetricIngestUseCase ingestUseCase;
    private final TextProtocolParser parser;

    public ConnectionHandler(Socket clientSocket, MetricIngestUseCase ingestUseCase, TextProtocolParser parser) {
        this.clientSocket = clientSocket;
        this.ingestUseCase = ingestUseCase;
        this.parser = parser;
    }

    @Override
    public void run() {
        try (
                clientSocket;
                BufferedReader reader = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()))
        ) {
            String line;
            while ((line = reader.readLine()) != null) {
                try {
                    var metric = parser.parse(line);
                    ingestUseCase.ingest(metric);
                } catch (IllegalArgumentException e) {
                    log.warn("Bad protocol data from {}: {}", clientSocket.getRemoteSocketAddress(), e.getMessage());
                }
            }
        } catch (IOException e) {
            log.info("Client disconnected: {}", clientSocket.getRemoteSocketAddress());
        }
    }
}