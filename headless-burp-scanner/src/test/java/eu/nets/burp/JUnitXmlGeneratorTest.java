package eu.nets.burp;

import com.google.common.base.Charsets;
import com.google.common.io.Files;
import java.io.File;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

import static org.assertj.core.api.Assertions.assertThat;

public class JUnitXmlGeneratorTest {

    @Rule
    public TemporaryFolder testFolder = new TemporaryFolder();

    @Test
    public void testJUnitXmlGeneration() throws Exception {
        // Given
        File jUnitReportXmlFile = testFolder.newFile("TEST-burp-report-new.xml");
        File burpReportFile = new File("src/test/resources/burp-report-new.xml");

        // When
        JUnitXmlGenerator.generateJUnitReportFromBurpReport(burpReportFile, jUnitReportXmlFile);

        /*// Then
        File expected = new File("src/test/resources/TEST-burp-report.xml");
        assertThat(jUnitReportXmlFile).exists();
        String actualContent = Files.toString(jUnitReportXmlFile, Charsets.UTF_8);
        assertThat(actualContent).isXmlEqualToContentOf(expected);*/
    }

}
