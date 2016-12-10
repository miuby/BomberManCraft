package fr.Toteltwent.BomberManCraft;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.block.Block;
import org.bukkit.metadata.FixedMetadataValue;
import org.bukkit.scheduler.BukkitScheduler;

import fr.Toteltwent.BomberManCraft.Manager.BonusManager;

public class Bomb {
	private final Main _plugin;
	private int _range;
	private Location _location;
	private Bomber _bomber;
	
	public Bomb(Main instance, Bomber bomber){
		_plugin = instance;
		_range = bomber.getRange();
		_location = bomber.getPlayer().getLocation();
		_bomber = bomber;
		
		createBomb();
	}
	
	private void createBomb(){
		_location.getBlock().setType(Material.TNT);
		_location.getBlock().setMetadata("bomber", new FixedMetadataValue(_plugin, _bomber));
		_location.getWorld().playSound(_location, Sound.FUSE, 1, 0);
		delayExplosion();
	}
	
	private void delayExplosion(){
    	//gere l'explosion apres 60 ticks
    	BukkitScheduler scheduler = Bukkit.getServer().getScheduler();
		scheduler.scheduleSyncDelayedTask(_plugin, new Runnable() {
			@Override
			public void run() {
				if(_location.getBlock().getType() == Material.TNT)
					explosion(_location);
			}
		}, 60);
    }
    
    private void explosion(Location location){
		Block block = location.getBlock();
		
		//joue le son
		location.getWorld().playSound(location, Sound.EXPLODE, 1, 0);
		
		//rend la TNT au bomber
		if(block.getMetadata("bomber").get(0) != null){
			if(block.getMetadata("bomber").get(0).value() instanceof Bomber){
				((Bomber)block.getMetadata("bomber").get(0).value()).TNTExplode();
			}else{
				_plugin.getLogger().warning("MetaData Bomb : invalid bomber !");
			}
		}else{
			_plugin.getLogger().warning("MetaData Bomb invalid !");
		}
		
		//explose
		block.setType(Material.FIRE);
		delayStopFire(block);
		
		//x+range
		for(int loop = 1; loop < _range; loop++){
			Location loc = new Location(location.getWorld(), location.getX() + loop, location.getY(), location.getZ());
			if(!destructBlock(loc.getBlock()))
				break;
		}
		//x-range
		for(int loop = 1; loop < _range; loop++){
			Location loc = new Location(location.getWorld(), location.getX() - loop, location.getY(), location.getZ());
			if(!destructBlock(loc.getBlock()))
				break;
		}
		//z+range
		for(int loop = 1; loop < _range; loop++){
			Location loc = new Location(location.getWorld(), location.getX(), location.getY(), location.getZ() + loop);
			if(!destructBlock(loc.getBlock()))
				break;
		}
		//z-range
		for(int loop = 1; loop < _range; loop++){
			Location loc = new Location(location.getWorld(), location.getX(), location.getY(), location.getZ() - loop);
			if(!destructBlock(loc.getBlock()))
				break;
		}
    }
    
    private boolean destructBlock(Block block){
    	//return true si le feu continue
    	if(block.getType() == Material.AIR){
			block.setType(Material.FIRE);
			
			block.setMetadata("bomber", new FixedMetadataValue(_plugin, _bomber));
			
			delayStopFire(block);
			return true;
		}else if(block.getType() == Material.COBBLESTONE){
			block.setType(Material.FIRE);
			
			block.setMetadata("bomber", new FixedMetadataValue(_plugin, _bomber));
			delaySpawnBonus(block); //spawn bonus
			
			delayStopFire(block);
			return false;
		}else if(block.getType() == Material.TNT){
			explosion(block.getLocation());
			return false;
		}
    	return false;
    }
    
    private void delayStopFire(final Block block){
    	//arrete le feu apres 10 ticks
    	BukkitScheduler scheduler = Bukkit.getServer().getScheduler();
		scheduler.scheduleSyncDelayedTask(_plugin, new Runnable() {
			@Override
			public void run() {
				block.setType(Material.AIR);
			}
		}, 10);
    }
    
    private void delaySpawnBonus(final Block block){
    	//fait spawn un bonus apres 11 ticks
    	BukkitScheduler scheduler = Bukkit.getServer().getScheduler();
		scheduler.scheduleSyncDelayedTask(_plugin, new Runnable() {
			@Override
			public void run() {
				BonusManager.getInstance().newBonus(block);
			}
		}, 11);
    }
}
