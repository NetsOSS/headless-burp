package eu.nets.burp.config;

public enum ReportType {

    JUNIT,
    HTML,
    XML;

    public String value() {
        return name();
    }

    public static ReportType fromValue(String v) {
        return valueOf(v);
    }

}
