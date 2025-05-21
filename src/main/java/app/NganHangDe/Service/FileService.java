package app.NganHangDe.Service;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

public class FileService {
    private static final String AUDIO_DIR = "src/main/resources/audio/";

    public String saveAudioFile(byte[] data, String fileName) throws IOException {
        Path dirPath = Paths.get(AUDIO_DIR);
        if (!Files.exists(dirPath)) {
            Files.createDirectories(dirPath);
        }

        Path filePath = dirPath.resolve(fileName);
        Files.write(filePath, data);
        return filePath.toString();
    }

    public void deleteAudioFile(String filePath) throws IOException {
        Files.deleteIfExists(Paths.get(filePath));
    }
}
