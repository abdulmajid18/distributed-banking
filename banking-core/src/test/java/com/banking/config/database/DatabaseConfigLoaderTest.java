package com.banking.config.database;
import com.banking.config.exception.ConfigurationException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.yaml.snakeyaml.Yaml;
import org.yaml.snakeyaml.error.YAMLException;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.assertj.core.api.AssertionsForInterfaceTypes.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class DatabaseConfigLoaderTest {

    @Mock
    private Yaml mockYaml;

    @Test
    void constructor_shouldThrowExceptionWhenConfigFileNull() {
        String missingFile = "nonexistent_db_config.yml";
        InputStream missingStream = getClass().getClassLoader().getResourceAsStream(missingFile);
        assertThatThrownBy(() -> new DatabaseConfigLoader(mockYaml, null))
                .isInstanceOf(ConfigurationException.class)
                .hasMessageContaining("Could not find config file");
    }

    @Test
    void constructor_shouldThrowExceptionWhenConfigInvalid(@Mock InputStream mockStream) {
        when(mockYaml.load(any(InputStream.class))).thenReturn(null);
        assertThatThrownBy(() -> new DatabaseConfigLoader(mockYaml, mockStream))
                .isInstanceOf(ConfigurationException.class)
                .hasMessageContaining("Database configuration is empty or invalid");
    }

    @Test
    void loadConfiguration_shouldThrowWhenYamlMalformed() {
        Yaml mockYaml = mock(Yaml.class);
        InputStream mockStream = mock(InputStream.class);

        when(mockYaml.load(any(InputStream.class)))
                .thenThrow(new YAMLException("Invalid YAML Syntax"));

        assertThatThrownBy(() -> new DatabaseConfigLoader(mockYaml, mockStream))
                .isInstanceOf(ConfigurationException.class)
                .hasMessageContaining("Malformed database configuration file");
    }

    @Test
    void loadConfiguration_shouldThrowWhenYamlException() {
        Yaml mockYaml = mock(Yaml.class);
        InputStream mockStream = mock(InputStream.class);

        when(mockYaml.load(any(InputStream.class)))
                .thenThrow(new RuntimeException("Failed to load"));

        assertThatThrownBy(() -> new DatabaseConfigLoader(mockYaml, mockStream))
                .isInstanceOf(ConfigurationException.class)
                .hasMessageContaining("Failed to load database configuration");
    }

    @Test
    void getDatabaseConfig_shouldReturnConfigWhenValid() {
        InputStream inputstream = getClass().getClassLoader().getResourceAsStream("db_config.yml");
        Yaml yaml = new Yaml();
        DatabaseConfigLoader loader = new DatabaseConfigLoader(yaml, inputstream);

        assertThat(loader.getDatabaseConfig())
                .containsExactlyInAnyOrderEntriesOf(
                        Map.of("url", "test_url",
                                "username", "test_user",
                                "password", "test_pass")
                );
    }

    @Test
    void getDatabaseConfig_shouldThrowWhenDatabaseSectionMissing() {
        Map<String, Object> testConfig = Map.of("other_section", "value");

        when(mockYaml.load(any(InputStream.class))).thenReturn(testConfig);
        DatabaseConfigLoader loader = new DatabaseConfigLoader(mockYaml,
                new ByteArrayInputStream("".getBytes()));


        assertThatThrownBy(loader::getDatabaseConfig)
                .isInstanceOf(ConfigurationException.class)
                .hasMessageContaining("Database configuration section not found");
    }

    @Test
    void getDatabaseUrl_shouldReturnUrlWhenPresent() {
        Map<String, Object> testConfig = Map.of(
                "database", Map.of("url", "test_url")
        );
        when(mockYaml.load(any(InputStream.class))).thenReturn(testConfig);

        DatabaseConfigLoader loader = new DatabaseConfigLoader(mockYaml,
                new ByteArrayInputStream("".getBytes()));

        assertThat(loader.getDatabaseUrl()).isEqualTo("test_url");
    }

    @Test
    void getDatabaseUrl_shouldThrowWhenUrlMissing() {
        Map<String, Object> testConfig = Map.of(
                "database", Map.of("username", "test_user")
        );
        when(mockYaml.load(any(InputStream.class))).thenReturn(testConfig);

        DatabaseConfigLoader loader = new DatabaseConfigLoader(mockYaml,
                new ByteArrayInputStream("".getBytes()));

        assertThatThrownBy(loader::getDatabaseUrl)
                .isInstanceOf(NullPointerException.class)
                .hasMessageContaining("Database URL not found in configuration");
    }

    @Test
    void getDatabaseUser_shouldReturnUsernameWhenPresent() {
        Map<String, Object> testConfig = Map.of(
                "database", Map.of("username", "test_user")
        );
        when(mockYaml.load(any(InputStream.class))).thenReturn(testConfig);
        DatabaseConfigLoader loader = new DatabaseConfigLoader(mockYaml,
                new ByteArrayInputStream("".getBytes()));

        assertThat(loader.getDatabaseUser()).isEqualTo("test_user");
    }

    @Test
    void getDatabasePassword_shouldReturnPasswordWhenPresent() {
        Map<String, Object> testConfig = Map.of(
                "database", Map.of("password", "test_pass")
        );
        when(mockYaml.load(any(InputStream.class))).thenReturn(testConfig);
        DatabaseConfigLoader loader = new DatabaseConfigLoader(mockYaml,
                new ByteArrayInputStream("".getBytes()));

        assertThat(loader.getDatabasePassword()).isEqualTo("test_pass");
    }
}
