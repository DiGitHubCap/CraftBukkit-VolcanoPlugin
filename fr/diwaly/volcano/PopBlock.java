package fr.diwaly.volcano;

import java.util.ArrayList;

import org.bukkit.Material;

public class PopBlock {

	private ArrayList<Integer> rateM = new ArrayList<Integer>();
	private ArrayList<Double> rateH = new ArrayList<Double>();
	private ArrayList<Material> mat = new ArrayList<Material>();
	
	
	public PopBlock() {
		
	}
	
	public double getRateH(int i){
		return rateH.get(i);
	}
	public int getRateM(int i){
		return rateM.get(i);
	}
	public Material getMat(int i){
		return mat.get(i);
	}
	public int size(){
		return mat.size();
	}
	
	public boolean put(Double rate, Material m){
		int size = rateH.size();
		int i;
		for(i=0 ; i < size ; i++){
			if(rate > rateH.get(i)){
				rateH.add(i,rate);
				mat.add(i,m);
				return true;
			}
		}
		rateH.add(rate);
		mat.add(m);
		return false;
	}
	
	public PopBlock human(){
		rateH = new ArrayList<Double>();
		int pre = 0;
		for (int i=0 ; i < rateM.size() ; i++) {
			rateH.add((double)(rateM.get(i) - pre)/100);
			pre = rateM.get(i);
		}
		return this;
	}
	public PopBlock machine(){
		rateM = new ArrayList<Integer>();
		int pre = 0;
		for (int i=0 ; i < rateH.size() ; i++) {
			int rate = (int)(rateH.get(i)*100);
			rateM.add(rate + pre);
			pre += rate;
		}
		return this;
	}
}
