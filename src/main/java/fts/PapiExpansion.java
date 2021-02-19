package fts;

import com.sun.istack.internal.NotNull;
import fts.cardpoints.CardPoints;
import fts.onlinetimes.OnlineTimes;
import me.clip.placeholderapi.expansion.PlaceholderExpansion;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;

public class PapiExpansion extends PlaceholderExpansion {

    public @NotNull
    String getAuthor() {
        return "[南外丶仓鼠 ]";
    }

    public @NotNull
    String getIdentifier() {
        return "fts";
    }

    public @NotNull
    String getVersion() {
        return "1.0";
    }

    @Override
    public String onRequest(OfflinePlayer off_player, @NotNull String identifier) {
        if (off_player == null) {
            return null;
        }
        Player player = off_player.getPlayer();
        if (player == null) {
            return "";
        }
        switch (identifier) {
            case "dayOnlineTime":
                return OnlineTimes.turnToString(OnlineTimes.getDayTime(player));
            case "totalOnlineTime":
                return OnlineTimes.turnToString(OnlineTimes.getTotalTime(player));
            case "cardPoints":
                return String.valueOf(CardPoints.stats.get(player.getUniqueId()).points);
            default:
                return identifier;
        }
    }
}
