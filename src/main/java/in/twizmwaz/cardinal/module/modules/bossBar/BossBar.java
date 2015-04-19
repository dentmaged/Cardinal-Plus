package in.twizmwaz.cardinal.module.modules.bossBar;

import in.twizmwaz.cardinal.Cardinal;
import in.twizmwaz.cardinal.GameHandler;
import in.twizmwaz.cardinal.chat.ChatMessage;
import in.twizmwaz.cardinal.event.CycleCompleteEvent;
import in.twizmwaz.cardinal.module.Module;

import java.util.Map;
import java.util.Map.Entry;

import net.minecraft.server.v1_8_R1.PlayerConnection;

import org.apache.commons.lang.Validate;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.craftbukkit.v1_8_R1.entity.CraftPlayer;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.player.PlayerRespawnEvent;
import org.bukkit.event.player.PlayerTeleportEvent;

import com.google.common.collect.Maps;

public class BossBar implements Module {

    private final Map<Player, FakeWither> players = Maps.newHashMap();

    @Override
    public void unload() {
        for (Player player : players.keySet()) {
            players.get(player);
        }
    }

    public void sendMessage(Player player, ChatMessage message, float percent) {
        Validate.isTrue(0F <= percent && percent <= 100F, "Percent must be between 0F and 100F, but was: ", percent);
        FakeWither wither = players.get(player);
        handleTeleport(player, player.getLocation(), true);
        
        wither.name = checkMessageLength(message.getMessage(player.getLocale()));
        wither.health = (percent / 100f) * wither.getMaxHealth();
        sendWither(wither, player);
    }

    private String checkMessageLength(String message) {
        if (message.length() > 64) {
            message = message.substring(0, 63);
        }
        return message;
    }

    public void handleTeleport(final Player player, final Location location) {
        if (players.get(player) != null) {
            handleTeleport(player, location, players.get(player).isVisible());
        } else {
            handleTeleport(player, location, false);
        }
    }

    public void handleTeleport(final Player player, final Location location, final boolean visible) {
        if (players.containsKey(player)) {
            Bukkit.getScheduler().runTaskLater(Cardinal.getInstance(), new Runnable() {
                @Override
                public void run() {
                    if (players.containsKey(player)) {
                        FakeWither oldWither = getWither(player, "");
                        float health = oldWither.health;
                        String message = oldWither.name;
                        if (oldWither.isVisible()) {
                            ((CraftPlayer) player).getHandle().playerConnection.sendPacket(getWither(player, "").getDestroyPacket());
                        }
                        players.remove(player);
                        FakeWither wither = addWither(player, message, visible);
                        wither.health = health;
                        sendWither(wither, player);
                    }
                }
            }, 1L);
        }
    }

    void sendWither(FakeWither wither, Player player) {
        PlayerConnection connection = ((CraftPlayer) player).getHandle().playerConnection;
        connection.sendPacket(wither.getMetaPacket(wither.getWatcher()));
        connection.sendPacket(wither.getTeleportPacket(getWitherLocation(player)));
    }

    FakeWither getWither(Player player, String message) {
        if (players.containsKey(player)) {
            return players.get(player);
        } else {
            return addWither(player, checkMessageLength(message));
        }
    }

    FakeWither addWither(Player player, String message) {
        FakeWither wither = new FakeWither(message, getWitherLocation(player));
        if (wither.isVisible()) {
            ((CraftPlayer) player).getHandle().playerConnection.sendPacket(wither.getSpawnPacket());
        }
        players.put(player, wither);
        return wither;
    }

    private FakeWither addWither(Player player, String message, boolean visible) {
        FakeWither wither = new FakeWither(message, getWitherLocation(player));
        wither.setVisible(visible);
        if (visible) {
            ((CraftPlayer) player).getHandle().playerConnection.sendPacket(wither.getSpawnPacket());
        }
        players.put(player, wither);
        return wither;
    }

    private Location getWitherLocation(Player player) {
        return player.getLocation().add(player.getEyeLocation().getDirection().multiply(100));
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onPlayerJoin(PlayerJoinEvent event) {
        addWither(event.getPlayer(), "", false);
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onCycleComplete(CycleCompleteEvent event) {
        for (Player player : Bukkit.getOnlinePlayers()) {
            addWither(player, "", false);
        }
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onPlayerLogout(PlayerQuitEvent event) {
        players.remove(event.getPlayer());
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onPlayerMove(final PlayerMoveEvent event) {
        handleTeleport(event.getPlayer(), event.getTo());
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onPlayerTeleport(final PlayerTeleportEvent event) {
        handleTeleport(event.getPlayer(), event.getTo());
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onPlayerRespawn(final PlayerRespawnEvent event) {
        handleTeleport(event.getPlayer(), event.getRespawnLocation().clone());
    }

    public static void sendGlobalMessage(ChatMessage message, float percent) {
        for (Player player : Bukkit.getOnlinePlayers()) {
            send(player, message, percent);
        }
    }

    public static void send(Player player, ChatMessage message, float percent) {
        GameHandler.getGameHandler().getMatch().getModules().getModule(BossBar.class).sendMessage(player, message, percent);
    }

    public static void hideWitherGlobally() {
        for (Player player : Bukkit.getOnlinePlayers()) {
            GameHandler.getGameHandler().getMatch().getModules().getModule(BossBar.class).handleTeleport(player, player.getLocation(), false);
        }
    }

}
