package fts;

import fts.capablegui.CapableGui;
import fts.cardpoints.CardPointRewards;
import fts.checkplayer.CheckContainers;
import fts.checkplayer.CheckInventory;
import fts.chunkrestore.ChunkRestore;
import fts.particle.ParticleOverHead;
import fts.particle.ParticleUnderFeet;
import fts.randomcredit.RandomCredits;
import fts.rtp.RandomTeleport;
import fts.scoreboard.ScoreBoard;
import fts.superjump.SuperJump;
import fts.xpfly.XPFly;
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

import java.util.Collection;

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
                sender.sendMessage("指令参数选择错误");
            }
            return true;
        } catch (Exception exception) {
            if (exception instanceof IndexOutOfBoundsException) {
                sender.sendMessage("指令参数数量错误");
            } else if (exception instanceof NumberFormatException) {
                sender.sendMessage("请输入整数！");
            } else if (exception instanceof ClassCastException) {
                sender.sendMessage("控制台无法执行此指令！");
            } else {
                sender.sendMessage("指令运行时出现蜜汁错误，请将以下报错交给插件作者以便修复！");
                exception.printStackTrace();
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
                CardPointRewards.givePoints(sender, Bukkit.getPlayer(args[2]), Integer.parseInt(args[3]));
                break;
            case "set":
                CardPointRewards.setPoints(sender, Bukkit.getPlayer(args[2]), Integer.parseInt(args[3]));
                break;
            case "take":
                CardPointRewards.takePoints(sender, Bukkit.getPlayer(args[2]), Integer.parseInt(args[3]));
                break;
            case "check":
                CardPointRewards.checkPoints(sender, Bukkit.getPlayer(args[2]));
                break;
            case "get":
                CardPointRewards.getReward((Player) sender, args[2], (args[3].equalsIgnoreCase("TRUE")));
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
            CapableGui.addItemStack(player, loc, args[1]);
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
            CapableGui.addItemStack(player, villager, args[1]);
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
