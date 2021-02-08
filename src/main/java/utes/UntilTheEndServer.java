package utes;

import com.comphenix.protocol.ProtocolLibrary;
import com.comphenix.protocol.ProtocolManager;
import me.clip.placeholderapi.PlaceholderAPI;
import net.milkbowl.vault.permission.Permission;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.bukkit.plugin.java.JavaPlugin;
import utes.actioncmd.ActionCommand;
import utes.bancmd.CommandBanner;
import utes.bugfix.BugFixer;
import utes.capablegui.CapableGui;
import utes.chunkrestore.ChunkRestore;
import utes.customexp.CustomExpMechenism;
import utes.deathchest.DeathChest;
import utes.easycmd.EasyCommand;
import utes.information.NoLoginQuitMessage;
import utes.information.TranslateMessage;
import utes.lift.IronBlockLift;
import utes.linkingdig.LinkingDig;
import utes.modelock.ModeLocking;
import utes.onlinetimes.OnlineTimes;
import utes.particle.ParticleOverHead;
import utes.particle.ParticleUnderFeet;
import utes.randomcredit.RandomCredits;
import utes.rtp.RandomTeleport;
import utes.scoreboard.ScoreBoard;
import utes.showoff.ShowOff;
import utes.timeoperate.QuickNight;
import utes.timeoperate.TimeSynchronization;
import utes.trueexplode.TrueExplode;
import utes.worldboarder.WorldBoarder;
import utes.xpfly.XPFly;

public class UntilTheEndServer extends JavaPlugin {
    public static ProtocolManager pm;
    public static Permission vaultPermission = null;
    private static UntilTheEndServer instance;

    public static UntilTheEndServer getInstance() {
        return instance;
    }

    public static String getPapi(Player player, String origin) {
        try {
            return PlaceholderAPI.setPlaceholders(player, origin);
        } catch (Exception e) {
            return origin;
        }
    }

    private static boolean initVault() {
        boolean hasNull = false;
        RegisteredServiceProvider<Permission> permissionProvider = Bukkit.getServicesManager()
                .getRegistration(Permission.class);
        if (permissionProvider != null) {
            if ((vaultPermission = permissionProvider.getProvider()) == null)
                hasNull = true;
        }
        return !hasNull;
    }

    @Override
    public void onEnable() {
        try {
            if (!initVault()) {
                UntilTheEndServer.getInstance().getLogger().severe("Vault未安装|无法加载随机抽取权限的功能.");
                return;
            }

            getLogger().info("正在启用核心功能插件UTES中...");
            instance = this;

            getLogger().info("正在启用收发包控制中...");
            pm = ProtocolLibrary.getProtocolManager();

            getLogger().info("正在注册指令中...");
            this.getCommand("utes").setExecutor(new UTESCommands());

            getLogger().info("正在启用随机传送功能中...");
            new RandomTeleport();

            getLogger().info("正在启用经验飞行功能中...");
            new XPFly();

            getLogger().info("正在启用计分板功能中...");
            new ScoreBoard();

            getLogger().info("正在启用铁块电梯功能中...");
            new IronBlockLift();

            getLogger().info("正在启用增加信息前缀功能中...");
            new TranslateMessage();

            getLogger().info("正在启用屏蔽进出信息功能中...");
            new NoLoginQuitMessage();

            getLogger().info("正在启用统计在线时间功能中...");
            new OnlineTimes();

            getLogger().info("正在启用粒子特效功能中...");
            new ParticleOverHead();
            new ParticleUnderFeet();

            getLogger().info("正在启用随机抽奖功能中...");
            new RandomCredits();

            getLogger().info("正在启用死亡物品存储箱功能中...");
            new DeathChest();

            getLogger().info("正在启用世界禁用指令功能中...");
            new CommandBanner();

            getLogger().info("正在启用修复bug功能中...");
            new BugFixer();

            getLogger().info("正在启用炫耀物品功能中...");
            new ShowOff();

            getLogger().info("正在启用快捷动作指令功能中...");
            new ActionCommand();

            getLogger().info("正在启用自定义升级经验功能中...");
            new CustomExpMechenism();

            getLogger().info("正在启用连锁挖矿功能中...");
            new LinkingDig();

            getLogger().info("正在启用更真实的爆炸功能中...");
            new TrueExplode();

            getLogger().info("正在启用便携容器功能中...");
            new CapableGui();

            getLogger().info("正在启用模式锁定功能中...");
            new ModeLocking();

            getLogger().info("正在启用区块重生功能中...");
            new ChunkRestore();

            getLogger().info("正在启用世界边界功能中...");
            new WorldBoarder();

//            getLogger().info("正在启用更棒的聊天功能中...");
//            new ChatBar();

            getLogger().info("正在启用指令简化功能中...");
            new EasyCommand();

            getLogger().info("正在启用同步时间功能中...");
            new TimeSynchronization();

            getLogger().info("正在启用快速睡眠功能中...");
            new QuickNight();

        } catch (Exception exception) {
            getLogger().info("哎呀这步好像出了些小问题呢！");
            exception.printStackTrace();
        }
    }

    @Override
    public void onDisable() {
        for (Player player : Bukkit.getOnlinePlayers()) {
            player.kickPlayer("服务器重载，请稍后再进");
        }
        ChunkRestore.save();
    }
}
