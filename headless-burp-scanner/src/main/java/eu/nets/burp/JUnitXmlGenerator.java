package eu.nets.burp;

import com.google.common.base.Charsets;
import com.google.common.io.BaseEncoding;
import com.google.common.io.Files;
import com.google.common.io.Resources;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;

public class JUnitXmlGenerator {

    public static void generateJUnitReportFromBurpReport(File burpReportFile, File junitReportFile) {
        try {
            URL burpToJUnitXslUrl = Resources.getResource("burp-to-junit.xsl");
            StreamSource burpToJunitXsl = new StreamSource(burpToJUnitXslUrl.openStream());
            StreamSource input = new StreamSource(Files.newReader(burpReportFile, Charsets.UTF_8));
            Transformer transformer = TransformerFactory.newInstance().newTransformer(burpToJunitXsl);
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            transformer.transform(input, new StreamResult(outputStream));

            Files.write(outputStream.toByteArray(), junitReportFile);
        } catch (IOException | TransformerException e) {
            throw new RuntimeException("Could not generate JUnit report", e);
        }
    }

    public static String decodeBase64(String encodedText) {
        return new String(BaseEncoding.base64().decode(encodedText), Charsets.UTF_8);
    }
}
