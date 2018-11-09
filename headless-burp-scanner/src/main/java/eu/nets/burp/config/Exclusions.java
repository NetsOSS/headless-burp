package eu.nets.burp.config;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.ArrayList;
import java.util.List;

public class Exclusions {

    @JsonProperty(required = true)
    private List<String> exclusion;

    public List<String> getExclusion() {
        if (exclusion == null) {
            exclusion = new ArrayList<String>();
        }
        return this.exclusion;
    }

}
