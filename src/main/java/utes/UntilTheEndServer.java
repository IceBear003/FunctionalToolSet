package utes;

import com.comphenix.protocol.ProtocolLibrary;
import com.comphenix.protocol.ProtocolManager;
import net.milkbowl.vault.permission.Permission;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.bukkit.plugin.java.JavaPlugin;
import utes.actioncmd.ActionCommand;
import utes.bancmd.CommandBanner;
import utes.capablegui.CapableGui;
import utes.chair.Chair;
import utes.chatbar.ChatBar;
import utes.checkplayer.CheckContainers;
import utes.checkplayer.CheckInventory;
import utes.chunkrestore.ChunkRestore;
import utes.customexp.CustomExpMechenism;
import utes.deathchest.DeathChest;
import utes.easycmd.EasyCommand;
import utes.information.NoLoginQuitMessage;
import utes.information.TranslateMessage;
import utes.joincmd.JoinCommand;
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

    private static boolean initPapi() {
        try {
            Class.forName("me.clip.placeholderapi.PlaceholderAPI");
            return true;
        } catch (ClassNotFoundException e) {
            return false;
        }
    }

    private static boolean initVault() {
        boolean hasNull = false;
        RegisteredServiceProvider<Permission> permissionProvider = Bukkit.getServicesManager()
                .getRegistration(Permission.class);
        if (permissionProvider != null) {
            if ((vaultPermission = permissionProvider.getProvider()) == null) {
                hasNull = true;
            }
        }
        return !hasNull;
    }

    @Override
    public void onEnable() {
        instance = this;
        getLogger().info("正在启用基础功能插件UTES中...");
        ResourceUtils.initialize(this);
        getLogger().info("您使用的语言是：" + getConfig().getString("language"));
        try {
            boolean hasVault = initVault();
            if (!hasVault) {
                getLogger().info("Vault未安装|无法加载随机抽取权限的功能.");
            }
            boolean hasPapi = initPapi();
            if (!hasPapi) {
                getLogger().info("PAPI未安装|无法使用插件变量.");
            }
            boolean hasPLib = initPLib();
            if (!hasPLib) {
                getLogger().info("PLib未安装|无法使用发包相关功能");
            }

            getLogger().info("正在注册指令中...");
            this.getCommand("utes").setExecutor(new UTESCommands());
            getLogger().info("正在启用随机传送功能中...");
            RandomTeleport.initialize(this);
            getLogger().info("正在启用经验飞行功能中...");
            XPFly.initialize(this);
            getLogger().info("正在启用计分板功能中...");
            ScoreBoard.initialize(this);
            getLogger().info("正在启用铁块电梯功能中...");
            IronBlockLift.initialize(this);
            if (hasPLib) {
                getLogger().info("正在启用增加信息前缀功能中...");
                TranslateMessage.initialize(this);
            }
            getLogger().info("正在启用屏蔽进出信息功能中...");
            NoLoginQuitMessage.initialize(this);
            getLogger().info("正在启用统计在线时间功能中...");
            OnlineTimes.initialize(this);
            getLogger().info("正在启用粒子特效功能中...");
            ParticleOverHead.initialize(this);
            ParticleUnderFeet.initialize(this);
            if (hasVault) {
                getLogger().info("正在启用随机抽奖功能中...");
                RandomCredits.initialize(this);
            }
            getLogger().info("正在启用死亡物品存储箱功能中...");
            DeathChest.initialize(this);
            getLogger().info("正在启用世界禁用指令功能中...");
            CommandBanner.initialize(this);
            if (hasPLib) {
                getLogger().info("正在启用炫耀物品功能中...");
                ShowOff.initialize(this);
            }
            getLogger().info("正在启用快捷动作指令功能中...");
            ActionCommand.initialize(this);
            getLogger().info("正在启用自定义升级经验功能中...");
            CustomExpMechenism.initialize(this);
            getLogger().info("正在启用连锁挖矿功能中...");
            LinkingDig.initialize(this);
            getLogger().info("正在启用更真实的爆炸功能中...");
            TrueExplode.initialize(this);
            getLogger().info("正在启用便携容器功能中...");
            CapableGui.initialize(this);
            getLogger().info("正在启用模式锁定功能中...");
            ModeLocking.initialize(this);
            getLogger().info("正在启用区块重生功能中...");
            ChunkRestore.initialize(this);
            getLogger().info("正在启用世界边界功能中...");
            WorldBoarder.initialize(this);
            if (hasPLib) {
                getLogger().info("正在启用更棒的聊天功能中...");
                ChatBar.initialize(this);
            }
            getLogger().info("正在启用指令简化功能中...");
            EasyCommand.initialize(this);
            getLogger().info("正在启用同步时间功能中...");
            TimeSynchronization.initialize(this);
            getLogger().info("正在启用快速睡眠功能中...");
            QuickNight.initialize(this);
            getLogger().info("正在启用进服操作功能中...");
            JoinCommand.initialize(this);
            getLogger().info("正在启用查询离线背包功能中...");
            getLogger().info("正在启用查询离线末影箱功能中...");
            CheckInventory.initialize(this);
            getLogger().info("正在启用椅子功能中...");
            Chair.initialize(this);
            getLogger().info("正在启用查询容器记录功能中...");
            CheckContainers.initialize(this);

        } catch (Exception exception) {
            getLogger().info("哎呀这步好像出了些小问题呢！");
            exception.printStackTrace();
        }
    }

    private boolean initPLib() {
        try {
            pm = ProtocolLibrary.getProtocolManager();
            return true;
        } catch (NoClassDefFoundError error) {
            return false;
        }
    }

    @Override
    public void onDisable() {
        for (Player player : Bukkit.getOnlinePlayers()) {
            player.kickPlayer("服务器重载，请稍后再进");
        }
        try {
            pm.removePacketListeners(this);
            ChunkRestore.save();
        } catch (Throwable ignored) {

        }
    }
}
