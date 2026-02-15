package gomule.d2s;

import com.google.common.io.Resources;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import randall.d2files.D2TxtFile;

import java.io.File;
import java.nio.file.Files;
import java.util.stream.Stream;

import static java.util.Objects.requireNonNull;
import static org.junit.jupiter.api.Assertions.assertEquals;

@SuppressWarnings("UnstableApiUsage")
public class D2CharacterTest {

    @BeforeAll
    public static void setup() {
        D2TxtFile.constructTxtFiles("./d2111");
    }

    public static class CharacterTestCase {
        final String name;
        final D2Character character;
        final File expectedFile;

        CharacterTestCase(String name, D2Character character, File expectedFile) {
            this.name = name;
            this.character = character;
            this.expectedFile = expectedFile;
        }

        @Override
        public String toString() {
            return name;
        }
    }

    static Stream<CharacterTestCase> charFileProvider() throws Exception {
        File charFilesDir = new File(Resources.getResource("charFiles").toURI());
        return Stream.of(requireNonNull(charFilesDir.listFiles()))
                .filter(file -> file.getName().endsWith(".d2s"))
                .map(file -> {
                    try {
                        String charName = file.getName().substring(0, file.getName().length() - 4);
                        D2Character character = new D2Character(file.getAbsolutePath());
                        File expectedFile = new File(charFilesDir, charName + ".d2s.expected");
                        return new CharacterTestCase(charName, character, expectedFile);
                    } catch (Exception e) {
                        throw new RuntimeException("Error with: " + file.getName(), e);
                    }
                });
    }

    @ParameterizedTest
    @MethodSource("charFileProvider")
    public void testCharacter(CharacterTestCase testCase) throws Exception {
        String actual = testCase.character.fullDumpStr().replaceAll("\r", "");
        String expected = new String(Files.readAllBytes(testCase.expectedFile.toPath()));
        assertEquals(expected, actual, "Character dump for " + testCase.name + " does not match expected output");
    }

    @Test
    @Disabled
    public void regenerateExpectedFiles() throws Exception {
        File sourceCharFilesDir = new File("src/test/resources/charFiles");
        if (!sourceCharFilesDir.exists()) {
            throw new IllegalStateException("Could not find source directory: " + sourceCharFilesDir.getAbsolutePath());
        }

        charFileProvider().forEach(testCase -> {
            try {
                String output = testCase.character.fullDumpStr().replaceAll("\r", "");
                File expectedFile = new File(sourceCharFilesDir, testCase.name + ".d2s.expected");
                Files.write(expectedFile.toPath(), output.getBytes());
                System.out.println("Generated: " + expectedFile.getAbsolutePath());
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        });
    }
}