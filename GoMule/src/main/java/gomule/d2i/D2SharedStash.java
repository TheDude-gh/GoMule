package gomule.d2i;

import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.stream.Collectors;

import gomule.gui.D2ItemListAdapter;
import static gomule.gui.sharedStash.SharedStashPanel.getGridPointForItemCode;
import gomule.item.D2Item;
import gomule.util.D2Backup;
import gomule.util.D2BitReader;
import gomule.util.D2Project;

public class D2SharedStash extends D2ItemListAdapter {
    private final List<D2SharedStashPane> panes;
    private final byte[] originalContent;
    private final D2SharedStashWriter sharedStashWriter;

    public D2SharedStash(String pFileName, List<D2SharedStashPane> panes, byte[] originalContent) {
        super(pFileName);
        this.panes = panes;
        this.originalContent = originalContent;
        this.sharedStashWriter = new D2SharedStashWriter(pFileName, originalContent);
    }

    public D2SharedStashPane getPane(int index) {
        return panes.get(index);
    }

    public List<D2SharedStashPane> getPanes() {
        return panes;
    }

    public int GetPanesCount() {
        return this.panes.size();
    }

    @Override
    public boolean containsItem(D2Item pItem) {
        return panes.stream().anyMatch(it -> it.items.contains(pItem));
    }

    @Override
    public void removeItem(D2Item pItem) {
        //Handled by panes
    }

    @Override
    public void addItem(D2Item pItem) {
        //Handled by panes
    }

    @Override
    public List<D2Item> getItemList() {
        return panes.stream().flatMap(it -> it.getItems().stream()).collect(Collectors.toList());
    }

    @Override
    public int getNrItems() {
        return panes.stream().map(it -> it.items.size()).reduce(0, Integer::sum);
    }

    @Override
    public String getFilename() {
        return iFileName;
    }

    @Override
    public boolean isSC() {
        return iFileName.toLowerCase(Locale.forLanguageTag("UTF-8")).contains("softcore");
    }

    @Override
    public boolean isHC() {
        return !isSC();
    }

    @Override
    public void fullDump(PrintWriter pWriter) {
        pWriter.println(iFileName);
        pWriter.println();
        List<D2Item> items = getItemList();
        for (D2Item item : items) {
            item.toWriter(pWriter);
        }
        pWriter.println("Finished: " + iFileName);
        pWriter.println();
    }

    @Override
    protected void saveInternal(D2Project d2Project) {
        if (d2Project != null) D2Backup.backup(d2Project, iFileName, new D2BitReader(originalContent.clone()));
        sharedStashWriter.write(this);
        setModified(false);
    }

    public void replacePane(int paneIndex, D2SharedStashPane newPane) {
        panes.set(paneIndex, newPane);
    }

    public static class D2SharedStashPane {
        private final List<D2Item> items;
        private final D2Item[][] paneGrid;
        private final int gold;
        private final int stash_num;

        D2SharedStashPane(List<D2Item> items, D2Item[][] paneGrid, int gold, int stash_num) {
            this.items = items;
            this.paneGrid = paneGrid;
            this.gold = gold;
            this.stash_num = stash_num;
        }

        public static D2SharedStashPane fromItems(List<D2Item> items, int gold, int stash_num) {
            return new D2SharedStashPane(items, constructPaneGrid(items, stash_num), gold, stash_num);
        }

        private static D2Item[][] constructPaneGrid(List<D2Item> items, int stash_num) {
            //D2Item[][] grid = new D2Item[10][10];
            D2Item[][] grid;
            
            if(stash_num >= 6) {                
                grid = new D2Item[11][9];
                for (D2Item item : items) {
                    int[] point = getGridPointForItemCode(item.getItemCode());
                    int x = point[0]; //row
                    int y = point[1]; //col
                    if(x == -1) continue;
                    grid[x][y] = item;
                }
                return grid;
            }

            grid = new D2Item[10][10];

            for (D2Item item : items) {
                for (int i = item.get_col(); i < (int) item.get_col() + (int) item.get_width(); i++) {
                    for (int j = item.get_row(); j < (int) item.get_row() + (int) item.get_height(); j++) {
                        if (grid[i][j] != null) throw new RuntimeException("Failed to create shared stash pane");
                        grid[i][j] = item;
                    }
                }
            }
            return grid;
        }

        public List<D2Item> getItems() {
            return items;
        }

        public int getGold() {
            return gold;
        }

        public int getStashNum() {
            return this.stash_num;
        }

        public D2Item getItemCovering(int col, int row) {
            return paneGrid[col][row];
        }

        public boolean canDropItem(int col, int row, D2Item item) {
            if (item.isQuestItem()) return false;
            if (col > paneGrid.length - 1 || col < 0 || row > paneGrid[0].length - 1 || row < 0) return false;
            for (int i = col; i < col + item.get_width(); i++) {
                for (int j = row; j < row + item.get_height(); j++) {
                    if (i > paneGrid.length - 1 || j > paneGrid[0].length - 1) return false;
                    if (paneGrid[i][j] != null) return false;
                }
            }
            return true;
        }

        @Override
        public String toString() {
            return "D2SharedStashPane{" +
                    "items=" + items +
                    ", paneGrid=" + Arrays.toString(paneGrid) +
                    '}';
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            D2SharedStashPane that = (D2SharedStashPane) o;
            return Objects.equals(items, that.items) && Arrays.deepEquals(paneGrid, that.paneGrid);
        }

        @Override
        public int hashCode() {
            int result = Objects.hash(items);
            result = 31 * result + Arrays.deepHashCode(paneGrid);
            return result;
        }

        public D2SharedStashPane addItem(int col, int row, D2Item item) {
            item.set_col((short) col);
            item.set_row((short) row);
            item.set_location((short) 0);
            item.set_body_position((short) 0);
            item.set_panel((short) 5);
            item.setCharLvl(75);
            List<D2Item> items = new ArrayList<>(this.items);
            items.add(item);
            return D2SharedStashPane.fromItems(items, gold, this.stash_num);
        }

        public D2SharedStashPane removeItem(D2Item item) {
            List<D2Item> items = new ArrayList<>(this.items);
            items.remove(item);
            return D2SharedStashPane.fromItems(items, gold, this.stash_num);
        }
    }

    static class Header {
        private final long version;
        private final int gold;
        private final long length;

        public Header(long version, int gold, long length) {
            this.version = version;
            this.gold = gold;
            this.length = length;
        }

        public static Header fromBytes(D2BitReader bitReader) {
            bitReader.skipBytes(8);
            long version = bitReader.read(8);
            bitReader.skipBytes(3);
            int gold = (int) bitReader.read(24);
            bitReader.skipBytes(1);
            long length = bitReader.read(32);
            return new D2SharedStash.Header(version, gold, length);
        }

        public long getVersion() {
            return version;
        }

        public int getGold() {
            return gold;
        }

        public long getLength() {
            return length;
        }

        @Override
        public String toString() {
            return "Header{" +
                    "version=" + version +
                    ", gold=" + gold +
                    ", length=" + length +
                    '}';
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            Header header = (Header) o;
            return version == header.version && gold == header.gold && length == header.length;
        }

        @Override
        public int hashCode() {
            return Objects.hash(version, gold, length);
        }
    }
}
