package fts.gui.customrecipes.stat;

import java.util.HashMap;

public class IPlayer {
    public HashMap<Integer, Integer> recipeCache;

    public IPlayer(HashMap<Integer, Integer> recipeCache) {
        this.recipeCache = recipeCache;
    }
}
