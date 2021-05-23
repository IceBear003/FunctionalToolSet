package fts.gui.checkplayer;

import fts.spi.FTSInvHolder;

public class HolderCheckContainerGui implements FTSInvHolder {
    public static final HolderCheckContainerGui INSTANCE = new HolderCheckContainerGui();
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