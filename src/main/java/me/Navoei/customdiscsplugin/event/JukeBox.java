package me.Navoei.customdiscsplugin.event;

import de.tr7zw.nbtapi.NBT;
import de.tr7zw.nbtapi.NBTCompound;
import de.tr7zw.nbtapi.iface.ReadWriteNBT;
import me.Navoei.customdiscsplugin.CustomDiscs;
import me.Navoei.customdiscsplugin.PlayerManager;
import me.Navoei.customdiscsplugin.VoicePlugin;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.Jukebox;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.entity.EntityExplodeEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.material.MaterialData;
import org.bukkit.metadata.FixedMetadataValue;
import org.bukkit.metadata.MetadataValue;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicBoolean;

public class JukeBox implements Listener{

    CustomDiscs customDiscs = CustomDiscs.getInstance();
    PlayerManager playerManager = PlayerManager.instance();

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onInsert(PlayerInteractEvent event) throws IOException {

        Player player = event.getPlayer();
        Block block = event.getClickedBlock();

        if (event.getAction() != Action.RIGHT_CLICK_BLOCK || event.getClickedBlock() == null || event.getItem() == null || event.getItem().getItemMeta() == null || block == null) return;
        if (event.getClickedBlock().getType() != Material.JUKEBOX) return;

        if (isCustomMusicDisc(event) && !jukeboxContainsDisc(block)) {
            Jukebox jukebox = (Jukebox) block.getState();


            NBT.modify(event.getItem(), nbt -> {
                ReadWriteNBT customDiscNBT = nbt.getCompound("customdiscs");

                String soundFileName = customDiscNBT.getString("file");
                System.out.println(soundFileName);

                if(soundFileName == null) return;

                Path soundFilePath = Paths.get(customDiscs.getDataFolder().getPath(), "musicdata", soundFileName);
                System.out.println(soundFilePath);

                if (soundFilePath.toFile().exists()) {

                    String soundName = Objects.requireNonNull(event.getItem().getItemMeta().getLore()).get(0);
                    Component songNameComponent = Component.text()
                            .content(soundName)
                            .color(NamedTextColor.GOLD)
                            .build();

                    String songName = PlainTextComponentSerializer.plainText().serialize(songNameComponent);

                    TextComponent customActionBarSongPlaying = Component.text()
                            .content("Now Playing: " + songName)
                            .color(NamedTextColor.GOLD)
                            .build();

                    assert VoicePlugin.voicechatServerApi != null;

                    MaterialData materialData = event.getItem().getData();
                    MetadataValue fileMetadata = new FixedMetadataValue(customDiscs, soundFileName);
                    jukebox.setMetadata("customdiscs", fileMetadata);

                    /*NBT.modify(jukebox, data -> {
                        System.out.println("Jukebox NBT: " + data.toString());
                        ReadWriteNBT jukeboxNBT = data.getOrCreateCompound("customdiscs");
                        System.out.println("1");
                        jukeboxNBT.setString("file", soundFileName);

                        System.out.println("Jukebox NBT: " + jukeboxNBT.toString());
                    });*/

                    playerManager.playLocationalAudio(VoicePlugin.voicechatServerApi, soundFilePath, block, customActionBarSongPlaying.asComponent());
                } else {
                    player.sendMessage(ChatColor.RED + "Sound file not found.");
                    event.setCancelled(true);
                    try {
                        throw new FileNotFoundException("Sound file is missing!");
                    } catch (FileNotFoundException e) {
                        throw new RuntimeException(e);
                    }
                }
            });
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onEject(PlayerInteractEvent event) {

        Player player = event.getPlayer();
        Block block = event.getClickedBlock();

        if (event.getAction() != Action.RIGHT_CLICK_BLOCK || event.getClickedBlock() == null || block == null) return;
        if (event.getClickedBlock().getType() != Material.JUKEBOX) return;

        if (jukeboxContainsDisc(block)) {

            ItemStack itemInvolvedInEvent;
            if (event.getMaterial().equals(Material.AIR)) {

                if (!player.getInventory().getItemInMainHand().getType().equals(Material.AIR)) {
                    itemInvolvedInEvent = player.getInventory().getItemInMainHand();
                } else if (!player.getInventory().getItemInOffHand().getType().equals(Material.AIR)) {
                    itemInvolvedInEvent = player.getInventory().getItemInOffHand();
                } else {
                    itemInvolvedInEvent = new ItemStack(Material.AIR);
                }

            } else {
                itemInvolvedInEvent = new ItemStack(event.getMaterial());
            }

            if (player.isSneaking() && !itemInvolvedInEvent.getType().equals(Material.AIR)) return;
            stopDisc(block);

            Jukebox jukebox = (Jukebox) block.getState();

            jukebox.removeMetadata("customdiscs", customDiscs);
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onJukeboxBreak(BlockBreakEvent event) {

        Block block = event.getBlock();
        Player player = event.getPlayer();

        if (block.getType() != Material.JUKEBOX) return;

        stopDisc(block);
        Jukebox jukebox = (Jukebox) block.getState();
        jukebox.removeMetadata("customdiscs", customDiscs);
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onJukeboxExplode(EntityExplodeEvent event) {

        for (Block explodedBlock : event.blockList()) {
            if (explodedBlock.getType() != Material.JUKEBOX) return;
                stopDisc(explodedBlock);
                Jukebox jukebox = (Jukebox) explodedBlock.getState();
                jukebox.removeMetadata("customdiscs", customDiscs);
            }
        }

    }

    public boolean jukeboxContainsDisc(Block b) {
        Jukebox jukebox = (Jukebox) b.getLocation().getBlock().getState();
        return jukebox.getPlaying() != Material.AIR;
    }

    public boolean isCustomMusicDisc(PlayerInteractEvent e) {

        if (e.getItem()==null) return false;

        AtomicBoolean isCustomDisc = new AtomicBoolean(false);

        NBT.modify(e.getItem(), nbt -> {
            isCustomDisc.set(nbt.hasTag("customdiscs"));
        });

        return isCustomDisc.get() &&
                (
                        e.getItem().getType().equals(Material.RECORD_3) ||
                                e.getItem().getType().equals(Material.RECORD_4) ||
                                e.getItem().getType().equals(Material.RECORD_5) ||
                                e.getItem().getType().equals(Material.RECORD_6) ||
                                e.getItem().getType().equals(Material.RECORD_7) ||
                                e.getItem().getType().equals(Material.RECORD_8) ||
                                e.getItem().getType().equals(Material.RECORD_9) ||
                                e.getItem().getType().equals(Material.RECORD_10) ||
                                e.getItem().getType().equals(Material.RECORD_11) ||
                                e.getItem().getType().equals(Material.RECORD_12)
                );
    }

    private void stopDisc(Block block) {
        playerManager.stopLocationalAudio(block.getLocation());
    }

}
