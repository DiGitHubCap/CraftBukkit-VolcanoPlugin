package fr.diwaly.volcano;

public class VolcanoDelete implements Runnable{

	int taskId;
	Volcano volcano;

	public VolcanoDelete (Volcano volcano){
		taskId = Plugin.server.getScheduler().scheduleSyncRepeatingTask(Plugin.him,this,40,40);
		this.volcano = volcano;
		volcano.stop();
	}
	
	@Override
	public void run() {
		
		if(volcano.listFlowed.isEmpty()){
			Plugin.server.getScheduler().cancelTask(taskId);
			volcano.delete();
		}
	}
}
