package fr.diwaly.volcano;

import org.bukkit.ChatColor;
import org.bukkit.block.Block;
import org.bukkit.event.Event;
import org.bukkit.event.player.PlayerBucketEmptyEvent;
import org.bukkit.event.player.PlayerListener;
import org.bukkit.plugin.PluginManager;

public class VolcanoPlayerListener extends PlayerListener{

	public VolcanoPlayerListener() {
		PluginManager pm = Plugin.server.getPluginManager();
		pm.registerEvent(Event.Type.PLAYER_BUCKET_EMPTY, this, Event.Priority.Normal, Plugin.him);
	}

	@Override
	public void onPlayerBucketEmpty(PlayerBucketEmptyEvent event) {
		Block block = event.getBlockClicked();
		for (Volcano volcano : Plugin.listVolcano) {
			if(volcano.world.getName().equals(block.getWorld().getName())){	
				if(volcano.insideChunk(block.getX(), block.getZ())){
					event.setCancelled(true);
					event.getPlayer().sendMessage(ChatColor.GRAY+"Bucket empty isn't allow on volcano.");
				}
			}
		}
	}

	
	
}
