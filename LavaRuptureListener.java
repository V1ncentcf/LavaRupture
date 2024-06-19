package org.example;

import com.projectkorra.projectkorra.BendingPlayer;
import com.projectkorra.projectkorra.ability.CoreAbility;
import com.projectkorra.projectkorra.event.PlayerSwingEvent;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.entity.EntityDamageByBlockEvent;
import org.bukkit.event.player.PlayerInteractEvent;

public class LavaRuptureListener implements Listener {

    @EventHandler
    private void playerLeftClick(PlayerSwingEvent event) {
        Player player = event.getPlayer();
        BendingPlayer bPlayer = BendingPlayer.getBendingPlayer(player);

        LavaRupture rupture = CoreAbility.getAbility(player, LavaRupture.class);
        if (rupture != null) {
            rupture.hasSpiked();
        }
    }

    @EventHandler
    private void blockDamage(EntityDamageByBlockEvent event) {

        if (!(event.getEntity() instanceof Player)) return;

        Block block = event.getDamager();
        Player player = (Player) event.getEntity();

        if (block.getType() == Material.MAGMA_BLOCK && block.hasMetadata(LavaRupture.METADATA_KEY_MAGMABLOCK)) {
            event.setCancelled(true);
        }

        if (block.getType() == Material.LAVA && block.hasMetadata(LavaRupture.METADATA_KEY_LAVAWAVE)) {
            event.setCancelled(true);
        }
    }


}
