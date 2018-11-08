package eu.nets.burp.config;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;

@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "issue", propOrder = {
        "type",
        "path"
})
public class Issue {

    private int type;

    @XmlElement(required = true)
    private String path;

    public int getType() {
        return type;
    }

    public void setType(int value) {
        this.type = value;
    }

    public String getPath() {
        return path;
    }

    public void setPath(String value) {
        this.path = value;
    }

}
