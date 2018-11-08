package eu.nets.burp.config;

import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlElementWrapper;
import java.util.ArrayList;
import java.util.List;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlType;

@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "scope", propOrder = {
        "url",
        "exclusions"
})
public class Scope {

    @JacksonXmlElementWrapper(useWrapping = false)
    private List<String> url;

    private Exclusions exclusions;

    public List<String> getUrl() {
        if (url == null) {
            url = new ArrayList<String>();
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
