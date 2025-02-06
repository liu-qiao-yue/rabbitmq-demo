package com.example.common;

import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;

/**
 * @Author: ellie
 * @CreateTime: 2025-02-03
 * @Description:
 * @Version: 1.0
 */
public class Utils {

    private Utils() {
    }
    public static  <T> T loadJson(String fileName, Class<T> valueType) throws IOException {
        // 使用ClassLoader获取资源路径，适用于src/test/resources下的文件
        ClassLoader classLoader = Utils.class.getClassLoader();
        try {
            // 读取文件为字符串
            String content = new String(Files.readAllBytes(Paths.get(classLoader.getResource(fileName).toURI())), StandardCharsets.UTF_8);

            // 使用Jackson ObjectMapper将JSON字符串转换为目标类型
            ObjectMapper objectMapper = new ObjectMapper();
            return objectMapper.readValue(content, valueType);
        } catch (Exception e) {
            throw new IOException("Could not read or parse file: " + fileName, e);
        }
    }
}
