import lombok.SneakyThrows;

import java.io.File;
import java.nio.file.Files;

public class test {

    @SneakyThrows
    public static void main(String[] args) {
        Files.walk(new File("C:/Users/oleks/Desktop/1.18-test/plugins/HungerGames/kits").toPath()).forEach(path -> System.out.println(new File(path.toString()).getName()));
    }

}
