package utes;

import com.comphenix.protocol.ProtocolLibrary;
import com.comphenix.protocol.ProtocolManager;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import utes.actioncmd.ActionCommand;
import utes.bancmd.CommandBanner;
import utes.bugfix.BugFixer;
import utes.capablegui.CapableGui;
import utes.customexp.CustomExpMechenism;
import utes.deathchest.DeathChest;
import utes.information.NoLoginQuitMessage;
import utes.information.TranslateMessage;
import utes.lift.IronBlockLift;
import utes.linkingdig.LinkingDig;
import utes.onlinetimes.OnlineTimes;
import utes.particle.ParticleOverHead;
import utes.particle.ParticleUnderFeet;
import utes.randomcredit.RandomCredits;
import utes.rtp.RandomTeleport;
import utes.scoreboard.ScoreBoard;
import utes.showoff.ShowOff;
import utes.trueexplode.TrueExplode;
import utes.xpfly.XPFly;

public class UntilTheEndServer extends JavaPlugin {
    public static ProtocolManager pm;
    private static UntilTheEndServer instance;

    public static UntilTheEndServer getInstance() {
        return instance;
    }

    @Override
    public void onEnable() {
        try {
            System.out.println("[UntilTheEndServer] 正在启用核心功能插件UTES中...");
            instance = this;

            System.out.println("[UntilTheEndServer] 正在启用收发包控制中...");
            pm = ProtocolLibrary.getProtocolManager();

            System.out.println("[UntilTheEndServer] 正在注册指令中...");
            this.getCommand("utes").setExecutor(new UTESCommands());

            System.out.println("[UntilTheEndServer] 正在启用随机传送功能中...");
            new RandomTeleport();

            System.out.println("[UntilTheEndServer] 正在启用经验飞行功能中...");
            new XPFly();

            System.out.println("[UntilTheEndServer] 正在启用计分板功能中...");
            new ScoreBoard();

            System.out.println("[UntilTheEndServer] 正在启用铁块电梯功能中...");
            new IronBlockLift();

            System.out.println("[UntilTheEndServer] 正在启用增加信息前缀功能中...");
            new TranslateMessage();

            System.out.println("[UntilTheEndServer] 正在启用屏蔽进出信息功能中...");
            new NoLoginQuitMessage();

            System.out.println("[UntilTheEndServer] 正在启用统计在线时间功能中...");
            new OnlineTimes();

            System.out.println("[UntilTheEndServer] 正在启用粒子特效功能中...");
            new ParticleOverHead();
            new ParticleUnderFeet();

            System.out.println("[UntilTheEndServer] 正在启用随机抽奖功能中...");
            new RandomCredits();

            System.out.println("[UntilTheEndServer] 正在启用死亡物品存储箱功能中...");
            new DeathChest();

            System.out.println("[UntilTheEndServer] 正在启用世界禁用指令功能中...");
            new CommandBanner();

            System.out.println("[UntilTheEndServer] 正在启用修复bug功能中...");
            new BugFixer();

            System.out.println("[UntilTheEndServer] 正在启用炫耀物品功能中...");
            new ShowOff();

            System.out.println("[UntilTheEndServer] 正在启用快捷动作指令功能中...");
            new ActionCommand();

            System.out.println("[UntilTheEndServer] 正在启用自定义升级经验功能中...");
            new CustomExpMechenism();

            System.out.println("[UntilTheEndServer] 正在启用连锁挖矿功能中...");
            new LinkingDig();

            System.out.println("[UntilTheEndServer] 正在启用更真实的爆炸功能中...");
            new TrueExplode();

            System.out.println("[UntilTheEndServer] 正在启用便携容器功能中...");
            new CapableGui();

        } catch (Exception exception) {
            System.out.println("[UntilTheEndServer] 哎呀这步好像出了些小问题呢！");
            exception.printStackTrace();
        }
    }

    @Override
    public void onDisable() {
        for (Player player : Bukkit.getOnlinePlayers()) {
            player.kickPlayer("服务器重载，请稍后再进");
        }
    }
}
