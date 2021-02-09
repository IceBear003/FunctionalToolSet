package utes;

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
import utes.capablegui.CapableGui;
import utes.cardpoints.CardPointRewards;
import utes.chunkrestore.ChunkRestore;
import utes.particle.ParticleOverHead;
import utes.particle.ParticleUnderFeet;
import utes.randomcredit.RandomCredits;
import utes.rtp.RandomTeleport;
import utes.scoreboard.ScoreBoard;
import utes.superjump.SuperJump;
import utes.xpfly.XPFly;

import java.util.Collection;

public class UTESCommands implements CommandExecutor {

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        try {
            boolean flag = goCommand(sender, cmd, label, args);
            if (!flag) {
                sender.sendMessage("指令参数选择错误");
            }
            return true;
        } catch (Exception exception) {
            if (exception instanceof IndexOutOfBoundsException) {
                sender.sendMessage("指令参数数量错误");
            }
            if (exception instanceof NumberFormatException) {
                sender.sendMessage("请输入整数！");
            }
            if (exception instanceof ClassCastException) {
                sender.sendMessage("控制台无法执行此指令！");
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

        if (command.startsWith("utes rtp")) {
            RandomTeleport.initRTP((Player) sender);
        } else if (command.startsWith("utes xpfly")) {
            XPFly.initXPFly((Player) sender);
        } else if (command.startsWith("utes superjump")) {
            if (args[1].equalsIgnoreCase("off")) {
                SuperJump.removeEffect((Player) sender);
            } else {
                SuperJump.addEffect((Player) sender, Integer.parseInt(args[1]));
            }
        } else if (command.startsWith("utes scoreboard")) {
            ScoreBoard.changeState((Player) sender);
        } else if (command.startsWith("utes cardpoints")) {
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
        } else if (command.startsWith("utes particle")) {
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
                break;
            default:
                return false;
            }
        } else if (command.startsWith("utes randomcredits")) {
            if (Math.random() <= 0.5) {
                RandomCredits.goRandomPermission(sender, Bukkit.getPlayer(args[1]));
            } else {
                RandomCredits.goRandomCommand(sender, Bukkit.getPlayer(args[1]));
            }
        } else if (command.startsWith("utes addgui")) {
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
        } else if (command.startsWith("utes addmerchant")) {
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
        } else if (command.startsWith("utes opengui")) {
            Player player = (Player) sender;
            CapableGui.openGui(player);
        } else if (command.startsWith("utes regenchunk")) {
            Player player = (Player) sender;
            ChunkRestore.regenChunk(player.getLocation().getChunk());
        } else if (command.startsWith("utes help")) {
            sender.sendMessage("{ignore}§e-----------§6§lUntilTheEndServer插件指令简介§e-----------");
            sender.sendMessage("{ignore}§a/utes rtp §e-随机传送");
            sender.sendMessage("{ignore}§a/utes xpfly §e-开关经验飞行");
            sender.sendMessage("{ignore}§a/utes superjump <等级/off> §e-开关超级跳");
            sender.sendMessage("{ignore}§a/utes scoreboard §e-开关计分板");
            sender.sendMessage("{ignore}§a/utes cardpoints <give/set/take/check> <玩家名> [数值] §e-操作玩家的积分点数");
            sender.sendMessage("{ignore}§a/utes cardpoints get <礼包名字> <TRUE/FALSE> §e-用已经得到的积分领取礼包(TRUE和FALSE表示是否双倍领取)");
            sender.sendMessage("{ignore}§a/utes particle <under/up/off> [粒子效果名称] §e-在头顶/脚下开启粒子效果，off代表关闭所有效果");
            sender.sendMessage("{ignore}§a/utes randomcredits <玩家名> §e-随机奖励权限和指令");
            sender.sendMessage("{ignore}§a/utes addgui <方块备注> §e-在便携容器中加入一个新的方块");
            sender.sendMessage("{ignore}§a/utes opengui §e-打开便携容器管理");
            sender.sendMessage("{ignore}§a/utes regenchunk §e-重新生成区块");
            sender.sendMessage("{ignore}§e----------------------------------------------------------");
        } else {
            sender.sendMessage("输入/utes help 查看帮助");
        }
        return true;
    }
}
