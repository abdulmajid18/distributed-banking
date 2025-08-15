package com.banking.config.database;

import com.banking.config.exception.ConfigurationException;
import org.yaml.snakeyaml.Yaml;
import org.yaml.snakeyaml.error.YAMLException;

import java.io.InputStream;
import java.util.Map;
import java.util.Objects;

public class DatabaseConfigLoader {

    private final Map<String, Object> config;

    public DatabaseConfigLoader(Yaml yaml, InputStream inputStream) {
        if (inputStream == null) {
            throw new ConfigurationException("Could not find config file");
        }
        try {
            this.config = loadConfiguration(yaml, inputStream);
        } catch (ConfigurationException e) {
            throw e;
        } catch (Exception e) {
            throw new ConfigurationException("Failed to load database configuration", e);
        }
    }

    Map<String, Object> loadConfiguration(Yaml yaml, InputStream inputStream) {
        try {
            Map<String, Object> loadedConfig = yaml.load(inputStream);
            if (loadedConfig == null) {
                throw new ConfigurationException("Database configuration is empty or invalid");
            }
            return loadedConfig;
        } catch (YAMLException e) {
            throw new ConfigurationException("Malformed database configuration file", e);
        }
    }

    public Map<String, String> getDatabaseConfig() throws ConfigurationException {
        try {
            @SuppressWarnings("unchecked")
            Map<String, String> dbConfig = (Map<String, String>) config.get("database");
            if (dbConfig == null) {
                throw new ConfigurationException("Database configuration section not found");
            }
            return dbConfig;
        } catch (ClassCastException e) {
            throw new ConfigurationException("Database configuration structure is invalid", e);
        }

    }

    public String getDatabaseUrl() throws ConfigurationException {
        return Objects.requireNonNull(getDatabaseConfig().get("url"),
                "Database URL not found in configuration");
    }

    public String getDatabaseUser() throws ConfigurationException {
        return Objects.requireNonNull(getDatabaseConfig().get("username"),
                "Database username not found in configuration");
    }

    public String getDatabasePassword() throws ConfigurationException {
        return Objects.requireNonNull(getDatabaseConfig().get("password"),
                "Database password not found in configuration");
    }

}
