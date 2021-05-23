package fts;

import fts.cmd.randomcredit.RandomCredits;
import fts.gui.capablegui.CapableGui;
import fts.gui.checkplayer.CheckContainers;
import fts.gui.checkplayer.CheckInventory;
import fts.gui.customrecipes.gui.ops.RecipeEditor;
import fts.gui.customrecipes.gui.player.RecipeWorkbench;
import fts.gui.customrecipes.gui.shared.RecipeSawer;
import fts.gui.customrecipes.stat.FileManager;
import fts.info.scoreboard.ScoreBoard;
import fts.mechanism.player.freecam.FreeCam;
import fts.mechanism.player.particle.ParticleOverHead;
import fts.mechanism.player.particle.ParticleUnderFeet;
import fts.mechanism.player.superjump.SuperJump;
import fts.mechanism.player.xpfly.XPFly;
import fts.mechanism.player.xplimit.ExpLimit;
import fts.mechanism.world.chunkrestore.ChunkRestore;
import fts.mechanism.world.rtp.RandomTeleport;
import fts.stat.cardpoints.CardPoints;
import fts.stat.pluginmanage.PluginManager;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.entity.Villager;
import org.bukkit.event.HandlerList;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

//TODO LANGUAGE
public class FTSCommands implements CommandExecutor {

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        if (FunctionalToolSet.isLoading) {
            sender.sendMessage("插件重载中，无法使用指令！");
            return true;
        }
        try {
            boolean flag = goCommand(sender, cmd, label, args);
            if (!flag) {
                sender.sendMessage("指令参数选择错误！");
            }
            return true;
        } catch (Exception exception) {
            exception.printStackTrace();
            if (exception instanceof IndexOutOfBoundsException) {
                sender.sendMessage("指令参数数量错误！");
            } else if (exception instanceof NumberFormatException) {
                sender.sendMessage("请输入整数！");
            } else if (exception instanceof ClassCastException) {
                sender.sendMessage("控制台无法执行此指令！");
            } else {
                sender.sendMessage("指令运行时出现蜜汁错误！");
            }
            return true;
        }
    }

    public boolean goCommand(CommandSender sender, Command cmd, String label, String[] args) {
        StringBuilder commandBuilder = new StringBuilder(label);
        for (String arg : args) {
            commandBuilder.append(" ").append(arg);
        }
        String command = commandBuilder.toString();

        if (command.startsWith("fts rtp")) {
            RandomTeleport.initRTP((Player) sender);
        } else if (command.startsWith("fts xpfly")) {
            XPFly.initXPFly((Player) sender);
        } else if (command.startsWith("fts superjump")) {
            if (args[1].equalsIgnoreCase("off")) {
                SuperJump.removeEffect((Player) sender);
            } else {
                SuperJump.addEffect((Player) sender, Integer.parseInt(args[1]));
            }
        } else if (command.startsWith("fts scoreboard")) {
            ScoreBoard.changeState((Player) sender);
        } else if (command.startsWith("fts cardpoints")) {
            switch (args[1]) {
                case "give":
                    CardPoints.givePoints(sender, Bukkit.getPlayer(args[2]), Integer.parseInt(args[3]));
                    break;
                case "set":
                    CardPoints.setPoints(sender, Bukkit.getPlayer(args[2]), Integer.parseInt(args[3]));
                    break;
                case "take":
                    CardPoints.takePoints(sender, Bukkit.getPlayer(args[2]), Integer.parseInt(args[3]));
                    break;
                case "check":
                    CardPoints.checkPoints(sender, Bukkit.getPlayer(args[2]));
                    break;
                case "get":
                    CardPoints.getReward((Player) sender, args[2], (args[3].equalsIgnoreCase("TRUE")));
                    break;
                default:
                    return false;
            }
        } else if (command.startsWith("fts particle")) {
            switch (args[1]) {
                case "under":
                    ParticleUnderFeet.drawParticle((Player) sender, args[2]);
                    break;
                case "up":
                    ParticleOverHead.drawParticle((Player) sender, args[2]);
                    break;
                case "off":
                    ParticleOverHead.stop((Player) sender);
                    ParticleUnderFeet.stop((Player) sender);
                    sender.sendMessage("已经关闭所有粒子效果");
                    break;
                default:
                    return false;
            }
        } else if (command.startsWith("fts randomcredits")) {
            if (Math.random() <= 0.5) {
                RandomCredits.goRandomPermission(sender, Bukkit.getPlayer(args[1]));
            } else {
                RandomCredits.goRandomCommand(sender, Bukkit.getPlayer(args[1]));
            }
        } else if (command.startsWith("fts addgui")) {
            Player player = (Player) sender;
            Location loc = player.getEyeLocation();
            while (loc.getBlock().getType() == Material.AIR) {
                if (loc.distance(player.getEyeLocation()) >= 10.0) {
                    player.sendMessage("您没有看向一个方块!");
                    return true;
                }
                loc.add(loc.getDirection());
            }
            CapableGui.addGui(player, loc, args[1]);
        } else if (command.startsWith("fts addmerchant")) {
            Player player = (Player) sender;
            Location loc = player.getEyeLocation();
            Collection<Entity> entities = player.getWorld().getNearbyEntities(loc, 0.2, 0.2, 0.2);
            boolean flag = false;
            Villager villager = null;
            while (!flag) {
                if (loc.distance(player.getEyeLocation()) >= 10.0) {
                    player.sendMessage("您没有看向一个村民!");
                    return true;
                }
                for (Entity entity : entities) {
                    if (entity.getType() == EntityType.VILLAGER) {
                        flag = true;
                        villager = (Villager) entity;
                    }
                }
                loc.add(loc.getDirection());
                entities = player.getWorld().getNearbyEntities(loc, 0.2, 0.2, 0.2);
            }
            CapableGui.addGui(player, villager, args[1]);
        } else if (command.startsWith("fts opengui")) {
            Player player = (Player) sender;
            CapableGui.openGui(player);
        } else if (command.startsWith("fts regenchunk")) {
            Player player = (Player) sender;
            if (!player.hasPermission("fts.regenchunk")) {
                player.sendMessage("你没有权限重生成区块！");
                return true;
            }
            ChunkRestore.regenChunk(player.getLocation().getChunk());
        } else if (command.startsWith("fts checkinv")) {
            Player player = (Player) sender;
            if (!player.hasPermission("fts.checkinv")) {
                player.sendMessage("你没有权限查询玩家的背包！");
                return true;
            }
            Inventory inv = CheckInventory.getInv(Bukkit.getOfflinePlayer(args[1]));
            if (inv != null) {
                player.openInventory(inv);
            } else {
                player.sendMessage("玩家不存在");
            }
        } else if (command.startsWith("fts checkchest")) {
            Player player = (Player) sender;
            if (!player.hasPermission("fts.checkchest")) {
                player.sendMessage("你没有权限查询玩家的末影箱！");
                return true;
            }
            Inventory inv = CheckInventory.getEnderChest(Bukkit.getOfflinePlayer(args[1]));
            if (inv != null) {
                player.openInventory(inv);
            } else {
                player.sendMessage("玩家不存在");
            }
        } else if (command.startsWith("fts checkcontainer")) {
            Player player = (Player) sender;
            if (!player.hasPermission("fts.checkcontainer")) {
                player.sendMessage("你没有权限查询玩家的容器记录！");
                return true;
            }
            Inventory inv = CheckContainers.getContainers(Bukkit.getOfflinePlayer(args[1]), player);
            if (inv != null) {
                player.openInventory(inv);
            } else {
                player.sendMessage("玩家不存在");
            }
        } else if (command.startsWith("fts plugin")) {
            if (!sender.hasPermission("fts.plugin")) {
                sender.sendMessage("你没有权限管理插件！");
                return true;
            }
            if (args[2].equalsIgnoreCase("FunctionalToolSet")) {
                sender.sendMessage("本插件无法对自身进行载入/卸载/重载的操作！");
                return true;
            }
            switch (args[1]) {
                case "load":
                    PluginManager.load(sender, args[2], true);
                    break;
                case "reload":
                    PluginManager.reload(sender, args[2], true);
                    break;
                case "unload":
                    PluginManager.unload(sender, args[2], true);
                    break;
                default:
                    return false;
            }
        } else if (command.startsWith("fts freecam")) {
            FreeCam.goFreeCam(Bukkit.getPlayer(args[1]));
            return true;
        } else if (command.startsWith("fts xplimit")) {
            Player player = (Player) sender;
            ItemStack item = player.getItemInHand();
            if (item == null) {
                sender.sendMessage("§6[经验限制]§r 你手上必须持有物品！");
                return true;
            }
            ItemMeta meta = item.getItemMeta();
            if (meta == null) {
                sender.sendMessage("§6[经验限制]§r 你手上必须持有物品！");
                return true;
            }
            List<String> lore = meta.hasLore() ? meta.getLore() : new ArrayList<>();
            lore.add(ExpLimit.limitOrigin.replace("%exp%", args[1]));
            meta.setLore(lore);
            item.setItemMeta(meta);
            sender.sendMessage("§6[经验限制]§r 设置成功！");
            return true;
        } else if (command.startsWith("fts cr")) {
            if (args.length == 1) {
                sender.sendMessage("§6[合成系统]§r CustomRecipes模块指令：");
                sender.sendMessage("§6★ §r /fts cr help - 查看CustomRecipes模块指令帮助");
                sender.sendMessage("§6★ §r /fts cr editor - 打开配方管理控制面板");
                sender.sendMessage("§6★ §r /fts cr opengui - 打开合成面板");
                sender.sendMessage("§6★ §r /fts cr checkrecipes - 查询所有合成");
                sender.sendMessage("§6★ §r /fts cr delete <编号> - 删除某个配方");
                sender.sendMessage("§6★ §r /fts cr money <编号> <需要的金钱> - 设置配方合成需要的金钱");
                sender.sendMessage("§6★ §r /fts cr exp <编号> <需要的经验等级> - 设置配方合成需要的经验等级");
                sender.sendMessage("§6★ §r /fts cr percent <编号> <成功率> - 设置配方合成的成功率（0-100）");
                sender.sendMessage("§6★ §r /fts cr special <编号> <触发概率> - 设置指定编号的配方在合成时，触发特殊成品的几率（0-100）");
                sender.sendMessage("§6★ §r /fts cr addluckylore <成功率> - 设置手中物品用于合成时提高的成功率");
                return true;
            }
            if (args[1].equalsIgnoreCase("help")) {
                sender.sendMessage("§6[合成系统]§r CustomRecipes模块指令：");
                sender.sendMessage("§6★ §r /fts cr help - 查看CustomRecipes模块指令帮助");
                sender.sendMessage("§6★ §r /fts cr editor - 打开配方管理控制面板");
                sender.sendMessage("§6★ §r /fts cr opengui - 打开合成面板");
                sender.sendMessage("§6★ §r /fts cr checkrecipes - 查询所有合成");
                sender.sendMessage("§6★ §r /fts cr delete <编号> - 删除某个配方");
                sender.sendMessage("§6★ §r /fts cr money <编号> <需要的金钱> - 设置配方合成需要的金钱");
                sender.sendMessage("§6★ §r /fts cr exp <编号> <需要的经验等级> - 设置配方合成需要的经验等级");
                sender.sendMessage("§6★ §r /fts cr percent <编号> <成功率> - 设置配方合成的成功率（0-100）");
                sender.sendMessage("§6★ §r /fts cr special <编号> <触发概率> - 设置指定编号的配方在合成时，触发特殊成品的几率（0-100）");
                sender.sendMessage("§6★ §r /fts cr addluckylore <成功率> - 设置手中物品用于合成时提高的成功率");
                return true;
            }
            if (args[1].equalsIgnoreCase("opengui")) {
                ((Player) sender).openInventory(RecipeWorkbench.invs.get(((Player) sender).getUniqueId()));
                sender.sendMessage("§6[合成系统]§r 打开合成面板中...");
                return true;
            }
            if (args[1].equalsIgnoreCase("checkrecipes")) {
                ((Player) sender).openInventory(RecipeSawer.chooseInv);
                sender.sendMessage("§6[合成系统]§r 打开合成查询面板中...");
                return true;
            }

            if (!sender.isOp()) {
                sender.sendMessage("§6[合成系统]§r 你没有权限！");
                return true;
            }
            if (args[1].equalsIgnoreCase("editor")) {
                ((Player) sender).openInventory(RecipeEditor.inv);
                sender.sendMessage("§6[合成系统]§r 打开控制面板中...");
                return true;
            }
            if (args[1].equalsIgnoreCase("addluckylore")) {
                Player player = (Player) sender;
                ItemStack item = player.getInventory().getItemInMainHand();
                if (item == null) {
                    sender.sendMessage("§6[合成系统]§r 手上没有物品！");
                    return true;
                }
                ItemMeta meta = item.getItemMeta();
                List<String> lore = meta.getLore();
                if (lore == null) {
                    lore = new ArrayList<>();
                }
                lore.add("§6提升成功率:§r " + args[2] + "/100");
                meta.setLore(lore);
                item.setItemMeta(meta);
                player.getInventory().setItemInMainHand(item);
                sender.sendMessage("§6[合成系统]§r 添加提成lore成功。");
                return true;
            }

            if (!FileManager.recipes.containsKey(Integer.parseInt(args[2]))) {
                sender.sendMessage("§6[合成系统]§r 配方不存在！");
                return true;
            }
            if (args[1].equalsIgnoreCase("delete")) {
                FileManager.delRecipe(Integer.parseInt(args[2]));
                sender.sendMessage("§6[合成系统]§r 删除配方成功。");
                return true;
            }
            if (args[1].equalsIgnoreCase("money")) {
                FileManager.changeMoney(Integer.parseInt(args[2]), Integer.parseInt(args[3]));
                sender.sendMessage("§6[合成系统]§r 修改配置成功。");
                return true;
            }
            if (args[1].equalsIgnoreCase("exp")) {
                FileManager.changeExp(Integer.parseInt(args[2]), Integer.parseInt(args[3]));
                sender.sendMessage("§6[合成系统]§r 修改配置成功。");
                return true;
            }
            if (args[1].equalsIgnoreCase("percent")) {
                FileManager.changePercent(Integer.parseInt(args[2]), Integer.parseInt(args[3]));
                sender.sendMessage("§6[合成系统]§r 修改配置成功。");
                return true;
            }
            if (args[1].equalsIgnoreCase("special")) {
                Player player = (Player) sender;
                if (player.getInventory().getItemInMainHand() == null) {
                    sender.sendMessage("§6[合成系统]§r 手上必须拿着一个物品，作为特殊成品！");
                    return true;
                }
                FileManager.changeSpecialPercent(Integer.parseInt(args[2]), Integer.parseInt(args[3]));
                FileManager.changeSpecialResult(Integer.parseInt(args[2]), player.getInventory().getItemInMainHand());
                sender.sendMessage("§6[合成系统]§r 修改配置成功。");
                return true;
            }
            return true;
        } else if (command.startsWith("fts help")) {
            sender.sendMessage("{ignore}§e-----------§6§lFunctionalToolSet插件指令简介§e-----------");
            sender.sendMessage("{ignore}§a/fts rtp §e-随机传送");
            sender.sendMessage("{ignore}§a/fts xpfly §e-开关经验飞行");
            sender.sendMessage("{ignore}§a/fts superjump <等级/off> §e-开关超级跳");
            sender.sendMessage("{ignore}§a/fts scoreboard §e-开关计分板");
            sender.sendMessage("{ignore}§a/fts cardpoints <give/set/take/check> <玩家名> [数值] §e-操作玩家的积分点数");
            sender.sendMessage("{ignore}§a/fts cardpoints get <礼包名字> <TRUE/FALSE> §e-用已经得到的积分领取礼包(TRUE和FALSE表示是否双倍领取)");
            sender.sendMessage("{ignore}§a/fts particle <under/up/off> [粒子效果名称] §e-在头顶/脚下开启粒子效果，off代表关闭所有效果");
            sender.sendMessage("{ignore}§a/fts randomcredits <玩家名> §e-随机奖励权限和指令");
            sender.sendMessage("{ignore}§a/fts addgui <方块备注> §e-在便携容器中加入一个新的方块");
            sender.sendMessage("{ignore}§a/fts opengui §e-打开便携容器管理");
            sender.sendMessage("{ignore}§a/fts regenchunk §e-重新生成区块（仅1.12.x-）");
            sender.sendMessage("{ignore}§a/fts checkinv <玩家名> §e-查水表-查询一个玩家的背包");
            sender.sendMessage("{ignore}§a/fts checkchest <玩家名> §e-查水表-查询一个玩家的末影箱");
            sender.sendMessage("{ignore}§a/fts checkcontainer <玩家名> §e-查水表-查询一个玩家的容器打开记录");
            sender.sendMessage("{ignore}§a/fts freecam <玩家名> §e-灵魂侦查-开始侦查周围地形");
            sender.sendMessage("{ignore}§a/fts xplimit <等级> §e-等级限制-为手上的物品增加等级限制");
            sender.sendMessage("{ignore}§a/fts cr §e-自定义4*4合成-查看合成模块帮助");
            sender.sendMessage("{ignore}§a/fts plugin load/unload/reload <插件名> §e-插件管理-载入/重载/卸载插件");
            sender.sendMessage("{ignore}§a/fts reload §e-重新载入所有配置文件，但是对新老版本更新无用，请使用/reload");
            sender.sendMessage("{ignore}§e----------------------------------------------------------");
        } else if (command.startsWith("fts reload")) {
            if (!sender.hasPermission("fts.reload")) {
                sender.sendMessage("你没有权限重载FunctionalToolSet！");
                return true;
            }
            HandlerList.unregisterAll(FunctionalToolSet.getInstance());
            FunctionalToolSet.getInstance().onDisable();
            FunctionalToolSet.getInstance().onEnable();
            sender.sendMessage("FunctionalToolSet重载成功！");
        } else {
            sender.sendMessage("输入/fts help 查看帮助");
        }
        return true;
    }
}
