package com.metrics.collector.infrastructure.in.cli;

import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.ExitCodeGenerator;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;
import picocli.CommandLine;
import picocli.CommandLine.IFactory;

@Component
@Profile("!test")
public class AppStartupRunner implements CommandLineRunner, ExitCodeGenerator {

    private final IFactory factory;
    private final StartServerCommand startCommand;
    private int exitCode;

    public AppStartupRunner(IFactory factory, StartServerCommand startCommand) {
        this.factory = factory;
        this.startCommand = startCommand;
    }

    @Override
    public void run(String[] args) {
        exitCode = new CommandLine(startCommand, factory).execute(args);
    }

    @Override
    public int getExitCode() {
        return exitCode;
    }
}
