package gomule.d2i;

import java.util.ArrayList;
import java.util.List;

import com.google.common.io.BaseEncoding;

import gomule.d2i.D2SharedStash.D2SharedStashPane;
import gomule.item.D2Item;
import gomule.util.D2BitReader;

public class D2SharedStashReader {

    boolean bIsTypeROW = false;

    static final byte[] STASH_HEADER_START = BaseEncoding.base16().decode("55AA55AA");

    public D2SharedStash readStash(String filename) throws Exception {
        return readStash(filename, new D2BitReader(filename));
    }

    public D2SharedStash readStash(String filename, D2BitReader bitReader) throws Exception {
        int[] stashHeaderOffsets = bitReader.findBytes(STASH_HEADER_START);
        
        /*
            D2R stash has 3 parts, each has gold (2.5M limit)
            D2R ROW stash has 7 parts: 
              first 5 parts is shared stash 10x10, only 1st part has gold (12.5M limit)
              6th part is material stash (gems, runes, materials)
              7th part is unknown, it has some items in in, but seem they are just cache of maybe last added items to 6th part
        */
        if (stashHeaderOffsets.length == 3) {
            this.bIsTypeROW = false;
        }
        else if(stashHeaderOffsets.length == 7) {
            this.bIsTypeROW = true;
        }
        else {
            throw new RuntimeException("Stash unsupported");
        }        
        
        int stash_num = 1;
        List<D2SharedStashPane> result = new ArrayList<>();
        for (int stashHeaderOffset : stashHeaderOffsets) {
            bitReader.set_byte_pos(stashHeaderOffset);
            result.add(readSharedStashPane(bitReader, filename, stash_num));
            System.err.println("Stash page " + stash_num);
            stash_num++;
        }
        return new D2SharedStash(filename, result, bitReader.getFileContent());
    }

    private D2SharedStashPane readSharedStashPane(D2BitReader bitReader, String filename, int stash_num) throws Exception {
        int stashPaneStart = bitReader.get_byte_pos();
        D2SharedStash.Header header = D2SharedStash.Header.fromBytes(bitReader);
        if (header.getVersion() != 105) {
            throw new RuntimeException("Incorrect shared stash version: " + header.getVersion());
        }        

        List<D2Item> result = new ArrayList<>();

        int jmpos = bitReader.findNextFlag("JM", bitReader.get_byte_pos());
        if(jmpos == -1) {
            return D2SharedStashPane.fromItems(result, header.getGold(), stash_num);
        }

        bitReader.set_byte_pos(jmpos);
        bitReader.skipBytes(2); //skip JM

        int numItems = (int) bitReader.read(16);
        for (int i = 0; i < numItems; i++) {
            result.add(new D2Item(filename, bitReader, 75));            
        }
        int calculatedLength = bitReader.get_byte_pos() - stashPaneStart;
        if (calculatedLength != header.getLength())
            throw new RuntimeException("Incorrect shared stash length: " + calculatedLength + " expected: " + header.getLength());
        return D2SharedStashPane.fromItems(result, header.getGold(), stash_num);
    }
}
