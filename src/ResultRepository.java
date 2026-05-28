import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class ResultRepository {

    private final Path namesPath;
    private final Path resultPath;

    public ResultRepository(Path namesPath, Path resultPath) {
        this.namesPath = namesPath;
        this.resultPath = resultPath;
    }

    public List<String> readNames() {
        try {
            if (!Files.exists(namesPath)) {
                return new ArrayList<>();
            }

            return Files.readAllLines(namesPath, StandardCharsets.UTF_8)
                    .stream()
                    .map(String::trim)
                    .filter(name -> !name.isEmpty())
                    .distinct()
                    .collect(Collectors.toList());
        } catch (IOException e) {
            return new ArrayList<>();
        }
    }

    public void saveResult(List<String> names) {
        try {
            if (!Files.exists(resultPath.getParent())) {
                Files.createDirectories(resultPath.getParent());
            }

            String time = LocalDateTime.now()
                    .format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
            String text = time + " 抽中：" + String.join("、", names) + System.lineSeparator();

            Files.writeString(
                    resultPath,
                    text,
                    StandardOpenOption.CREATE,
                    StandardOpenOption.APPEND
            );
        } catch (IOException e) {
            System.out.println("保存结果失败：" + e.getMessage());
        }
    }
}
