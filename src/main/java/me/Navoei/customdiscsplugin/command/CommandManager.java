package me.Navoei.customdiscsplugin.command;

import me.Navoei.customdiscsplugin.command.SubCommands.CreateCommand;
import me.Navoei.customdiscsplugin.command.SubCommands.DownloadCommand;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;

public class CommandManager implements CommandExecutor, TabCompleter {

    private final ArrayList<SubCommand> subCommands = new ArrayList<>();

    public CommandManager() {
        subCommands.add(new CreateCommand());
        subCommands.add(new DownloadCommand());
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {

        if (!(sender instanceof Player)) {
            sender.sendMessage(ChatColor.RED + "Only players can use this command!");
            return true;
        }

        Player player = (Player) sender;

        if (args.length > 0) {
            for (int i = 0; i < getSubCommands().size(); i++) {
                if (args[0].equalsIgnoreCase(getSubCommands().get(i).getName())) {
                    getSubCommands().get(i).perform(player, args);
                }
            }
        } else {
            player.sendMessage(ChatColor.AQUA + "----[ Custom Discs ]----");
            for (int i = 0; i < getSubCommands().size(); i++) {
                player.sendMessage(getSubCommands().get(i).getSyntax() + ChatColor.DARK_GRAY + " - " + getSubCommands().get(i).getDescription());
            }
            player.sendMessage(ChatColor.AQUA + "---------------------");
            return true;
        }

        return true;
    }

    public ArrayList<SubCommand> getSubCommands() {
        return subCommands;
    }
    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String label, String[] args) {

        if (args.length == 1) {
            List<String> arguments = new ArrayList<>();
            for (int i = 0; i < getSubCommands().size(); i++) {
                arguments.add(getSubCommands().get(i).getName());
            }
            return arguments;
        }

        return null;
    }
}
