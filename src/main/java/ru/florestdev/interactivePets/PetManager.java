package ru.florestdev.interactivePets;

import net.milkbowl.vault.economy.Economy;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.*;
import org.bukkit.entity.Cat.Type;
import org.bukkit.entity.Wolf;
import org.bukkit.metadata.FixedMetadataValue;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class PetManager {
    private final InteractivePets plugin;
    private final Economy economy;
    private final DatabaseManager database;
    private final Map<UUID, LivingEntity> playerPets = new HashMap<>();

    public PetManager(InteractivePets plugin, DatabaseManager database) {
        this.plugin = plugin;
        this.database = database;
        this.economy = plugin.getEconomy();
    }

    public boolean buyPet(Player player, String petType, String petName) {
        int cost = plugin.getConfig().getInt("pet-cost." + petType.toLowerCase(), 50);

        // Check if player has enough money
        if (!economy.has(player, cost)) {
            player.sendMessage(ChatColor.RED + "У вас недостаточно средств! Нужно: " + cost + " монет.");
            return false;
        }

        // Check if player already has this pet
        if (database.hasPet(player.getUniqueId(), petType)) {
            player.sendMessage(ChatColor.RED + "У вас уже есть питомец типа " + petType + "!");
            return false;
        }

        // Withdraw money
        economy.withdrawPlayer(player, cost);

        // Save to database
        database.addPet(player.getUniqueId(), petType, petName);

        // Spawn pet
        spawnPet(player, petType, petName);

        player.sendMessage(ChatColor.GREEN + "Вы успешно приобрели питомца " + petType + " по имени " + petName + "!");
        return true;
    }

    public void spawnPet(Player player, String petType, String petName) {
        Location location = player.getLocation().add(0, 1, 1);
        World world = player.getWorld();

        Entity entity = null;

        switch (petType.toLowerCase()) {
            case "dog":
                Wolf wolf = (Wolf) world.spawnEntity(location, EntityType.WOLF);
                wolf.setTamed(true);
                wolf.setOwner(player);
                wolf.setCustomName(petName);
                wolf.setCustomNameVisible(true);
                wolf.setMetadata("pet_owner", new FixedMetadataValue(plugin, player.getUniqueId().toString()));
                wolf.setMetadata("pet_type", new FixedMetadataValue(plugin, "dog"));
                entity = wolf;
                break;

            case "cat":
                Cat cat = (Cat) world.spawnEntity(location, EntityType.CAT);
                cat.setTamed(true);
                cat.setOwner(player);
                cat.setCatType(Type.RED);
                cat.setCustomName(petName);
                cat.setCustomNameVisible(true);
                cat.setMetadata("pet_owner", new FixedMetadataValue(plugin, player.getUniqueId().toString()));
                cat.setMetadata("pet_type", new FixedMetadataValue(plugin, "cat"));
                entity = cat;
                break;

            case "parrot":
                Parrot parrot = (Parrot) world.spawnEntity(location, EntityType.PARROT);
                parrot.setTamed(true);
                parrot.setOwner(player);
                parrot.setVariant(Parrot.Variant.RED);
                parrot.setCustomName(petName);
                parrot.setCustomNameVisible(true);
                parrot.setMetadata("pet_owner", new FixedMetadataValue(plugin, player.getUniqueId().toString()));
                parrot.setMetadata("pet_type", new FixedMetadataValue(plugin, "parrot"));
                entity = parrot;
                break;

            case "fox":
                Fox fox = (Fox) world.spawnEntity(location, EntityType.FOX);
                fox.setFoxType(Fox.Type.RED);
                fox.setCustomName(petName);
                fox.setCustomNameVisible(true);
                fox.setMetadata("pet_owner", new FixedMetadataValue(plugin, player.getUniqueId().toString()));
                fox.setMetadata("pet_type", new FixedMetadataValue(plugin, "fox"));
                entity = fox;
                break;

            default:
                player.sendMessage(ChatColor.RED + "Неизвестный тип питомца!");
                return;
        }

        if (entity != null) {
            playerPets.put(player.getUniqueId(), (LivingEntity) entity);
        }
    }

    public void removePet(Player player) {
        LivingEntity pet = playerPets.remove(player.getUniqueId());
        if (pet != null && !pet.isDead()) {
            pet.remove();
        }
    }

    public LivingEntity getPlayerPet(Player player) {
        return playerPets.get(player.getUniqueId());
    }

    public void loadPlayerPets(Player player) {
        // Check database for pets and spawn them
        String[] petTypes = {"dog", "cat", "parrot", "fox"};
        for (String type : petTypes) {
            if (database.hasPet(player.getUniqueId(), type)) {
                String name = database.getPetName(player.getUniqueId(), type);
                if (name != null) {
                    spawnPet(player, type, name);
                }
            }
        }
    }
}