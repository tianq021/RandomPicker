import com.sun.net.httpserver.HttpExchange;

import java.io.IOException;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class FormParser {

    private FormParser() {
    }

    public static Map<String, String> parse(HttpExchange exchange) throws IOException {
        String body = new String(exchange.getRequestBody().readAllBytes(), StandardCharsets.UTF_8);

        return Stream.of(body.split("&"))
                .filter(part -> !part.isBlank())
                .map(part -> part.split("=", 2))
                .collect(Collectors.toMap(
                        pair -> decodeUrl(pair[0]),
                        pair -> pair.length > 1 ? decodeUrl(pair[1]) : "",
                        (oldValue, newValue) -> newValue
                ));
    }

    private static String decodeUrl(String text) {
        return URLDecoder.decode(text, StandardCharsets.UTF_8);
    }
}
