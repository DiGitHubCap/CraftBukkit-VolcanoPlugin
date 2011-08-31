package fr.diwaly.volcano;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.Random;
import java.util.Scanner;

import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.CreatureSpawner;
import org.bukkit.entity.CreatureType;
import fr.diwaly.volcano.Flowed;
import fr.diwaly.volcano.Plugin;
import fr.diwaly.volcano.VolcanoErupt;

public class Volcano {
	
	private int preNum = 0;					// Num�ro de la pr�c�dente coul�
	private long hourNextFlowed = 5;		// Heure de la prochaine coul�e
	private long hourEndFlowed = 5;			// Heure de la fin de la coul�e
	private long timerBetweenFlowed = 10*1000; 	// 600s = 10min Dur� entre 2 coul�es de lave
	private long timerFlowed = 7*1000; 			// 60s Dur� de la coul�e
	private int[] xF;
	private int[] zF;
	private int[] yF;							// hauteur des coul�es
	private boolean enCours = false;			// Coul� en cours, une seule coul� � la fois
	public int maxY = 0;
	private int currentY;
	private int aura;
	public boolean delete = false;
	public String name = "";
	public boolean enable;
	private boolean start;
	public boolean canErupt = false;				// Eruption is possible
	public boolean firstStart = true;
	public World world;
	public boolean explosive = true;
	public boolean effusive = true;
	public long timerExplo = 10*20;
	public int delayExplo = 10*20;
	public int nbExplo = 2;
	
	public int refPointX;					// R�f�rence point central du volcan 
    public int refPointY;
    public int refPointZ;
	
    private int[] layerMinX;
    private int[] layerMaxX;
    private int[] layerMinY;
    private int[] layerMaxY;
    private int[] layerMinZ;
    private int[] layerMaxZ;
    public int[] layerRadius;
    
	protected static Random rand = new Random();
	public PopBlock[] popLayer;
	public LinkedList<Flowed> listFlowed = new LinkedList<Flowed>();	
	
	public Volcano(File file){
		name = file.getName();
		load(file);
		initCaldeira();
		start = enable;
		firstStart = !layerIsinit();
	}
	
	public static void wait(int n)
	{
        
        long t0, t1;

        t0 =  System.currentTimeMillis();

        do{
            t1 = System.currentTimeMillis();
        }
        while ((t1 - t0) < (n * 1000));
    }
	
	public Volcano(World world, String name, int maxY, int x, int y, int z){
		this.world = world;
		this.maxY = maxY;
		this.name = name;
		refPointX = x;
		refPointY = y;
		refPointZ = z;
		enable = false;
		initCaldeira();
		initLayer();
		start = enable;
		firstStart = !layerIsinit();
	}
	
	public void addLayer(int radius, PopBlock pop){
		if(!layerIsinit()){
			addLayer(0, radius, pop);
		}else{
			addLayer(layerMinX.length, radius, pop);
		}
	}
	public boolean addLayer(int num, int radius, PopBlock pop){
		int length = layerMinX.length;
		if(num > length){
			return false;
		}else if(num == length){
			length++;
			layerMinX = Arrays.copyOf(layerMinX, length);
			layerMaxX = Arrays.copyOf(layerMaxX, length);
			layerMinY = Arrays.copyOf(layerMinY, length);
			layerMaxY = Arrays.copyOf(layerMaxY, length);
			layerMinZ = Arrays.copyOf(layerMinZ, length);
			layerMaxZ = Arrays.copyOf(layerMaxZ, length);
			layerRadius = Arrays.copyOf(layerRadius, length);
			popLayer = Arrays.copyOf(popLayer, length);
		}	
		popLayer[num] = pop;
		layerRadius[num] = radius;
		layerMinX[num] = refPointX-radius;
		layerMaxX[num] = refPointX+radius;
		layerMinY[num] = refPointY-radius;
		layerMaxY[num] = refPointY+radius;
		layerMinZ[num] = refPointZ-radius;
		layerMaxZ[num] = refPointZ+radius;		
		return true;
	}
	
	public boolean delLayer(int num){
		if(num <= layerMinX.length && num >= 0){			
			layerMinX = supprLine(layerMinX, num); 
			layerMaxX = supprLine(layerMaxX, num);
			layerMinY = supprLine(layerMinY, num);
			layerMaxY = supprLine(layerMaxY, num);
			layerMinZ = supprLine(layerMinZ, num);
			layerMaxZ = supprLine(layerMaxZ, num);
			layerRadius = supprLine(layerRadius, num);
			popLayer = supprLine(popLayer, num);
			return true;
		}
		return false;
	}
	
	private void initCaldeira(){
		int x = refPointX;
		int y = refPointY;
		int z = refPointZ;		

		y+=2; // for a beautiful volcano
		currentY = y;
		xF = new int []{x-2,x-1,x,x+1,x+2,x+2,x+2,x+2,x+2,x+1,x,x-1,x-2,x-2,x-2,x-2};
		yF = new int []{y,y,y,y,y,y,y,y,y,y,y,y,y,y,y,y};
		zF = new int []{z-2,z-2,z-2,z-2,z-2,z-1,z,z+1,z+2,z+2,z+2,z+2,z+2,z+1,z,z-1};
		
		aura = getAura();

		// Eruption
		if(explosive)
			new VolcanoErupt(this, VolcanoErupt.CHECK);
		
		initHeight();
	}
	
	private void initLayer(){
		layerMinX = new int[]{0};
	    layerMaxX = new int[]{0};
	    layerMinY = new int[]{0};
	    layerMaxY = new int[]{0};
	    layerMinZ = new int[]{0};
	    layerMaxZ = new int[]{0};
	    layerRadius = new int[]{0};
	    popLayer = new PopBlock[]{null};
	}
	
	public void initFirstStart(){
		if(firstStart){
			// Petit monticule pour avoir une belle forme
			Block block;
			for (int i = 0 ; i < xF.length ; i++){
				block = world.getBlockAt(xF[i], yF[i], zF[i]);
				block.setType(randomBlock(getLayer(xF[i], yF[i], zF[i])));
			}
			firstStart = false;
		}
	}
	
	 // Ajout dans la liste des coul�s a transform�
    public void newFlowed(Block block){
		// Sauvegarde des positions de la lave
    	Flowed flowed = new Flowed(block);
    	flowed.timeFlowed = getTimeFlowed(timerFlowed);
    	if(!listFlowed.contains(flowed)){
    		listFlowed.offer(flowed);
    	}
	}
    
    public void update(){
    	while(!listFlowed.isEmpty() && listFlowed.peek().timeFlowed < Plugin.getTime()){
    		Flowed flowed = listFlowed.poll();
			// Suppression des bloc en dessous de la coul�e
			if(flowed.y() == maxY){
				removeBlock(flowed.block);
			}else if (effusive){
				 flowed.block.setType(Material.AIR);
			}else{
				int num = getLayer(flowed.x(), flowed.y(), flowed.z());
				// Adding mineral block randomly
				 flowed.block.setType(randomBlock(num));
				 if(flowed.block.getType() == Material.MOB_SPAWNER){
					 ((CreatureSpawner)flowed.block.getState()).setCreatureType(CreatureType.PIG_ZOMBIE);
				 }				 
			}
		}	
	}
    
    public void check(){
		// Add flowed
		if(Plugin.getTime() >= hourNextFlowed){
			if(enable){
				int num = rand.nextInt(xF.length);
				preNum = num;
				Block block = goodHeight(num);
				
				block.setType(Material.LAVA);
				enCours = true;
				hourNextFlowed = getTimeFlowed(timerBetweenFlowed);
				hourEndFlowed = getTimeFlowed(timerFlowed);
				aura = getAura();
				//if(explosive){
				//	effusive = false;
				//}else{
					effusive = isEffusive();
				//}
			}
		}
		// End FLowed
		if(enCours && Plugin.getTime() >= hourEndFlowed){
			Block block = world.getBlockAt(xF[preNum], yF[preNum], zF[preNum]);
			if(block.getY() == maxY){
				removeBlock(block);
			}
			enCours = false;
		}
	}  
    
    public void save(){
    	try {
    		Plugin.log("save "+name);
			BufferedWriter writer = new BufferedWriter(new FileWriter(getFile()));
			
			writer.append("enable: "+String.valueOf(isStart()));
			writer.newLine();
			writer.append("world: "+world.getName());
			writer.newLine();
			writer.append("explosive: "+String.valueOf(explosive));
			writer.newLine();
			writer.append("timerExplo: "+getTimerExplo());
			writer.newLine();
			writer.append("delayExplo: "+getDelayExplo());
			writer.newLine();
			writer.append("nbExplo: "+nbExplo);
			writer.newLine();
			writer.append("refPointX: "+refPointX);
			writer.newLine();
			writer.append("refPointY: "+refPointY);
			writer.newLine();
			writer.append("refPointZ: "+refPointZ);
			writer.newLine();
			writer.append("maxY: "+maxY);
			writer.newLine();
			writer.append("timerBetweenFlowed: "+getTimerBetweenFlowed());
			writer.newLine();
			writer.append("timerFlowed: "+getTimerFlowed());
			for (int num = 0 ; num < popLayer.length ; num++) {
				int radius = layerRadius[num];
				writer.newLine();
				writer.append("layerRadius: "+radius);		
				PopBlock popy = popLayer[num];
				int size = popy.size();
				for (int i=0 ; i < size ; i++) {
					writer.newLine();
					writer.append("popLayer: "+popy.getRateH(i)+" "+popy.getMat(i));
				}	
			}					
			writer.close();
		} catch (IOException e) {
			Plugin.log("Error save volcano : "+e.getMessage());
		}
    }
    
    public void load(File file){
    	try {
    		Plugin.log("load "+name);
			Scanner scan = new Scanner(file);
			scan.next();
			start = scan.nextBoolean();
			enable = start;
			scan.next();
			world = Plugin.server.getWorld(scan.next());
			scan.next();
			explosive = scan.nextBoolean();
			scan.next();
			setTimerExplo(scan.nextInt());
			scan.next();
			setDelayExplo(scan.nextInt());
			scan.next();
			nbExplo = scan.nextInt();
			scan.next();
			refPointX = scan.nextInt();
			scan.next();
			refPointY = scan.nextInt();
			scan.next();
			refPointZ = scan.nextInt();
			scan.next();
			maxY = scan.nextInt();
			scan.next();
			initTimerBetweenFlowed(scan.nextLong());
			scan.next();
			initTimerFlowed(scan.nextLong());
			int i = 0;
			initLayer();
			String next="";if(scan.hasNext())next = scan.next();	
			int radius = 0;
			while(scan.hasNext()){
				if(next.equals("layerRadius:")){
					radius = scan.nextInt();
					if(scan.hasNext())next = scan.next();
				}else{
					PopBlock popy = new PopBlock();
					while(next.equals("popLayer:")){
						popy.put(Double.valueOf(scan.next()), Material.getMaterial(scan.next()));
						if(scan.hasNext()){
							next = scan.next();
						}else{break;}
					}
					addLayer(i,radius,popy.machine());
					i++;
				}
			}
			scan.close();
		} catch (FileNotFoundException e) {
			Plugin.log("Error load volcano : "+e.getMessage());
		}
    }
    
    public void load(){
		File file = getFile();
    	if(file.exists())
    		load(file);
    }
    
    public void delete(){
    	Plugin.listVolcano.remove(this);		
		File file = getFile();
		if(file.exists())
			file.delete();
		Plugin.server.broadcastMessage(ChatColor.GRAY+this.name+" deleted.");
    }
    
    static final protected void removeBlock(Block block){
		block.setType(Material.AIR);
	}
	
	public Material randomBlock(int num){		
		int nb = rand.nextInt(10000);
		PopBlock popy = popLayer[num];
		int size = popy.size();
		for (int i=0 ; i < size ; i++) {
			if(nb < popy.getRateM(i)){
				
				return popy.getMat(i);
			}
		}
		Plugin.log("Error random block: "+nb);
		for (int i=0 ; i < size ; i++) {
			Plugin.log("Error rate: "+popy.getRateM(i)+" Mat: "+popy.getMat(i));
		}
		return Material.LAPIS_BLOCK;
	}
	
	public boolean inside(int x, int y, int z){
		return y >= refPointY - aura && x < refPointX + aura && x > refPointX - aura && z < refPointZ + aura && z > refPointZ - aura && y <= maxY;
	}
	public boolean insideChunk(int x, int z){
		return x < refPointX + aura && x > refPointX - aura && z < refPointZ + aura && z > refPointZ - aura;
	}
	public boolean inZoneActiv(int x, int z){
		int zone = 150;
		return x < refPointX + zone && x > refPointX - zone && z < refPointZ + zone && z > refPointZ - zone;
	}
	public boolean inLayer(int i, int x, int y, int z){
		return y >= layerMinY[i] && x < layerMaxX[i] && x > layerMinX[i] && z < layerMaxZ[i] && z > layerMinZ[i] && y <= layerMaxY[i];
	}
	public void setEruption(){
		int num = rand.nextInt(xF.length);
		Block block = goodHeight(num);
		
		int x = block.getX();
		int y = block .getY();
		int z = block.getZ();
		Block tntA = world.getBlockAt(x-2, y, z-2);
		Block tntB = world.getBlockAt(x+2, y, z-2);
		Block tntC = world.getBlockAt(x+2, y, z+2);
		Block tntD = world.getBlockAt(x-2, y, z+2);
		tntA.setType(Material.TNT);
		tntB.setType(Material.TNT);
		tntC.setType(Material.TNT);
		tntD.setType(Material.TNT);
		
		Block tntZ = world.getBlockAt(x, y, z);
		tntZ.setType(Material.TNT);
		world.createExplosion(x, y, z, 5, true);
		wait(3);
		{
			int tntAID = tntA.getTypeId();
			int tntBID = tntA.getTypeId();
			int tntCID = tntA.getTypeId();
			int tntDID = tntA.getTypeId();
			if(tntAID == 46)
			{
				removeBlock(tntA);
			}
			if(tntBID == 46)
			{
				removeBlock(tntB);
			}
			if(tntCID == 46)
			{
				removeBlock(tntC);
			}
			if(tntDID == 46)
			{
				removeBlock(tntD);
			}
		}
		afterEruption();
		effusive = false;
		Plugin.log(name+" Boom !");
	}
	
	public boolean inEruption(){
		if(effusive){
			return true;
		}
		return false;
	}
	public void afterEruption()
	{
		// Hauteur modifi� par l'explosion
		for (int i = 0; i < yF.length; i++)
		{
			yF[i]--;
		}
	}
	public boolean isEffusive(){
		for (int i = 0; i < yF.length; i++) {
			if(yF[i] == maxY){
				return true;
			}
		}
		return false;
	}
	
	public void setExplosive(){
		explosive = true;
		//effusive = false;
	}
	public void setEffusive(){
		explosive = false;
	}
	
	public void initHeight(){
		for (int i = 0; i < yF.length; i++) {
			goodHeight(i);
		}	
	}
	
	private Block goodHeight(int i){
		Block bTmp;
		Block block = world.getBlockAt(xF[i], yF[i], zF[i]);
		// DESCENTE
		if(block.getType() == Material.AIR){
			bTmp = block;
			bTmp = world.getBlockAt(xF[i], yF[i]-1, zF[i]);
			while (bTmp.getType() == Material.AIR){
				if(yF[i] > refPointY){
					block = bTmp;
					bTmp = world.getBlockAt(xF[i], --yF[i], zF[i]);
				}else{
					break;
				}
			}
		// MONTER
		}else{
			while (block.getType() != Material.AIR){
				if(yF[i] < maxY){
					block = world.getBlockAt(xF[i], ++yF[i], zF[i]);
					if(yF[i] > currentY)
						currentY = yF[i];
				}else{
					break;
				}
			}
		}
		return block;
	}
	
	public int getLayer(int x, int y, int z){
		int num = layerMaxX.length;
		for(num -= 1 ; num >= 0 ; num--){
			if(!inLayer(num, x, y, z))
				return num;
		}
		return num;
	}
	
	public boolean isStart(){
		return start;
	}
	public void stop(){
		start = false;
		enable = false;
	}
	public void start(){
		start = true;
		enable = true;
		initHeight();
	}
	private int getAura(){
		return currentY - refPointY + 12;
	}
	static final public long getTimeFlowed(long time){
		return Plugin.getTime() + time;
	}
	public long getTimerBetweenFlowed(){
		return timerBetweenFlowed/1000;
	}
	public long getTimerFlowed(){
		return timerFlowed/1000;
	}
	public void initTimerBetweenFlowed(long timerBetweenFlowed) {
		this.timerBetweenFlowed = timerBetweenFlowed * 1000;
	}
	public void initTimerFlowed(long timerFlowed) {
		this.timerFlowed = timerFlowed * 1000;
	}
	public void setHourNextFlowed(long hourNextFlowed) {
		this.hourNextFlowed = hourNextFlowed;
	}
	public long getHourNextFlowed() {
		return this.hourNextFlowed;
	}
	public long getTimerExplo(){
		return timerExplo/20;
	}
	public void setTimerExplo(int timer){
		this.timerExplo = timer*20;
	}
	public long getDelayExplo(){
		return delayExplo/20;
	}
	public void setDelayExplo(int timer){
		this.delayExplo = timer*20;
	}
	
	public boolean layerIsinit(){
		return !(popLayer[0] == null);
	}
	public File getFile(){
		return getFile(name);
	}
	static final public File getFile(String name){
		File dir = new File(Plugin.dataFolder,"conf");
		dir.mkdir();
		File file = new File(dir, name);
		return file;
	}
	static final public File getDir(){
		File dir = new File(Plugin.dataFolder,"conf");
		dir.mkdir();
		return dir;
	}
	
	private int[] supprLine(int[] tab, int num){
		if(num == 0)
			return Arrays.copyOfRange(tab, 1, tab.length);
		if(num == tab.length)
			return Arrays.copyOfRange(tab, 0, num-1);
		int[] r = new int[tab.length-1];
		for (int i = 0; i < r.length; i++) {
			if(i >= num){
				r[i] = tab[i+1];
			}else{
				r[i] = tab[i];
			}
		}
		return r;
	}
	private PopBlock[] supprLine(PopBlock[] tab, int num){
		if(num == 0)
			return Arrays.copyOfRange(tab, 1, tab.length);
		if(num == tab.length)
			return Arrays.copyOfRange(tab, 0, num-1);
		PopBlock[] r = new PopBlock[tab.length-1];
		for (int i = 0; i < r.length; i++) {
			if(i >= num){
				r[i] = tab[i+1];
			}else{
				r[i] = tab[i];
			}
		}
		return r;
	}	
}
