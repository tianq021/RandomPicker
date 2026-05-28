import com.sun.net.httpserver.HttpServer;
import com.sun.net.httpserver.HttpExchange;

import java.io.IOException;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

public class Main {

    private static final Path NAMES_PATH = Paths.get("data", "names.txt");
    private static final Path RESULT_PATH = Paths.get("output", "result.txt");
    private static final Random RANDOM = new Random();

    public static void main(String[] args) throws IOException {
        HttpServer server = HttpServer.create(new InetSocketAddress(8080), 0);

        server.createContext("/", Main::handleIndex);
        server.createContext("/api/pick", Main::handlePick);

        server.setExecutor(null);
        server.start();

        System.out.println("随机点名系统已启动");
        System.out.println("浏览器打开：http://localhost:8080");
    }

    // 首页 HTML
    private static void handleIndex(HttpExchange exchange) throws IOException {
        String html = """
                <!DOCTYPE html>
                <html lang="zh-CN">
                <head>
                    <meta charset="UTF-8">
                    <title>随机点名系统</title>
                    <style>
                        body {
                            font-family: Arial, "Microsoft YaHei", sans-serif;
                            background: #f5f7fb;
                            display: flex;
                            justify-content: center;
                            align-items: center;
                            height: 100vh;
                            margin: 0;
                        }
                        .card {
                            width: 420px;
                            background: white;
                            padding: 30px;
                            border-radius: 16px;
                            box-shadow: 0 8px 30px rgba(0,0,0,0.12);
                            text-align: center;
                        }
                        h1 {
                            margin-bottom: 10px;
                        }
                        .desc {
                            color: #666;
                            margin-bottom: 25px;
                        }
                        button {
                            width: 180px;
                            height: 46px;
                            border: none;
                            border-radius: 8px;
                            background: #2f6fed;
                            color: white;
                            font-size: 18px;
                            cursor: pointer;
                        }
                        button:hover {
                            background: #1f56c9;
                        }
                        .result {
                            margin-top: 25px;
                            font-size: 26px;
                            font-weight: bold;
                            color: #222;
                        }
                        .tip {
                            margin-top: 15px;
                            color: #888;
                            font-size: 14px;
                        }
                    </style>
                </head>
                <body>
                    <div class="card">
                        <h1>随机点名系统</h1>
                        <p class="desc">点击按钮，从名单中随机抽取一个人</p>

                        <button onclick="pickName()">开始点名</button>

                        <div class="result" id="result">等待抽取</div>
                        <div class="tip">名单来自 data/names.txt</div>
                    </div>

                    <script>
                        function pickName() {
                            fetch("/api/pick")
                                .then(response => response.json())
                                .then(data => {
                                    if (data.success) {
                                        document.getElementById("result").innerText = "抽中：" + data.name;
                                    } else {
                                        document.getElementById("result").innerText = data.message;
                                    }
                                })
                                .catch(error => {
                                    document.getElementById("result").innerText = "请求失败";
                                    console.error(error);
                                });
                        }
                    </script>
                </body>
                </html>
                """;

        sendResponse(exchange, html, "text/html; charset=UTF-8");
    }

    // 随机点名接口
    private static void handlePick(HttpExchange exchange) throws IOException {
        if (!"GET".equalsIgnoreCase(exchange.getRequestMethod())) {
            sendResponse(exchange, "{\"success\":false,\"message\":\"只支持 GET 请求\"}", "application/json; charset=UTF-8");
            return;
        }

        List<String> names = readNames();

        if (names.isEmpty()) {
            sendResponse(exchange, "{\"success\":false,\"message\":\"名单为空，请检查 data/names.txt\"}", "application/json; charset=UTF-8");
            return;
        }

        String selectedName = names.get(RANDOM.nextInt(names.size()));

        saveResult(selectedName);

        String json = """
                {
                  "success": true,
                  "name": "%s"
                }
                """.formatted(escapeJson(selectedName));

        sendResponse(exchange, json, "application/json; charset=UTF-8");
    }

    // 读取名单
    private static List<String> readNames() {
        try {
            if (!Files.exists(NAMES_PATH)) {
                return new ArrayList<>();
            }

            List<String> lines = Files.readAllLines(NAMES_PATH, StandardCharsets.UTF_8);
            List<String> names = new ArrayList<>();

            for (String line : lines) {
                String name = line.trim();
                if (!name.isEmpty()) {
                    names.add(name);
                }
            }

            return names;

        } catch (IOException e) {
            return new ArrayList<>();
        }
    }

    // 保存抽取结果
    private static void saveResult(String name) {
        try {
            if (!Files.exists(RESULT_PATH.getParent())) {
                Files.createDirectories(RESULT_PATH.getParent());
            }

            String time = LocalDateTime.now()
                    .format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));

            String text = time + " 抽中：" + name + System.lineSeparator();

            Files.write(
                    RESULT_PATH,
                    text.getBytes(StandardCharsets.UTF_8),
                    StandardOpenOption.CREATE,
                    StandardOpenOption.APPEND
            );

        } catch (IOException e) {
            System.out.println("保存结果失败：" + e.getMessage());
        }
    }

    // 返回响应
    private static void sendResponse(HttpExchange exchange, String text, String contentType) throws IOException {
        byte[] bytes = text.getBytes(StandardCharsets.UTF_8);

        exchange.getResponseHeaders().set("Content-Type", contentType);
        exchange.sendResponseHeaders(200, bytes.length);

        try (OutputStream os = exchange.getResponseBody()) {
            os.write(bytes);
        }
    }

    // 简单处理 JSON 特殊字符
    private static String escapeJson(String text) {
        return text
                .replace("\\", "\\\\")
                .replace("\"", "\\\"");
    }
}