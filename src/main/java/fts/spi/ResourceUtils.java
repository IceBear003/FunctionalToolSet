package fts.spi;

import com.sun.istack.internal.NotNull;
import fts.FunctionalToolSet;
import me.clip.placeholderapi.PlaceholderAPI;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.logging.Level;
import java.util.stream.Collectors;

public class ResourceUtils {
    private static FunctionalToolSet plugin;
    private static YamlConfiguration lang;

    public static void initialize(FunctionalToolSet plugin) {
        ResourceUtils.plugin = plugin;

        if (!new File(plugin.getDataFolder(), "config.yml").exists()) {
            plugin.saveResource("config.yml", true);
        } else {
            autoUpdateConfigs("config.yml");
        }

        autoUpdateConfigs(plugin.getConfig().getString("language"));
        File file = new File(plugin.getDataFolder(), plugin.getConfig().getString("language"));
        lang = YamlConfiguration.loadConfiguration(file);
    }

    public static void sendMessage(CommandSender player, String path) {
        String message = getLang(path);
        if (!message.equalsIgnoreCase("")) {
            player.sendMessage(message);
        }
    }

    public static void sendSpecialMessage(CommandSender player, String path, List<String> elements) {
        String message = getSpecialLang(path, elements);
        if (!message.equalsIgnoreCase("")) {
            player.sendMessage(message);
        }
    }

    public static String getLang(String path) {
        if (lang.contains(path)) {
            return lang.getString(path);
        } else {
            return "";
        }
    }

    public static String getSpecialLang(String path, List<String> elements) {
        if (lang.contains(path)) {
            String origin = lang.getString(path);
            for (int index = 0; index < elements.size(); index += 2) {
                origin = origin.replace(elements.get(index), elements.get(index + 1));
            }
            return origin;
        } else {
            return "";
        }
    }

    public static String getPapi(Player player, String origin) {
        try {
            return PlaceholderAPI.setPlaceholders(player, origin);
        } catch (NoClassDefFoundError error) {
            return origin;
        }
    }

    public static YamlConfiguration autoUpdateConfigs(String name) {
        File file = new File(plugin.getDataFolder(), name);
        final InputStream resource = plugin.getResource(name);
        if (resource != null) {
            if (!file.isFile()) {
                plugin.saveResource(name, true);
                return YamlConfiguration.loadConfiguration(file);
            }
            try (InputStreamReader reader = new InputStreamReader(resource, StandardCharsets.UTF_8)) {
                try (BufferedReader buffer = new BufferedReader(reader)) {
                    final ArrayDeque<String> strings = buffer.lines().collect(Collectors.toCollection(ArrayDeque::new));
                    Map<String, List<String>> commits = new LinkedHashMap<>();
                    Map<String, List<String>> area = new LinkedHashMap<>();
                    Collection<String> copyright = new ArrayList<>();
                    YamlUpdater.parse(strings, commits, area, copyright);
                    try (RandomAccessFile data = new RandomAccessFile(file, "rw")) {
                        try (InputStreamReader fReader = new InputStreamReader(new InputStream() {
                            @Override
                            public int read() throws IOException {
                                return data.read();
                            }

                            @Override
                            public int read(@NotNull byte[] b) throws IOException {
                                return data.read(b);
                            }

                            @Override
                            public int read(@NotNull byte[] b, int off, int len) throws IOException {
                                return data.read(b, off, len);
                            }
                        }, StandardCharsets.UTF_8)) {
                            try (BufferedReader fBuffer = new BufferedReader(fReader)) {
                                Map<String, List<String>> output_commits = new LinkedHashMap<>();
                                Map<String, List<String>> output_areas = new LinkedHashMap<>();
                                YamlUpdater.parse(fBuffer.lines().collect(Collectors.toCollection(ArrayDeque::new)),
                                        output_commits, output_areas, null);
                                YamlUpdater.merge(commits, area, output_commits, output_areas);
                                commits = output_commits;
                                area = output_areas;
                            }
                        }
                        data.seek(0);
                        try (OutputStreamWriter writer = new OutputStreamWriter(new OutputStream() {
                            @Override
                            public void write(int b) throws IOException {
                                data.write(b);
                            }

                            @Override
                            public void write(@NotNull byte[] b, int off, int len) throws IOException {
                                data.write(b, off, len);
                            }

                            @Override
                            public void write(@NotNull byte[] b) throws IOException {
                                data.write(b);
                            }
                        }, StandardCharsets.UTF_8)) {
                            YamlUpdater.store(commits, area, copyright, writer);
                        }
                        data.setLength(data.getFilePointer());
                    }
                }
            } catch (IOException ioe) {
                plugin.getLogger().log(Level.SEVERE, "更新配置文件失败: " + name, ioe);
            }
        }
        return YamlConfiguration.loadConfiguration(file);
    }
}
