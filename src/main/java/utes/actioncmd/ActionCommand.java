package utes.actioncmd;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.*;
import org.bukkit.scheduler.BukkitRunnable;
import utes.ResourceUtils;
import utes.UntilTheEndServer;

import java.io.File;
import java.util.*;

public class ActionCommand implements Listener {
    private static final HashMap<String, IActions> standardActions = new HashMap<>();
    private static final HashMap<UUID, Actions> playerActions = new HashMap<>();
    private static final ArrayList<UUID> jumpers = new ArrayList<>();
    private static int judgeTime;
    private static HashSet<UUID> cd = new HashSet<>();

    public static void initialize(UntilTheEndServer plugin) {
        ResourceUtils.autoUpdateConfigs("actcmd.yml");
        File file = new File(plugin.getDataFolder(), "actcmd.yml");
        YamlConfiguration yaml = YamlConfiguration.loadConfiguration(file);
        if (!yaml.getBoolean("enable")) {
            return;
        }
        judgeTime = yaml.getInt("judgeTime");
        for (String path : yaml.getKeys(false)) {
            if (path.equalsIgnoreCase("enable") || path.equalsIgnoreCase("judgeTime")) {
                continue;
            }

            List<ActionType> types = new ArrayList<>();
            for (String toString : yaml.getStringList(path + ".actions")) {
                types.add(ActionType.valueOf(toString));
            }
            IActions actions = new IActions(types,
                    yaml.getIntegerList(path + ".values"),
                    yaml.getStringList(path + ".cmds"),
                    yaml.getStringList(path + ".messages"));
            standardActions.put(path, actions);
        }

        Bukkit.getPluginManager().registerEvents(new ActionCommand(), plugin);
    }

    private static void addCd(Player player) {
        cd.add(player.getUniqueId());
        new BukkitRunnable() {
            @Override
            public void run() {
                cd.remove(player.getUniqueId());
            }
        }.runTaskLater(UntilTheEndServer.getInstance(), 2L);
    }

    @EventHandler
    public void onJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();
        playerActions.put(player.getUniqueId(), new Actions(player.getUniqueId()));
    }

    @EventHandler
    public void onQuit(PlayerQuitEvent event) {
        Player player = event.getPlayer();
        playerActions.remove(player.getUniqueId());
    }

    @EventHandler
    public void onSneak(PlayerToggleSneakEvent event) {
        if (event.isCancelled()) {
            return;
        }
        Player player = event.getPlayer();
        if (!player.isSneaking() && !cd.contains(player.getUniqueId())) {
            addCd(player);
            playerActions.get(player.getUniqueId()).addAction(ActionType.SNEAK);
        }
    }

    @EventHandler
    public void onSwap(PlayerSwapHandItemsEvent event) {
        if (event.isCancelled()) {
            return;
        }
        Player player = event.getPlayer();
        if (!cd.contains(player.getUniqueId())) {
            addCd(player);
            playerActions.get(player.getUniqueId()).addAction(ActionType.SWAP);
        }
    }

    @EventHandler
    public void onInteract(PlayerInteractEvent event) {
        if (event.isCancelled()) {
            return;
        }
        Player player = event.getPlayer();
        if (cd.contains(player.getUniqueId())) {
            return;
        }
        addCd(player);
        if (event.getAction().toString().contains("LEFT")) {
            playerActions.get(player.getUniqueId()).addAction(ActionType.LEFTCLICK);
        }
        if (event.getAction().toString().contains("RIGHT")) {
            playerActions.get(player.getUniqueId()).addAction(ActionType.RIGHTCLICK);
        }
    }

    @EventHandler
    public void onInteractEntity(PlayerInteractAtEntityEvent event) {
        if (event.isCancelled()) {
            return;
        }
        Player player = event.getPlayer();
        playerActions.get(player.getUniqueId()).addAction(ActionType.INTERACTENTITY);
    }

    @EventHandler
    public void onMove(PlayerMoveEvent event) {
        if (event.isCancelled()) {
            return;
        }
        Location to = event.getTo();
        Location from = event.getFrom();
        Player player = event.getPlayer();

        if (player.isOnGround()) {
            jumpers.remove(player.getUniqueId());
        }
        if (!jumpers.contains(player.getUniqueId())) {
            if (to.getBlockX() == from.getBlockX() && to.getBlockZ() == from.getBlockZ() && to.getY() - from.getY() >= 0.3) {
                if (!player.isFlying() && !to.getBlock().isLiquid()) {
                    jumpers.add(player.getUniqueId());
                    playerActions.get(player.getUniqueId()).addAction(ActionType.JUMP);
                }
            }
        }

        float pitch = to.getPitch() - from.getPitch();
        if (pitch <= -40 && to.getPitch() <= -70) {
            playerActions.get(player.getUniqueId()).addAction(ActionType.UP);
        }
        if (pitch >= 40 && to.getPitch() >= 70) {
            playerActions.get(player.getUniqueId()).addAction(ActionType.DOWN);
        }
    }

    enum ActionType {
        SNEAK, SWAP, LEFTCLICK, RIGHTCLICK, INTERACTENTITY, JUMP, UP, DOWN
    }

    private static class Actions {
        private final UUID uuid;
        private final List<ActionType> types;
        private final List<Integer> values;
        private final List<Long> stamps;

        private boolean cd = true;

        private Actions(UUID uuid) {
            this.uuid = uuid;
            types = new ArrayList<>();
            values = new ArrayList<>();
            stamps = new ArrayList<>();
        }

        private void addAction(ActionType type) {
            long stamp = System.currentTimeMillis();
            gc();
            if (types.size() >= 1) {
                if (types.get(types.size() - 1) == type) {
                    values.set(types.size() - 1, values.get(types.size() - 1) + 1);
                    stamps.set(types.size() - 1, stamp);
                    judgeAction();
                    return;
                }
            }
            types.add(type);
            values.add(1);
            stamps.add(stamp);

            if (!cd) {
                return;
            }
            cd = true;

            judgeAction();
        }

        private void gc() {
            int size = types.size();
            for (int index = 0; index < size; index++) {
                long stamp = stamps.get(index);
                if (System.currentTimeMillis() - stamp > judgeTime * 2000) {
                    types.remove(index);
                    values.remove(index);
                    stamps.remove(index);
                    size--;
                }
            }
        }

        private void judgeAction() {

            for (String id : standardActions.keySet()) {
                IActions actions = standardActions.get(id);
                if (actions.types.size() > types.size()) {
                    continue;
                }

                boolean flag = true;
                for (int index = 0; index < actions.types.size(); index++) {
                    if (types.get(types.size() - actions.types.size() + index) != actions.types.get(index)) {
                        flag = false;
                        break;
                    }
                    if (values.get(types.size() - actions.types.size() + index) < actions.values.get(index)) {
                        flag = false;
                        break;
                    }
                }
                if (!flag) {
                    continue;
                } else {
                    int size = types.size();
                    for (int index = 0; index < actions.types.size(); index++) {
                        int index$ = size - index - 1;
                        types.remove(index$);
                        values.remove(index$);
                        stamps.remove(index$);
                    }
                    for (String cmd : actions.cmds) {
                        Bukkit.dispatchCommand(Bukkit.getConsoleSender(), cmd.replace("{player}", Bukkit.getPlayer(uuid).getName()));
                    }
                    for (String message : actions.messages) {
                        Bukkit.getPlayer(uuid).sendMessage(message);
                    }
                    break;
                }
            }
            new BukkitRunnable() {

                @Override
                public void run() {
                    cd = true;
                }
            }.runTaskLater(UntilTheEndServer.getInstance(), 60L);
        }
    }

    private static class IActions {
        private final List<ActionType> types;
        private final List<Integer> values;
        private final List<String> cmds;
        private final List<String> messages;

        public IActions(List<ActionType> types, List<Integer> values, List<String> cmds, List<String> messages) {
            this.types = types;
            this.values = values;
            this.cmds = cmds;
            this.messages = messages;
        }
    }
}
