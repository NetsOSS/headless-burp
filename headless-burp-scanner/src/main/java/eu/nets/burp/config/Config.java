package eu.nets.burp.config;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlRootElement;

@JacksonXmlRootElement
public class Config {

    @JsonProperty(required = true)
    private ReportType reportType;

    private Scope scope;

    private String targetSitemap;

    @JacksonXmlProperty(localName = "false-positives")
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
