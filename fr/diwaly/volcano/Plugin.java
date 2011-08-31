package fr.diwaly.volcano;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Scanner;
import java.util.Map.Entry;
import java.util.TreeMap;
import java.util.logging.Logger;

import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Server;
import org.bukkit.World;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.PluginDescriptionFile;
import org.bukkit.plugin.java.JavaPlugin;

public class Plugin extends JavaPlugin implements Runnable
{
	static private final ArrayList<Player> debugees = new ArrayList<Player>();
	static final private Logger log = Logger.getLogger("Minecraft");
	static private String debug = "";
	public static File dataFolder;
    static public ArrayList<Volcano> listVolcano;
    static private TreeMap<String, Double> allowsBlocks;
    static private ArrayList<String> allowsWorlds;
    static public Server server;
    static private boolean permissions = true;
    static public Plugin him;
    
    public Plugin()
    {
    }
    
	@Override
	public void onDisable()
	{	
	}

	@Override
	public void onEnable()
	{
		him = this;
		server = this.getServer();
		listVolcano = new ArrayList<Volcano>();
		
		allowsBlocks = new TreeMap<String, Double>();
		allowsWorlds = new ArrayList<String>();
		dataFolder = this.getDataFolder();
		dataFolder.mkdir();
		
		server.getScheduler().scheduleSyncRepeatingTask(Plugin.this, this,50,20); // tick second
		
		new VolcanoLavaListener();
		new VolcanoPlayerListener();
		new VolcanoActive();
		
		logDescription();
	}

	@Override
	public boolean onCommand(CommandSender sender, Command command,	String label, String[] args)
	{		
		if(!(sender instanceof Player))
    		return false;
    	Player player = (Player)sender;
    	String display = "";
    	for (int i = 0; i < args.length; i++)
    	{
    		display+=" "+args[i];
		}
    	log(player.getName()+":"+display);
    	
    	if(label.equalsIgnoreCase("volcano") && args.length > 0)
    	{
    		boolean permit = true;
    		if(permissions)
    			permit = player.hasPermission("volcano");
    		if(permit)
    		{
	    		try
	    		{
	    			int length = args.length;
	    			String cmd = args[0];   	    	
	    			if(cmd.equalsIgnoreCase("allow"))
	    			{
	    				if(player.hasPermission("volcano.allow"))
	    				{
	    					int page = 0;
	    					int i = 1;
	    					int end = allowsBlocks.size();
	    					if(length == 2)
	    					{
	    						page = Integer.valueOf(args[1]);  
	    						end = page +16;
	    					}
	    					player.sendMessage(ChatColor.GRAY+"*** ALLOW ***");
	    					for (Entry<String, Double> allow : allowsBlocks.entrySet())
	    					{
	    						if (i > page && i < end)
	    						{
	    							if(allow.getValue() == 100)
	    							{
	    								player.sendMessage(ChatColor.GRAY+"* block: "+allow.getKey());
	    							}
	    							else
	    							{
									player.sendMessage(ChatColor.GRAY+"* block: "+allow.getKey()+" rate: "+allow.getValue());
	    							}
	    						}
	    						i++;
	    					}
	    					return true;
	    				}
	    				else
	    				{
	    					player.sendMessage(ChatColor.GRAY+"You don't have permission to use that command");
	    				}
	    			}
	    			if(cmd.equalsIgnoreCase("load") && length == 2)
	    			{
	    				if(player.hasPermission("volcano.load"))
	    				{
	    					String name = args[1];
	    					Volcano volcano = giveVolcano(name);
	    					if(volcano == null)
	    					{
	    						volcano = new Volcano(Volcano.getFile(name));
		    				listVolcano.add(volcano);
	    					}
	    					else
	    					{
	    						volcano.load();
	    						volcano.initHeight();
	    					}
	    					player.sendMessage(ChatColor.GRAY+name+" is loaded.");
	    					return true;
	    				}
	    				else player.sendMessage(ChatColor.GRAY+"You don't have permission to use that command");
	    			
					}
					
	    			// **** INFO ****
	    			if(length == 1)
	    			{
	    				if(cmd.equalsIgnoreCase("help"))
	    				{
	    					if(player.hasPermission("volcano.help"))
	    					{
	    						player.sendMessage(ChatColor.GRAY+"*** HELP ***");
	    						player.sendMessage(ChatColor.GRAY+"/volcano create <name> <height> <rate nameBloc> ... - Create volcano.");
	    						player.sendMessage(ChatColor.GRAY+"/volcano layer help - Help for add the layers.");
	    						player.sendMessage(ChatColor.GRAY+"/volcano stop <name> - Stop a volcano.");
	    						player.sendMessage(ChatColor.GRAY+"/volcano start <name> - Start a volcano.");
	    						player.sendMessage(ChatColor.GRAY+"/volcano list - List of volcanoes.");
	    						player.sendMessage(ChatColor.GRAY+"/volcano del <name> - Delete a volcano file (not the blocks in game).");
	    						player.sendMessage(ChatColor.GRAY+"/volcano load <name> - Load a volcano file.");
	    						player.sendMessage(ChatColor.GRAY+"/volcano timer <name> [<second>] - Time between flowed.");
	    						player.sendMessage(ChatColor.GRAY+"/volcano flowed <name> [<second>] - Time of flowed before transformation.");
	    						player.sendMessage(ChatColor.GRAY+"/volcano erupt <name> explo|effu - Mode of eruption.");
	    						player.sendMessage(ChatColor.GRAY+"/volcano timerExplo <name> [<second>] - Fixe timer before explosion once at the top.");
								player.sendMessage(ChatColor.GRAY+"/volcano delayExplo <name> [<second>] - Random delay after the fixe timer.");
								player.sendMessage(ChatColor.GRAY+"/volcano nbExplo <name> [<number>] - Number of explosion.");
								player.sendMessage(ChatColor.GRAY+"/volcano info <name> - Information about a volcano.");
								player.sendMessage(ChatColor.GRAY+"/volcano near - Display the near volcanoes.");
								player.sendMessage(ChatColor.GRAY+"/volcano allow [<from>] - List of allow blocks.");
								player.sendMessage(ChatColor.GRAY+"/volcano world <nameWorld> - Allow/Forbidden a world for the volcano plugin. (permitted)");
								player.sendMessage(ChatColor.GRAY+"/volcano permitted - Permit the volcano plugin only for the permitted or allow for everybody. (permitted)");
								return true;
	    					}
	    					else player.sendMessage(ChatColor.GRAY+"You don't have permission to use that command");
	    				}
	    				else if(cmd.equalsIgnoreCase("list"))
	    				{
	    					if(player.hasPermission("volcano.list"))
	    					{
	    						player.sendMessage(ChatColor.GRAY+"List of volcanoes :");
	    						for (Volcano volcano : listVolcano)
	    						{
	    							player.sendMessage(ChatColor.GRAY+"- "+volcano.name+" "+String.valueOf(volcano.isStart()));
	    						}
	    						return true;
	    					}
	    					else player.sendMessage(ChatColor.GRAY+"You don't have permission to use that command");
	    				}
	    				else if(cmd.equalsIgnoreCase("near"))
	    				{
	    					if(player.hasPermission("volcano.near"))
	    					{
	    						player.sendMessage(ChatColor.GRAY+"*** NEAR ***");
	    						Location loc = player.getLocation();
	    						for (String name : nearVolcano(loc.getBlockX(), loc.getBlockZ()))
	    						{
	    							player.sendMessage(ChatColor.GRAY+"* "+name);
	    						}
	    						return true;
	    					}
	    					else player.sendMessage(ChatColor.GRAY+"You don't have permission to use that command");
	    				}
	    				else if(cmd.equalsIgnoreCase("permitted"))
	    				{
	    					if(player.hasPermission("volcano.permitted"))
	    					{
	    						if(permissions)
	    						{
	    							permissions = false;
	    							player.sendMessage(ChatColor.GREEN+"Free for all!");
	    						}
	    						else
	    						{
	    							permissions = true;
	    							player.sendMessage(ChatColor.RED+"Permitted only.");
	    						}
	    						savePropperties();
	    						return true;
	    					}
	    					else player.sendMessage(ChatColor.GRAY+"You don't have permission to use that command");
	    				}
	    				else if(cmd.equalsIgnoreCase("debug"))
	    				{
	    					if(debugees.contains(player))
	    					{
	    						debugees.remove(player);
	    						player.sendMessage(ChatColor.RED+"Debug disable.");
	    					}
	    					else
	    					{
	    						debugees.add(player);
	    						player.sendMessage(ChatColor.GREEN+"Debug enable.");
	    					}
	    					return true;
	    				}
	    			}
	    			// **** VOLCANO ****
					if(length == 2)
					{
	        			String name = args[1];
	        			Volcano volcano = giveVolcano(name);
	        			
	        			if(cmd.equalsIgnoreCase("layer"))
	        			{
	        				if(player.hasPermission("volcano.layerhelp"))
	        				{
	        					player.sendMessage(ChatColor.GRAY+"*** LAYER HELP ***");
	        					player.sendMessage(ChatColor.GRAY+"/volcano layer <name> <radius> <rate nameBloc> ... - Add layer.");
	        					player.sendMessage(ChatColor.GRAY+"/volcano modlayer <name> <num> <radius> <rate nameBloc> ... - Modification of layer.");
	        					player.sendMessage(ChatColor.GRAY+"/volcano dellayer <name> <num> - Delete layer.");
	        					return true;
	        				}
	        				else player.sendMessage(ChatColor.GRAY+"You don't have permission to use that command");
		    			} 
	        			if(cmd.equalsIgnoreCase("world"))
	        			{
	        				if(player.hasPermission("volcano.world"))
	        				{
	        					if(setWorld(name))
	        					{
	        						player.sendMessage(ChatColor.GREEN+"World "+name+" is allowed for the volcano plugin.");	
	        					}
	        					else
	        					{
	        						player.sendMessage(ChatColor.RED+"World "+name+" isn't allowed for the volcano plugin.");
	        					}
	        					return true;
	        				}
	        				else player.sendMessage(ChatColor.GRAY+"You don't have permission to use that command");
	        			}
	        			
	    				if(volcano != null)
	    				{
		        			if(cmd.equalsIgnoreCase("stop"))
		        			{
		        				if(player.hasPermission("volcano.stop"))
		        				{
		        					volcano.stop();
		        					volcano.save();
		        					player.sendMessage(ChatColor.GRAY+name+" is stopped.");
		        					return true;
		        				}
		        				else player.sendMessage(ChatColor.GRAY+"You don't have permission to use that command");
		        			}
		        			else if(cmd.equalsIgnoreCase("start"))
		        			{
		        				if(player.hasPermission("volcano.start"))
		        				{
		        					if(volcano.layerIsinit())
		        					{
		        						volcano.initFirstStart();
		        						volcano.start();
		        						volcano.save();
		        						player.sendMessage(ChatColor.GRAY+name+" starts.");
		        					}
		        					else
		        					{
		        						player.sendMessage(ChatColor.GRAY+name+" haven't of layer.");
		        					}
		        					return true;
		        				}
		        				else player.sendMessage(ChatColor.GRAY+"You don't have permission to use that command");
		        			}
		        			else if(cmd.equalsIgnoreCase("del"))
		        			{
		        				if(player.hasPermission("volcano.del"))
		        				{
		        					volcano.enable = false;
		        					new VolcanoDelete(volcano);
		        					player.sendMessage(ChatColor.GRAY+name+" deleting is pending.");
		        					return true;
		        				}
		        				else player.sendMessage(ChatColor.GRAY+"You don't have permission to use that command");
		        			}
		        			else if(cmd.equalsIgnoreCase("timer"))
		        			{
		        				if(player.hasPermission("volcano.timer"))
		        				{
		        					player.sendMessage(ChatColor.GRAY+name+" timer is "+volcano.getTimerBetweenFlowed());
		        					return true;
		        				}
		        				else player.sendMessage(ChatColor.GRAY+"You don't have permission to use that command");
		        			}
		        			else if(cmd.equalsIgnoreCase("flowed"))
		        			{
		        				if(player.hasPermission("volcano.flowed"))
		        				{
		        					player.sendMessage(ChatColor.GRAY+name+" flowed is "+volcano.getTimerFlowed());
		        					return true;
		        				}
		        				else player.sendMessage(ChatColor.GRAY+"You don't have permission to use that command");
		        			}
		        			else if(cmd.equalsIgnoreCase("info"))
		        			{
		        				if(player.hasPermission("volcano.info"))
		        				{
		        					player.sendMessage(ChatColor.GRAY+"*** Information about "+volcano.name+" ***");
		        					player.sendMessage(ChatColor.GRAY+"* enable : "+String.valueOf(volcano.isStart()));
		        					player.sendMessage(ChatColor.GRAY+"* world : "+volcano.world.getName());
		        					player.sendMessage(ChatColor.GRAY+"* eruption : "+((volcano.explosive)?"Explosive":"Effusive"));
		        					player.sendMessage(ChatColor.GRAY+"* timerExplo : "+volcano.getTimerExplo());
		        					player.sendMessage(ChatColor.GRAY+"* delayExplo : "+volcano.getDelayExplo());
		        					player.sendMessage(ChatColor.GRAY+"* nbExplo : "+volcano.nbExplo);
		        					player.sendMessage(ChatColor.GRAY+"* refPointX : "+volcano.refPointX);
		        					player.sendMessage(ChatColor.GRAY+"* refPointY : "+volcano.refPointY);
		        					player.sendMessage(ChatColor.GRAY+"* refPointZ : "+volcano.refPointZ);
		        					player.sendMessage(ChatColor.GRAY+"* maxY : "+volcano.maxY);
		        					player.sendMessage(ChatColor.GRAY+"* timerBetweenFlowed : "+volcano.getTimerBetweenFlowed());
		        					player.sendMessage(ChatColor.GRAY+"* timerFlowed : "+volcano.getTimerFlowed());
		        				
		        					for(int num = 0 ; num < volcano.popLayer.length ; num++)
		        					{
		        						player.sendMessage(ChatColor.GRAY+"* Layer"+num+" radius: "+volcano.layerRadius[num]);
		        						PopBlock popy = volcano.popLayer[num];
		        						int size = popy.size();
		        						for (int i=0 ; i < size ; i++)
		        						{
		        							player.sendMessage(ChatColor.GRAY+"* pop : "+popy.getRateH(i)+" % "+popy.getMat(i));
		        						}
		        					}
		        					player.sendMessage(ChatColor.GRAY+"*** END ***");
		        					return true;
		        				}
		        				else player.sendMessage(ChatColor.GRAY+"You don't have permission to use that command");
		        			}
		        			else if(cmd.equalsIgnoreCase("timerExplo"))
		        			{
		        				if(player.hasPermission("volcano.timerexplo"))
		        				{
		        					player.sendMessage(ChatColor.GRAY+name+" timer before explosion is "+volcano.getTimerExplo());
		        					return true;
		        				}
		        				else player.sendMessage(ChatColor.GRAY+"You don't have permission to use that command");
		        					
		        			}
		        			else if(cmd.equalsIgnoreCase("delayExplo"))
		        			{
		        				if(player.hasPermission("volcano.delayexplo"))
		        				{
		        					player.sendMessage(ChatColor.GRAY+name+" delay before explosion is "+volcano.getDelayExplo());
		        					return true;
		        				}
		        				else player.sendMessage(ChatColor.GRAY+"You don't have permission to use that command");
		        			}
		        			else if(cmd.equalsIgnoreCase("nbExplo"))
		        			{
		        				if(player.hasPermission("volcano.nbexplo"))
		        				{
		        					player.sendMessage(ChatColor.GRAY+name+" number of explosion is "+volcano.nbExplo);
		        					return true;
		        				}
		        				else player.sendMessage(ChatColor.GRAY+"You don't have permission to use that command");
		        			}
							
	        			}
	    				else
	    				{
	        				player.sendMessage(ChatColor.GRAY+"Volcano "+name+" doesn't exist.");
	        				return true;
	        			}
					}
	    			
	    			if(length == 3)
	    			{
	    				String name = args[1];       			
	        			Volcano volcano = giveVolcano(name);
						if(volcano != null)
						{
				// **** TIMER ****
		        			String param3 = args[2];
	    					if(cmd.equalsIgnoreCase("timer"))
	    					{
	    						if(player.hasPermission("volcano.timer"))
	    						{
	    							String second = param3;
	    							volcano.initTimerBetweenFlowed(Long.valueOf(second));
	    							player.sendMessage(ChatColor.GRAY+name+" timer : "+second);
	    							volcano.save();
	    							return true;
	    						}
	    						else player.sendMessage(ChatColor.GRAY+"You don't have permission to use that command");
	    					}
	    					else if(cmd.equalsIgnoreCase("flowed"))
	    					{
	    						if(player.hasPermission("volcano.flowed"))
	    						{
	    							String second = param3;
	    							volcano.initTimerFlowed(Long.valueOf(second));
	    							player.sendMessage(ChatColor.GRAY+name+" flowed : "+second);
	    							volcano.save();
	    							return true;
	    						}
	    						else player.sendMessage(ChatColor.GRAY+"You don't have permission to use that command");
				// **** EXPLOSION ****
							}
	    					else if(cmd.equalsIgnoreCase("timerExplo"))
	    					{
	    						if(player.hasPermission("volcano.timerexplo"))
	    						{
	    							String timer = param3;
	    							volcano.setTimerExplo(Integer.valueOf(timer));
	    							player.sendMessage(ChatColor.GRAY+name+" timer explosion : "+timer);
	    							volcano.save();
	    							return true;
	    						}
	    						else player.sendMessage(ChatColor.GRAY+"You don't have permission to use that command");
							}
	    					else if(cmd.equalsIgnoreCase("delayExplo"))
	    					{
	    						if(player.hasPermission("volcano.delayexplo"))
	    						{
	    							String delay = param3;
	    							volcano.setDelayExplo(Integer.valueOf(delay));
	    							player.sendMessage(ChatColor.GRAY+name+" delay explosion : "+delay);
	    							volcano.save();
	    							return true;
	    						}
	    						else player.sendMessage(ChatColor.GRAY+"You don't have permission to use that command");
							}
	    					else if(cmd.equalsIgnoreCase("nbExplo"))
	    					{
	    						if(player.hasPermission("volcano.nbexplo"))
	    						{
	    							String nb = param3;
	    							volcano.nbExplo = Integer.valueOf(nb);
	    							player.sendMessage(ChatColor.GRAY+name+" number explosion : "+nb);
	    							volcano.save();
	    							return true;
	    						}
	    						else player.sendMessage(ChatColor.GRAY+"You don't have permission to use that command");
				// **** DEL LAYER ****
	    					}
	    					else if(cmd.equalsIgnoreCase("dellayer"))
	    					{
	    						if(player.hasPermission("volcano.dellayer"))
	    						{
	    							int num = Integer.valueOf(param3);
	    							if(volcano.popLayer.length > 1)
	    							{
	    								if(volcano.delLayer(num)){
	    									volcano.save();
	    									player.sendMessage(ChatColor.GRAY+"Delete layer "+num+" on "+name+".");
	    								}
	    								else
	    								{
	    									player.sendMessage(ChatColor.GRAY+"Bad layer.");
	    								}
	    							}
	    							else
	    							{
	    								player.sendMessage(ChatColor.GRAY+"It remain one layer. You can't delete it.");
	    							}	
	    							return true;
	    						}
	    						else player.sendMessage(ChatColor.GRAY+"You don't have permission to use that command");
				// **** ERUPT ****
	    					}
	    					else if(cmd.equalsIgnoreCase("erupt"))
	    					{
	    						if(player.hasPermission("volcano.erupt"))
	    						{
	    							String mode = param3;
	    							if(mode.equalsIgnoreCase("explo"))
	    							{
	    								if(player.hasPermission("volcano.erupt.explo"))
	    								{
	    									volcano.setExplosive();
	    									volcano.save();
	    									player.sendMessage(ChatColor.GRAY+name+" has explosive eruption.");
	    								}
	    								else player.sendMessage(ChatColor.GRAY+"You don't have permission to use that command");
	    							}
	    							else if(mode.equalsIgnoreCase("effu"))
	    							{
	    								if(player.hasPermission("volcano.erupt.effu"))
	    								{
	    									volcano.setEffusive();
	    									volcano.save();
	    									player.sendMessage(ChatColor.GRAY+name+" has effusive eruption.");
	    								}
	    								else player.sendMessage(ChatColor.GRAY+"You don't have permission to use that command");
	    							}
	    							else
	    							{
	    								player.sendMessage(ChatColor.GRAY+"/volcano erupt <name> explo or effu");
	    							}
	    							return true;
	    						}
	    						else player.sendMessage(ChatColor.GRAY+"You don't have permission to use that command");
	    					}
						}
						else
						{
	    					player.sendMessage(ChatColor.RED+"Volcano "+name+" don't exist.");
	        				return true;
	    				}
					}		
	    			if (length > 3){
	    				String name = args[1];
	    				Volcano volcano = giveVolcano(name);    				
	    		// **** CREATE VOLCANO ****    		
						if(cmd.equalsIgnoreCase("create"))
						{
							if(player.hasPermission("volcano.create"))
							{
								if(allowsWorlds.contains(player.getWorld().getName()))
								{
									if(volcano == null){
										int x = player.getLocation().getBlockX();
										int y = player.getLocation().getBlockY();
										int z = player.getLocation().getBlockZ();
										int maxY = Integer.valueOf(args[2]) + y;
										if(!isNearVolcano(x, z))
										{
											volcano = new Volcano(player.getWorld(),name, maxY, x, y, z);
											listVolcano.add(volcano);
											player.sendMessage(ChatColor.GRAY+"Volcano "+name+" creates.");
										}
										else
										{
											player.sendMessage(ChatColor.RED+"You are near a volcano.");
											return true;
										}
									}
									else
									{
										player.sendMessage(ChatColor.RED+"Volcano "+name+" exists.");
										return true;
									}
								}
								else
								{
									player.sendMessage(ChatColor.RED+"Create a volcano on "+player.getWorld().getName()+" isn't allowed.");
									return true;
								}
							}
							else player.sendMessage(ChatColor.GRAY+"You don't have permission to use that command");
						}
				// **** LAYER ****		
	    				if(volcano != null)
	    				{
		    				if(cmd.equalsIgnoreCase("layer") || cmd.equalsIgnoreCase("modlayer") || cmd.equalsIgnoreCase("create"))
		    				{
		    					int begin = 3;
		    					int num=0;
		    					int radius = Integer.valueOf(args[2]);
		    					if(cmd.equalsIgnoreCase("modlayer"))
		    					{
		    						if(player.hasPermission("volcano.modlayer"))
		    						{
		    							begin = 4;
		    							num = Integer.valueOf(args[2]);
		    							radius = Integer.valueOf(args[3]);
		    						}
		    						else player.sendMessage(ChatColor.GRAY+"You don't have permission to use that command");
		    					}
		    					else if(cmd.equalsIgnoreCase("create"))
		    					{
		    						if(player.hasPermission("volcano.create"))
		    						{
		    							radius = 0;
		    						}
		    						else player.sendMessage(ChatColor.GRAY+"You don't have permission to use that command");
		    					}
		            			PopBlock popy = new PopBlock();
			        			double sum = 0;		        			
			        			for (int i = begin ; i < length ; i++)
			        			{
			        				double rate = Double.valueOf(args[i]);
			        				String nameMat = args[++i].toUpperCase();
			        				sum += rate;
			        				Material mat = Material.getMaterial(nameMat);
			        				if(mat != null)
			        				{
			        					if(mat.isBlock())
			        					{
			        						if(allowsBlocks.containsKey(nameMat) && rate <= allowsBlocks.get(nameMat))
			        						{
			        							popy.put(rate, mat);
			        						}
			        						else
			        						{
			        							player.sendMessage(ChatColor.RED+"Material "+nameMat+" is forbidden.");
			        							return true;
			        						}
			        					}
			        					else
			        					{
			        						player.sendMessage(ChatColor.RED+nameMat+" isn't a block.");
			        					}
			        				}
			        				else
			        				{
			        					player.sendMessage(ChatColor.RED+"Material "+nameMat+" doesn't exist.");
			        					return true;
			        				}
			        			}
			        			if(sum == 100)
			        			{
			        				if(cmd.equalsIgnoreCase("modlayer"))
			        				{
			        					if(player.hasPermission("volcano.modlayer"))
			        					{
			        						volcano.addLayer(num, radius, popy.machine());
			        						player.sendMessage(ChatColor.GRAY+"A layer "+name+" was modified.");
			        					}
			        					else player.sendMessage(ChatColor.GRAY+"You don't have permission to use that command");
			        				}
			        				else
			        				{
			        					volcano.addLayer(radius, popy.machine());
			        					player.sendMessage(ChatColor.GRAY+"A layer "+name+" was added.");
			        				}
				        			volcano.save();	
				        			return true;
			        			}
			        			else
			        			{
			        				player.sendMessage(ChatColor.RED+"100% of the blocks must be filled.");
			        				return true;
			        			}	
		    				}
		    			}
	    				else
	    				{
	        				player.sendMessage(ChatColor.RED+"Volcano "+name+" don't exist.");
	        				return true;
	        			}
	    			}
				} catch (Exception e)
				{
					log("Error on command: "+e.getMessage());
					for (StackTraceElement stack : e.getStackTrace())
					{
						log("Error on command: "+stack.toString());
					}			
				} 
				player.sendMessage(ChatColor.RED+"Bad command.");
		    	return false;
	    	}
    		else
    		{
	    		player.sendMessage(ChatColor.GRAY+"You don't have permissions to use volcano commands");
	    		return true;
	    	}
    	}
    	return false;
	}
	
	static public Volcano giveVolcano(String name)
	{
		for (Volcano volcano : listVolcano)
		{
			if(volcano.name.equals(name))
			{
				return volcano;
			}
		}
		return null;
	}
	
	static final public long getTime()
	{
		return System.currentTimeMillis();
	}
	
	@Override
	public void run() {
		for (Volcano volcano : listVolcano)
		{
			volcano.update();
			volcano.check();
		}
	}
	
	static public void load()
	{
		blocksPermit();
		worldPermit();
		
		// Add volcano files
		File dir = Volcano.getDir();
		for (File file : dir.listFiles())
		{
			if(file.isFile())
				listVolcano.add(new Volcano(file));
		}		
		// Permition ops
		File prop = getPropertieFile();
		if(!prop.exists())
			savePropperties();
		loadPropperties();
	}
	
	static private void worldPermit()
	{	
		File fileWorld = getWorldFile();		
		try
		{
			if(!fileWorld.exists())
			{
				for (World w : server.getWorlds())
				{
					allowsWorlds.add(w.getName());
				}
				saveWorld();
			}
			else
			{
				Scanner scan = new Scanner(fileWorld);
				while (scan.hasNext())
				{
					allowsWorlds.add(scan.next());
				}
				scan.close();
			}
		}
		catch (Exception e)
		{
			Plugin.log("Error load worlds permission : "+e.getMessage());
		}				
	}
	
	static private void blocksPermit()
	{
		dataFolder.mkdir();
		File listBlocks = new File(dataFolder, "listBlocks.txt");
		File allow = new File(dataFolder, "allowBlock.txt");
		String nameB = "";
		try
		{
			if(!listBlocks.exists())
			{			
				BufferedWriter wList = new BufferedWriter(new FileWriter(listBlocks));
				Material[] listMat = Material.values();
				ArrayList<String> listMatName = new ArrayList<String>();
				for (int i=0 ; i < listMat.length ; i++)
				{
					if(listMat[i].isBlock())
						listMatName.add(listMat[i].name());
				}
				Collections.sort(listMatName);
				for (String name : listMatName)
				{
						wList.append(name);
						wList.newLine();
				}
				wList.close();
			}
			if(!allow.exists())
			{
				BufferedWriter wAllow = new BufferedWriter(new FileWriter(allow));
				wAllow.append(Material.AIR.name()+" 100");
				wAllow.newLine();
				wAllow.append(Material.PUMPKIN.name()+" 100");
				wAllow.newLine();
				wAllow.append(Material.NETHERRACK.name()+" 100");
				wAllow.newLine();
				wAllow.append(Material.DIRT.name()+" 100");
				wAllow.newLine();
				wAllow.append(Material.SOUL_SAND.name()+" 100");
				wAllow.newLine();
				wAllow.append(Material.GLOWSTONE.name()+" 100");
				wAllow.newLine();
				wAllow.append(Material.JACK_O_LANTERN.name()+" 100");
				wAllow.newLine();
				wAllow.append(Material.GOLD_BLOCK.name()+" 100");
				wAllow.newLine();
				wAllow.append(Material.IRON_BLOCK.name()+" 100");
				wAllow.newLine();
				wAllow.append(Material.BRICK.name()+" 100");
				wAllow.newLine();
				wAllow.append(Material.MOSSY_COBBLESTONE.name()+" 100");
				wAllow.newLine();
				wAllow.append(Material.OBSIDIAN.name()+" 100");
				wAllow.newLine();
				wAllow.append(Material.MOB_SPAWNER.name()+" 0.5");
				wAllow.newLine();
				wAllow.append(Material.DIAMOND_ORE.name()+" 100");
				wAllow.newLine();
				wAllow.append(Material.DIAMOND_BLOCK.name()+" 100");
				wAllow.newLine();
				wAllow.append(Material.REDSTONE_ORE.name()+" 100");
				wAllow.newLine();
				wAllow.append(Material.SNOW_BLOCK.name()+" 100");
				wAllow.newLine();
				wAllow.append(Material.CLAY.name()+" 100");
				wAllow.newLine();
				wAllow.append(Material.COAL_ORE.name()+" 100");
				wAllow.newLine();
				wAllow.append(Material.GLASS.name()+" 100");
				wAllow.newLine();
				wAllow.append(Material.LAPIS_ORE.name()+" 100");
				wAllow.newLine();
				wAllow.append(Material.LAPIS_BLOCK.name()+" 100");
				wAllow.newLine();
				wAllow.append(Material.SANDSTONE.name()+" 100");
				wAllow.newLine();
				wAllow.append(Material.WEB.name()+" 100");
				wAllow.newLine();
				wAllow.append(Material.GRAVEL.name()+" 100");
				wAllow.newLine();
				wAllow.append(Material.GOLD_ORE.name()+" 100");
				wAllow.newLine();
				wAllow.append(Material.IRON_ORE.name()+" 100");
				wAllow.newLine();
				wAllow.append(Material.STONE.name()+" 100");
				wAllow.newLine();
				wAllow.append(Material.COBBLESTONE.name()+" 100");
				wAllow.newLine();
				wAllow.append(Material.SAND.name()+" 100");
				wAllow.newLine();
				wAllow.append(Material.TNT.name()+" 0.5");
				wAllow.newLine();
				wAllow.append(Material.GRASS.name()+" 100");
				wAllow.newLine();
				wAllow.close();
			}
			Scanner scan = new Scanner(allow);
			while (scan.hasNext())
			{
				nameB = scan.next();
				allowsBlocks.put(nameB, Double.valueOf(scan.next()));
			}
			scan.close();
			
		}
		catch (Exception e)
		{
			Plugin.log("Error blocks permission : "+e.getMessage()+" sur "+nameB);
			e.printStackTrace();
		}
	}
	
	static final public boolean isNearVolcano(int x, int z)
	{
		for (Volcano v : listVolcano)
		{
			if(v.insideChunk(x, z))
				return true;
		}
		return false;
	}
	
	static final public ArrayList<String> nearVolcano(int x, int z)
	{
		ArrayList<String> nears = new ArrayList<String>();
		for (Volcano volcano : listVolcano)
		{
			if(volcano.insideChunk(x, z))
			{
				nears.add(volcano.name);
			}
		}
		return nears;
	}
	
	static final private boolean setWorld(String world)
	{			
		 if(allowsWorlds.contains(world) || (server.getWorld(world) == null))
		 {
			 allowsWorlds.remove(world);
			 saveWorld();
			return false;
		}
		 else
		 {
			allowsWorlds.add(world);
			saveWorld();
			return true;
		}
	}
	static final public void saveWorld()
	{
		try
		{
			BufferedWriter writer = new BufferedWriter(new FileWriter(getWorldFile()));
			for (String w : allowsWorlds)
			{
				writer.append(w);
				writer.newLine();			
			}
			writer.close();
		}
		catch (Exception e)
		{
			Plugin.log("Error save worlds permission : "+e.getMessage());
		}
	}
	
	static final public void savePropperties()
	{
		try {
			BufferedWriter writer = new BufferedWriter(new FileWriter(getPropertieFile()));
			writer.append("onlyPermitted: "+permissions);
			writer.close();
			
		}
		catch (Exception e)
		{
			Plugin.log("Error save properties : "+e.getMessage());
		}
	}
	static final public void loadPropperties()
	{
		try
		{
			Scanner scan = new Scanner(getPropertieFile());
			scan.next();
			permissions = scan.nextBoolean();
			scan.close();
		} catch (Exception e)
		{
			Plugin.log("Error load properties : "+e.getMessage());
		}
	}
	
	static final public File getWorldFile()
	{
		return new File(dataFolder, "allowWorld.txt");
	}
	static final public File getPropertieFile()
	{
		return new File(dataFolder, "volcano.properties");
	}
	
	static final public void setDebugging(Player player)
	{
		debugees.add(player);
	}
	
	static final public void debug(String msg)
	{
		debug(msg, false);
	}
	static final public void debug(String msg, boolean sameLine)
	{
		debug = debug+msg+" ";
		if(!sameLine)
		{
			for (Player player : debugees)
			{
				player.sendMessage(ChatColor.AQUA+debug);
			}
			debug = "";
		}
	}
	static public void logDescription()
	{
		PluginDescriptionFile pdfFile = him.getDescription();
        log( pdfFile.getName() +" "+pdfFile.getVersion() + " is enabled!" );
	}
	static public void log(String msg)
	{
		log.info("[Volcano] "+msg);
	}
}
