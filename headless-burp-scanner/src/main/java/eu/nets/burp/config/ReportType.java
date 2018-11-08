package eu.nets.burp.config;

import javax.xml.bind.annotation.XmlEnum;
import javax.xml.bind.annotation.XmlType;

@XmlType(name = "reportType")
@XmlEnum
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
