package gomule.util;

import gomule.item.D2Item;

import java.io.File;

import static gomule.d2x.D2Stash.FIXED_STASH_CHAR_LEVEL;
import static java.io.File.separator;

public class TestHelpers {

    private TestHelpers() {
    }

    @SuppressWarnings("SameParameterValue")
    public static D2Item loadItem(byte[] b) throws Exception {
        return loadItem(b, 2, 2);
    }

    @SuppressWarnings("SameParameterValue")
    public static D2Item loadItem(byte[] b, int col, int row) throws Exception {
        D2Item item = new D2Item("foo", new D2BitReader(b), FIXED_STASH_CHAR_LEVEL);
        item.set_col((short) col);
        item.set_row((short) row);
        item.set_location((short) 0);
        item.set_body_position((short) 0);
        item.set_panel((short) 5);
        return item;
    }

    public static File loadTestResources(String... path) {
        File file = new File("src" + separator + "test" + separator + "resources" + separator + String.join(separator, path));
        if (!file.exists()) {
            throw new IllegalStateException("Could not find source directory: " + file.getAbsolutePath());
        }
        return file;
    }
}
