package com.banking.discovery.service.impl;

import java.util.Map;
import java.util.Objects;

public class ServiceMetadata {
    private String environment;
    private String version;
    private Map<String, String> tags;

    public ServiceMetadata() {}

    public ServiceMetadata(String environment, String version, Map<String, String> tags) {
        this.environment = environment;
        this.version = version;
        this.tags = tags;
    }

    public String getEnvironment() { return environment; }
    public void setEnvironment(String environment) { this.environment = environment; }
    public String getVersion() { return version; }
    public void setVersion(String version) { this.version = version; }
    public Map<String, String> getTags() { return tags; }
    public void setTags(Map<String, String> tags) { this.tags = tags; }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ServiceMetadata that = (ServiceMetadata) o;
        return Objects.equals(environment, that.environment) &&
                Objects.equals(version, that.version) &&
                Objects.equals(tags, that.tags);
    }

    @Override
    public int hashCode() {
        return Objects.hash(environment, version, tags);
    }
}
