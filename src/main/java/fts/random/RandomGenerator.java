package fts.random;

import fts.FunctionalToolSet;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/*
 * fts.random.use
 */
public class RandomGenerator implements Listener {
    public static void initialize(FunctionalToolSet plugin) {
        //        Bukkit.getPluginManager().registerEvents(new RandomGenerator(), FunctionalToolSet.getInstance());
    }

    public static int goRandomInt(int min, int max) {
        return (int) (Math.random() * (max - min) + min);
    }

    @EventHandler
    public void onCMD(PlayerCommandPreprocessEvent event) {
        if (event.isCancelled()) {
            return;
        }
        Player player = event.getPlayer();
        if (!player.hasPermission("fts.random.use")) {
            return;
        }
        String message = event.getMessage();
        String regex = "#ranInt%[0-9]\\d*,[0-9]\\d*%";
        Pattern pattern = Pattern.compile(regex);
        Matcher matcher = pattern.matcher(message);
        System.out.println(matcher.matches());
        //        while (matcher.find()) {
        //            String tmp = "";
        //            for (int i = 0; i < matcher.groupCount(); i++) {
        //                tmp += matcher.group(i);
        //            }
        //            System.out.println(tmp);
        //            if (matcher.groupCount() <= 1) {
        //                continue;
        //            }
        //            int min = Integer.parseInt(matcher.group(1));
        //            int max = Integer.parseInt(matcher.group(3));
        //            message = message.replace(tmp, String.valueOf(goRandomInt(min, max)));
        //        }
        //        event.setMessage(message);
    }
}
