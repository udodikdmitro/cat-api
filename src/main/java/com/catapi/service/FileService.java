package com.catapi.service;

import com.catapi.exception.ExternalApiException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.io.FileOutputStream;
import java.io.IOException;
import java.net.URL;
import java.nio.channels.Channels;
import java.nio.channels.FileChannel;
import java.nio.channels.ReadableByteChannel;

@Slf4j
@Service
public class FileService {
    public static String saveImage(String fullImagePath, URL url, String fileName) {
        try (FileOutputStream fileOutputStream = new FileOutputStream(fullImagePath)) {
            ReadableByteChannel readableByteChannel = Channels.newChannel(url.openStream());
            FileChannel fileChannel = fileOutputStream.getChannel();
            fileChannel.transferFrom(readableByteChannel, 0, Long.MAX_VALUE);
            log.debug("Image file is saved {}", fileName);
            return fileName;
        } catch (IOException e) {
            throw new ExternalApiException(e.getMessage());
        }
    }

}