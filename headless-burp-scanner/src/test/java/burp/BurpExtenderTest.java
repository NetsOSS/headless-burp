package burp;

import eu.nets.burp.BurpConfiguration;
import eu.nets.burp.config.Issue;
import java.io.File;
import java.net.URL;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import static java.util.Collections.singletonList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyBoolean;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class BurpExtenderTest {

    @InjectMocks
    private BurpExtender burpExtender;

    @Mock
    IBurpExtenderCallbacks burpExtenderCallbacks;

    @Rule
    public TemporaryFolder temporaryFolder = new TemporaryFolder();

    private File configurationFile;

    @Before
    public void setUp() {
        configurationFile = new File("src/test/resources/config.xml");
    }

    @Test
    public void testThatExtensionIsRegisteredOk() {
        // Given
        // Extensions is loaded on Burp

        // When
        burpExtender.registerExtenderCallbacks(burpExtenderCallbacks);

        // Then
        verify(burpExtenderCallbacks).setExtensionName(eq("Headless Burp Scanner"));
        verify(burpExtenderCallbacks).registerHttpListener(eq(burpExtender));
        verify(burpExtenderCallbacks).registerScannerListener(eq(burpExtender));
        verify(burpExtenderCallbacks).registerExtensionStateListener(eq(burpExtender));
    }

    @Test
    public void testThatRunWithNoCommandLineArgumentsDoesNotStartScannerAndDoesNotExitApplication() {
        // Given
        when(burpExtenderCallbacks.getCommandLineArguments()).thenReturn(new String[]{});

        // When
        burpExtender.registerExtenderCallbacks(burpExtenderCallbacks);

        // Then
        verify(burpExtenderCallbacks, never()).exitSuite(anyBoolean());
        verify(burpExtenderCallbacks, never()).includeInScope(any(URL.class));
        verify(burpExtenderCallbacks, never()).sendToSpider(any(URL.class));
    }

    @Test
    public void testThatRunWithInvalidConfigurationFileDoesNotStartScanner() throws Exception {
        // Given
        configurationFile = temporaryFolder.newFile("config.xml");
        when(burpExtenderCallbacks.getCommandLineArguments()).thenReturn(new String[]{"-c", configurationFile.getAbsolutePath()});

        // When
        burpExtender.registerExtenderCallbacks(burpExtenderCallbacks);

        // Then
        verify(burpExtenderCallbacks, never()).exitSuite(anyBoolean());
        verify(burpExtenderCallbacks, never()).includeInScope(any(URL.class));
        verify(burpExtenderCallbacks, never()).sendToSpider(any(URL.class));
    }

    @Test
    public void testThatRunWithValidConfigurationFileStartsScanner() {
        // Given
        when(burpExtenderCallbacks.getCommandLineArguments()).thenReturn(new String[]{"-c", configurationFile.getAbsolutePath()});

        // When
        burpExtender.registerExtenderCallbacks(burpExtenderCallbacks);

        // Then
        verify(burpExtenderCallbacks).exitSuite(eq(false));

        verify(burpExtenderCallbacks, times(3)).includeInScope(any(URL.class));
        verify(burpExtenderCallbacks, times(3)).sendToSpider(any(URL.class));
        verify(burpExtenderCallbacks).generateScanReport(eq("XML"), any(IScanIssue[].class), any(File.class));
    }

    @Test
    public void testThatScanReportDoesIncludeExcludedIssues() throws Exception {
        // Given
        BurpConfiguration mockBurpConfiguration = mock(BurpConfiguration.class);

        Issue exclusion = new Issue();
        exclusion.setPath(".*.test.js");
        exclusion.setType(123456);
        when(mockBurpConfiguration.getFalsePositives()).thenReturn(singletonList(exclusion));
        burpExtender.setConfig(mockBurpConfiguration);

        IScanIssue issueToBeExcluded = mock(IScanIssue.class);
        when(issueToBeExcluded.getIssueType()).thenReturn(123456);
        URL excluded = new URL("http://issue.to-be.excluded/test.js");
        when(issueToBeExcluded.getUrl()).thenReturn(excluded);

        IScanIssue issueToBeIncluded = mock(IScanIssue.class);
        when(issueToBeIncluded.getIssueType()).thenReturn(1234);
        URL included = new URL("http://issue.to-be.included");
        when(issueToBeIncluded.getUrl()).thenReturn(included);

        // When
        burpExtender.newScanIssue(issueToBeExcluded);
        burpExtender.newScanIssue(issueToBeIncluded);

        // Then
        assertThat(burpExtender.getScanIssues()).containsOnly(issueToBeIncluded);
    }
}