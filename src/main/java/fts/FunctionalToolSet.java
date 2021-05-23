package fts;

import com.comphenix.protocol.ProtocolLibrary;
import com.comphenix.protocol.ProtocolManager;
import fts.cmd.actioncmd.ActionCommand;
import fts.cmd.bancmd.CommandBanner;
import fts.cmd.easycmd.EasyCommand;
import fts.cmd.joincmd.JoinCommand;
import fts.cmd.randomcredit.RandomCredits;
import fts.gui.capablegui.CapableGui;
import fts.gui.capablegui.CapableGuiIO;
import fts.gui.checkplayer.CheckContainers;
import fts.gui.checkplayer.CheckInventory;
import fts.gui.customrecipes.CustomRecipes;
import fts.info.chatbar.ChatBar;
import fts.info.modify.NoLoginQuitMessage;
import fts.info.modify.TranslateMessage;
import fts.info.motd.MotdManager;
import fts.info.scoreboard.ScoreBoard;
import fts.info.showoff.ShowOff;
import fts.info.tablist.TabList;
import fts.mechanism.player.chair.Chair;
import fts.mechanism.player.customexp.CustomLevelExp;
import fts.mechanism.player.deathchest.DeathChest;
import fts.mechanism.player.freecam.FreeCam;
import fts.mechanism.player.particle.ParticleOverHead;
import fts.mechanism.player.particle.ParticleUnderFeet;
import fts.mechanism.player.xpfly.XPFly;
import fts.mechanism.player.xplimit.ExpLimit;
import fts.mechanism.world.lift.IronBlockLift;
import fts.mechanism.world.linkingdig.LinkingDig;
import fts.mechanism.world.modelock.ModeLocking;
import fts.mechanism.world.rtp.RandomTeleport;
import fts.mechanism.world.timeoperate.QuickNight;
import fts.mechanism.world.timeoperate.TimeSynchronization;
import fts.mechanism.world.trueexplode.TrueExplode;
import fts.mechanism.world.worldboarder.WorldBoarder;
import fts.spi.ResourceUtils;
import fts.stat.cardpoints.CardPoints;
import fts.stat.cardpoints.CardPointsIO;
import fts.stat.iprecorder.IPRecorder;
import fts.stat.onlinetimes.OnlineTimes;
import fts.stat.pluginmanage.PluginManager;
import me.clip.placeholderapi.PlaceholderAPI;
import net.milkbowl.vault.economy.Economy;
import net.milkbowl.vault.permission.Permission;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;

import java.io.BufferedReader;
import java.io.File;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.SocketTimeoutException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.logging.Level;

/**
 * @author HamsterYDS
 */
public class FunctionalToolSet extends JavaPlugin {
    public static ProtocolManager pm;
    public static Economy vaultEconomy = null;
    public static Permission vaultPermission = null;
    public static boolean hasPapi;
    public static boolean hasPLib;
    public static boolean hasVault;
    public static File pluginFile;
    public static boolean isLoading;
    public static boolean haveReloaded = false;
    private static FunctionalToolSet instance;
    private String latestVersion;
    private String versionUpdate;
    private boolean isLatest = true;

    public static FunctionalToolSet getInstance() {
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
        try {
            RegisteredServiceProvider<Permission> permissionProvider = Bukkit.getServicesManager()
                    .getRegistration(Permission.class);
            RegisteredServiceProvider<Economy> economyProvider = Bukkit.getServicesManager().getRegistration(Economy.class);
            if (permissionProvider != null) {
                if ((vaultPermission = permissionProvider.getProvider()) == null) {
                    return false;
                }
                if ((vaultEconomy = economyProvider.getProvider()) == null) {
                    return false;
                }
            }
            return true;
        } catch (NoClassDefFoundError e) {
            return false;
        }
    }

    public static String getLatestVersion() {
        HttpURLConnection connection = null;
        try {
            int timeout = 5000;
            URL url = new URL("https://raw.githubusercontent.com/HamsterYDS/FunctionalToolSet/master/FTSVersion.txt");
            connection = (HttpURLConnection) url.openConnection();
            connection.setConnectTimeout(timeout);
            final StringBuilder buffer = new StringBuilder(255);
            try (BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream(), StandardCharsets.UTF_8))) {
                char[] buffer0 = new char[255];
                while (true) {
                    int length = reader.read(buffer0);
                    if (length == -1) {
                        break;
                    }
                    buffer.append(buffer0, 0, length);
                }
            } catch (SocketTimeoutException exception) {
                getInstance().getLogger().info("访问最新版本时网路超时！");
            }
            return buffer.toString().trim();
        } catch (Exception exception) {
            instance.getLogger().log(Level.WARNING, "获取版本信息失败！");
        } finally {
            if (connection != null) {
                connection.disconnect();
            }
        }
        return null;
    }

    public static String getUpdateInfo() {
        HttpURLConnection connection = null;
        try {
            int timeout = 5000;
            URL url = new URL("https://raw.githubusercontent.com/HamsterYDS/FunctionalToolSet/master/VersionUpdate.txt");
            connection = (HttpURLConnection) url.openConnection();
            connection.setConnectTimeout(timeout);
            final StringBuilder buffer = new StringBuilder(255);
            try (BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream(), StandardCharsets.UTF_8))) {
                char[] buffer0 = new char[255];
                while (true) {
                    int length = reader.read(buffer0);
                    if (length == -1) {
                        break;
                    }
                    buffer.append(buffer0, 0, length);
                }
            } catch (SocketTimeoutException exception) {
                getInstance().getLogger().info("访问最新版本时网路超时！");
            }
            return buffer.toString().trim();
        } catch (Exception exception) {
            instance.getLogger().log(Level.WARNING, "获取版本信息失败！");
        } finally {
            if (connection != null) {
                connection.disconnect();
            }
        }
        return null;
    }

    public void initDepends() {
        hasVault = initVault();
        if (!hasVault) {
            getLogger().info("Vault未安装|无法加载随机抽取权限的功能.");
        }
        hasPapi = initPapi();
        if (!hasPapi) {
            getLogger().info("PAPI未安装|无法使用插件变量.");
        }
        hasPLib = initPLib();
        if (!hasPLib) {
            getLogger().info("PLib未安装|无法使用发包相关功能");
        }
    }

    @Override
    public void onEnable() {
        isLoading = true;
        instance = this;
        pluginFile = this.getFile();
        getLogger().info("正在启用基础功能插件UTES中...");
        ResourceUtils.initialize(this);
        getLogger().info("您使用的语言是：" + getConfig().getString("language"));
        try {
            if (!haveReloaded) {
                initDepends();
            }
            getLogger().info("正在注册指令中...");
            this.getCommand("fts").setExecutor(new FTSCommands());
            getLogger().info("正在启用随机传送功能中...");
            RandomTeleport.initialize(this);
            getLogger().info("正在启用经验飞行功能中...");
            XPFly.initialize(this);
            getLogger().info("正在启用赛季积分功能中...");
            CardPoints.initialize(this);
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
            CustomLevelExp.initialize(this);
            getLogger().info("正在启用连锁挖矿功能中...");
            LinkingDig.initialize(this);
            getLogger().info("正在启用更真实的爆炸功能中...");
            TrueExplode.initialize(this);
            getLogger().info("正在启用便携容器功能中...");
            CapableGui.initialize(this);
            getLogger().info("正在启用模式锁定功能中...");
            ModeLocking.initialize(this);
            getLogger().info("正在启用区块重生功能中...");
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
            getLogger().info("正在启用自定义玩家列表功能中...");
            TabList.initialize(this);
            getLogger().info("正在启用插件管理功能中...");
            PluginManager.initialize(this);
            getLogger().info("正在启用自定义MOTD功能中...");
            MotdManager.initialize(this);
            getLogger().info("正在启用灵魂侦查功能中...");
            FreeCam.initialize(this);
            getLogger().info("正在启用经验等级限制功能中...");
            ExpLimit.initialize(this);
            getLogger().info("正在启用自定义4*4合成功能中...");
            CustomRecipes.initialize(this);
            getLogger().info("正在启用IP记录功能中...");
            IPRecorder.initialize(this);

            if (!haveReloaded) {
                checkUpdate();
            }
            haveReloaded = true;
            isLoading = false;

            if (hasPapi) {
                new PapiExpansion().register();
            }

        } catch (Exception exception) {
            getLogger().info("哎呀这步好像出了些小问题呢！");
            exception.printStackTrace();
            isLoading = false;
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
        getLogger().info("正在关闭FunctionalToolSet中...");
        getLogger().info("正在保存数据中...");
        PlaceholderAPI.unregisterPlaceholderHook("fts");
        for (Player player : Bukkit.getOnlinePlayers()) {
            player.closeInventory();
            CapableGuiIO.save(player);
            CardPointsIO.save(player);
            CheckInventory.save(player);
            OnlineTimes.save(player);
            if (XPFly.isFlying(player)) {
                XPFly.cancelFly(player);
            }
        }
        if (hasPLib) {
            pm.removePacketListeners(this);
        }
    }

    private void checkUpdate() {
        String version = getDescription().getVersion().toLowerCase();
        new BukkitRunnable() {
            public void run() {
                getLogger().info("正在检查版本更新中...");
                getLogger().info("您服务器目前使用的FTS版本为：V" + version);
                latestVersion = getLatestVersion();
                if (latestVersion == null) {
                    return;
                }
                if (latestVersion.equalsIgnoreCase(getDescription().getVersion())) {
                    getLogger().info("您服务器使用的FTS已经是最新版！");
                } else {
                    isLatest = false;
                    versionUpdate = getUpdateInfo();
                    getLogger().info("您使用的FTS是旧版，可能存在bug或功能缺失，请尽快更新到新版！");
                    getLogger().info("\n新版更新内容：\n" + versionUpdate);
                    Bukkit.getOnlinePlayers().forEach(this::sendUpdate);
                    Bukkit.getPluginManager().registerEvents(new Listener() {
                        @EventHandler()
                        public void onPlayerJoin(PlayerJoinEvent event) {
                            sendUpdate(event.getPlayer());
                        }
                    }, FunctionalToolSet.instance);
                }
            }

            private void sendUpdate(Player player) {
                if (player.hasPermission("fts.update")) {
                    player.sendMessage("服务器使用的FTS是旧版，可能存在bug或功能缺失，请尽快更新到新版！");
                    player.sendMessage("新版更新内容：\n" + versionUpdate);
                }
            }
        }.runTaskAsynchronously(this);
    }
}
