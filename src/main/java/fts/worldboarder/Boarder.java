package fts.worldboarder;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.util.Vector;

public class Boarder {
    public boolean isTransparent;

    public Boarder(boolean isTransparent) {
        this.isTransparent = isTransparent;
    }

    public Location getTheOtherSide(Location loc) {
        Location centre = new Location(loc.getWorld(), 0, loc.getY(), 0);
        Vector vector = new Vector(loc.getBlockX(), 0, loc.getBlockZ());
        vector.multiply(-1);
        Location result = centre.add(vector).clone();
        vector.multiply(-1).normalize();
        result.add(vector);
        result.add(vector);
        result.add(vector);

        result.setY(256);
        while (result.getBlock().getType() == Material.AIR) {
            result.add(0, -1, 0);
        }
        return result.add(0, 1, 0);
    }
}