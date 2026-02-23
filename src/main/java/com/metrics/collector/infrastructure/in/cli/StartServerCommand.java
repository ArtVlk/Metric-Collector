package com.metrics.collector.infrastructure.in.cli;

import com.metrics.collector.infrastructure.in.tcp.TcpServer;
import org.springframework.stereotype.Component;
import picocli.CommandLine.Command;
import picocli.CommandLine.Option;

import java.util.concurrent.Callable;

@Component
@Command(name = "start", description = "Starts the TCP metrics server")
public class StartServerCommand implements Callable<Integer> {

    @Option(names = {"-p", "--port"}, description = "TCP Port to listen on", defaultValue = "8080")
    private int port;

    private final TcpServer tcpServer;

    public StartServerCommand(TcpServer tcpServer) {
        this.tcpServer = tcpServer;
    }

    @Override
    public Integer call() {
        tcpServer.start(port);
        return 0;
    }
}
