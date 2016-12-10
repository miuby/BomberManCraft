package fr.Toteltwent.BomberManCraft;

import org.bukkit.Material;

public enum BonusType {

	SPEED("Speed", Material.IRON_BOOTS, 33),
	STRENGTH("Strength", Material.BLAZE_POWDER, 33),
	POWER("Power", Material.TNT, 33);
	
	private String name;
	private Material material;
	private int dropChance;
	
	private BonusType(String name, Material material, int dropChance){
		this.name = name;
		this.material = material;
		this.dropChance = dropChance;
	}
	
	public String getName(){
		return name;
	}
	
	public Material getMaterial(){
		return material;
	}
	
	public int getDropChance(){
		return dropChance;
	}
	
	public void addEffect(Bomber bomber){
		switch(this){
		case SPEED:
			bomber.addSpeed();
			break;
		case STRENGTH:
			bomber.addRange();
			break;
		case POWER:
			bomber.addTNT();
			break;
		default:
			break;
		}
	}
}
