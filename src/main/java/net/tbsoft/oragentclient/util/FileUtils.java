package net.tbsoft.oragentclient.util;

import com.alibaba.fastjson2.JSON;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;

public class FileUtils {
    public static <T> T readObject(String fileName, Class<T> clazz) {

        File file = new File(fileName);
        try {
            return JSON.parseObject(Files.readAllBytes(file.toPath()), clazz);
        } catch (IOException e) {
            return null;
        }
    }

    public static <T> void writeObject(String fileName, T object) throws IOException {
        File file = new File(fileName);
        JSON.writeTo(Files.newOutputStream(file.toPath()), object);
    }
}
