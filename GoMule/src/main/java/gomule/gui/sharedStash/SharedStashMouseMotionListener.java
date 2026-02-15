package gomule.gui.sharedStash;

import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionAdapter;

import gomule.d2i.D2SharedStash;
import gomule.gui.D2ViewClipboard;
import static gomule.gui.sharedStash.SharedStashPanel.getAreaForXYCoord;
import static gomule.gui.sharedStash.SharedStashPanel.getColForXCoord;
import static gomule.gui.sharedStash.SharedStashPanel.getGridPointForItemCode;
import static gomule.gui.sharedStash.SharedStashPanel.getRowForYCoord;
import gomule.item.D2Item;
import gomule.item.D2ItemRenderer;

class SharedStashMouseMotionListener extends MouseMotionAdapter {
    private final SharedStashPanel sharedStashPanel;

    public SharedStashMouseMotionListener(SharedStashPanel sharedStashPanel) {
        this.sharedStashPanel = sharedStashPanel;
    }

    @Override
    public void mouseMoved(MouseEvent e) {
        if(sharedStashPanel.getSharedStash() == null) {
            return;
        }

        D2SharedStash.D2SharedStashPane stashPane = sharedStashPanel.getSelectedStashPane();
        int col, row;
        
        //grid stash
        if(stashPane.getStashNum() < 6) {
            col = getColForXCoord(e.getX());
            row = getRowForYCoord(e.getY());
            if (col < 0 || row < 0 || col > 9 || row > 9) {
                sharedStashPanel.setCursorNormal();
                return;
            }                   
        }
        //material stash
        else {
            String item_code = getAreaForXYCoord(e.getX(), e.getY());            
            int[] point = getGridPointForItemCode(item_code);
            col = point[0];
            row = point[1];
            if(row == -1) {
                sharedStashPanel.setCursorNormal();
                return;
            }
        }
        
        //D2SharedStash.D2SharedStashPane stashPane = sharedStashPanel.getSelectedStashPane();
        D2Item item = stashPane.getItemCovering(col, row);
        if (item != null) {
            sharedStashPanel.setCursorPickupItem();
            sharedStashPanel.setToolTipText(D2ItemRenderer.itemDumpHtml(item, false));
        } else {
            D2Item itemOnClipboard = D2ViewClipboard.getItem();
            boolean canDropItem = itemOnClipboard != null && stashPane.canDropItem(col, row, itemOnClipboard);
            if (itemOnClipboard != null && canDropItem) {
                sharedStashPanel.setCursorDropItem();
            } else {
                sharedStashPanel.setCursorNormal();
            }
            sharedStashPanel.setToolTipText(null);
        }
    }
}
