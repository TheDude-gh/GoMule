package gomule.d2x;

import gomule.item.D2Item;
import gomule.util.D2BitReader;

import java.io.File;
import java.util.ArrayList;

import static gomule.d2x.D2Stash.FIXED_STASH_CHAR_LEVEL;
import static gomule.d2x.D2StashWriter.*;
import static gomule.model.VersionController.CURRENT_FILE_VERSION;

public class D2StashReader {
    public D2Stash readStash(String filename) {
        return readStash(filename, new D2BitReader(filename));
    }

    public D2Stash readStash(String filename, D2BitReader bitReader) {
        if (filename == null || !filename.toLowerCase().endsWith(".d2x")) {
            throw new RuntimeException("Incorrect Stash file name");
        }
        File file = new File(filename);
        boolean iSC = file.getName().toLowerCase().startsWith("sc_");
        boolean iHC = file.getName().toLowerCase().startsWith("hc_");
        if (!iSC && !iHC) {
            iSC = true;
            iHC = true;
        }
        return new D2Stash(filename, checkHeaderAndLoadItems(filename, bitReader), iSC, iHC, bitReader.isNewFile());
    }

    private ArrayList<D2Item> checkHeaderAndLoadItems(String filename, D2BitReader bitReader) {
        if (!bitReader.isNewFile()) {
            bitReader.set_byte_pos(0);
            byte[] startingBytes = bitReader.get_bytes(3);
            String lStart = new String(startingBytes);
            if (!"D2X".equals(lStart)) throw new RuntimeException("Incorrect Stash type: " + lStart);
            return readItems(filename, bitReader);
        } else {
            return new ArrayList<>();
        }
    }

    private ArrayList<D2Item> readItems(String filename, D2BitReader bitReader) {
        bitReader.set_byte_pos(CHECKSUM_BYTE_OFFSET_START);
        long originalChecksum = bitReader.read(CHECKSUM_BYTE_LENGTH * 8);
        long calculatedChecksum = calculateChecksum(bitReader);
        if (originalChecksum == calculatedChecksum) {
            bitReader.set_byte_pos(3);
            long numItems = bitReader.read(16);
            long versionNumber = bitReader.read(16);
            if (versionNumber == CURRENT_FILE_VERSION) {
                return readItemBytes(filename, bitReader, numItems);
            } else {
                throw new RuntimeException("Stash Version Incorrect! Expected: " + CURRENT_FILE_VERSION + " Found: " + versionNumber);
            }
        } else {
            throw new RuntimeException("Checksum Incorrect! Expected: " + originalChecksum + " Found: " + calculatedChecksum);
        }
    }

    private ArrayList<D2Item> readItemBytes(String filename, D2BitReader bitReader, long numItems) {
        bitReader.set_byte_pos(HEADER_BYTE_LENGTH);
        ArrayList<D2Item> items = new ArrayList<>();
        for (int i = 0; i < numItems; i++) {
            D2Item lItem;
            try {
                lItem = new D2Item(filename, bitReader, FIXED_STASH_CHAR_LEVEL);
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
            items.add(lItem);
        }
        return items;
    }
}
