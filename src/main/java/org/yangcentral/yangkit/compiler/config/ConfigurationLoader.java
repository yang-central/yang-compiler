package org.yangcentral.yangkit.compiler.config;

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
import java.util.Map;

/**
 * 配置文件加载器，支持JSON和YAML格式
 */
public class ConfigurationLoader {
    private static final Logger logger = LoggerFactory.getLogger(ConfigurationLoader.class);

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
     * 解析YAML格式的构建配置
     */
    private static BuildOption parseYamlBuildConfig(File file) throws IOException {
        Yaml yaml = new Yaml();
        Map<String, Object> data = yaml.load(new FileInputStream(file));
        
        // 将YAML数据转换为JSON字符串，然后复用现有的BuildOption.parse方法
        String jsonString = convertToJsonString(data);
        JsonElement jsonElement = JsonParser.parseString(jsonString);
        return BuildOption.parse(jsonElement);
    }

    /**
     * 解析JSON格式的设置
     */
    private static Settings parseJsonSettings(File file) throws IOException {
        String content = FileUtil.readFile2String(file);
        return Settings.parse(content);
    }

    /**
     * 解析YAML格式的设置
     */
    private static Settings parseYamlSettings(File file) throws IOException {
        Yaml yaml = new Yaml();
        Map<String, Object> data = yaml.load(new FileInputStream(file));
        
        // 将YAML数据转换为JSON字符串，然后复用现有的Settings.parse方法
        String jsonString = convertToJsonString(data);
        return Settings.parse(jsonString);
    }

    /**
     * 将Map转换为JSON字符串（简化实现）
     * 实际项目中可以使用Gson或其他库进行转换
     */
    private static String convertToJsonString(Map<String, Object> data) {
        // 这里使用简单的递归转换
        // 在生产环境中，建议使用Gson的toJsonTree方法
        StringBuilder sb = new StringBuilder();
        convertMapToJson(data, sb, 0);
        return sb.toString();
    }

    private static void convertMapToJson(Map<String, Object> map, StringBuilder sb, int indent) {
        sb.append("{\n");
        boolean first = true;
        for (Map.Entry<String, Object> entry : map.entrySet()) {
            if (!first) {
                sb.append(",\n");
            }
            addIndent(sb, indent + 1);
            sb.append("\"").append(escapeJson(entry.getKey())).append("\": ");
            convertValueToJson(entry.getValue(), sb, indent + 1);
            first = false;
        }
        sb.append("\n");
        addIndent(sb, indent);
        sb.append("}");
    }

    private static void convertValueToJson(Object value, StringBuilder sb, int indent) {
        if (value == null) {
            sb.append("null");
        } else if (value instanceof String) {
            sb.append("\"").append(escapeJson((String) value)).append("\"");
        } else if (value instanceof Number || value instanceof Boolean) {
            sb.append(value.toString());
        } else if (value instanceof Map) {
            convertMapToJson((Map<String, Object>) value, sb, indent);
        } else if (value instanceof java.util.List) {
            convertListToJson((java.util.List<?>) value, sb, indent);
        } else {
            sb.append("\"").append(escapeJson(value.toString())).append("\"");
        }
    }

    private static void convertListToJson(java.util.List<?> list, StringBuilder sb, int indent) {
        sb.append("[\n");
        boolean first = true;
        for (Object item : list) {
            if (!first) {
                sb.append(",\n");
            }
            addIndent(sb, indent + 1);
            convertValueToJson(item, sb, indent + 1);
            first = false;
        }
        sb.append("\n");
        addIndent(sb, indent);
        sb.append("]");
    }

    private static void addIndent(StringBuilder sb, int level) {
        for (int i = 0; i < level; i++) {
            sb.append("  ");
        }
    }

    private static String escapeJson(String str) {
        return str.replace("\\", "\\\\")
                  .replace("\"", "\\\"")
                  .replace("\n", "\\n")
                  .replace("\r", "\\r")
                  .replace("\t", "\\t");
    }
}
