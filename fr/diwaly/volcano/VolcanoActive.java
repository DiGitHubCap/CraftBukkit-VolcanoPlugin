package fr.diwaly.volcano;

import org.bukkit.Location;
import org.bukkit.entity.Player;

public class VolcanoActive implements Runnable{

	int taskId;
	boolean start = true;
	
	public VolcanoActive(){
		taskId = Plugin.server.getScheduler().scheduleAsyncRepeatingTask(Plugin.him,this,10,300);
	}
	
	@Override
	public void run() {
		// Permet de load les volcans de different monde
		if(start){
			Plugin.load();
			start = false;
		}
		
		boolean enable;
		boolean canErupt;
		try{
			for (Volcano volcano : Plugin.listVolcano) {
				if(volcano.isStart()){
					enable = false;
					canErupt = false;
					for(Player player : Plugin.server.getWorld(volcano.world.getName()).getPlayers()){
						Location loc = player.getLocation();
						if(volcano.inZoneActiv(loc.getBlockX(), loc.getBlockZ())){
							enable = true;
							if(volcano.insideChunk(loc.getBlockX(), loc.getBlockZ())){
								canErupt = true;
							}
							break;
						}
					}
					volcano.enable = enable;
					volcano.canErupt = canErupt;
				}
			}
		}catch(Exception e){
			Plugin.log("Error Thread Active: "+e.getMessage());
		}
	}
}
