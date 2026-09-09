package com.campus.market.file;

import com.campus.market.common.BusinessException;
import com.campus.market.config.FileStorageProperties;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Locale;
import java.util.Set;
import java.util.UUID;

/**
 * 本地文件存储实现。
 * 文件写到 {@code <dir>/<yyyyMM>/<uuid>.<ext>},URL 由 {@code baseUrl + /uploads/<yyyyMM>/<uuid>.<ext>} 拼接。
 */
@Service
public class FileStorageService {

    private static final Set<String> ALLOWED_EXT = Set.of(".jpg", ".jpeg", ".png", ".webp", ".gif");
    private static final long MAX_SIZE = 5L * 1024 * 1024;

    private final FileStorageProperties props;

    public FileStorageService(FileStorageProperties props) {
        this.props = props;
    }

    public UploadResult save(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new BusinessException(400, "文件为空");
        }
        if (file.getSize() > MAX_SIZE) {
            throw new BusinessException(400, "文件超过 5MB");
        }
        String orig = file.getOriginalFilename();
        String ext = "";
        if (orig != null && orig.contains(".")) {
            ext = orig.substring(orig.lastIndexOf('.')).toLowerCase(Locale.ROOT);
        }
        if (!ALLOWED_EXT.contains(ext)) {
            throw new BusinessException(400, "图片格式不支持(支持 jpg/jpeg/png/webp/gif)");
        }
        String month = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyyMM"));
        Path dir = Path.of(props.dir(), month);
        try {
            Files.createDirectories(dir);
        } catch (IOException e) {
            throw new BusinessException(500, "目录创建失败");
        }
        String name = UUID.randomUUID().toString().replace("-", "") + ext;
        Path target = dir.resolve(name);
        try (InputStream in = file.getInputStream()) {
            Files.copy(in, target, StandardCopyOption.REPLACE_EXISTING);
        } catch (IOException e) {
            throw new BusinessException(500, "写盘失败");
        }
        String urlPath = "/uploads/" + month + "/" + name;
        return new UploadResult(props.baseUrl() + urlPath, urlPath);
    }

    public record UploadResult(String url, String path) {
    }
}