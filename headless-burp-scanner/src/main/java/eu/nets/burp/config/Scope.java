package eu.nets.burp.config;

import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlElementWrapper;
import java.util.ArrayList;
import java.util.List;

public class Scope {

    @JacksonXmlElementWrapper(useWrapping = false)
    private List<String> url;

    private Exclusions exclusions;

    public List<String> getUrl() {
        if (url == null) {
            url = new ArrayList<>();
        }
        return this.url;
    }

    public Exclusions getExclusions() {
        return exclusions;
    }

    public void setExclusions(Exclusions value) {
        this.exclusions = value;
    }

}
