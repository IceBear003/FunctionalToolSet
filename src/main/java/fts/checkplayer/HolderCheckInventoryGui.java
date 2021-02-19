package fts.checkplayer;

import fts.spi.FTSInvHolder;

public class HolderCheckInventoryGui implements FTSInvHolder {
    public static final HolderCheckInventoryGui INSTANCE = new HolderCheckInventoryGui();
    private String name;

    @Override
    public String getCustomName() {
        return name;
    }

    @Override
    public void setCustomName(String name) {
        this.name = name;
    }
}