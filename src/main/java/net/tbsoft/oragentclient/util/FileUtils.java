package net.tbsoft.oragentclient.util;

import com.alibaba.fastjson2.JSON;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;

public class FileUtils {
    public static <T> List<T> readObject(String fileName, Class<T> clazz) {
        List<T> result;
        File file = new File(fileName);
        try {
            result = JSON.parseArray(Files.readAllBytes(file.toPath()), clazz);
        } catch (IOException e) {
            result = new ArrayList<>();
        }
        return result;
    }

    public static <T> void writeObject(String fileName, List<T> list) throws IOException {
        File file = new File(fileName);
        JSON.writeTo(Files.newOutputStream(file.toPath()), list);
    }
}
