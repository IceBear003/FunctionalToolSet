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

    public static class YamlUpdater {
        public static void merge(
                Map<String, List<String>> latest_commits,
                Map<String, List<String>> latest_area,
                Map<String, List<String>> old_commits,
                Map<String, List<String>> old_area
        ) {
            for (Map.Entry<String, List<String>> commits : latest_commits.entrySet()) {
                old_commits.putIfAbsent(commits.getKey(), commits.getValue());
            }
            for (Map.Entry<String, List<String>> area : latest_area.entrySet()) {
                old_area.putIfAbsent(area.getKey(), area.getValue());
            }
        }

        public static void store(
                Map<String, List<String>> commits,
                Map<String, List<String>> areas,
                Collection<String> copyright,
                Writer writer) throws IOException {
            for (String cr : copyright) {
                writer.append('#').append(' ').append(cr).append('\n').flush();
            }
            writer.append('\n');
            for (Map.Entry<String, List<String>> area : areas.entrySet()) {
                final List<String> commit = commits.get(area.getKey());
                if (commit != null) {
                    for (String c : commit) {
                        writer.append('#').append(' ').append(c).append('\n').flush();
                    }
                }
                for (String line : area.getValue()) {
                    writer.append(line).append('\n').flush();
                }
            }
        }

        public static void parse(
                Deque<String> lines,
                Map<String, List<String>> commits,
                Map<String, List<String>> area,
                Collection<String> copyright) {
            ArrayDeque<String> commitBuffer = new ArrayDeque<>(20);
            ArrayDeque<String> dataBuffer = new ArrayDeque<>(20);
            do {
                final String s = lines.peek();
                if (s == null) {
                    break;
                }
                String trim = s.trim();
                if (trim.isEmpty()) {
                    lines.poll();
                    if (copyright != null) {
                        copyright.addAll(commitBuffer);
                    }
                    commitBuffer.clear();
                    break;
                }
                if (trim.charAt(0) == '#') {
                    commitBuffer.add(trim.substring(1).trim());
                    lines.poll();
                    continue;
                }
                break;
            } while (true);
            do {
                do {
                    String next = lines.peek();
                    if (next == null) {
                        break;
                    }
                    String trim = next.trim();
                    if (trim.isEmpty()) {
                        lines.poll();
                        continue;
                    }
                    if (trim.charAt(0) == '#') {
                        commitBuffer.add(trim.substring(1).trim());
                        lines.poll();
                        continue;
                    }
                    break;
                } while (!lines.isEmpty());
                do {
                    String next = lines.peek();
                    if (next != null) {
                        if (next.isEmpty()) {
                            lines.poll();
                            dataBuffer.add(next);
                        } else {
                            if (dataBuffer.isEmpty()) {
                                lines.poll();
                                dataBuffer.add(next);
                            } else {
                                String trim = next.trim();
                                if (trim.isEmpty()) {
                                    lines.poll();
                                    dataBuffer.add(next);
                                    continue;
                                }
                                if (trim.charAt(0) == '-') {
                                    lines.poll();
                                    dataBuffer.add(next);
                                    continue;
                                }
                                if (!Character.isSpaceChar(next.charAt(0))) {
                                    break;
                                }
                                lines.poll();
                                dataBuffer.add(next);
                                continue;
                            }
                        }
                        continue;
                    }
                    break;
                } while (!lines.isEmpty());
                if (dataBuffer.isEmpty()) {
                    continue;
                }
                String header = dataBuffer.peek();
                int index = header.indexOf(':');
                if (index == -1) {
                    continue;
                }
                String path = header.substring(0, index);
                if (commits != null && !commitBuffer.isEmpty()) {
                    commits.put(path, new ArrayList<>(commitBuffer));
                }
                if (area != null) {
                    area.put(path, new ArrayList<>(dataBuffer));
                }
                commitBuffer.clear();
                dataBuffer.clear();
            } while (!lines.isEmpty());
        }
    }
}
