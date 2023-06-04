package me.Navoei.customdiscsplugin;

import de.tr7zw.nbtapi.NBT;
import de.tr7zw.nbtapi.iface.ReadWriteNBT;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.BlockState;
import org.bukkit.block.Jukebox;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryMoveItemEvent;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.event.world.ChunkLoadEvent;
import org.bukkit.inventory.ItemStack;

import java.nio.file.Path;
import java.util.Arrays;
import java.util.Objects;

public class HopperManager implements Listener {

    CustomDiscs customDiscs = CustomDiscs.getInstance();

    PlayerManager playerManager = PlayerManager.instance();

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onJukeboxInsertFromHopper(InventoryMoveItemEvent event) {

        // TODO: Fix this
        /*
        if (event.getDestination().getLocation() == null) return;
        if (!event.getDestination().getType().equals(InventoryType.JUKEBOX)) return;
        if (!isCustomMusicDisc(event.getItem())) return;

        String songNameLore = Objects.requireNonNull(event.getItem().getItemMeta().getLore()).get(0);
        Component songNameComponent = Component.text()
                .content(songNameLore)
                .color(NamedTextColor.GOLD)
                .build();
        String songName = PlainTextComponentSerializer.plainText().serialize(songNameComponent);

        TextComponent customActionBarSongPlaying = Component.text()
                .content("Now Playing: " + songName)
                .color(NamedTextColor.GOLD)
                .build();

        // NBT get string
        ReadWriteNBT nbt = NBT.itemStackToNBT(event.getItem());
        String soundFileName = nbt.getString("customdisc");

        if (soundFileName == null) return;

        Path soundFilePath = Path.of(customDiscs.getDataFolder().getPath(), "musicdata", soundFileName);
        assert VoicePlugin.voicechatServerApi != null;
        playerManager.playLocationalAudio(VoicePlugin.voicechatServerApi, soundFilePath, event.getDestination().getLocation().getBlock(), customActionBarSongPlaying.asComponent());*/
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onJukeboxEjectToHopper(InventoryMoveItemEvent event) {

        // TODO - Fix this
        /*if (event.getSource().getLocation() == null) return;
        if (!event.getSource().getType().equals(InventoryType.JUKEBOX)) return;
        if (event.getItem().getItemMeta() == null) return;
        if (!isCustomMusicDisc(event.getItem())) return;

        event.setCancelled(playerManager.isAudioPlayerPlaying(event.getSource().getLocation()));*/

    }

    public void discToHopper(Block block) {

        if (block == null) return;
        if (!block.getLocation().getChunk().isLoaded()) return;
        if (!block.getType().equals(Material.JUKEBOX)) return;
        if (!block.getRelative(BlockFace.DOWN).getType().equals(Material.HOPPER)) return;

        Block hopperBlock = block.getRelative(BlockFace.DOWN);
        org.bukkit.block.Hopper hopper = (org.bukkit.block.Hopper) hopperBlock.getState();

        Jukebox jukebox = (Jukebox) block.getState();

        // TODO - Fix this

        /*InventoryMoveItemEvent event = new InventoryMoveItemEvent(jukebox.getInventory(), jukebox.getRecord(), hopper.getInventory(), false);
        Bukkit.getPluginManager().callEvent(event);

        if (!event.isCancelled()) {
            if (!Arrays.toString(hopper.getInventory().getContents()).contains("null")) return;

            hopper.getInventory().setItem(hopper.getInventory().firstEmpty(), new ItemStack(jukebox.getPlaying()));

            block.setType(Material.AIR);
            block.setType(Material.JUKEBOX);
        }*/

    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onChunkLoad(ChunkLoadEvent event) {
        for (BlockState blockState : event.getChunk().getTileEntities()) {
            if (blockState instanceof Jukebox) {
                Jukebox jukebox = (Jukebox) blockState;
                if (!PlayerManager.instance().isAudioPlayerPlaying(blockState.getLocation()) && !jukebox.isPlaying()) {
                    discToHopper(blockState.getBlock());
                }
            }
        }
    }

    private boolean isCustomMusicDisc (ItemStack item) {

        ReadWriteNBT nbt = NBT.itemStackToNBT(item);
        return nbt.hasTag("customdisc") && (
                        item.getType().equals(Material.RECORD_3) ||
                        item.getType().equals(Material.RECORD_4) ||
                        item.getType().equals(Material.RECORD_5) ||
                        item.getType().equals(Material.RECORD_6) ||
                        item.getType().equals(Material.RECORD_7) ||
                        item.getType().equals(Material.RECORD_8) ||
                        item.getType().equals(Material.RECORD_9) ||
                        item.getType().equals(Material.RECORD_10) ||
                        item.getType().equals(Material.RECORD_11) ||
                        item.getType().equals(Material.RECORD_12)
                );
    }

    private static HopperManager instance;

    public static HopperManager instance() {
        if (instance == null) {
            instance = new HopperManager();
        }
        return instance;
    }

}
