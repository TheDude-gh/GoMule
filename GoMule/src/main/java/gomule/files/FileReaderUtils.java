package gomule.files;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import static java.nio.charset.StandardCharsets.UTF_8;
import static java.util.Arrays.asList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import com.google.common.collect.Streams;
import com.google.common.io.Closeables;

public class FileReaderUtils {

    @SuppressWarnings("UnstableApiUsage")
    public static <T> List<T> readTsv(InputStream inputStream, LineParser<T> lineParser) {
        try {
            Stream<String> lines = new BufferedReader(new InputStreamReader(inputStream, UTF_8)).lines();
            Iterator<String> iterator = lines.iterator();
            Map<String, Integer> header = parseHeader(iterator.next());
            return Streams.stream(iterator)
                    .map(it -> it.split("\t"))
                    .map(it -> lineParser.parseLine(new Line(asList(it), header)))
                    .filter(Objects::nonNull)
                    .collect(Collectors.toList());
        } finally {
            Closeables.closeQuietly(inputStream);
        }
    }

    public static InputStream getResource(String name) {
        InputStream resourceAsStream = FileReaderUtils.class.getResourceAsStream("/" + name);
        if (resourceAsStream == null) throw new IllegalStateException("Failed to load " + name);
        return resourceAsStream;
    }

    public static InputStream getResourceJSON(String name) {
        FileInputStream inputStream;
        try {
            inputStream = new FileInputStream("Resources" + File.separator + name);
        }
        catch(IOException e) {
            throw new IllegalStateException("Failed to load " + name);
        }

        InputStream resourceAsStream = inputStream;
        if (resourceAsStream == null) {
            throw new IllegalStateException("Failed to load " + name);
        }
        return resourceAsStream;
    }

    public static Map<String, Integer> parseHeader(String headerLine) {
        String[] headers = headerLine.split("\t");
        return IntStream.range(0, headers.length).boxed().collect(Collectors.toMap(i -> headers[i], i -> i));
    }
}
