package gomule.gui.sharedStash;

import java.awt.Dimension;
import java.awt.GridLayout;
import java.text.DecimalFormat;
import java.util.function.Consumer;
import java.util.function.Supplier;

import javax.swing.JButton;
import javax.swing.JPanel;
import javax.swing.JTextField;

import gomule.d2i.D2SharedStash;
import gomule.gui.D2FileManager;
import randall.util.RandallPanel;

class SharedStashGoldTransferPanel extends JPanel {
    private final SharedStashPanel sharedStashPanel;

    public SharedStashGoldTransferPanel(SharedStashPanel sharedStashPanel) {
        super(new GridLayout(0, 1));
        this.sharedStashPanel = sharedStashPanel;

        Integer gold_current = this.sharedStashPanel.getSelectedStashPane().getGold();
        DecimalFormat df = new DecimalFormat("###,###,###");

        setSize(300, 100);
        setPreferredSize(new Dimension(300, 100));
        JTextField transferGoldAmount = new JTextField(df.format(gold_current));
        JButton transferGoldOut = new JButton("From Stash");
        transferGoldOut.addActionListener(pEvent -> transferGoldOut(getGoldAmount(transferGoldAmount)));
        JButton transferGoldIn = new JButton("To Stash");
        transferGoldIn.addActionListener(pEvent -> transferGoldIn(getGoldAmount(transferGoldAmount)));

        RandallPanel transferPanel = new RandallPanel(true);
        transferPanel.setBorder("Transfer");
        transferPanel.addToPanel(transferGoldIn, 0, 0, 1, RandallPanel.NONE);
        transferPanel.addToPanel(transferGoldAmount, 1, 0, 1, RandallPanel.HORIZONTAL);
        transferPanel.addToPanel(transferGoldOut, 2, 0, 1, RandallPanel.NONE);
        add(transferPanel);
    }

    private void transferGoldOut(int goldAmount) {
        transferGold(
                goldAmount,
                Integer.MAX_VALUE,
                this::getSelectedStashGoldValue,
                this::getBankGoldValue,
                this::updateSelectedStashGoldValue,
                this::updateBankGoldValue
        );
    }

    private void transferGoldIn(int goldAmount) {
        transferGold(
                goldAmount,
                2_500_000,
                this::getBankGoldValue,
                this::getSelectedStashGoldValue,
                this::updateBankGoldValue,
                this::updateSelectedStashGoldValue
        );
    }

    private void updateBankGoldValue(int gold) {
        D2FileManager.getInstance().getProject().setBankValue(gold);
    }

    private void updateSelectedStashGoldValue(int gold) {
        D2SharedStash sharedStash = sharedStashPanel.getSharedStash();
        sharedStash.replacePane(
            sharedStashPanel.getSelectedStashPaneIndex(),
            D2SharedStash.D2SharedStashPane.fromItems(sharedStashPanel.getSelectedStashPane().getItems(), gold, sharedStashPanel.getSelectedStashPane().getStashNum()));
        sharedStash.setModified(true);
    }

    private int getBankGoldValue() {
        return D2FileManager.getInstance().getProject().getBankValue();
    }

    private int getSelectedStashGoldValue() {
        return sharedStashPanel.getSelectedStashPane().getGold();
    }

    private void transferGold(int goldAmount, int maxGold, Supplier<Integer> sourceGold, Supplier<Integer> destinationGold, Consumer<Integer> updateSource, Consumer<Integer> updateDestination) {
        //System.err.println("g=" + goldAmount + "  mg=" + maxGold + " sg=" + destinationGold.get());
        if (goldAmount > sourceGold.get()) {
            goldAmount = sourceGold.get();
        }
        if (goldAmount + destinationGold.get() > maxGold) {
            goldAmount = maxGold - destinationGold.get();
        }

        int newSourceGoldValue = sourceGold.get() - goldAmount;
        int newDestinationGoldValue = destinationGold.get() + goldAmount;
        updateSource.accept(newSourceGoldValue);
        updateDestination.accept(newDestinationGoldValue);
    }

    public int getGoldAmount(JTextField transferGoldAmount) {
        try {
            String goldtext = transferGoldAmount.getText();
            String gold = "";
            for (int i = 0; i < goldtext.length(); i++) {
                char c = goldtext.charAt(i);
                if(c >= '0' && c <= '9') {
                    gold += c;
                }
            }

            return Integer.parseInt(gold);
        } catch (NumberFormatException e) {
            return 0;
        }
    }
}
