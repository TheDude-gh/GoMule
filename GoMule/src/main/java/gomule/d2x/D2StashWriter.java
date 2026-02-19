package gomule.d2x;

import gomule.item.D2Item;
import gomule.model.VersionController;
import gomule.model.VersionController.Variant;
import gomule.util.D2BitReader;

import java.io.File;

public class D2StashWriter {
    private final Variant variant;
    private final File file;
    public static final int CHECKSUM_BYTE_OFFSET_START = 9;
    public static final int CHECKSUM_BYTE_LENGTH = 4;
    public static final int HEADER_BYTE_LENGTH = 13;

    public D2StashWriter(Variant variant, File file) {
        this.variant = variant;
        this.file = file;
    }

    public D2StashWriter(Variant variant, String filename) {
        this(variant, new File(filename));
    }

    private void writeHeaderWithoutChecksum(D2Stash stash, D2BitReader bitWriter) {
        bitWriter.write('D', 8);
        bitWriter.write('2', 8);
        bitWriter.write('X', 8);
        bitWriter.write(stash.getNrItems(), 16);
        bitWriter.write(VersionController.Version.D2R3.getFileVersionIdentifier(), 16);
        bitWriter.write(variant.getStashIdentifier(), 16);
        bitWriter.skipBytes(4);
    }

    private void writeItemBytes(D2Stash stash, D2BitReader bitWriter) {
        for (D2Item d2Item : stash.getItemList()) {
            byte[] bytesToWrite = d2Item.get_bytes();
            bitWriter.setBytes(bitWriter.get_byte_pos(), bytesToWrite);
            bitWriter.set_byte_pos(bitWriter.get_byte_pos() + bytesToWrite.length);
        }
    }

    private void writeChecksum(D2BitReader bitWriter) {
        bitWriter.set_byte_pos(CHECKSUM_BYTE_OFFSET_START);
        bitWriter.write(calculateChecksum(bitWriter), CHECKSUM_BYTE_LENGTH * 8);
    }

    public static long calculateChecksum(D2BitReader bitWriter) {
        long lCheckSum = 0;
        int originalPos = bitWriter.get_pos();
        bitWriter.set_byte_pos(0);
        for (int i = 0; i < bitWriter.get_length(); i++) {
            long lByte = bitWriter.read(8);
            if (i >= CHECKSUM_BYTE_OFFSET_START && i < (CHECKSUM_BYTE_OFFSET_START + CHECKSUM_BYTE_LENGTH)) {
                lByte = 0;
            }

            long upshift = lCheckSum << 33 >>> 32;
            long add = lByte + ((lCheckSum >>> 31) == 1 ? 1 : 0);
            lCheckSum = upshift + add;
        }
        bitWriter.set_pos(originalPos);
        return lCheckSum;
    }

    public void write(D2Stash stash) {
        int itemByteLength = stash.getItemList().stream().map(it -> it.get_bytes().length).reduce(0, Integer::sum);
        D2BitReader bitWriter = new D2BitReader(new byte[HEADER_BYTE_LENGTH + itemByteLength]);
        writeHeaderWithoutChecksum(stash, bitWriter);
        writeItemBytes(stash, bitWriter);
        writeChecksum(bitWriter);
        bitWriter.save(file.getAbsolutePath());
    }
}



