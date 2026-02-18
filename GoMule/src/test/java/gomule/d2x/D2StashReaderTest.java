package gomule.d2x;

import gomule.util.D2BitReader;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import randall.d2files.D2TxtFile;

import java.io.PrintWriter;
import java.io.StringWriter;

import static gomule.item.D2ItemTest.decode;
import static org.junit.jupiter.api.Assertions.*;

public class D2StashReaderTest {

    @BeforeAll
    public static void setup() {
        D2TxtFile.constructTxtFiles("./d2111");
    }

    @Test
    public void testBadFilenames() {
        assertEquals("Incorrect Stash file name", assertThrows(RuntimeException.class, () -> new D2StashReader().readStash(null)).getMessage());
        assertEquals("Incorrect Stash file name", assertThrows(RuntimeException.class, () -> new D2StashReader().readStash("bla.wrong")).getMessage());
    }

    @Test
    public void testNonD2XStash() {
        String stashBytes = "46325801006900640267071004A0080588144FB400";
        assertEquals("Incorrect Stash type: F2X", assertThrows(RuntimeException.class, () -> new D2StashReader().readStash("foo.d2x", new D2BitReader(decode(stashBytes)))).getMessage());
    }

    @Test
    public void testWrongVersionStash() {
        String stashBytes = "44325801006300640264071004A0080588144FB400";
        assertEquals("Stash Version Incorrect! Expected: 105 Found: 99", assertThrows(RuntimeException.class, () -> new D2StashReader().readStash("foo.d2x", new D2BitReader(decode(stashBytes)))).getMessage());
    }

    @Test
    public void testWrongChecksumStash() {
        String stashBytes = "44325801006900640267071004A0080588144DB400";
        assertEquals("Checksum Incorrect! Expected: 124191332 Found: 124191324", assertThrows(RuntimeException.class, () -> new D2StashReader().readStash("foo.d2x", new D2BitReader(decode(stashBytes)))).getMessage());
    }

    @Test
    public void testEmptyRead() {
        D2Stash d2Stash = new D2StashReader().readStash("foo.d2x");
        StringWriter actual = new StringWriter();
        d2Stash.fullDump(new PrintWriter(actual));
        Assertions.assertEquals("foo.d2x\n" +
                "\n" +
                "Finished: foo.d2x\n\n", actual.toString().replace("\r", ""));
    }

    @Test
    public void testSimpleRead() {
        D2Stash d2Stash = new D2StashReader().readStash("foo.d2x", new D2BitReader(decode("44325801006900640267071004A0080588144FB400")));
        assertTrue(d2Stash.isHC());
        assertTrue(d2Stash.isSC());
        StringWriter actual = new StringWriter();
        d2Stash.fullDump(new PrintWriter(actual));
        Assertions.assertEquals("foo.d2x\n" +
                "\n" +
                "\n" +
                "Super Healing Potion\n" +
                "Version: Resurrected\n" +
                "Replenish Life +320\n" +
                "Finished: foo.d2x\n\n", actual.toString().replace("\r", ""));
    }

    @Test
    public void testSCRead() {
        D2Stash d2Stash = new D2StashReader().readStash("sc_foo.d2x");
        assertFalse(d2Stash.isHC());
        assertTrue(d2Stash.isSC());
    }

    @Test
    public void testHCRead() {
        D2Stash d2Stash = new D2StashReader().readStash("hc_foo.d2x");
        assertTrue(d2Stash.isHC());
        assertFalse(d2Stash.isSC());
    }
}
