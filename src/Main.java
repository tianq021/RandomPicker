import java.io.IOException;
import java.nio.file.Paths;

public class Main {

    public static void main(String[] args) throws IOException {
        RandomPickerServer server = new RandomPickerServer(
                8080,
                Paths.get("web"),
                Paths.get("data", "names.txt"),
                Paths.get("output", "result.txt")
        );

        server.start();

        System.out.println("随机点名系统已启动");
        System.out.println("浏览器打开：http://localhost:8080");
    }
}
