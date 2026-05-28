package com.randompicker.handler;

import com.randompicker.util.ContentTypes;
import com.randompicker.util.HttpResponses;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

public class StaticFileHandler implements HttpHandler {

    private final Path webRoot;

    public StaticFileHandler(Path webRoot) {
        this.webRoot = webRoot.toAbsolutePath().normalize();
    }

    @Override
    public void handle(HttpExchange exchange) throws IOException {
        if (!"GET".equalsIgnoreCase(exchange.getRequestMethod())) {
            HttpResponses.text(exchange, 405, "只支持 GET 请求");
            return;
        }

        String requestPath = exchange.getRequestURI().getPath();
        if (requestPath.equals("/")) {
            requestPath = "/index.html";
        }

        Path filePath = webRoot.resolve(requestPath.substring(1)).normalize();
        if (!filePath.startsWith(webRoot) || !Files.exists(filePath) || Files.isDirectory(filePath)) {
            HttpResponses.text(exchange, 404, "页面不存在");
            return;
        }

        byte[] bytes = Files.readAllBytes(filePath);
        HttpResponses.bytes(exchange, 200, bytes, ContentTypes.fromPath(filePath));
    }
}
