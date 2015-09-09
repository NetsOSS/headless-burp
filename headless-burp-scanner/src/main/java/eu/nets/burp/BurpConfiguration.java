package eu.nets.burp;

import eu.nets.burp.config.Config;
import eu.nets.burp.config.Issue;
import eu.nets.burp.config.ReportType;
import eu.nets.burp.config.Scope;
import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;
import java.util.stream.Collectors;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.Unmarshaller;

public class BurpConfiguration {

    private final Config config;

    public BurpConfiguration(File configurationFile) {
        config = loadConfiguration(configurationFile);
    }

    private Config loadConfiguration(File configurationFile) {
        try {
            JAXBContext jaxbContext = JAXBContext.newInstance(Config.class);
            Unmarshaller jaxbUnmarshaller = jaxbContext.createUnmarshaller();

            Config configuration = (Config) jaxbUnmarshaller.unmarshal(configurationFile);
            validateConfig(configuration);

            return configuration;
        } catch (Exception e) {
            throw new RuntimeException("Could not parse configuration file [" + configurationFile.getName() + "]", e);
        }
    }

    private void validateConfig(Config configuration) {
        if (configuration.getFalsePositives() != null && !configuration.getFalsePositives().getIssue().isEmpty()) {
            for (Issue exclusion : configuration.getFalsePositives().getIssue()) {
                try {
                    Pattern.compile(exclusion.getPath());
                } catch (PatternSyntaxException e) {
                    throw new RuntimeException(e.getDescription(), e);
                }
            }
        }
    }

    public List<URL> getUrls() {
        return Optional.of(config.getScope()).map(scope -> scope.getUrl()
                .stream()
                .map(this::getUrl)
                .filter(url -> url != null)
                .collect(Collectors.toList()))
                .orElse(Collections.<URL>emptyList());
    }

    private URL getUrl(String input) {
        try {
            return new URL(input);
        } catch (MalformedURLException e) {
            return null;
        }
    }

    public List<Issue> getFalsePositives() {
        return config.getFalsePositives() != null ? config.getFalsePositives().getIssue() : Collections.<Issue>emptyList();
    }

    public File getReportFile() {
        String scanReportType = config.getReportType() != ReportType.HTML ? ReportType.XML.value() : ReportType.HTML.value();
        return new File("burp-report" + "." + scanReportType);
    }

    public String getScanReportType() {
        return config.getReportType() != ReportType.HTML ? ReportType.XML.value() : ReportType.HTML.value();
    }

    public ReportType getReportType() {
        return config.getReportType();
    }

    public URL getSiteMap() {
        return config.getTargetSitemap() == null ? null : getUrl(config.getTargetSitemap());
    }

    public List<URL> getExclusions() {
        return Optional.of(config.getScope())
                .map(Scope::getExclusions)
                .map(exclusions -> exclusions.getExclusion()
                        .stream()
                        .map(this::getUrl)
                        .filter(url -> url != null)
                        .collect(Collectors.toList()))
                .orElse(Collections.<URL>emptyList());
    }
}
