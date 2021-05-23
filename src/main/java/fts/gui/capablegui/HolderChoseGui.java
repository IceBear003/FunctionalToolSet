package fts.gui.capablegui;

import fts.spi.FTSInvHolder;

public class HolderChoseGui implements FTSInvHolder {
    public static final HolderChoseGui INSTANCE = new HolderChoseGui();
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