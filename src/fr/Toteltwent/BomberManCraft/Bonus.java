package fr.Toteltwent.BomberManCraft;

import org.bukkit.block.Block;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Item;
import org.bukkit.inventory.ItemStack;

public class Bonus {
	private Block block;
	private ArmorStand armorStand;
	private Item item;
	private BonusType type;
	
	public Bonus(Block block, BonusType type){
		this.block = block;
		this.type = type;

		item = block.getLocation().getWorld().dropItem(block.getLocation(), new ItemStack(type.getMaterial()));
		item.setPickupDelay(Integer.MAX_VALUE);
		
		//armorStand pour tenir l'item
		armorStand = block.getLocation().getWorld().spawn(block.getLocation().clone().add(0.5, -1, 0.5), ArmorStand.class);
		armorStand.setPassenger(item);
		armorStand.setVisible(false);
		armorStand.setGravity(false);
	}
	
	public void remove(){
		if(item != null)
			item.remove();
		armorStand.remove();
	}

	public Block getBlock() {
		return block;
	}
	
	public BonusType getType(){
		return type;
	}
	
	public Entity getPassenger(){
		return armorStand.getPassenger();
	}
	
	public void addToBomber(Bomber bomber){
		type.addEffect(bomber);
	}
}
