package fr.Toteltwent.BomberManCraft.Manager;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import org.bukkit.block.Block;

import fr.Toteltwent.BomberManCraft.Bonus;
import fr.Toteltwent.BomberManCraft.BonusType;

public class BonusManager {
	private static BonusManager instance;
	public static BonusManager getInstance(){
		if(instance == null){
			instance = new BonusManager();
		}
		return instance;
	}
	
	private List<Bonus> bonusList = new ArrayList<Bonus>();
	private int dropChance = 50;
	
	public Bonus newBonus(Block block){
		Random random = new Random();
		int rand = random.nextInt(100);
		BonusType type;
		
		//pourcentage de chance qu'il y ai un bonus
		if(rand < dropChance){
			//pourcentage de chance d'avoir tel bonus
			Distributor<BonusType> distributor = new Distributor<>();
			for(BonusType bonus : BonusType.values()){
				distributor.Add(bonus, bonus.getDropChance());
			}
			distributor.Seal();
			type = distributor.Distribute();
			
			Bonus bonus = new Bonus(block, type);
			bonusList.add(bonus);
			return bonus;
		}
		return null;
	}
	
	public void removeBonus(Bonus bonus){
		bonus.remove();
		this.bonusList.remove(bonus);
	}
	
	public void removeAllBonus(){
		for(Bonus bonus: bonusList){
			bonus.remove();
		}
		this.bonusList.clear();
	}
	
	public List<Bonus> getBonusList(){
		return bonusList;
	}
}
