package gomule.model;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Arrays;
import java.util.Optional;
import java.util.function.Function;

public class VersionController {

    public enum Version {
        D2R3(105, "Resurrected: 3+");

        private final int fileVersionIdentifier;
        private final String humanName;

        Version(int fileVersionIdentifier, String humanName) {
            this.fileVersionIdentifier = fileVersionIdentifier;
            this.humanName = humanName;
        }

        public int getFileVersionIdentifier() {
            return fileVersionIdentifier;
        }

        public String getHumanName() {
            return humanName;
        }

        public static Version tryParse(int stashIdentifier) {
            return firstOrNull(Version.values(), it -> it.fileVersionIdentifier, stashIdentifier);
        }

        public static Version fromHumanName(String humanName) {
            return getFirst(Version.values(), it -> it.humanName, humanName).orElseThrow(() -> new IllegalArgumentException("Unknown version: " + humanName));
        }
    }

    public enum Variant {

//        CLASSIC(1, "Classic"), //TODO
        EXPANSION(2, "Expansion"),
        ROW(3, "Return of the Warlock");

        private final int stashIdentifier;
        private final String humanName;

        Variant(int stashIdentifier, String humanName) {
            this.stashIdentifier = stashIdentifier;
            this.humanName = humanName;
        }

        public int getStashIdentifier() {
            return stashIdentifier;
        }

        public String getHumanName() {
            return humanName;
        }

        public static Variant tryParse(int stashIdentifier) {
            return firstOrNull(Variant.values(), it -> it.stashIdentifier, stashIdentifier);
        }

        public static Variant fromHumanName(String humanName) {
            return getFirst(Variant.values(), it -> it.humanName, humanName).orElseThrow(() -> new IllegalArgumentException("Unknown variant: " + humanName));
        }
    }

    @Nullable
    private static <T> T firstOrNull(T[] values, Function<T, Integer> identifierExtractor, int identifier) {
        return getFirst(values, identifierExtractor, identifier).orElse(null);
    }

    private static <T, U> @NotNull Optional<T> getFirst(T[] values, Function<T, U> identifierExtractor, U identifier) {
        return Arrays.stream(values).filter(it -> identifierExtractor.apply(it).equals(identifier)).findFirst();
    }
}
