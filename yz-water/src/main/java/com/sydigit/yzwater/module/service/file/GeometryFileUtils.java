package com.sydigit.yzwater.module.service.file;

import lombok.experimental.UtilityClass;
import org.springframework.web.multipart.MultipartFile;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

/**
 * 压缩包辅助工具
 *
 * @author Lijun
 */
@UtilityClass
public class GeometryFileUtils {

    public static Path unzipToTemp(MultipartFile file, String prefix) throws IOException {
        Path tempDir = Files.createTempDirectory(prefix);
        try {
            unzip(file, tempDir);
        } catch (IOException ex) {
            deleteQuietly(tempDir);
            throw ex;
        }
        return tempDir;
    }

    public static void unzip(MultipartFile file, Path targetDir) throws IOException {
        try (InputStream inputStream = new BufferedInputStream(file.getInputStream());
             ZipInputStream zipInputStream = new ZipInputStream(inputStream, StandardCharsets.UTF_8)) {
            ZipEntry entry;
            while ((entry = zipInputStream.getNextEntry()) != null) {
                if (entry.isDirectory()) {
                    continue;
                }
                Path resolvedPath = targetDir.resolve(entry.getName()).normalize();
                if (!resolvedPath.startsWith(targetDir)) {
                    continue;
                }
                Path parent = resolvedPath.getParent();
                if (parent != null) {
                    Files.createDirectories(parent);
                }
                Files.copy(zipInputStream, resolvedPath, StandardCopyOption.REPLACE_EXISTING);
            }
        }
    }

    public static Path findComponent(Path directory, String suffix) throws IOException {
        try (var pathStream = Files.walk(directory)) {
            return pathStream.filter(path -> path.toString().toLowerCase().endsWith(suffix))
                    .findFirst()
                    .orElse(null);
        }
    }

    public static void deleteQuietly(Path path) {
        if (path != null) {
            try {
                Files.walk(path)
                        .sorted((a, b) -> b.compareTo(a))
                        .forEach(p -> {
                            try {
                                Files.deleteIfExists(p);
                            } catch (IOException ignored) {
                            }
                        });
            } catch (IOException ignored) {
            }
        }
    }
}

