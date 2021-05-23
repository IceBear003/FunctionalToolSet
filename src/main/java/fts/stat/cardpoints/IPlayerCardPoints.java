package fts.stat.cardpoints;

import java.util.List;

public class IPlayerCardPoints {
    public final List<String> received;
    public int points;

    public IPlayerCardPoints(int points, List<String> received) {
        this.points = points;
        this.received = received;
    }
}
