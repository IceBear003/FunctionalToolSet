package fts.gui.customrecipes;

import fts.FunctionalToolSet;
import fts.gui.customrecipes.gui.ops.RecipeAdder;
import fts.gui.customrecipes.gui.ops.RecipeEditor;
import fts.gui.customrecipes.gui.player.RecipeWorkbench;
import fts.gui.customrecipes.gui.shared.RecipeSawer;
import fts.gui.customrecipes.stat.FileManager;
import fts.spi.ResourceUtils;
import org.bukkit.Bukkit;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;

import java.io.File;

public class CustomRecipes {
    public static int successEx;

    public static void initialize(FunctionalToolSet plugin) {
        ResourceUtils.autoUpdateConfigs("customrecipes.yml");
        File file = new File(plugin.getDataFolder(), "customrecipes.yml");
        YamlConfiguration yaml = YamlConfiguration.loadConfiguration(file);
        successEx = yaml.getInt("raise");

        FileManager.initialize(plugin);
        RecipeEditor.initialize();
        Bukkit.getPluginManager().registerEvents(new RecipeAdder(), plugin);
        RecipeSawer.updateInvs();
        Bukkit.getPluginManager().registerEvents(new RecipeSawer(), plugin);
        Bukkit.getPluginManager().registerEvents(new RecipeWorkbench(), plugin);
        for (Player player : Bukkit.getOnlinePlayers()) {
            RecipeWorkbench.invs.put(player.getUniqueId(), RecipeWorkbench.initInv());
            FileManager.load(player);
        }
    }
}
