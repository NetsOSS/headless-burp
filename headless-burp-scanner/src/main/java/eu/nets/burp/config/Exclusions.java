package eu.nets.burp.config;

import java.util.ArrayList;
import java.util.List;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;

@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "exclusions", propOrder = {
        "exclusion"
})
public class Exclusions {

    @XmlElement(required = true)
    private List<String> exclusion;

    public List<String> getExclusion() {
        if (exclusion == null) {
            exclusion = new ArrayList<String>();
        }
        return this.exclusion;
    }

}
