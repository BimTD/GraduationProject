package org.example.graduationproject.services;

import org.springframework.web.multipart.MultipartFile;

import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;

public class FileSystemStorageService implements StorageService{

    private final Path rootLocation = Paths.get("D:/DATN/GraduationProject/src/main/resources/static/uploads");

    @Override
    public void store(MultipartFile file) {
        try{
            Path destinationFile = this.rootLocation.resolve(Paths.get(file.getOriginalFilename())).normalize().toAbsolutePath();
            try(InputStream inputStream = file.getInputStream()){
                Files.copy(inputStream, destinationFile, StandardCopyOption.REPLACE_EXISTING);
            }

        }catch (Exception e)
        {
            e.printStackTrace();
        }
    }
}
