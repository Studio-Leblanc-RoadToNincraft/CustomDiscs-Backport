package me.Navoei.customdiscsplugin.command.SubCommands;

import de.tr7zw.nbtapi.NBT;
import de.tr7zw.nbtapi.NBTCompound;
import de.tr7zw.nbtapi.iface.ReadWriteItemNBT;
import de.tr7zw.nbtapi.iface.ReadWriteNBT;
import de.tr7zw.nbtapi.iface.ReadWriteNBTCompoundList;
import me.Navoei.customdiscsplugin.CustomDiscs;
import me.Navoei.customdiscsplugin.command.SubCommand;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class CreateCommand extends SubCommand {

    @Override
    public String getName() {
        return "create";
    }

    @Override
    public String getDescription() {
        return ChatColor.GRAY + "Creates a custom music disc.";
    }

    @Override
    public String getSyntax() {
        return ChatColor.GREEN + "/customdisc create <filename> \"Custom Lore\"";
    }

    @Override
    public void perform(Player player, String[] args) {
        if (isMusicDisc(player)) {
            if (args.length >= 3) {

                if (!player.hasPermission("customdiscs.create")) {
                    player.sendMessage(ChatColor.RED + "You do not have permission to execute this command!");
                    return;
                }

                // /cd create test.mp3 "test"
                //      [0]     [1]     [2]
                //Find file, if file not there then say "file not there"
                String songname = "";
                String filename = args[1];
                if (filename.contains("../")) {
                    player.sendMessage(ChatColor.RED + "This is an invalid filename!");
                    return;
                }

                if (customName(readQuotes(args)).equalsIgnoreCase("")) {
                    player.sendMessage(ChatColor.RED + "You must provide a name for your disc.");
                    return;
                }

                File getDirectory = new File(CustomDiscs.getInstance().getDataFolder(), "musicdata");
                File songFile = new File(getDirectory.getPath(), filename);
                if (songFile.exists()) {
                    if (getFileExtension(filename).equals("wav") || getFileExtension(filename).equals("mp3") || getFileExtension(filename).equals("flac")) {
                        songname = args[1];
                    } else {
                        player.sendMessage(ChatColor.RED + "File is not in wav, flac, or mp3 format!");
                        return;
                    }
                } else {
                    player.sendMessage(ChatColor.RED + "File not found!");
                    return;
                }

                //Sets the lore of the item to the quotes from the command.
                ItemStack disc = new ItemStack(player.getInventory().getItemInMainHand());
                ItemMeta meta = disc.getItemMeta();
                @Nullable List<String> itemLore = new ArrayList<>();
                final TextComponent customLoreSong = Component.text()
                        .decoration(TextDecoration.ITALIC, false)
                        .content(customName(readQuotes(args)))
                        .color(NamedTextColor.GRAY)
                        .build();
                itemLore.add(PlainTextComponentSerializer.plainText().serialize(customLoreSong));
                meta.addItemFlags(ItemFlag.values());
                meta.setLore(itemLore);
                disc.setItemMeta(meta);

                NBT.modify(disc, nbt -> {
                    ReadWriteNBT customdiscs = nbt.getOrCreateCompound("customdiscs");
                    customdiscs.setString("file", filename);
                });


                player.getInventory().setItemInMainHand(disc);

                player.sendMessage("Your filename is: " + ChatColor.GRAY + songname);
                player.sendMessage("Your custom name is: " + ChatColor.GRAY + customName(readQuotes(args)));

            } else {
                player.sendMessage(ChatColor.RED + "Insufficient arguments! ( /customdisc create <filename> \"Custom Lore\" )");
            }
        } else {
            player.sendMessage(ChatColor.RED + "You are not holding a music disc in your main hand!");
        }
    }

    private String getFileExtension(String s) {
        int index = s.lastIndexOf(".");
        if (index > 0) {
            return s.substring(index + 1);
        } else {
            return "";
        }
    }

    private ArrayList<String> readQuotes(String[] args) {
        ArrayList<String> quotes = new ArrayList<>();
        String temp = "";
        boolean inQuotes = false;

        for (String s : args) {
            if (s.startsWith("\"") && s.endsWith("\"")) {
                temp += s.substring(1, s.length()-1);
                quotes.add(temp);
            } else if (s.startsWith("\"")) {
                temp += s.substring(1);
                quotes.add(temp);
                inQuotes = true;
            } else if (s.endsWith("\"")) {
                temp += s.substring(0, s.length()-1);
                quotes.add(temp);
                inQuotes = false;
            } else if (inQuotes) {
                temp += s;
                quotes.add(temp);
            }
            temp = "";
        }

        return quotes;
    }

    private String customName(ArrayList<String> q) {
        StringBuilder sb = new StringBuilder();

        for (String s : q) {
            sb.append(s);
            sb.append(" ");
        }

        if (sb.length() == 0) {
            return sb.toString();
        } else {
            return sb.toString().substring(0, sb.length()-1);
        }
    }

    private boolean isMusicDisc(Player p) {
        return p.getInventory().getItemInMainHand().getType().equals(Material.RECORD_3) ||
                p.getInventory().getItemInMainHand().getType().equals(Material.RECORD_4) ||
                p.getInventory().getItemInMainHand().getType().equals(Material.RECORD_5) ||
                p.getInventory().getItemInMainHand().getType().equals(Material.RECORD_6) ||
                p.getInventory().getItemInMainHand().getType().equals(Material.RECORD_7) ||
                p.getInventory().getItemInMainHand().getType().equals(Material.RECORD_8) ||
                p.getInventory().getItemInMainHand().getType().equals(Material.RECORD_9) ||
                p.getInventory().getItemInMainHand().getType().equals(Material.RECORD_10) ||
                p.getInventory().getItemInMainHand().getType().equals(Material.RECORD_11) ||
                p.getInventory().getItemInMainHand().getType().equals(Material.RECORD_12);
    }

}
