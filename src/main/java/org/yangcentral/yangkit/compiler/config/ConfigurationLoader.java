package org.yangcentral.yangkit.compiler.config;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.yangcentral.yangkit.compiler.BuildOption;
import org.yangcentral.yangkit.compiler.Settings;
import org.yangcentral.yangkit.utils.file.FileUtil;
import org.yaml.snakeyaml.Yaml;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;

/**
 * 配置文件加载器，支持JSON和YAML格式
 */
public class ConfigurationLoader {
    private static final Logger logger = LoggerFactory.getLogger(ConfigurationLoader.class);
    private static final Gson GSON = new Gson();

    /**
     * 加载构建配置（build.json或build.yaml）
     *
     * @param configFile 配置文件
     * @return BuildOption对象
     * @throws IOException 读取文件失败
     */
    public static BuildOption loadBuildConfig(File configFile) throws IOException {
        if (!configFile.exists()) {
            throw new IOException("Configuration file not found: " + configFile.getAbsolutePath());
        }

        String fileName = configFile.getName().toLowerCase();
        logger.info("Loading build configuration from: {}", configFile.getAbsolutePath());

        if (fileName.endsWith(".yaml") || fileName.endsWith(".yml")) {
            return parseYamlBuildConfig(configFile);
        } else if (fileName.endsWith(".json")) {
            return parseJsonBuildConfig(configFile);
        } else {
            throw new IllegalArgumentException("Unsupported config format: " + fileName
                + ". Supported formats: .json, .yaml, .yml");
        }
    }

    /**
     * 加载设置配置（settings.json或settings.yaml）
     *
     * @param configFile 配置文件
     * @return Settings对象
     * @throws IOException 读取文件失败
     */
    public static Settings loadSettings(File configFile) throws IOException {
        if (!configFile.exists()) {
            logger.warn("Settings file not found: {}, using default settings", configFile.getAbsolutePath());
            return new Settings();
        }

        String fileName = configFile.getName().toLowerCase();
        logger.info("Loading settings from: {}", configFile.getAbsolutePath());

        if (fileName.endsWith(".yaml") || fileName.endsWith(".yml")) {
            return parseYamlSettings(configFile);
        } else if (fileName.endsWith(".json")) {
            return parseJsonSettings(configFile);
        } else {
            throw new IllegalArgumentException("Unsupported settings format: " + fileName);
        }
    }

    /**
     * 解析JSON格式的构建配置
     */
    private static BuildOption parseJsonBuildConfig(File file) throws IOException {
        String content = FileUtil.readFile2String(file);
        JsonElement jsonElement = JsonParser.parseString(content);
        return BuildOption.parse(jsonElement);
    }

    /**
     * 解析YAML格式的构建配置（使用Gson转换，避免手写转换的脆弱性）
     */
    private static BuildOption parseYamlBuildConfig(File file) throws IOException {
        Yaml yaml = new Yaml();
        try (FileInputStream fis = new FileInputStream(file)) {
            Object data = yaml.load(fis);
            String jsonString = GSON.toJson(data);
            JsonElement jsonElement = JsonParser.parseString(jsonString);
            return BuildOption.parse(jsonElement);
        }
    }

    /**
     * 解析JSON格式的设置
     */
    private static Settings parseJsonSettings(File file) throws IOException {
        String content = FileUtil.readFile2String(file);
        return Settings.parse(content);
    }

    /**
     * 解析YAML格式的设置（使用Gson转换，避免手写转换的脆弱性）
     */
    private static Settings parseYamlSettings(File file) throws IOException {
        Yaml yaml = new Yaml();
        try (FileInputStream fis = new FileInputStream(file)) {
            Object data = yaml.load(fis);
            String jsonString = GSON.toJson(data);
            return Settings.parse(jsonString);
        }
    }
}
