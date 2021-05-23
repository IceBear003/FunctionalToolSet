package fts.mechanism.world.worldboarder;

import org.bukkit.Location;

public class RoundBoarder extends Boarder {
    public int radius;

    public RoundBoarder(boolean isTransparent, int radius) {
        super(isTransparent);
        this.radius = radius;
    }

    public boolean isOutOfWorld(Location loc) {
        return loc.distance(new Location(loc.getWorld(), 0, loc.getY(), 0)) <= radius;
    }
}
