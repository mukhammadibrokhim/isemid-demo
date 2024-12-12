package com.abdulatif.emis.service.file;


import com.abdulatif.emis.exception.RecordBadRequestException;
import com.abdulatif.emis.model.File;
import com.abdulatif.emis.model.enums.Status;
import com.abdulatif.emis.repository.file.FileRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.compress.utils.FileNameUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.FileOutputStream;
import java.io.IOException;

@Service
@RequiredArgsConstructor
@Slf4j
public class FileService {

    private final FileRepository fileRepository;


    @Value("${file.path}")
    public String filePath;

    public File storeFile(MultipartFile file, String pinfl) {
        try {
            String extension = FileNameUtils.getExtension(file.getContentType());
            String fileName = pinfl + "." + extension;
            java.io.File storeFile = new java.io.File(filePath + fileName);
            FileOutputStream fos = new FileOutputStream(storeFile);
            fos.write(file.getBytes());
            fos.close();
            return fileRepository.save(
                    File.builder()
                            .name(file.getOriginalFilename())
                            .contentType(extension)
                            .status(Status.ACTIVE)
                            .absolutePath(filePath)
                            .size(file.getSize())
                            .url(filePath + fileName)
                            .build()
            );
        } catch (IOException e) {
            log.warn("Could not store file", e);
        }
        return null;
    }

    public File storeBase64File(String pinfl, String base64File) {
        MultipartFile multipartFile;
        try {
            multipartFile = BASE64DecodedMultipartFile.base64ToMultipart(base64File);
        } catch (Exception e) {
            log.warn("Base64 to Multipart failure on FileService");
            throw new RecordBadRequestException("Base64 to Multipart failure");
        }
        return storeFile(multipartFile, pinfl);
    }

}
