package ru.florestdev.interactivePets;

import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class PetCommand implements CommandExecutor {
    private final InteractivePets plugin;

    public PetCommand(InteractivePets plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage(ChatColor.RED + "Только игроки могут использовать эту команду!");
            return true;
        }

        Player player = (Player) sender;

        if (args.length < 2) {
            player.sendMessage(ChatColor.YELLOW + "Использование: /pet <тип> <имя>");
            player.sendMessage(ChatColor.YELLOW + "Доступные типы: dog, cat, parrot, fox");
            return true;
        }

        String petType = args[0].toLowerCase();
        String petName = args[1];

        // Validate pet name (prevent long names)
        if (petName.length() > 20) {
            player.sendMessage(ChatColor.RED + "Имя питомца не может быть длиннее 20 символов!");
            return true;
        }

        // Check if pet type exists in config
        if (!plugin.getConfig().contains("pet-cost." + petType)) {
            player.sendMessage(ChatColor.RED + "Неизвестный тип питомца! Доступные: dog, cat, parrot, fox");
            return true;
        }

        // Process purchase
        plugin.getPetManager().buyPet(player, petType, petName);
        return true;
    }
}