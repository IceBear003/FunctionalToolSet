package fts.gui.customrecipes.stat;

import fts.FunctionalToolSet;
import fts.gui.customrecipes.gui.shared.RecipeSawer;
import org.bukkit.Bukkit;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.inventory.ItemStack;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.UUID;

public class FileManager implements Listener {
    public static FunctionalToolSet plugin;
    public static int tot = 0;
    public static HashMap<Integer, Recipe> recipes = new HashMap<>();

    public static HashMap<UUID, IPlayer> stats = new HashMap<>();

    public static void load(Player player) {
        File file = new File(plugin.getDataFolder() + "/customrecipes/stats/", player.getUniqueId() + ".yml");
        if (!file.exists()) {
            stats.put(player.getUniqueId(), new IPlayer(new HashMap<>()));
            return;
        }
        IPlayer stat = new IPlayer(new HashMap<>());
        YamlConfiguration yaml = YamlConfiguration.loadConfiguration(file);
        for (String path : yaml.getKeys(false)) {
            int times = yaml.getInt(path);
            stat.recipeCache.put(Integer.parseInt(path), times);
        }
        stats.put(player.getUniqueId(), stat);
    }

    public static void save(Player player) {
        File file = new File(plugin.getDataFolder() + "/customrecipes/stats/", player.getUniqueId() + ".yml");
        YamlConfiguration yaml = YamlConfiguration.loadConfiguration(file);
        IPlayer stat = stats.get(player.getUniqueId());
        if (stat == null) {
            load(player);
            save(player);
            return;
        }
        for (int id : stat.recipeCache.keySet()) {
            int times = stat.recipeCache.get(id);
            yaml.set(String.valueOf(id), times);
        }
        try {
            yaml.save(file);
        } catch (IOException e) {
        }
    }

    public static void initialize(FunctionalToolSet plugin) {
        FileManager.plugin = plugin;
        File directory = new File(plugin.getDataFolder() + "/customrecipes/recipes/");
        if (!directory.exists()) {
            directory.mkdir();
        }
        if (directory.listFiles() == null) {
            return;
        }
        for (File file : directory.listFiles()) {
            if (file.isFile()) {
                String sub = file.getName().split(".ym")[0];
                tot = Integer.parseInt(sub);
                YamlConfiguration loader = YamlConfiguration.loadConfiguration(file);
                List<Integer> slots = loader.getIntegerList("slots");
                HashMap<Integer, ItemStack> materials = new HashMap<>();
                for (int slot : slots) {
                    materials.put(slot, loader.getItemStack(String.valueOf(slot)));
                }

                Recipe recipe = new Recipe(
                        loader.getItemStack("result"),
                        materials,
                        loader.getDouble("money"),
                        loader.getDouble("exp"),
                        loader.getDouble("percent"),
                        loader.getItemStack("specialResult"),
                        loader.getDouble("specialPercent"));

                recipes.put(tot, recipe);

                plugin.getLogger().info("配方" + sub + "加载成功！");
            }
        }
        Bukkit.getPluginManager().registerEvents(new FileManager(), plugin);
    }

    public static void addRecipe(Recipe recipe) {
        recipes.put(++tot, recipe);
        RecipeSawer.items.put(recipe.result, RecipeSawer.getCraftInv(tot, recipe));
        save(tot);
    }

    public static void changeMoney(int id, double value) {
        Recipe recipe = recipes.get(id);
        recipe.money = value;
        save(id);
    }

    public static void changeExp(int id, double value) {
        Recipe recipe = recipes.get(id);
        recipe.exp = value;
        save(id);
    }

    public static void changePercent(int id, double value) {
        Recipe recipe = recipes.get(id);
        recipe.percent = value;
        save(id);
    }

    public static void changeSpecialPercent(int id, double value) {
        Recipe recipe = recipes.get(id);
        recipe.specialPercent = value;
        save(id);
    }

    public static void changeSpecialResult(int id, ItemStack item) {
        Recipe recipe = recipes.get(id);
        recipe.specialResult = item;
        save(id);
    }

    public static void delRecipe(int id) {
        Recipe recipe = FileManager.recipes.get(id);
        RecipeSawer.items.remove(recipe.result);
        recipes.remove(id);
        File file = new File(plugin.getDataFolder() + "/customrecipes/recipes/", id + ".yml");
        file.delete();
    }

    public static void save(int id) {
        Recipe recipe = recipes.get(id);
        File file = new File(plugin.getDataFolder() + "/customrecipes/recipes/", id + ".yml");
        YamlConfiguration loader = YamlConfiguration.loadConfiguration(file);
        loader.set("result", recipe.result);
        ArrayList<Integer> slots = new ArrayList<>();
        for (int slot : recipe.materials.keySet()) {
            slots.add(slot);
            loader.set(String.valueOf(slot), recipe.materials.get(slot));
        }
        loader.set("slots", slots);
        loader.set("money", recipe.money);
        loader.set("exp", recipe.exp);
        loader.set("percent", recipe.percent);
        loader.set("specialResult", recipe.specialResult);
        loader.set("specialPercent", recipe.specialPercent);
        try {
            loader.save(file);
        } catch (IOException e) {
            plugin.getLogger().info("配方添加时出现错误！");
        }
        RecipeSawer.updateInvs();
    }

    @EventHandler
    public void onJoin(PlayerJoinEvent event) {
        load(event.getPlayer());
    }

    @EventHandler
    public void onQuit(PlayerJoinEvent event) {
        save(event.getPlayer());
        stats.remove(event.getPlayer().getUniqueId());
    }
}
