package in.twizmwaz.cardinal.util;

import in.twizmwaz.cardinal.module.modules.permissions.PermissionModule;
import org.bukkit.Material;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffect;

public class PlayerUtils {

    public static void resetPlayer(Player player) {
        player.setHealth(player.getMaxHealth());
        player.setFoodLevel(20);
        player.setSaturation(20);
        player.getInventory().clear();
        player.getInventory().setArmorContents(new ItemStack[]{new ItemStack(Material.AIR), new ItemStack(Material.AIR), new ItemStack(Material.AIR), new ItemStack(Material.AIR)});
        for (PotionEffect effect : player.getActivePotionEffects()) {
            try {
                player.removePotionEffect(effect.getType());
            } catch (NullPointerException e) {
            }
        }
        player.setTotalExperience(0);
        player.setExp(0);
        player.setPotionParticles(true);
        player.setWalkSpeed(0.2F);
        player.setFlySpeed(0.2F);
    }

    public static double getSnowflakeMultiplier(OfflinePlayer player) {
        double highest = 1.0;
        for (String rank : PermissionModule.getRanks(player.getUniqueId())) {
            double multiplier = PermissionModule.getMultiplier(rank);
            if (multiplier > highest) {
                highest = multiplier;
            }
        }
        if (player.isOp()) highest = 2.5;
        if (PermissionModule.isDev(player.getUniqueId())) highest = 2.0;
        return highest;
    }
}
