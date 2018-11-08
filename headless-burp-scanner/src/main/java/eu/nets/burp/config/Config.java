package eu.nets.burp.config;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlSchemaType;
import javax.xml.bind.annotation.XmlType;

@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "", propOrder = {
        "reportType",
        "scope",
        "targetSitemap",
        "falsePositives"
})
@XmlRootElement(name = "config")
public class Config {

    @XmlElement(required = true)
    @XmlSchemaType(name = "string")
    private ReportType reportType;

    private Scope scope;

    @XmlSchemaType(name = "anyURI")
    private String targetSitemap;

    @XmlElement(name = "false-positives")
    private FalsePositives falsePositives;

    public ReportType getReportType() {
        return reportType;
    }

    public void setReportType(ReportType value) {
        this.reportType = value;
    }

    public Scope getScope() {
        return scope;
    }

    public void setScope(Scope value) {
        this.scope = value;
    }

    public String getTargetSitemap() {
        return targetSitemap;
    }

    public void setTargetSitemap(String value) {
        this.targetSitemap = value;
    }

    public FalsePositives getFalsePositives() {
        return falsePositives;
    }

    public void setFalsePositives(FalsePositives value) {
        this.falsePositives = value;
    }

}
