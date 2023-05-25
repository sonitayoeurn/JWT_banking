package co.istad.mbanking.api.file;

import co.istad.mbanking.util.FileUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

@Service
@Slf4j
public class FileServiceImpl implements FileService {

    @Value("${file.download-url}")
    private String fileDownloadUrl;

    private FileUtil fileUtil;

    @Value("${file.server-path}")
    private String fileServerPath;

    @Value("${file.base-url}")
    private String fileBaseUrl;


    @Autowired
    public void setFileUtil(FileUtil fileUtil) {
        this.fileUtil = fileUtil;
    }

    @Override
    public Resource download(String name) {
        return fileUtil.findByName(name);
    }

    @Override
    public void deleteByName(String name) {
        fileUtil.deleteByName(name);
    }

    @Override
    public FileDto findByName(String name) throws IOException {
        Resource resource = fileUtil.findByName(name);
        return FileDto.builder()
                .name(resource.getFilename())
                .extension(fileUtil.getExtension(resource.getFilename()))
                .url(String.format("%s%s", fileUtil.getFileBaseUrl(), resource.getFilename()))
                .downloadUrl(String.format("%s%s", fileDownloadUrl, name))
                .size(resource.contentLength())
                .build();
    }

    @Override
    public FileDto uploadSingle(MultipartFile file) {
        return fileUtil.upload(file);
    }

    @Override
    public List<FileDto> uploadMultiple(List<MultipartFile> files) {

        List<FileDto> filesDto = new ArrayList<>();

        for (MultipartFile file : files) {
            filesDto.add(fileUtil.upload(file));
        }

        return filesDto;
    }

    @Override
    public List<FileDto> findAllFiles() {
        List<FileDto> files = new ArrayList<>();
        try (Stream<Path> paths = Files.walk(Paths.get(fileServerPath))) {
            paths
                    .filter(Files::isRegularFile)
                    .forEach(path -> {
                        String fileName = path.getFileName().toString();
                        long size = path.toFile().length();
                        String url = String.format("%s%s", fileBaseUrl, fileName);
                        int lastDotIndex = fileName.lastIndexOf(".");
                        String extension = lastDotIndex > 0 ? fileName.substring(lastDotIndex + 1) : "";
                        String downloadUrl = String.format("%s%s", fileDownloadUrl, fileName);
                        FileDto file = FileDto.builder()
                                .name(fileName)
                                .url(url)
                                .downloadUrl(downloadUrl)
                                .extension(extension)
                                .size(size)
                                .build();
                        files.add(file);
                    });
        } catch (IOException e) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR,
                    "Failed to retrieve files ...!");
        }
        return files;
    }

    @Override
    public boolean deleteAllFile() {
        if (this.findAllFiles().isEmpty()) throw new ResponseStatusException(HttpStatus.NOT_FOUND,
                "File is empty.");
        this.findAllFiles().forEach(fileDto -> this.deleteByName(fileDto.name()));
        return true;
    }

}
