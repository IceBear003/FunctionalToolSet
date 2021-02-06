package utes;

import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import utes.cardpoints.CardPointRewards;
import utes.particle.ParticleOverHead;
import utes.particle.ParticleUnderFeet;
import utes.randomcredit.RandomCredits;
import utes.rtp.RandomTeleport;
import utes.scoreboard.ScoreBoard;
import utes.superjump.SuperJump;
import utes.xpfly.XPFly;

public class UTESCommands implements CommandExecutor {

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        try {
            boolean flag = goCommand(sender, cmd, label, args);
            if (!flag)
                sender.sendMessage("指令参数选择错误");
            return true;
        } catch (Exception exception) {
            if (exception instanceof IndexOutOfBoundsException)
                sender.sendMessage("指令参数数量错误");
            if (exception instanceof NumberFormatException)
                sender.sendMessage("请输入整数！");
            if (exception instanceof ClassCastException)
                sender.sendMessage("控制台无法执行此指令！");
            return true;
        }
    }

    public boolean goCommand(CommandSender sender, Command cmd, String label, String[] args) {
        String command = label;
        for (String arg : args)
            command += " " + arg;
        if (command.startsWith("utes rtp")) {
            RandomTeleport.initRTP((Player) sender);
        } else if (command.startsWith("utes xpfly")) {
            XPFly.initXPFly((Player) sender);
        } else if (command.startsWith("utes superjump")) {
            if (args[1].equalsIgnoreCase("off"))
                SuperJump.removeEffect((Player) sender);
            else
                SuperJump.addEffect((Player) sender, Integer.valueOf(args[1]));
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
            if (Math.random() <= 0.5)
                RandomCredits.goRandomPermission(sender, Bukkit.getPlayer(args[1]));
            else
                RandomCredits.goRandomCommand(sender, Bukkit.getPlayer(args[1]));
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
            sender.sendMessage("{ignore}§e----------------------------------------------------------");
        }
        return true;
    }
}
