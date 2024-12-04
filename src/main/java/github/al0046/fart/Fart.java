package github.al0046.fart;

import org.bukkit.Bukkit;
import org.bukkit.Sound;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.util.Objects;

public class Fart extends JavaPlugin {

    private FileConfiguration config;
    private FileConfiguration langConfig;

    @Override
    public void onEnable() {
        this.saveDefaultConfig();
        this.config = this.getConfig();

        try {
            saveDefaultConfig(); // Ensure config.yml exists
            reloadLangConfig();  // Load language files
            registerCommands();  // Register commands
            getLogger().info("Fart plugin enabled successfully!");
        } catch (Exception e) {
            getLogger().severe("Failed to enable Fart plugin: " + e.getMessage());
            e.printStackTrace();
            getServer().getPluginManager().disablePlugin(this); // Disable plugin if initialization fails
        }

        // Load language files
        loadLangFile("en");
        loadLangFile("sv");
        loadLangFile("da");
        loadLangFile("he");

        getLogger().info("Fart plugin enabled!");
    }

    private void registerCommands() {
        // Register all commands here
        Objects.requireNonNull(getCommand("fart")).setExecutor(this);
        Objects.requireNonNull(getCommand("poop")).setExecutor(this);
        Objects.requireNonNull(getCommand("toilet")).setExecutor(this);
        Objects.requireNonNull(getCommand("diarrhea")).setExecutor(this);
        Objects.requireNonNull(getCommand("fartreload")).setExecutor(this);
        Objects.requireNonNull(getCommand("farthelp")).setExecutor(this);
    }

    private void loadLangFile(String lang) {
        File langFile = new File(getDataFolder(), "lang/" + lang + ".yml");
        if (!langFile.exists()) {
            saveResource("lang/" + lang + ".yml", false);
        }
        langConfig = YamlConfiguration.loadConfiguration(langFile);
    }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        switch (cmd.getName().toLowerCase()) {
            case "fart":
            case "poop":
            case "toilet":
            case "diarrhea":
                return handleEffectCommand(sender, cmd.getName().toLowerCase(), args);

            case "farthelp":
                return handleHelpCommand(sender);

            case "fartreload":
                return handleReloadCommand(sender);

            default:
                return false;
        }
    }

    private boolean handleHelpCommand(CommandSender sender) {
        sender.sendMessage(getMessage("help.title"));
        sender.sendMessage(getMessage("help.fart"));
        sender.sendMessage(getMessage("help.poop"));
        sender.sendMessage(getMessage("help.toilet"));
        sender.sendMessage(getMessage("help.diarrhea"));
        sender.sendMessage(getMessage("help.reload"));
        return true;
    }

    private boolean handleEffectCommand(CommandSender sender, String effectType, String[] args) {
        if (args.length == 0) {
            if (sender instanceof Player) {
                Player player = (Player) sender;
                applyEffect(player, effectType);
                sender.sendMessage(getMessage("effect." + effectType + ".self"));
            } else {
                sender.sendMessage(getMessage("effect.onlyPlayer"));
            }
        } else if (args.length == 1) {
            Player targetPlayer = Bukkit.getPlayer(args[0]);
            if (targetPlayer != null) {
                applyEffect(targetPlayer, effectType);
                sender.sendMessage(getMessage("effect." + effectType + ".other").replace("{player}", targetPlayer.getName()));
                targetPlayer.sendMessage(getMessage("effect." + effectType + ".self"));
            } else {
                sender.sendMessage(getMessage("effect.playerNotFound"));
            }
        } else {
            sender.sendMessage(getMessage("effect.usage").replace("{command}", "/" + effectType));
        }
        return true;
    }

    private void applyEffect(Player player, String effectType) {
        switch (effectType) {
            case "fart":
                player.getWorld().spawnParticle(org.bukkit.Particle.REDSTONE, player.getLocation(), 20, new org.bukkit.Particle.DustOptions(org.bukkit.Color.fromRGB(0, 255, 0), 1.0f));
                player.playSound(player.getLocation(), Sound.ENTITY_PLAYER_BURP, 1.0f, 1.0f);
                break;

            case "poop":
                player.getWorld().spawnParticle(org.bukkit.Particle.REDSTONE, player.getLocation(), 20, new org.bukkit.Particle.DustOptions(org.bukkit.Color.fromRGB(139, 69, 19), 1.0f));
                player.playSound(player.getLocation(), Sound.ENTITY_PLAYER_HURT, 1.0f, 1.0f);
                break;

            case "toilet":
                player.getWorld().spawnParticle(org.bukkit.Particle.REDSTONE, player.getLocation().add(0, 1, 0), 20, new org.bukkit.Particle.DustOptions(org.bukkit.Color.fromRGB(139, 69, 19), 1.0f));
                player.getWorld().spawnParticle(org.bukkit.Particle.REDSTONE, player.getLocation().add(0, 1, 0), 20, new org.bukkit.Particle.DustOptions(org.bukkit.Color.fromRGB(255, 255, 255), 1.0f));
                player.playSound(player.getLocation(), Sound.BLOCK_WATER_AMBIENT, 1.0f, 1.0f);
                break;

            case "diarrhea":
                player.getWorld().spawnParticle(org.bukkit.Particle.REDSTONE, player.getLocation(), 20, new org.bukkit.Particle.DustOptions(org.bukkit.Color.fromRGB(139, 69, 19), 1.0f));
                player.playSound(player.getLocation(), Sound.ENTITY_SLIME_SQUISH, 1.0f, 1.0f);
                break;
        }
    }

    private boolean handleReloadCommand(CommandSender sender) {
        if (sender.hasPermission("fart.admin")) {
            reloadConfig();
            reloadLangFiles();
            reloadLangConfig();
            sender.sendMessage(getMessage("reload.success"));
        } else {
            sender.sendMessage(getMessage("reload.permissionDenied"));
        }
        return true;
    }

    private void reloadLangFiles() {
        loadLangFile("en");
        loadLangFile("sv");
        loadLangFile("da");
        loadLangFile("il");
    }

    private String getMessage(String key) {
        String prefix = config.getString("messages.prefix", "&7[&6FartPlugin&7] ");
        String message = langConfig.getString(key, "Message not found for key: " + key);
        return prefix + message.replace("&", "§");
    }

    public void reloadLangConfig() {
        String lang = getConfig().getString("language", "en"); // Default to English
        File langFile = new File(getDataFolder(), "lang/" + lang + ".yml");

        if (!langFile.exists()) {
            getLogger().warning("Language file '" + lang + "' not found! Defaulting to English.");
            langFile = new File(getDataFolder(), "lang/en.yml");
        }

        langConfig = YamlConfiguration.loadConfiguration(langFile);
        getLogger().info("Loaded language file: " + lang);
    }
}
