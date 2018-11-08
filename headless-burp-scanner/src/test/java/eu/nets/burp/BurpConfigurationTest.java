package eu.nets.burp;

import eu.nets.burp.config.ReportType;
import java.io.File;
import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class BurpConfigurationTest {

    @Test
    public void testLoadConfiguration() {
        // Given
        File configurationFile = new File("src/test/resources/config.xml");

        // When
        BurpConfiguration burpConfiguration = new BurpConfiguration(configurationFile);

        // Then
        assertThat(burpConfiguration).isNotNull();
        assertThat(burpConfiguration.getUrls()).hasSize(3);
        assertThat(burpConfiguration.getReportType()).isEqualTo(ReportType.JUNIT);
        assertThat(burpConfiguration.getFalsePositives()).hasSize(2);
        assertThat(burpConfiguration.getSiteMap().toString()).isEqualTo("http://localhost:20756/");

        assertThat(burpConfiguration.getScanReportType()).isEqualTo("XML");
        assertThat(burpConfiguration.getReportFile().getName()).contains("burp-report");
        assertThat(burpConfiguration.getReportFile().getName()).endsWith("xml");
    }

}