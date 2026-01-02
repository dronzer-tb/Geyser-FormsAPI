package org.formsapi.config;

import org.formsapi.FormsAPIExtension;
import org.yaml.snakeyaml.Yaml;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;

/**
 * Configuration handler for FormsAPI extension.
 */
public class FormsConfig {

    private final FormsAPIExtension extension;
    private int port = 9876;

    public FormsConfig(FormsAPIExtension extension) {
        this.extension = extension;
        loadConfig();
    }

    private void loadConfig() {
        Path dataFolder = extension.dataFolder();
        Path configFile = dataFolder.resolve("config.yml");

        try {
            // Create data folder if it doesn't exist
            if (!Files.exists(dataFolder)) {
                Files.createDirectories(dataFolder);
            }

            // Copy default config if it doesn't exist
            if (!Files.exists(configFile)) {
                try (InputStream in = getClass().getResourceAsStream("/config.yml")) {
                    if (in != null) {
                        Files.copy(in, configFile);
                        extension.logger().info("Created default config.yml");
                    }
                }
            }

            // Load config
            if (Files.exists(configFile)) {
                Yaml yaml = new Yaml();
                try (InputStream in = Files.newInputStream(configFile)) {
                    Map<String, Object> config = yaml.load(in);
                    if (config != null) {
                        if (config.containsKey("port")) {
                            this.port = ((Number) config.get("port")).intValue();
                        }
                    }
                }
                extension.logger().info("Loaded config: port=" + port);
            }

        } catch (IOException e) {
            extension.logger().warning("Failed to load config: " + e.getMessage());
        }
    }

    public int getPort() {
        return port;
    }
}
