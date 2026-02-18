package com.ramadhan.schedule;

import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.plugin.java.JavaPlugin;
import java.util.Calendar;
import java.util.HashSet;
import java.util.Set;
import java.util.TimeZone;

public class GoldenSchedule extends JavaPlugin implements CommandExecutor {
    private final Set<String> activeEvents = new HashSet<>();

    @Override
    public void onEnable() {
        saveDefaultConfig();
        getCommand("gs").setExecutor(this);
        
        // Pengecekan tiap 30 detik (600 ticks) biar akurat tapi enteng
        Bukkit.getScheduler().runTaskTimer(this, this::checkSchedule, 0L, 600L);
        getLogger().info("GoldenSchedule Aktif (Abidjan Time)!");
    }

    private void checkSchedule() {
        Calendar cal = Calendar.getInstance(TimeZone.getTimeZone("Africa/Abidjan"));
        String currentTime = String.format("%02d:%02d", cal.get(Calendar.HOUR_OF_DAY), cal.get(Calendar.MINUTE));

        ConfigurationSection section = getConfig().getConfigurationSection("events");
        if (section == null) return;

        for (String key : section.getKeys(false)) {
            String start = section.getString(key + ".start_time");
            String end = section.getString(key + ".end_time");

            // Jalanin Commands Start
            if (currentTime.equals(start) && !activeEvents.contains(key)) {
                activeEvents.add(key);
                executeCommands(section.getStringList(key + ".commands_start"));
            } 
            
            // Jalanin Commands End
            if (currentTime.equals(end) && activeEvents.contains(key)) {
                activeEvents.remove(key);
                executeCommands(section.getStringList(key + ".commands_end"));
            }
        }
    }

    private void executeCommands(java.util.List<String> commands) {
        if (commands == null) return;
        for (String cmd : commands) {
            Bukkit.dispatchCommand(Bukkit.getConsoleSender(), cmd);
        }
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (args.length > 0 && args[0].equalsIgnoreCase("reload")) {
            if (!sender.hasPermission("goldenschedule.admin")) {
                sender.sendMessage("§cNo permission!");
                return true;
            }
            reloadConfig();
            sender.sendMessage("§aGoldenSchedule Config Reloaded!");
            return true;
        }
        return false;
    }
}

