package fts.gui.customrecipes.stat;

import org.bukkit.inventory.ItemStack;

import java.util.HashMap;

public class Recipe {
    public ItemStack result;
    public HashMap<Integer, ItemStack> materials;
    public double money;
    public double exp;
    public double percent;
    public ItemStack specialResult;
    public double specialPercent;

    public Recipe(ItemStack result, HashMap<Integer, ItemStack> materials, double money, double exp, double percent, ItemStack specialResult, double specialPercent) {
        this.result = result;
        this.materials = materials;
        this.money = money;
        this.exp = exp;
        this.percent = percent;
        this.specialResult = specialResult;
        this.specialPercent = specialPercent;
    }
}
