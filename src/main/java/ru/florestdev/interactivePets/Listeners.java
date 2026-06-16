package ru.florestdev.interactivePets;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityTameEvent;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.event.player.PlayerInteractEntityEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.metadata.MetadataValue;

import java.util.Collection;

public class Listeners implements Listener {
    private final InteractivePets plugin;

    public Listeners(InteractivePets plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();
        // Load player's pets on join
        plugin.getPetManager().loadPlayerPets(player);
    }

    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event) {
        Player player = event.getPlayer();
        // Remove pets to prevent duplication
        plugin.getPetManager().removePet(player);
    }

    @EventHandler
    public void onChat(AsyncPlayerChatEvent event) {
        Player player = event.getPlayer();
        LivingEntity pet = plugin.getPetManager().getPlayerPet(player);

        if (pet != null && !pet.isDead()) {
            String message = event.getMessage();

            // Only respond if player mentions pet or says keywords
            if (message.toLowerCase().contains(pet.getCustomName().toLowerCase()) ||
                    message.toLowerCase().contains("питомец") ||
                    message.toLowerCase().contains("pet")) {

                // Send AI request
                plugin.getAI().processAI(message).thenAccept(response -> {
                    if (response != null) {
                        Bukkit.getScheduler().runTask(plugin, () -> {
                            player.sendMessage(ChatColor.GOLD + "[" + pet.getCustomName() + "] " +
                                    ChatColor.WHITE + response);

                            // Broadcast to nearby players (alternative to getNearbyPlayers)
                            for (Player p : Bukkit.getOnlinePlayers()) {
                                if (!p.equals(player) && p.getWorld().equals(pet.getWorld())) {
                                    double distance = p.getLocation().distance(pet.getLocation());
                                    if (distance <= 10) {
                                        p.sendMessage(ChatColor.GOLD + "[" + pet.getCustomName() + "] " +
                                                ChatColor.WHITE + response);
                                    }
                                }
                            }
                        });
                    }
                }).exceptionally(ex -> {
                    plugin.getLogger().warning("AI Error: " + ex.getMessage());
                    return null;
                });
            }
        }
    }

    @EventHandler
    public void onPetDamage(EntityDamageByEntityEvent event) {
        // Prevent players from damaging their own pets
        if (event.getDamager() instanceof Player) {
            Player damager = (Player) event.getDamager();
            LivingEntity pet = plugin.getPetManager().getPlayerPet(damager);

            if (pet != null && event.getEntity().equals(pet)) {
                // Отменяем урон
                event.setCancelled(true);

                // Отправляем сообщение игроку
                damager.sendMessage(ChatColor.RED + "Вы не можете атаковать своего питомца!");

                // Отправляем AI запрос
                plugin.getAI().processAI("Твой хозяин ударил тебя.").thenAccept(response -> {
                    if (response != null) {
                        Bukkit.getScheduler().runTask(plugin, () -> {
                            // Отправляем ответ владельцу
                            damager.sendMessage(ChatColor.GOLD + "[" + pet.getCustomName() + "] " +
                                    ChatColor.WHITE + response);

                            // Отправляем ответ игрокам рядом
                            for (Player p : Bukkit.getOnlinePlayers()) {
                                if (!p.equals(damager) && p.getWorld().equals(pet.getWorld())) {
                                    double distance = p.getLocation().distance(pet.getLocation());
                                    if (distance <= 10) {
                                        p.sendMessage(ChatColor.GOLD + "[" + pet.getCustomName() + "] " +
                                                ChatColor.WHITE + response);
                                    }
                                }
                            }
                        });
                    }
                }).exceptionally(ex -> {
                    plugin.getLogger().warning("AI Error in onPetDamage: " + ex.getMessage());
                    return null;
                });
            }
        }
    }

    @EventHandler
    public void onPetTame(EntityTameEvent event) {
        // Cancel taming of wild animals (to prevent bypassing the buy system)
        if (event.getOwner() instanceof Player) {
            Player player = (Player) event.getOwner();
            String entityType = event.getEntity().getType().name().toLowerCase();

            // Check if this pet type requires purchase
            if (plugin.getConfig().contains("pet-cost." + entityType)) {
                event.setCancelled(true);
                player.sendMessage(ChatColor.RED + "Этот питомец доступен только через команду /pet!");
            }
        }
    }

    @EventHandler
    public void onInteract(PlayerInteractEntityEvent event) {
        Player player = event.getPlayer();
        LivingEntity pet = plugin.getPetManager().getPlayerPet(player);

        if (event.getRightClicked().equals(pet)) {
            // Right-click interaction with pet - show info or trigger AI
            player.sendMessage(ChatColor.GREEN + "Ваш питомец: " + pet.getCustomName());
            player.sendMessage(ChatColor.YELLOW + "Здоровье: " + pet.getHealth() + "/" + pet.getMaxHealth());
        }
    }
}