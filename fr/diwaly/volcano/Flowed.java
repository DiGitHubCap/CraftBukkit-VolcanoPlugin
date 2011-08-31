package fr.diwaly.volcano;

import org.bukkit.block.Block;

public class Flowed implements Comparable<Flowed>{

	public Block block;
	public Long timeFlowed;
	
	public Flowed(Block block){
		this(block,0);
	}
	public Flowed(Block block, long timeFlowed){
		this.block = block;
		this.timeFlowed = timeFlowed;
	}
	
	public int x(){
		return (block).getX();
	}
	public int y(){
		return (block).getY();
	}
	public int z(){
		return (block).getZ();
	}
	@Override
	public int compareTo(Flowed o) {
		return this.timeFlowed.compareTo(o.timeFlowed);
	}
	@Override
	public boolean equals(Object obj) {
		// TODO Auto-generated method stub
		Flowed o = (Flowed)obj;
		return this.x() == o.x() && this.y() == o.y() && this.z() == o.z();
	}
	
	
	
}
