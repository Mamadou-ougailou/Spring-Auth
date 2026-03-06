package demo.dto;

public class EventHeaders {
    private String correlationId; // x-correlation-id
    private int schemaVersion; // x-schema-version

    // Constructors
    public EventHeaders() {
    }

    public EventHeaders(String correlationId, int schemaVersion) {
        this.correlationId = correlationId;
        this.schemaVersion = schemaVersion;
    }

    // Getters and Setters
    public String getCorrelationId() {
        return correlationId;
    }

    public void setCorrelationId(String correlationId) {
        this.correlationId = correlationId;
    }

    public int getSchemaVersion() {
        return schemaVersion;
    }

    public void setSchemaVersion(int schemaVersion) {
        this.schemaVersion = schemaVersion;
    }
}
