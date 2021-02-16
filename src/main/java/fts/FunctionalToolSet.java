package fts;

import com.comphenix.protocol.ProtocolLibrary;
import com.comphenix.protocol.ProtocolManager;
import fts.actioncmd.ActionCommand;
import fts.bancmd.CommandBanner;
import fts.capablegui.CapableGui;
import fts.cardpoints.CardPointRewards;
import fts.chair.Chair;
import fts.chatbar.ChatBar;
import fts.checkplayer.CheckContainers;
import fts.checkplayer.CheckInventory;
import fts.customexp.CustomExpMechenism;
import fts.deathchest.DeathChest;
import fts.easycmd.EasyCommand;
import fts.information.NoLoginQuitMessage;
import fts.information.TranslateMessage;
import fts.joincmd.JoinCommand;
import fts.lift.IronBlockLift;
import fts.linkingdig.LinkingDig;
import fts.modelock.ModeLocking;
import fts.motd.MotdManager;
import fts.onlinetimes.OnlineTimes;
import fts.particle.ParticleOverHead;
import fts.particle.ParticleUnderFeet;
import fts.pluginmanage.PluginManager;
import fts.random.RandomGenerator;
import fts.randomcredit.RandomCredits;
import fts.rtp.RandomTeleport;
import fts.scoreboard.ScoreBoard;
import fts.showoff.ShowOff;
import fts.spi.ResourceUtils;
import fts.tablist.TabList;
import fts.timeoperate.QuickNight;
import fts.timeoperate.TimeSynchronization;
import fts.trueexplode.TrueExplode;
import fts.worldboarder.WorldBoarder;
import fts.xpfly.XPFly;
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
    private static FunctionalToolSet instance;
    public static ProtocolManager pm;
    public static Permission vaultPermission = null;

    private String latestVersion;
    private String versionUpdate;
    private boolean isLatest = true;

    public static boolean hasPapi;
    public static boolean hasPLib;
    public static boolean hasVault;
    public static File pluginFile;
    public static boolean isLoading;
    public static boolean haveReloaded = false;

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
            if (permissionProvider != null) {
                if ((vaultPermission = permissionProvider.getProvider()) == null) {
                    return false;
                }
            }
            return true;
        } catch (NoClassDefFoundError e) {
            return false;
        }
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
        if (hasPapi) {
            getLogger().info("正在注册PAPI变量中...");
            new PapiExpansion().register();
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
            CardPointRewards.initialize(this);
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
            //TODO
            getLogger().info("正在启用插件管理功能中...");
            PluginManager.initialize(this);
            getLogger().info("正在启用自定义MOTD功能中...");
            MotdManager.initialize(this);

            getLogger().info("正在启用随机数生成器功能中...");
            RandomGenerator.initialize(this);

            checkUpdate();

            isLoading = false;
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
        haveReloaded = true;
        getLogger().info("正在关闭FunctionalToolSet中...");
        getLogger().info("正在保存数据中...");
        for (Player player : Bukkit.getOnlinePlayers()) {
            player.closeInventory();
            CapableGui.save(player);
            CardPointRewards.saveYaml(player);
            CheckInventory.save(player);
            OnlineTimes.saveYaml(player);
            if (XPFly.isFlying(player)) {
                XPFly.cancelFly(player);
            }
        }
        if (hasPLib) {
            pm.removePacketListeners(this);
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
