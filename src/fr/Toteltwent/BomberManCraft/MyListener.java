package fr.Toteltwent.BomberManCraft;

import net.md_5.bungee.api.ChatColor;

import org.bukkit.GameMode;
import org.bukkit.Material;
import org.bukkit.entity.Item;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityCombustEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityDamageEvent.DamageCause;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.player.PlayerAchievementAwardedEvent;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.metadata.MetadataValue;

import fr.Toteltwent.BomberManCraft.Manager.BonusManager;

public class MyListener implements Listener{
	private final Main plugin;

	public MyListener(Main instance) {
		plugin = instance;
	}
	
	@EventHandler
	public void onPlayerMove(PlayerMoveEvent event){
		//si on s'eloigne trop
		if(event.getTo().distance(plugin.getArenaLocation()) > plugin.getArenaSize()*2){
			event.getPlayer().teleport(plugin.getArenaLocation().clone().add(plugin.spectateLocation));
			event.getPlayer().sendMessage(ChatColor.RED+"Do not stray too much !");
		}
		
		//in game
		if(plugin.isStarted()){
			if(event.getPlayer().getGameMode() == GameMode.ADVENTURE){
				//preparation
				if(plugin.isRoot()){
					event.getPlayer().teleport(event.getFrom());
				}
				
				//empeche de sauter
				if(event.getFrom().getBlockY() != event.getTo().getBlockY())
					event.getPlayer().teleport(event.getFrom());

				//recupere un bonus
				Bonus bonusTemp = null;
				for(Bonus bonus: BonusManager.getInstance().getBonusList()){
					if(event.getPlayer().getLocation().getBlock().getLocation().equals(bonus.getBlock().getLocation())){
						bonus.addToBomber(plugin.playerToBomber(event.getPlayer()));

						bonusTemp = bonus;
					}
				}
				if(bonusTemp != null){
					BonusManager.getInstance().removeBonus(bonusTemp);
				}
			}
		}
	}
	
	@EventHandler
	public void onPlayerInteract(PlayerInteractEvent event){
		if(event.getItem() == null)
			return;
		Player player = event.getPlayer();
		if(plugin.isStarted()){
			if(!plugin.isRoot()){
				//pose de TNT
				if(event.getItem().getType() == Material.TNT){
					if(player.getLocation().getBlock().getType() == Material.AIR){
						Bomber bomber = plugin.playerToBomber(player);
						if(bomber.getTNTUse() < bomber.getNumberTNT()){
							new Bomb(plugin, bomber);
							bomber.TNTUse();
						}
					}
				}
			}
		}else{
			//ready
			if(event.getItem().getType() == Material.TNT){
				plugin.playerToBomber(player).setReady(true);
			}else if(event.getItem().getType() == Material.WOOD_DOOR){
				plugin.playerToBomber(player).setReady(false);
			}
		}
		
		if(player.getGameMode() != GameMode.CREATIVE)
			event.setCancelled(true);
	}
	
	@EventHandler
	public void onEntityCombust(EntityCombustEvent event){
		//detruit le bonus si il brule
		if(event.getEntity() instanceof Item){
			Bonus bonusTemp = null;
			for(Bonus bonus: BonusManager.getInstance().getBonusList()){
				if(bonus.getPassenger() == null){
					bonusTemp = bonus;
				}else if(bonus.getPassenger().isEmpty()){
					bonusTemp = bonus;
				}
			}
			if(bonusTemp != null)
				BonusManager.getInstance().removeBonus(bonusTemp);
		}
	}
	
	@EventHandler
	public void onEntityDamage(EntityDamageEvent event){
		//la mort du joueur
		if(plugin.isStarted()){
			if(event.getEntity() instanceof Player){
				Player player = (Player)event.getEntity();
				if(event.getCause() == DamageCause.FIRE || event.getCause() == DamageCause.FIRE_TICK){
					if(player.getLocation().getBlock().getMetadata("bomber") != null){
						MetadataValue metadata = player.getLocation().getBlock().getMetadata("bomber").get(0);
						if(metadata != null){
							if(metadata.value() instanceof Bomber){
								((Bomber)metadata.value()).kill(plugin.playerToBomber(player)); //kill the player
							}else{
								plugin.getLogger().warning("MetaData Bomb : invalid bomber !");
							}
						}else{
							plugin.getLogger().warning("MetaData Bomb invalid !");
						}
					}

					plugin.playerToBomber((Player)event.getEntity()).setAlive(false);
				}
			}
		}
		event.setCancelled(true);
	}
	
	@EventHandler
	public void onIventoryClick(InventoryClickEvent event){
		if(event.getCurrentItem() == null)
			return;
		Player player = (Player)event.getWhoClicked();
		if(event.getWhoClicked().getGameMode() != GameMode.CREATIVE){
			if(plugin.isStarted()){
				if(!plugin.isRoot()){
					//pose de TNT
					if(event.getCurrentItem().getType() == Material.TNT){
						if(player.getLocation().getBlock().getType() == Material.AIR){
							Bomber bomber = plugin.playerToBomber(player);
							if(bomber.getTNTUse() < bomber.getNumberTNT()){
								new Bomb(plugin, bomber);
								bomber.TNTUse();
							}
						}
					}
				}
			}else{
				//ready
				if(event.getCurrentItem().getType() == Material.TNT){
					plugin.playerToBomber(player).setReady(true);
				}else if(event.getCurrentItem().getType() == Material.WOOD_DOOR){
					plugin.playerToBomber(player).setReady(false);
				}
			}
			event.setCancelled(true);
		}
	}

	@EventHandler
	public void onPlayerAchievementAwarded(PlayerAchievementAwardedEvent event){
		event.setCancelled(true);
	}
	
	@EventHandler
	public void onPlayerDropItem(PlayerDropItemEvent event){
		event.setCancelled(true);
	}

	@EventHandler
	public void onPlayerJoin(PlayerJoinEvent event){
		Player player = event.getPlayer();
		player.getInventory().clear();
		
		new Bomber(plugin, player);
	}
	
	@EventHandler
	public void onPlayerQuit(PlayerQuitEvent event){
		Bomber bomber = plugin.playerToBomber(event.getPlayer());
		bomber.setReady(false);
		bomber.setAlive(false);
		plugin.getBombers().remove(bomber);
	}
}
