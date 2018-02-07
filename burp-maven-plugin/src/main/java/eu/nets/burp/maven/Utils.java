package eu.nets.burp.maven;

import com.google.common.collect.Lists;
import java.io.File;
import java.util.List;
import java.util.Locale;
import org.codehaus.plexus.util.StringUtils;

public class Utils {

    /**
     * Splits the path on / or \ and reconstructs the path with the file.separator character so it can be executed on any OS
     *
     * @param path path to file or directory
     * @return reconstructed path
     */
    public static String normalize(String path) {
        String deUnixedPath = path.replace("/", File.separator);
        return deUnixedPath.replace("\\", File.separator);
    }

    public static String encloseInDoubleQuotes(String in) {
        return "\"" + in + "\"";
    }

    public static boolean isWindows() {
        return System.getProperty("os.name").toLowerCase(Locale.ENGLISH).contains("windows");
    }

    public static class ClassPathBuilder {

        private List<String> entries = Lists.newArrayList();

        private ClassPathBuilder() {
        }

        public static ClassPathBuilder newInstance() {
            return new ClassPathBuilder();
        }

        public ClassPathBuilder path(String path) {
            entries.add(normalize(path));
            return this;
        }

        public String build() {
            String classPathEntrySeparator = isWindows() ? ";" : ":";
            return encloseInDoubleQuotes(StringUtils.join(entries.iterator(), classPathEntrySeparator));
        }

    }
}
