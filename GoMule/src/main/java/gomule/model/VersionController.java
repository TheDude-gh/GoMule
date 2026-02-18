package gomule.model;

import java.util.Arrays;

public class VersionController {

    public static final long CURRENT_FILE_VERSION = 105;

    public enum Variant {

        CLASSIC(1),
        EXPANSION(2),
        ROW(3);

        private final int stashIdentifier;

        Variant(int stashIdentifier) {
            this.stashIdentifier = stashIdentifier;
        }

        public int getStashIdentifier() {
            return stashIdentifier;
        }

        public static Variant tryParse(int stashIdentifier) {
            return Arrays.stream(Variant.values()).filter(it -> it.stashIdentifier == stashIdentifier).findFirst().orElse(null);
        }
    }

}
