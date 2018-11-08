package eu.nets.burp.config;

import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlElementWrapper;
import java.util.ArrayList;
import java.util.List;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlType;

@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "false-positives", propOrder = {
        "issue"
})
public class FalsePositives {

    @JacksonXmlElementWrapper(useWrapping = false)
    private List<Issue> issue;

    public List<Issue> getIssue() {
        if (issue == null) {
            issue = new ArrayList<Issue>();
        }
        return this.issue;
    }

}
