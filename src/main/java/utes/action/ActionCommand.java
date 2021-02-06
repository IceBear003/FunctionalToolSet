package utes.action;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.*;
import org.bukkit.scheduler.BukkitRunnable;
import utes.UntilTheEndServer;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.UUID;

public class ActionCommand implements Listener {
    private static HashMap<String,IActions> standardActions=new HashMap<String,IActions>();
    private static HashMap<UUID,Actions> playerActions=new HashMap<UUID,Actions>();
    private static int judgeTime;
    private static YamlConfiguration yaml;
    public ActionCommand(){
        File file = new File(UntilTheEndServer.getInstance().getDataFolder(), "actcmd.yml");
        if (!file.exists())
            UntilTheEndServer.getInstance().saveResource("actcmd.yml", false);
        yaml = YamlConfiguration.loadConfiguration(file);
        if (!yaml.getBoolean("enable")) {
            return;
        }

        judgeTime=yaml.getInt("judgeTime");
        for(String path:yaml.getKeys(false)){
            if(path.equalsIgnoreCase("enable")||path.equalsIgnoreCase("judgeTime"))
                continue;

            List<ActionType> types=new ArrayList<ActionType>();
            for(String toString:yaml.getStringList(path+".actions"))
                types.add(ActionType.valueOf(toString));
            IActions actions=new IActions(types,
                    yaml.getIntegerList(path+".values"),
                    yaml.getStringList(path+".cmds"),
                    yaml.getStringList(path+".messages"));
            standardActions.put(path,actions);
        }

        Bukkit.getPluginManager().registerEvents(this, UntilTheEndServer.getInstance());
    }
    @EventHandler
    public void onJoin(PlayerJoinEvent event){
        Player player=event.getPlayer();
        playerActions.put(player.getUniqueId(),new Actions(player.getUniqueId()));
    }
    @EventHandler
    public void onQuit(PlayerQuitEvent event){
        Player player=event.getPlayer();
        playerActions.remove(player.getUniqueId());
    }
    @EventHandler
    public void onSneak(PlayerToggleSneakEvent event){
        Player player=event.getPlayer();
        if(!event.isSneaking())
            playerActions.get(player.getUniqueId()).addAction(ActionType.SNEAK,1,System.currentTimeMillis());
    }
    @EventHandler
    public void onSwap(PlayerSwapHandItemsEvent event){
        Player player=event.getPlayer();
        playerActions.get(player.getUniqueId()).addAction(ActionType.SWAP,1,System.currentTimeMillis());
    }
    @EventHandler
    public void onInteract(PlayerInteractEvent event){
        Player player=event.getPlayer();
        if(event.getAction().toString().contains("LEFT"))
            playerActions.get(player.getUniqueId()).addAction(ActionType.LEFTCLICK,1,System.currentTimeMillis());
        if(event.getAction().toString().contains("RIGHT"))
            playerActions.get(player.getUniqueId()).addAction(ActionType.RIGHTCLICK,1,System.currentTimeMillis());
    }
    @EventHandler
    public void onInteractEntity(PlayerInteractAtEntityEvent event){
        Player player=event.getPlayer();
        playerActions.get(player.getUniqueId()).addAction(ActionType.INTERACTENTITY,1,System.currentTimeMillis());
    }
    private static ArrayList<UUID> jumpers=new ArrayList<UUID>();
    @EventHandler
    public void onMove(PlayerMoveEvent event){
        Location to=event.getTo();
        Location from=event.getFrom();
        Player player=event.getPlayer();

        //TODO JUMP

        if(to.getBlockX()==from.getBlockX() && to.getBlockZ()==from.getBlockZ()){
            if(to.getY()>from.getY()
                    &&to.getBlock().getType()== Material.AIR
                    &&from.getBlock().getType()==Material.AIR
                    &&!player.isFlying()){
                jumpers.add(player.getUniqueId());
            }
        }

        float yaw=event.getTo().getYaw()-event.getFrom().getYaw();
        float pitch=event.getTo().getPitch()-event.getFrom().getPitch();

    }

    private static class Actions{
        private UUID uuid;
        private List<ActionType> types;
        private List<Integer> values;
        private List<Long> stamps;
        private Actions(UUID uuid){
            this.uuid=uuid;
            types=new ArrayList<ActionType>();
            values=new ArrayList<Integer>();
            stamps=new ArrayList<Long>();
        }
        private void addAction(ActionType type,int value,long stamp){
            gc();
            if(types.size()>=1)
                if(types.get(types.size()-1)==type){
                    values.set(types.size()-1,values.get(types.size()-1)+value);
                    stamps.set(types.size()-1,stamp);
                    judgeAction();
                    return;
                }
            types.add(type);
            values.add(value);
            stamps.add(stamp);
            judgeAction();
        }
        private void gc(){
            for(int index=0;index<types.size();index++){
                long stamp=stamps.get(index);
                if(System.currentTimeMillis()-stamp>judgeTime*2000){
                    types.remove(index);
                    values.remove(index);
                    stamps.remove(index);
                }
            }
        }
        private void judgeAction(){
            for(String id:standardActions.keySet()){
                IActions actions=standardActions.get(id);
                if(actions.types.size()>types.size())
                    continue;

                boolean flag=true;
                for(int index=0;index<actions.types.size();index++){
                    if(types.get(types.size()-index-1)!=actions.types.get(index)){
                        flag=false;
                        break;
                    }
                    if(values.get(types.size()-index-1)<actions.values.get(index)) {
                        flag=false;
                        break;
                    }
                }
                if(!flag)
                    continue;
                else{
                    for(int index=0;index<actions.types.size();index++){
                        int index$=types.size()-index-1;
                        types.remove(index$);
                        values.remove(index$);
                        stamps.remove(index$);
                    }
                }
                for(String cmd:actions.cmds)
                    Bukkit.dispatchCommand(Bukkit.getConsoleSender(),cmd.replace("{player}",Bukkit.getPlayer(uuid).getName()));
                for(String message: actions.messages)
                    Bukkit.getPlayer(uuid).sendMessage(message);
                break;
            }
        }
    }
    private static class IActions{
        private List<ActionType> types;
        private List<Integer> values;
        private List<String> cmds;
        private List<String> messages;
        public IActions(List<ActionType> types, List<Integer> values, List<String> cmds, List<String> messages) {
            this.types = types;
            this.values = values;
            this.cmds = cmds;
            this.messages = messages;
        }
    }
    enum ActionType{
        SNEAK,SWAP,LEFTCLICK,RIGHTCLICK,INTERACTENTITY;
    }
}
