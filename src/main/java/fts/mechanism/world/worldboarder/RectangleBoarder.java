package fts.mechanism.world.worldboarder;

import org.bukkit.Location;

public class RectangleBoarder extends Boarder {
    public int x1, z1, x2, z2;

    public RectangleBoarder(boolean isTransparent, int x1, int z1, int x2, int z2) {
        super(isTransparent);
        this.x1 = Math.min(x1, x2);
        this.z1 = Math.min(z1, z2);
        this.x2 = Math.max(x1, x2);
        this.z2 = Math.max(z1, z2);
    }

    public boolean isOutOfWorld(Location loc) {
        int x = loc.getBlockX();
        int z = loc.getBlockZ();
        if (x1 <= x && x <= x2) {
            return z1 > z || z > z2;
        }
        return true;
    }
}