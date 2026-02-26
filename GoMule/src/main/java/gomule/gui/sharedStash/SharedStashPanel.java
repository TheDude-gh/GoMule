package gomule.gui.sharedStash;

import java.awt.BorderLayout;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.Transparency;
import java.util.ArrayList;
import static java.util.Collections.emptyList;
import java.util.LinkedHashMap;
import java.util.Map;

import javax.swing.JPanel;
import javax.swing.ToolTipManager;

import gomule.d2i.D2SharedStash;
import gomule.gui.D2FileManager;
import gomule.gui.D2ImageCache;
import gomule.item.D2Item;

public class SharedStashPanel extends JPanel {

    public static final int BG_WIDTH = 362;
    public static final int BG_HEIGHT = 470;
    private final D2FileManager fileManager;
    private final D2ViewSharedStash sharedStashView;
    private int selectedStashPaneIndex = 0;
    private Image background;

    private int pane_count = 0;
    private static LinkedHashMap<String, PointXY> material_grid;

    public SharedStashPanel(D2FileManager fileManager, D2ViewSharedStash sharedStashView) {
        this.InitMaterialGridMap();
        this.fileManager = fileManager;
        this.sharedStashView = sharedStashView;
        setLayout(new BorderLayout());
        setSize(BG_WIDTH, BG_HEIGHT);
        Dimension lSize = new Dimension(BG_WIDTH, BG_HEIGHT);
        setPreferredSize(lSize);
        addMouseListener(new SharedStashPanelMouseClickHandler(this));
        addMouseMotionListener(new SharedStashMouseMotionListener(this));
        setVisible(true);
        ToolTipManager.sharedInstance().setDismissDelay(40000);
        ToolTipManager.sharedInstance().setInitialDelay(300);
        build();
    }

    public void build() {
        D2SharedStash sharedStash = getSharedStash();
        int panesCount = 0;
        if(sharedStash != null) {
            panesCount = sharedStash.GetPanesCount();
        }

        //select image according to stash version (D2R or D2R ROW)
        String image = (panesCount == 3) ? "stash" + (selectedStashPaneIndex + 1) + ".jpg" : "stashw" + (selectedStashPaneIndex + 1) + ".jpg";

        Image lEmptyBackground = D2ImageCache.getImage(image);
        //System.err.println("Pane Sel " + selectedStashPaneIndex);
        background = fileManager.getGraphicsConfiguration().createCompatibleImage(BG_WIDTH, BG_HEIGHT, Transparency.BITMASK);
        Graphics2D lGraphics = (Graphics2D) background.getGraphics();
        lGraphics.drawImage(lEmptyBackground, 0, 0, this);
        if (getSharedStash() != null) placeItemsInView();
        repaint();
    }

    private void placeItemsInView() {
        D2SharedStash.D2SharedStashPane pane = getSelectedStashPane();
        pane.getItems().forEach(item -> {
            int x, y, stack_count = 0;
            if (item.get_location() != 0 && item.get_body_position() != 0 && item.get_panel() != 5) return;
            Image image = D2ImageCache.getDC6Image(item);
            int col = item.get_col();
            int row = item.get_row();
            //grid stash panel
            if(pane.getStashNum() < 6) {
                x = getXCoordForCol(col);
                y = getYCoordForRow(row);
            }
            //materials stash panel
            else {
                stack_count = item.GetStashCount();
                PointXY point = this.getCoordForItemCode(item.getItemCode());
                x = point.x;
                y = point.y;
                //System.err.println(item.getItemCode() + " w=" + image.getWidth(this) + " h=" + image.getHeight(this));
            }
            background.getGraphics().drawImage(image, x, y, this);
            //draw stack count after image
            if(stack_count > 0) {
                background.getGraphics().drawString(Integer.toString(stack_count), x + 1, y + image.getHeight(this) - 3);
            }
        });

        if (pane.getStashNum() < 6) {
            background.getGraphics().drawString(Long.toString(pane.getGold()), 155, 417);
        }
    }

    public static int getXCoordForCol(int col) {
        int diffx = (col / 2);
        return 29 + (col * 28) + ((diffx * 3) + ((col - diffx) * 2));
    }

    public static int getYCoordForRow(int row) {
        int diffy = (row / 2);
        return 75 + (row * 28) + ((diffy * 3) + ((row - diffy) * 2));
    }

    public static int getColForXCoord(int x) {
        if (x < 29) return -1;
        return ((2 * x) - 58) / 61;
    }

    public static int getRowForYCoord(int y) {
        if (y < 75) return -1;
        return ((2 * y) - 150) / 61;
    }

    public static String getAreaForXYCoord(int x, int y) {
        if (material_grid.isEmpty()) {
            InitMaterialGridMap();
        }

        for (Map.Entry<String, PointXY> en : material_grid.entrySet()) {
            PointXY point = en.getValue();

            if(x >= point.x && x <= point.x + 28 && y >= point.y && y < point.y + 28) {
                return en.getKey();
            }

        }
        return "";
    }

    public static void InitMaterialGridMap() {
        material_grid = new LinkedHashMap<>();

        material_grid.put("gcv", new PointXY(186, 81)); //Chipped Amethyst
        material_grid.put("gfv", new PointXY(186, 112)); //Flawed Amethyst
        material_grid.put("gsv", new PointXY(186, 141)); //Amethyst
        material_grid.put("gzv", new PointXY(186, 172)); //Flawless Amethyst
        material_grid.put("gpv", new PointXY(186, 203)); //Perfect Amethyst
        material_grid.put("gcy", new PointXY(149, 81)); //Chipped Topaz
        material_grid.put("gfy", new PointXY(149, 112)); //Flawed Topaz
        material_grid.put("gsy", new PointXY(149, 141)); //Topaz
        material_grid.put("gly", new PointXY(149, 172)); //Flawless Topaz
        material_grid.put("gpy", new PointXY(149, 203)); //Perfect Topaz
        material_grid.put("gcb", new PointXY(224, 81)); //Chipped Sapphire
        material_grid.put("gfb", new PointXY(224, 112)); //Flawed Sapphire
        material_grid.put("gsb", new PointXY(224, 141)); //Sapphire
        material_grid.put("glb", new PointXY(224, 172)); //Flawless Sapphire
        material_grid.put("gpb", new PointXY(224, 203)); //Perfect Sapphire
        material_grid.put("gcg", new PointXY(73, 81)); //Chipped Emerald
        material_grid.put("gfg", new PointXY(73, 112)); //Flawed Emerald
        material_grid.put("gsg", new PointXY(73, 141)); //Emerald
        material_grid.put("glg", new PointXY(73, 172)); //Flawless Emerald
        material_grid.put("gpg", new PointXY(73, 203)); //Perfect Emerald
        material_grid.put("gcr", new PointXY(111, 81)); //Chipped Ruby
        material_grid.put("gfr", new PointXY(111, 112)); //Flawed Ruby
        material_grid.put("gsr", new PointXY(111, 141)); //Ruby
        material_grid.put("glr", new PointXY(111, 172)); //Flawless Ruby
        material_grid.put("gpr", new PointXY(111, 203)); //Perfect Ruby
        material_grid.put("gcw", new PointXY(35, 81)); //Chipped Diamond
        material_grid.put("gfw", new PointXY(35, 112)); //Flawed Diamond
        material_grid.put("gsw", new PointXY(35, 141)); //Diamond
        material_grid.put("glw", new PointXY(35, 172)); //Flawless Diamond
        material_grid.put("gpw", new PointXY(35, 203)); //Perfect Diamond
        material_grid.put("skc", new PointXY(262, 81)); //Chipped Skull
        material_grid.put("skf", new PointXY(262, 112)); //Flawed Skull
        material_grid.put("sku", new PointXY(262, 141)); //Skull
        material_grid.put("skl", new PointXY(262, 172)); //Flawless Skull
        material_grid.put("skz", new PointXY(262, 203)); //Perfect Skull

        material_grid.put("rvs", new PointXY(262, 335)); //Rejuvenation Potion
        material_grid.put("rvl", new PointXY(294, 335)); //Full Rejuvenation Potion

        material_grid.put("r01", new PointXY(36, 236)); //El Rune
        material_grid.put("r02", new PointXY(68, 236)); //Eld Rune
        material_grid.put("r03", new PointXY(101, 236)); //Tir Rune
        material_grid.put("r04", new PointXY(133, 236)); //Nef Rune
        material_grid.put("r05", new PointXY(165, 236)); //Eth Rune
        material_grid.put("r06", new PointXY(197, 236)); //Ith Rune
        material_grid.put("r07", new PointXY(230, 236)); //Tal Rune
        material_grid.put("r08", new PointXY(262, 236)); //Ral Rune
        material_grid.put("r09", new PointXY(294, 236)); //Ort Rune
        material_grid.put("r10", new PointXY(36, 269)); //Thul Rune
        material_grid.put("r11", new PointXY(68, 269)); //Amn Rune
        material_grid.put("r12", new PointXY(101, 269)); //Sol Rune
        material_grid.put("r13", new PointXY(133, 269)); //Shael Rune
        material_grid.put("r14", new PointXY(165, 269)); //Dol Rune
        material_grid.put("r15", new PointXY(197, 269)); //Hel Rune
        material_grid.put("r16", new PointXY(230, 269)); //Io Rune
        material_grid.put("r17", new PointXY(262, 269)); //Lum Rune
        material_grid.put("r18", new PointXY(294, 269)); //Ko Rune
        material_grid.put("r19", new PointXY(36, 301)); //Fal Rune
        material_grid.put("r20", new PointXY(68, 301)); //Lem Rune
        material_grid.put("r21", new PointXY(101, 301)); //Pul Rune
        material_grid.put("r22", new PointXY(133, 301)); //Um Rune
        material_grid.put("r23", new PointXY(165, 301)); //Mal Rune
        material_grid.put("r24", new PointXY(197, 301)); //Ist Rune
        material_grid.put("r25", new PointXY(230, 301)); //Gul Rune
        material_grid.put("r26", new PointXY(262, 301)); //Vex Rune
        material_grid.put("r27", new PointXY(294, 301)); //Ohm Rune
        material_grid.put("r28", new PointXY(36, 332)); //Lo Rune
        material_grid.put("r29", new PointXY(68, 332)); //Sur Rune
        material_grid.put("r30", new PointXY(101, 332)); //Ber Rune
        material_grid.put("r31", new PointXY(133, 332)); //Jah Rune
        material_grid.put("r32", new PointXY(165, 332)); //Cham Rune
        material_grid.put("r33", new PointXY(197, 332)); //Zod Rune

        material_grid.put("pk1", new PointXY(37, 368)); //Key of Terror
        material_grid.put("pk2", new PointXY(70, 368)); //Key of Hate
        material_grid.put("pk3", new PointXY(102, 368)); //Key of Destruction
        material_grid.put("dhn", new PointXY(36, 430)); //Diablo"s Horn
        material_grid.put("bey", new PointXY(69, 430)); //Baal"s Eye
        material_grid.put("mbr", new PointXY(102, 430)); //Mephisto"s Brain

        material_grid.put("tes", new PointXY(302, 79)); //Twisted Essence of Suffering
        material_grid.put("ceh", new PointXY(302, 111)); //Charged Essense of Hatred
        material_grid.put("bet", new PointXY(302, 144)); //Burning Essence of Terror
        material_grid.put("fed", new PointXY(302, 177)); //Festering Essence of Destruction
        material_grid.put("toa", new PointXY(302, 204)); //Token of Absolution

        material_grid.put("ua1", new PointXY(162, 365)); //Uber Ancient Summon Material Act 1
        material_grid.put("ua2", new PointXY(195, 365)); //Uber Ancient Summon Material Act 2
        material_grid.put("ua3", new PointXY(228, 365)); //Uber Ancient Summon Material Act 3
        material_grid.put("ua4", new PointXY(261, 365)); //Uber Ancient Summon Material Act 4
        material_grid.put("ua5", new PointXY(294, 365)); //Uber Ancient Summon Material Act5
        material_grid.put("xa1", new PointXY(162, 429)); //Western Worldstone Shard
        material_grid.put("xa2", new PointXY(195, 429)); //Eastern Worldstone Shard
        material_grid.put("xa3", new PointXY(228, 429)); //Southern Worldstone Shard
        material_grid.put("xa4", new PointXY(261, 429)); //Deep Worldstone Shard
        material_grid.put("xa5", new PointXY(294, 429)); //Northern Worldstone Shard
    }

    public PointXY getCoordForItemCode(String item_code) {

        for (Map.Entry<String, PointXY> en : material_grid.entrySet()) {
            String key = en.getKey();
            if(key.equals(item_code)) {
                return en.getValue();
            }
        }
        return new PointXY(0, 0);

        /*switch(item_code) {
            case "gcv": return new PointXY(186, 81); //Chipped Amethyst
            case "gfv": return new PointXY(186, 112); //Flawed Amethyst
            case "gsv": return new PointXY(186, 141); //Amethyst
            case "gzv": return new PointXY(186, 172); //Flawless Amethyst
            case "gpv": return new PointXY(186, 203); //Perfect Amethyst
            case "gcy": return new PointXY(149, 81); //Chipped Topaz
            case "gfy": return new PointXY(149, 112); //Flawed Topaz
            case "gsy": return new PointXY(149, 141); //Topaz
            case "gly": return new PointXY(149, 172); //Flawless Topaz
            case "gpy": return new PointXY(149, 203); //Perfect Topaz
            case "gcb": return new PointXY(224, 81); //Chipped Sapphire
            case "gfb": return new PointXY(224, 112); //Flawed Sapphire
            case "gsb": return new PointXY(224, 141); //Sapphire
            case "glb": return new PointXY(224, 172); //Flawless Sapphire
            case "gpb": return new PointXY(224, 203); //Perfect Sapphire
            case "gcg": return new PointXY(73, 81); //Chipped Emerald
            case "gfg": return new PointXY(73, 112); //Flawed Emerald
            case "gsg": return new PointXY(73, 141); //Emerald
            case "glg": return new PointXY(73, 172); //Flawless Emerald
            case "gpg": return new PointXY(73, 203); //Perfect Emerald
            case "gcr": return new PointXY(111, 81); //Chipped Ruby
            case "gfr": return new PointXY(111, 112); //Flawed Ruby
            case "gsr": return new PointXY(111, 141); //Ruby
            case "glr": return new PointXY(111, 172); //Flawless Ruby
            case "gpr": return new PointXY(111, 203); //Perfect Ruby
            case "gcw": return new PointXY(35, 81); //Chipped Diamond
            case "gfw": return new PointXY(35, 112); //Flawed Diamond
            case "gsw": return new PointXY(35, 141); //Diamond
            case "glw": return new PointXY(35, 172); //Flawless Diamond
            case "gpw": return new PointXY(35, 203); //Perfect Diamond
            case "skc": return new PointXY(262, 81); //Chipped Skull
            case "skf": return new PointXY(262, 112); //Flawed Skull
            case "sku": return new PointXY(262, 141); //Skull
            case "skl": return new PointXY(262, 172); //Flawless Skull
            case "skz": return new PointXY(262, 203); //Perfect Skull

            case "rvs": return new PointXY(262, 335); //Rejuvenation Potion
            case "rvl": return new PointXY(294, 335); //Full Rejuvenation Potion

            case "r01": return new PointXY(36, 236); //El Rune
            case "r02": return new PointXY(68, 236); //Eld Rune
            case "r03": return new PointXY(101, 236); //Tir Rune
            case "r04": return new PointXY(133, 236); //Nef Rune
            case "r05": return new PointXY(165, 236); //Eth Rune
            case "r06": return new PointXY(197, 236); //Ith Rune
            case "r07": return new PointXY(230, 236); //Tal Rune
            case "r08": return new PointXY(262, 236); //Ral Rune
            case "r09": return new PointXY(294, 236); //Ort Rune
            case "r10": return new PointXY(36, 269); //Thul Rune
            case "r11": return new PointXY(68, 269); //Amn Rune
            case "r12": return new PointXY(101, 269); //Sol Rune
            case "r13": return new PointXY(133, 269); //Shael Rune
            case "r14": return new PointXY(165, 269); //Dol Rune
            case "r15": return new PointXY(197, 269); //Hel Rune
            case "r16": return new PointXY(230, 269); //Io Rune
            case "r17": return new PointXY(262, 269); //Lum Rune
            case "r18": return new PointXY(294, 269); //Ko Rune
            case "r19": return new PointXY(36, 301); //Fal Rune
            case "r20": return new PointXY(68, 301); //Lem Rune
            case "r21": return new PointXY(101, 301); //Pul Rune
            case "r22": return new PointXY(133, 301); //Um Rune
            case "r23": return new PointXY(165, 301); //Mal Rune
            case "r24": return new PointXY(197, 301); //Ist Rune
            case "r25": return new PointXY(230, 301); //Gul Rune
            case "r26": return new PointXY(262, 301); //Vex Rune
            case "r27": return new PointXY(294, 301); //Ohm Rune
            case "r28": return new PointXY(36, 332); //Lo Rune
            case "r29": return new PointXY(68, 332); //Sur Rune
            case "r30": return new PointXY(101, 332); //Ber Rune
            case "r31": return new PointXY(133, 332); //Jah Rune
            case "r32": return new PointXY(165, 332); //Cham Rune
            case "r33": return new PointXY(197, 332); //Zod Rune

            case "pk1": return new PointXY(37, 368); //Key of Terror
            case "pk2": return new PointXY(70, 368); //Key of Hate
            case "pk3": return new PointXY(102, 368); //Key of Destruction
            case "dhn": return new PointXY(36, 430); //Diablo"s Horn
            case "bey": return new PointXY(69, 430); //Baal"s Eye
            case "mbr": return new PointXY(102, 430); //Mephisto"s Brain

            case "tes": return new PointXY(302, 79); //Twisted Essence of Suffering
            case "ceh": return new PointXY(302, 111); //Charged Essense of Hatred
            case "bet": return new PointXY(302, 144); //Burning Essence of Terror
            case "fed": return new PointXY(302, 177); //Festering Essence of Destruction
            case "toa": return new PointXY(302, 204); //Token of Absolution

            case "ua1": return new PointXY(162, 365); //Uber Ancient Summon Material Act 1
            case "ua2": return new PointXY(195, 365); //Uber Ancient Summon Material Act 2
            case "ua3": return new PointXY(228, 365); //Uber Ancient Summon Material Act 3
            case "ua4": return new PointXY(261, 365); //Uber Ancient Summon Material Act 4
            case "ua5": return new PointXY(294, 365); //Uber Ancient Summon Material Act5
            case "xa1": return new PointXY(162, 429); //Western Worldstone Shard
            case "xa2": return new PointXY(195, 429); //Eastern Worldstone Shard
            case "xa3": return new PointXY(228, 429); //Southern Worldstone Shard
            case "xa4": return new PointXY(261, 429); //Deep Worldstone Shard
            case "xa5": return new PointXY(294, 429); //Northern Worldstone Shard
        }
        return new PointXY(0, 0);*/
    }

    public static int[] getGridPointForItemCode(String item_code) {
        switch(item_code) {
            //case "A": return int[2](1, 2;
            case "gcv": return new int[] {0, 4}; //Chipped Amethyst
            case "gfv": return new int[] {1, 4}; //Flawed Amethyst
            case "gsv": return new int[] {2, 4}; //Amethyst
            case "gzv": return new int[] {3, 4}; //Flawless Amethyst
            case "gpv": return new int[] {4, 4}; //Perfect Amethyst
            case "gcy": return new int[] {0, 3}; //Chipped Topaz
            case "gfy": return new int[] {1, 3}; //Flawed Topaz
            case "gsy": return new int[] {2, 3}; //Topaz
            case "gly": return new int[] {3, 3}; //Flawless Topaz
            case "gpy": return new int[] {4, 3}; //Perfect Topaz
            case "gcb": return new int[] {0, 5}; //Chipped Sapphire
            case "gfb": return new int[] {1, 5}; //Flawed Sapphire
            case "gsb": return new int[] {2, 5}; //Sapphire
            case "glb": return new int[] {3, 5}; //Flawless Sapphire
            case "gpb": return new int[] {4, 5}; //Perfect Sapphire
            case "gcg": return new int[] {0, 1}; //Chipped Emerald
            case "gfg": return new int[] {1, 1}; //Flawed Emerald
            case "gsg": return new int[] {2, 1}; //Emerald
            case "glg": return new int[] {3, 1}; //Flawless Emerald
            case "gpg": return new int[] {4, 1}; //Perfect Emerald
            case "gcr": return new int[] {0, 2}; //Chipped Ruby
            case "gfr": return new int[] {1, 2}; //Flawed Ruby
            case "gsr": return new int[] {2, 2}; //Ruby
            case "glr": return new int[] {3, 2}; //Flawless Ruby
            case "gpr": return new int[] {4, 2}; //Perfect Ruby
            case "gcw": return new int[] {0, 0}; //Chipped Diamond
            case "gfw": return new int[] {1, 0}; //Flawed Diamond
            case "gsw": return new int[] {2, 0}; //Diamond
            case "glw": return new int[] {3, 0}; //Flawless Diamond
            case "gpw": return new int[] {4, 0}; //Perfect Diamond
            case "skc": return new int[] {0, 6}; //Chipped Skull
            case "skf": return new int[] {1, 6}; //Flawed Skull
            case "sku": return new int[] {2, 6}; //Skull
            case "skl": return new int[] {3, 6}; //Flawless Skull
            case "skz": return new int[] {4, 6}; //Perfect Skull

            case "rvs": return new int[] {8, 7}; //Rejuvenation Potion
            case "rvl": return new int[] {8, 8}; //Full Rejuvenation Potion

            case "r01": return new int[] {5, 0}; //El Rune
            case "r02": return new int[] {5, 1}; //Eld Rune
            case "r03": return new int[] {5, 2}; //Tir Rune
            case "r04": return new int[] {5, 3}; //Nef Rune
            case "r05": return new int[] {5, 4}; //Eth Rune
            case "r06": return new int[] {5, 5}; //Ith Rune
            case "r07": return new int[] {5, 6}; //Tal Rune
            case "r08": return new int[] {5, 7}; //Ral Rune
            case "r09": return new int[] {5, 8}; //Ort Rune
            case "r10": return new int[] {6, 0}; //Thul Rune
            case "r11": return new int[] {6, 1}; //Amn Rune
            case "r12": return new int[] {6, 2}; //Sol Rune
            case "r13": return new int[] {6, 3}; //Shael Rune
            case "r14": return new int[] {6, 4}; //Dol Rune
            case "r15": return new int[] {6, 5}; //Hel Rune
            case "r16": return new int[] {6, 6}; //Io Rune
            case "r17": return new int[] {6, 7}; //Lum Rune
            case "r18": return new int[] {6, 8}; //Ko Rune
            case "r19": return new int[] {7, 0}; //Fal Rune
            case "r20": return new int[] {7, 1}; //Lem Rune
            case "r21": return new int[] {7, 2}; //Pul Rune
            case "r22": return new int[] {7, 3}; //Um Rune
            case "r23": return new int[] {7, 4}; //Mal Rune
            case "r24": return new int[] {7, 5}; //Ist Rune
            case "r25": return new int[] {7, 6}; //Gul Rune
            case "r26": return new int[] {7, 7}; //Vex Rune
            case "r27": return new int[] {7, 8}; //Ohm Rune
            case "r28": return new int[] {8, 0}; //Lo Rune
            case "r29": return new int[] {8, 1}; //Sur Rune
            case "r30": return new int[] {8, 2}; //Ber Rune
            case "r31": return new int[] {8, 3}; //Jah Rune
            case "r32": return new int[] {8, 4}; //Cham Rune
            case "r33": return new int[] {8, 5}; //Zod Rune

            case "pk1": return new int[] {9, 0}; //Key of Terror
            case "pk2": return new int[] {9, 1}; //Key of Hate
            case "pk3": return new int[] {9, 2}; //Key of Destruction
            case "dhn": return new int[] {10, 0}; //Diablo"s Horn
            case "bey": return new int[] {10, 1}; //Baal"s Eye
            case "mbr": return new int[] {10, 2}; //Mephisto"s Brain

            case "tes": return new int[] {0, 8}; //Twisted Essence of Suffering
            case "ceh": return new int[] {1, 8}; //Charged Essense of Hatred
            case "bet": return new int[] {2, 8}; //Burning Essence of Terror
            case "fed": return new int[] {3, 8}; //Festering Essence of Destruction
            case "toa": return new int[] {4, 8}; //Token of Absolution

            case "ua1": return new int[] {9, 4}; //Uber Ancient Summon Material Act 1
            case "ua2": return new int[] {9, 5}; //Uber Ancient Summon Material Act 2
            case "ua3": return new int[] {9, 6}; //Uber Ancient Summon Material Act 3
            case "ua4": return new int[] {9, 7}; //Uber Ancient Summon Material Act 4
            case "ua5": return new int[] {9, 8}; //Uber Ancient Summon Material Act5
            case "xa1": return new int[] {10, 4}; //Western Worldstone Shard
            case "xa2": return new int[] {10, 5}; //Eastern Worldstone Shard
            case "xa3": return new int[] {10, 6}; //Southern Worldstone Shard
            case "xa4": return new int[] {10, 7}; //Deep Worldstone Shard
            case "xa5": return new int[] {10, 8}; //Northern Worldstone Shard
        }
        return new int[] {-1, -1};
    }

    @Override
    public void paint(Graphics pGraphics) {
        super.paint(pGraphics);
        Graphics2D lGraphics = (Graphics2D) pGraphics;
        lGraphics.drawImage(background, 0, 0, this);
    }

    public void setCursorPickupItem() {
        setCursor(new Cursor(Cursor.HAND_CURSOR));
    }

    public void setCursorDropItem() {
        setCursor(new Cursor(Cursor.CROSSHAIR_CURSOR));
    }

    public void setCursorNormal() {
        setCursor(new Cursor(Cursor.DEFAULT_CURSOR));
    }

    public java.util.List<D2Item> removeAllItems() {
        D2SharedStash sharedStash = getSharedStash();
        if (sharedStash == null) return emptyList();
        D2SharedStash.D2SharedStashPane stashPane = getSelectedStashPane();
        sharedStash.replacePane(selectedStashPaneIndex, D2SharedStash.D2SharedStashPane.fromItems(emptyList(), stashPane.getGold(), stashPane.getStashNum()));
        sharedStash.setModified(true);
        return stashPane.getItems();
    }

    public java.util.List<D2Item> tryToAddItems(java.util.List<D2Item> items) {
        D2SharedStash sharedStash = getSharedStash();
        if (sharedStash == null) return emptyList();
        D2SharedStash.D2SharedStashPane stashPane = getSelectedStashPane();
        java.util.List<D2Item> successfullyAddedItems = new ArrayList<>();
        for (D2Item item : items) {
            stashPane = getD2SharedStashPane(stashPane, successfullyAddedItems, item);
        }
        sharedStash.replacePane(selectedStashPaneIndex, stashPane);
        sharedStash.setModified(true);
        return successfullyAddedItems;
    }

    public D2SharedStash.D2SharedStashPane getSelectedStashPane() {
        return getSharedStash().getPane(selectedStashPaneIndex);
    }

    public D2SharedStash getSharedStash() {
        return sharedStashView.getSharedStash();
    }

    public int getSelectedStashPaneIndex() {
        return selectedStashPaneIndex;
    }

    public void setSelectedStashPaneIndex(int selectedStashPaneIndex) {
        this.selectedStashPaneIndex = selectedStashPaneIndex;
    }

    private D2SharedStash.D2SharedStashPane getD2SharedStashPane(D2SharedStash.D2SharedStashPane stashPane, java.util.List<D2Item> successfullyAddedItems, D2Item item) {
        for (int i = 0; i < 10; i++) {
            for (int j = 0; j < 10; j++) {
                if (stashPane.canDropItem(j, i, item)) {
                    stashPane = stashPane.addItem(j, i, item);
                    successfullyAddedItems.add(item);
                    return stashPane;
                }
            }
        }
        return stashPane;
    }

    public static class PointXY {
        public int x;
        public int y;

        public PointXY(int x, int y) {
            this.x = x;
            this.y = y;
        }
    }

}


