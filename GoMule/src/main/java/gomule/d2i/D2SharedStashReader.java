package gomule.d2i;

import com.google.common.io.BaseEncoding;
import gomule.d2i.D2SharedStash.D2SharedStashPane;
import gomule.item.D2Item;
import gomule.model.VersionController;
import gomule.model.VersionController.Variant;
import gomule.util.D2BitReader;

import java.util.ArrayList;
import java.util.List;

import static gomule.model.VersionController.Version.D2R3;

public class D2SharedStashReader {

    static final byte[] STASH_HEADER_START = BaseEncoding.base16().decode("55AA55AA");

    public D2SharedStash readStash(Variant expectedVariant, String filename) throws Exception {
        return readStash(expectedVariant, filename, new D2BitReader(filename));
    }

    public D2SharedStash readStash(Variant expectedVariant, String filename, D2BitReader bitReader) throws Exception {
        List<D2SharedStashPane> result = new ArrayList<>();
        int[] stashHeaderOffsets = getStashHeaderOffsets(expectedVariant, bitReader);
        for (int i = 0; i < expectedVariant.getSharedStashConfig().getItemStashPaneCount(); i++) {
            bitReader.set_byte_pos(stashHeaderOffsets[i]);
            result.add(readSharedStashPane(bitReader, filename));
        }
        return new D2SharedStash(expectedVariant, filename, result, bitReader.getFileContent());
    }

    public static int[] getStashHeaderOffsets(Variant expectedVariant, D2BitReader bitReader) {
        int[] stashHeaderOffsets = bitReader.findBytes(STASH_HEADER_START);
        Variant variantOrNull = Variant.tryParseSharedStashPaneCount(stashHeaderOffsets.length);
        if (variantOrNull != expectedVariant)
            throw VersionController.VersionException.forVariant(expectedVariant, variantOrNull);
        return stashHeaderOffsets;
    }

    private D2SharedStashPane readSharedStashPane(D2BitReader bitReader, String filename) throws Exception {
        int stashPaneStart = bitReader.get_byte_pos();
        D2SharedStash.Header header = D2SharedStash.Header.fromBytes(bitReader);
        if (header.getVersion() != D2R3.getFileVersionIdentifier())
            throw VersionController.VersionException.forVersion(D2R3, VersionController.Version.tryParseFileVersionIdentifier((int) header.getVersion()));
        bitReader.set_byte_pos(bitReader.findNextFlag("JM", bitReader.get_byte_pos()));
        bitReader.skipBytes(2);
        int numItems = (int) bitReader.read(16);
        List<D2Item> result = new ArrayList<>();
        for (int i = 0; i < numItems; i++) {
            result.add(new D2Item(filename, bitReader, 75));
        }
        int calculatedLength = bitReader.get_byte_pos() - stashPaneStart;
        if (calculatedLength != header.getLength())
            throw new RuntimeException("Incorrect shared stash length: " + calculatedLength + " expected: " + header.getLength());
        return D2SharedStashPane.fromItems(result, header.getGold());
    }
}
