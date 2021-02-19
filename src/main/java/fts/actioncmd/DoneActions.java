package fts.actioncmd;

import fts.FunctionalToolSet;
import org.bukkit.Bukkit;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class DoneActions {
    private final UUID uuid;
    private final List<ActionType> types;
    private final List<Integer> values;
    private final List<Long> stamps;

    private boolean cd = true;

    public DoneActions(UUID uuid) {
        this.uuid = uuid;
        types = new ArrayList<>();
        values = new ArrayList<>();
        stamps = new ArrayList<>();
    }

    public void addAction(ActionType type) {
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
            if (System.currentTimeMillis() - stamp > ActionCommand.judgeTime * 2000) {
                types.remove(index);
                values.remove(index);
                stamps.remove(index);
                size--;
            }
        }
    }

    private void judgeAction() {

        for (String id : ActionCommand.standardActions.keySet()) {
            IActions actions = ActionCommand.standardActions.get(id);
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
        }.runTaskLater(FunctionalToolSet.getInstance(), 60L);
    }
}