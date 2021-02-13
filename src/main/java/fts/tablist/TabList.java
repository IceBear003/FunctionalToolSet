package fts.tablist;

import fts.FunctionalToolSet;
import fts.NMSManager;
import org.bukkit.event.Listener;

public class TabList implements Listener {
    private static int lines;
    private static String head;
    private static String down;

    public static void initialize(FunctionalToolSet plugin) {

    }

    private static Class IChatBaseComponent;
    private static Class PacketPlayOutPlayerListHeaderFooter;

    static {
        IChatBaseComponent = NMSManager.getClass("IChatBaseComponent");
        PacketPlayOutPlayerListHeaderFooter = NMSManager.getClass("PacketPlayOutPlayerListHeaderFooter");
    }

    
}
