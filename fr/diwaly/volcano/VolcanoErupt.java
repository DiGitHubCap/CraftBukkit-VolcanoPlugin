package fr.diwaly.volcano;

import java.util.Random;

public class VolcanoErupt implements Runnable{

	static final public int CHECK = 0;
	static final public int ERUPT = 1;
	private int state;
	private Volcano volcano;
	private boolean inEruption = false;
	private VolcanoErupt master;
	
	public VolcanoErupt (Volcano volcano, int state){
		this(volcano, state, null);
	}
	
	public VolcanoErupt (Volcano volcano, int state, VolcanoErupt master){
		this.volcano = volcano;
		this.state = state;
		this.master = master;
		if(state == CHECK){
			Plugin.server.getScheduler().scheduleAsyncRepeatingTask(Plugin.him,this,new Random().nextInt(150)+50,150);
		}
	}
	
	@Override
	public void run() {
		if(state == CHECK && !inEruption){
			if(volcano.explosive && volcano.canErupt && volcano.enable && volcano.inEruption()){
				inEruption = true;
				int delay = 0;
				if(volcano.delayExplo > 0)
					delay = new Random().nextInt(volcano.delayExplo);
				long second = volcano.timerExplo + delay;
				for(int i = 0 ; i < volcano.nbExplo ; i++){
					Plugin.server.getScheduler().scheduleSyncDelayedTask(Plugin.him, new VolcanoErupt(volcano, ERUPT, this), second);
					second += 40; //  +2seconds
				}
			}
		}else if(state == ERUPT){
			master.inEruption = false;
			volcano.setEruption();
		}	
	}	
	
}
