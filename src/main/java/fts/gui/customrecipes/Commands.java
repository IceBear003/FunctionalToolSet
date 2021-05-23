package fts.gui.customrecipes;

import fts.gui.customrecipes.gui.ops.RecipeEditor;
import fts.gui.customrecipes.gui.player.RecipeWorkbench;
import fts.gui.customrecipes.gui.shared.RecipeSawer;
import fts.gui.customrecipes.stat.FileManager;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;
import java.util.List;

public class Commands implements CommandExecutor {

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        if (label.equalsIgnoreCase("cr")) {
            try {
                goCommand(sender, cmd, label, args);
            } catch (ClassCastException exception) {
                sender.sendMessage("§6[合成系统]§r 只有玩家才能使用该指令");
            } catch (IndexOutOfBoundsException exception) {
                sender.sendMessage("§6[合成系统]§r 参数个数错误");
            } catch (NumberFormatException exception) {
                sender.sendMessage("§6[合成系统]§r 参数必须是个整数");
            }
        }
        return true;
    }

    public void goCommand(CommandSender sender, Command cmd, String label, String[] args) {
        if (args.length == 1) {
            sender.sendMessage("§6[合成系统]§r CustomRecipes模块指令：");
            sender.sendMessage("§6★ §r /fts cr editor - 打开配方管理控制面板");
            sender.sendMessage("§6★ §r /fts cr opengui - 打开合成面板");
            sender.sendMessage("§6★ §r /fts cr checkrecipes - 查询所有合成");
            sender.sendMessage("§6★ §r /fts cr delete <编号> - 删除某个配方");
            sender.sendMessage("§6★ §r /fts cr money <编号> <需要的金钱> - 设置配方合成需要的金钱");
            sender.sendMessage("§6★ §r /fts cr exp <编号> <需要的经验等级> - 设置配方合成需要的经验等级");
            sender.sendMessage("§6★ §r /fts cr percent <编号> <成功率> - 设置配方合成的成功率（0-100）");
            sender.sendMessage("§6★ §r /fts cr special <编号> <触发概率> - 设置指定编号的配方在合成时，触发特殊成品的几率（0-100）");
            sender.sendMessage("§6★ §r /fts cr addluckylore <成功率> - 设置手中物品用于合成时提高的成功率");
            return;
        }
        if (args[0].equalsIgnoreCase("help")) {
            sender.sendMessage("§6[合成系统]§r CustomRecipes模块指令：");
            sender.sendMessage("§6★ §r /fts cr editor - 打开配方管理控制面板");
            sender.sendMessage("§6★ §r /fts cr opengui - 打开合成面板");
            sender.sendMessage("§6★ §r /fts cr checkrecipes - 查询所有合成");
            sender.sendMessage("§6★ §r /fts cr delete <编号> - 删除某个配方");
            sender.sendMessage("§6★ §r /fts cr money <编号> <需要的金钱> - 设置配方合成需要的金钱");
            sender.sendMessage("§6★ §r /fts cr exp <编号> <需要的经验等级> - 设置配方合成需要的经验等级");
            sender.sendMessage("§6★ §r /fts cr percent <编号> <成功率> - 设置配方合成的成功率（0-100）");
            sender.sendMessage("§6★ §r /fts cr special <编号> <触发概率> - 设置指定编号的配方在合成时，触发特殊成品的几率（0-100）");
            sender.sendMessage("§6★ §r /fts cr addluckylore <成功率> - 设置手中物品用于合成时提高的成功率");
            return;
        }
        if (args[0].equalsIgnoreCase("opengui")) {
            ((Player) sender).openInventory(RecipeWorkbench.invs.get(((Player) sender).getUniqueId()));
            sender.sendMessage("§6[合成系统]§r 打开合成面板中...");
            return;
        }
        if (args[0].equalsIgnoreCase("checkrecipes")) {
            ((Player) sender).openInventory(RecipeSawer.chooseInv);
            sender.sendMessage("§6[合成系统]§r 打开合成查询面板中...");
            return;
        }

        if (!sender.isOp()) {
            sender.sendMessage("§6[合成系统]§r 你没有权限！");
            return;
        }
        if (args[0].equalsIgnoreCase("editor")) {
            ((Player) sender).openInventory(RecipeEditor.inv);
            sender.sendMessage("§6[合成系统]§r 打开控制面板中...");
            return;
        }
        if (args[0].equalsIgnoreCase("addluckylore")) {
            Player player = (Player) sender;
            ItemStack item = player.getInventory().getItemInMainHand();
            if (item == null) {
                sender.sendMessage("§6[合成系统]§r 手上没有物品！");
                return;
            }
            ItemMeta meta = item.getItemMeta();
            List<String> lore = meta.getLore();
            if (lore == null) {
                lore = new ArrayList<>();
            }
            lore.add("§6提升成功率:§r " + args[1] + "/100");
            meta.setLore(lore);
            item.setItemMeta(meta);
            player.getInventory().setItemInMainHand(item);
            sender.sendMessage("§6[合成系统]§r 添加提成lore成功。");
            return;
        }

        if (!FileManager.recipes.containsKey(Integer.parseInt(args[1]))) {
            sender.sendMessage("§6[合成系统]§r 配方不存在！");
            return;
        }
        if (args[0].equalsIgnoreCase("delete")) {
            FileManager.delRecipe(Integer.parseInt(args[1]));
            sender.sendMessage("§6[合成系统]§r 删除配方成功。");
            return;
        }
        if (args[0].equalsIgnoreCase("money")) {
            FileManager.changeMoney(Integer.parseInt(args[1]), Integer.parseInt(args[2]));
            sender.sendMessage("§6[合成系统]§r 修改配置成功。");
            return;
        }
        if (args[0].equalsIgnoreCase("exp")) {
            FileManager.changeExp(Integer.parseInt(args[1]), Integer.parseInt(args[2]));
            sender.sendMessage("§6[合成系统]§r 修改配置成功。");
            return;
        }
        if (args[0].equalsIgnoreCase("percent")) {
            FileManager.changePercent(Integer.parseInt(args[1]), Integer.parseInt(args[2]));
            sender.sendMessage("§6[合成系统]§r 修改配置成功。");
            return;
        }
        if (args[0].equalsIgnoreCase("special")) {
            Player player = (Player) sender;
            if (player.getInventory().getItemInMainHand() == null) {
                sender.sendMessage("§6[合成系统]§r 手上必须拿着一个物品，作为特殊成品！");
                return;
            }
            FileManager.changeSpecialPercent(Integer.parseInt(args[1]), Integer.parseInt(args[2]));
            FileManager.changeSpecialResult(Integer.parseInt(args[1]), player.getInventory().getItemInMainHand());
            sender.sendMessage("§6[合成系统]§r 修改配置成功。");
            return;
        }
    }
}
