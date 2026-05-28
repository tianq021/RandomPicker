import com.sun.net.httpserver.HttpExchange;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;

public class HttpResponses {

    private HttpResponses() {
    }

    public static void text(HttpExchange exchange, int statusCode, String text) throws IOException {
        bytes(exchange, statusCode, text.getBytes(StandardCharsets.UTF_8), "text/plain; charset=UTF-8");
    }

    public static void json(HttpExchange exchange, int statusCode, boolean success, String message) throws IOException {
        String json = """
                {
                  "success": %s,
                  "message": "%s"
                }
                """.formatted(success, JsonUtil.escape(message));

        json(exchange, statusCode, json);
    }

    public static void json(HttpExchange exchange, int statusCode, String json) throws IOException {
        bytes(exchange, statusCode, json.getBytes(StandardCharsets.UTF_8), "application/json; charset=UTF-8");
    }

    public static void bytes(HttpExchange exchange, int statusCode, byte[] bytes, String contentType) throws IOException {
        exchange.getResponseHeaders().set("Content-Type", contentType);
        exchange.sendResponseHeaders(statusCode, bytes.length);

        try (OutputStream os = exchange.getResponseBody()) {
            os.write(bytes);
        }
    }
}
