package fr.diwaly.volcano;

import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.event.Event;
import org.bukkit.event.block.BlockFromToEvent;
import org.bukkit.event.block.BlockListener;
import org.bukkit.plugin.PluginManager;

public class VolcanoLavaListener extends BlockListener
{
	
	public VolcanoLavaListener()
	{

		PluginManager pm = Plugin.server.getPluginManager();
		pm.registerEvent(Event.Type.BLOCK_FROMTO, this, Event.Priority.Normal, Plugin.him);
	}

	public void onBlockFromTo(BlockFromToEvent event)
	{
		Block block = event.getBlock();
	
		for (Volcano volcano : Plugin.listVolcano)
		{
			if(volcano.world.getName().equals(block.getWorld().getName()))
			{		
				if(block.getType() == Material.LAVA || block.getType() == Material.STATIONARY_LAVA)
				{				
					if(volcano.inside(block.getX(),block.getY(), block.getZ()))
					{
						volcano.newFlowed(block);
					}
				}
			}
		}
	}
}
