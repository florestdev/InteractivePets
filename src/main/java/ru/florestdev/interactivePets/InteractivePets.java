package ru.florestdev.interactivePets;

import net.milkbowl.vault.economy.Economy;
import org.bukkit.Bukkit;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.bukkit.plugin.java.JavaPlugin;

public final class InteractivePets extends JavaPlugin {
    private Economy economy;
    private DatabaseManager database;
    private PetManager petManager;
    private AIConnect ai;
    private static InteractivePets instance;

    @Override
    public void onEnable() {
        instance = this;
        getLogger().info("Starting our pets...");

        // Save default config
        saveDefaultConfig();

        // Setup Vault economy
        if (!setupEconomy()) {
            getLogger().severe("Vault economy not found! Disabling plugin...");
            getServer().getPluginManager().disablePlugin(this);
            return;
        }

        // Initialize database
        database = new DatabaseManager(this);

        // Initialize AI
        ai = new AIConnect(this,
                getConfig().getString("ai-key"),
                getConfig().getString("ai-system-prompt"),
                getConfig().getString("ai-service"),
                getConfig().getString("ai-model"));

        // Initialize pet manager
        petManager = new PetManager(this, database);

        // Register commands
        getCommand("pet").setExecutor(new PetCommand(this));

        // Register events
        getServer().getPluginManager().registerEvents(new Listeners(this), this);

        getLogger().info("InteractivePets successfully enabled!");
    }

    @Override
    public void onDisable() {
        getLogger().info("Disabling InteractivePets...");

        // Clean up pets
        if (petManager != null) {
            Bukkit.getOnlinePlayers().forEach(petManager::removePet);
        }

        // Close database
        if (database != null) {
            database.close();
        }

        getLogger().info("InteractivePets disabled!");
    }

    private boolean setupEconomy() {
        if (getServer().getPluginManager().getPlugin("Vault") == null) {
            return false;
        }
        RegisteredServiceProvider<Economy> rsp = getServer().getServicesManager().getRegistration(Economy.class);
        if (rsp == null) {
            return false;
        }
        economy = rsp.getProvider();
        return economy != null;
    }

    public Economy getEconomy() {
        return economy;
    }

    public DatabaseManager getDatabase() {
        return database;
    }

    public PetManager getPetManager() {
        return petManager;
    }

    public AIConnect getAI() {
        return ai;
    }

    public static InteractivePets getInstance() {
        return instance;
    }
}
