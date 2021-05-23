package fts.cmd.actioncmd;

import fts.FunctionalToolSet;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.*;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.UUID;

public class ActionListener implements Listener {
    private static final ArrayList<UUID> jumpers = new ArrayList<>();
    private static final HashSet<UUID> cd = new HashSet<>();

    private static void addCd(Player player) {
        cd.add(player.getUniqueId());
        new BukkitRunnable() {
            @Override
            public void run() {
                cd.remove(player.getUniqueId());
            }
        }.runTaskLater(FunctionalToolSet.getInstance(), 2L);
    }

    @EventHandler
    public void onSneak(PlayerToggleSneakEvent event) {
        if (!event.isCancelled()) {
            Player player = event.getPlayer();
            if (!player.isSneaking() && !cd.contains(player.getUniqueId())) {
                addCd(player);
                ActionCommand.playerActions.get(player.getUniqueId()).addAction(ActionType.SNEAK);
            }
        }
    }

    @EventHandler
    public void onSwap(PlayerSwapHandItemsEvent event) {
        if (!event.isCancelled()) {
            Player player = event.getPlayer();
            if (!cd.contains(player.getUniqueId())) {
                addCd(player);
                ActionCommand.playerActions.get(player.getUniqueId()).addAction(ActionType.SWAP);
            }
        }
    }

    @EventHandler
    public void onInteract(PlayerInteractEvent event) {
        if (!event.isCancelled()) {
            Player player = event.getPlayer();
            if (!cd.contains(player.getUniqueId())) {
                addCd(player);
                if (event.getAction().toString().contains("LEFT")) {
                    ActionCommand.playerActions.get(player.getUniqueId()).addAction(ActionType.LEFTCLICK);
                }
                if (event.getAction().toString().contains("RIGHT")) {
                    ActionCommand.playerActions.get(player.getUniqueId()).addAction(ActionType.RIGHTCLICK);
                }
            }
        }
    }

    @EventHandler
    public void onInteractEntity(PlayerInteractAtEntityEvent event) {
        if (!event.isCancelled()) {
            Player player = event.getPlayer();
            ActionCommand.playerActions.get(player.getUniqueId()).addAction(ActionType.INTERACTENTITY);
        }
    }

    @EventHandler
    public void onMove(PlayerMoveEvent event) {
        if (!event.isCancelled()) {
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
                        ActionCommand.playerActions.get(player.getUniqueId()).addAction(ActionType.JUMP);
                    }
                }
            }

            float pitch = to.getPitch() - from.getPitch();
            if (pitch <= -40 && to.getPitch() <= -70) {
                ActionCommand.playerActions.get(player.getUniqueId()).addAction(ActionType.UP);
            }
            if (pitch >= 40 && to.getPitch() >= 70) {
                ActionCommand.playerActions.get(player.getUniqueId()).addAction(ActionType.DOWN);
            }
        }
    }

    @EventHandler
    public void onJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();
        ActionCommand.playerActions.put(player.getUniqueId(), new DoneActions(player.getUniqueId()));
    }

    @EventHandler
    public void onQuit(PlayerQuitEvent event) {
        Player player = event.getPlayer();
        ActionCommand.playerActions.remove(player.getUniqueId());
    }
}
