package eu.nets.burp.config;

import com.fasterxml.jackson.annotation.JsonProperty;

public class Issue {

    private int type;

    @JsonProperty(required = true)
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
