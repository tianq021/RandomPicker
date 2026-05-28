import java.util.List;
import java.util.stream.Collectors;

public class JsonUtil {

    private JsonUtil() {
    }

    public static String toJsonArrayItems(List<String> names) {
        return names.stream()
                .map(name -> "\"" + escape(name) + "\"")
                .collect(Collectors.joining(", "));
    }

    public static String escape(String text) {
        return text
                .replace("\\", "\\\\")
                .replace("\"", "\\\"")
                .replace("\r", "\\r")
                .replace("\n", "\\n");
    }
}
