package com.randompicker.server;

import com.randompicker.handler.PickHandler;
import com.randompicker.handler.StaticFileHandler;
import com.randompicker.repository.ResultRepository;
import com.randompicker.service.NamePickerService;
import com.sun.net.httpserver.HttpServer;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.file.Path;

public class RandomPickerServer {

    private final int port;
    private final Path webRoot;
    private final Path namesPath;
    private final Path resultPath;

    public RandomPickerServer(int port, Path webRoot, Path namesPath, Path resultPath) {
        this.port = port;
        this.webRoot = webRoot;
        this.namesPath = namesPath;
        this.resultPath = resultPath;
    }

    public void start() throws IOException {
        HttpServer server = HttpServer.create(new InetSocketAddress(port), 0);
        ResultRepository resultRepository = new ResultRepository(namesPath, resultPath);
        NamePickerService namePickerService = new NamePickerService();

        server.createContext("/api/pick", new PickHandler(namePickerService, resultRepository));
        server.createContext("/", new StaticFileHandler(webRoot));

        server.setExecutor(null);
        server.start();
    }
}
