package fr.Toteltwent.BomberManCraft;

import net.md_5.bungee.api.ChatColor;

import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import fr.Toteltwent.BomberManCraft.Manager.ScoreboardSign;

public class Bomber{
	private final Main plugin;
	private final int baseRange = 3;
	private final float baseSpeed = 0.1f;
	private final int basenumberTNT = 1;
	
	private Player player;
	private int range = baseRange;
	private float speed = baseSpeed;
	private int speedPoint = 0;
	private int numberTNT = basenumberTNT;
	private int TNTUse = 0;
	
	private boolean alive = false;
	private boolean ready = false;
	private int points = 0;
	
	private ScoreboardSign scoreboardSign;

	public Bomber(Main instance, Player player){
		plugin = instance;
		this.player = player;
		plugin.addBombers(this);
		
		scoreboard();
		tp();
	}
	
	public void disableBomber(){
		scoreboardSign.destroy();
		setReady(false);
		resetSpeed();
	}
	
	private void scoreboard(){
		scoreboardSign = new ScoreboardSign(player, "BomberCraft");
		scoreboardSign.create();
		scoreboardSign.setLine(1, "§1");
		scoreboardSign.setLine(2, "Player: " + plugin.getNbrPlayer() + "/" + plugin.getNbrPlayerMax());
		scoreboardSign.setLine(3, "§2");
	}
	
	private void tp(){
		if(player.getGameMode() != GameMode.CREATIVE){
			if(plugin.isStarted()){
				if(ready){
					setAlive(true);
					player.setGameMode(GameMode.ADVENTURE);
					player.getInventory().clear();
					player.getInventory().addItem(new ItemStack(Material.TNT, 1));

					//scoreboard
					getScoreboardSign().setLine(3, "TNT: " + numberTNT);
					getScoreboardSign().setLine(4, "Speed: " + speedPoint);
					getScoreboardSign().setLine(5, "Power: " + range);
					
					Location location;
					//tp dans les coins
					switch(plugin.getSpawnUse()){
					case 0:
						location = plugin.getArenaLocation().clone().add(plugin.getArenaSize()/2, 0, -plugin.getArenaSize()/2);
						location.setYaw(45);
						player.teleport(location);
						break;
					case 1:
						location = plugin.getArenaLocation().clone().add(plugin.getArenaSize()/2, 0, plugin.getArenaSize()/2);
						location.setYaw(135);
						player.teleport(location);
						break;
					case 2:
						location = plugin.getArenaLocation().clone().add(-plugin.getArenaSize()/2, 0, plugin.getArenaSize()/2);
						location.setYaw(225);
						player.teleport(location);
						break;
					case 3:
						location = plugin.getArenaLocation().clone().add(-plugin.getArenaSize()/2, 0, -plugin.getArenaSize()/2);
						location.setYaw(315);
						player.teleport(location);
						break;
					}

					plugin.addSpawnUse();
				}else{
					spectate();
				}
			}else{
				player.setGameMode(GameMode.ADVENTURE);
				player.getInventory().clear();
				player.teleport(plugin.getArenaLocation().clone().add(plugin.baseSpawnLocation));

				//scoreboard
				getScoreboardSign().setLine(3, "§2");
				getScoreboardSign().removeLine(4);
				getScoreboardSign().removeLine(5);
				
				//ajout d'objet pour etre pret
				ItemStack tnt = new ItemStack(Material.TNT);
				ItemMeta tntMeta = tnt.getItemMeta();
				tntMeta.setDisplayName("Play");
				tnt.setItemMeta(tntMeta);
				player.getInventory().addItem(tnt);
				
				ItemStack door = new ItemStack(Material.WOOD_DOOR);
				ItemMeta doorMeta = door.getItemMeta();
				doorMeta.setDisplayName("Spectate");
				door.setItemMeta(doorMeta);
				player.getInventory().addItem(door);
			}
		}
	}
	
	private void spectate(){
		player.setGameMode(GameMode.SPECTATOR);
		player.getInventory().clear();
		player.teleport(plugin.getArenaLocation().clone().add(plugin.spectateLocation));
	}
	
	
	public ScoreboardSign getScoreboardSign(){
		return scoreboardSign;
	}
	
	public Player getPlayer(){
		return player;
	}
	
	public boolean isAlive(){
		return alive;
	}
	
	public void setAlive(boolean value){
		if(alive != value){
			alive = value;
			if(value == false){
				spectate();
				
				//verif des morts
				int dead = 0;
				Player winner = null;
				for(Bomber bomber: plugin.getBombers()){
					if(bomber.isAlive()){
						dead++;
						winner = bomber.getPlayer();
					}
				}
				if(dead <= 1){
					if(winner != null)
						Bukkit.broadcastMessage(ChatColor.GOLD + "WINNER : " + winner.getName() + " ! GG");
					plugin.end();
				}
			}
		}
	}
	
	public void kill(Bomber killed){
		if(killed != this){
			addPoints();
			killed.setAlive(false);
			Bukkit.broadcastMessage(ChatColor.GREEN + killed.getPlayer().getName() + " was killed by " + getPlayer().getName() + " !");
	    }else{
			Bukkit.broadcastMessage(ChatColor.GREEN + getPlayer().getName() + " committed suicide !");
	    }
	}
	
	public boolean isReady(){
		return ready;
	}
	
	public void setReady(boolean value){
		if(value){
			if(!ready){
				plugin.setNbrPlayer(plugin.getNbrPlayer() + 1);
				ready = value;
				getScoreboardSign().setLine(3, ChatColor.GREEN+"Ready");
				
		    	plugin.getReadyTeam().addEntry(player.getName());
			}
		}else{
			if(ready){
				plugin.setNbrPlayer(plugin.getNbrPlayer() - 1);
				ready = value;
				getScoreboardSign().setLine(3, "§2");

		    	plugin.getReadyTeam().removeEntry(player.getName());
			}
		}
	}
	
	public void resetReady(){
		plugin.resetNbrPlayer();
		ready = false;
    	plugin.getReadyTeam().removeEntry(player.getName());
	}
	
	public void reset(){
		tp();
		resetReady();
		resetRange();
		resetSpeed();
		resetTNT();
		resetPoints();
	}

	public int getPoints() {
		return points;
	}
	
	public void resetPoints() {
		points = 0;
		getScoreboardSign().setLine(2, "Kill: " + getPoints());
	}
	
	public void addPoints() {
		points++;
		getScoreboardSign().setLine(2, "Kill: " + getPoints());
	}
	
	public void resetSpeed(){
		if(plugin.isStarted()){
			speed = baseSpeed;
		}else{
			speed = 0.2f;
		}
		speedPoint = 0;
		player.setWalkSpeed(speed);
	}
	
	public void addSpeed(){
		speed += 0.02f;
		speedPoint++;
		getScoreboardSign().setLine(4, "Speed: " + speedPoint);
		player.setWalkSpeed(speed);
	}
	
	public void resetTNT(){
		numberTNT = basenumberTNT;
	}
	
	public void addTNT(){
		numberTNT++;
		player.getInventory().clear();
		player.getInventory().addItem(new ItemStack(Material.TNT, numberTNT - TNTUse));
		getScoreboardSign().setLine(3, "TNT: " + numberTNT);
	}
	
	public int getRange(){
		return range;
	}
	
	public void resetRange(){
		range = baseRange;
	}
	
	public void addRange(){
		range++;
		getScoreboardSign().setLine(5, "Power: " + range);
	}

	public int getTNTUse() {
		return TNTUse;
	}

	public int getNumberTNT() {
		return numberTNT;
	}

	public void TNTUse() {
		TNTUse++;
		player.getInventory().clear();
		player.getInventory().addItem(new ItemStack(Material.TNT, numberTNT - TNTUse));
		if(TNTUse > numberTNT){
			plugin.getLogger().warning("TNT use > total TNT for " + player.getName());
		}
	}
	
	public void TNTExplode() {
		TNTUse--;
		player.getInventory().clear();
		player.getInventory().addItem(new ItemStack(Material.TNT, numberTNT - TNTUse));
		if(TNTUse < 0){
			plugin.getLogger().warning("TNT < 0 for " + player.getName());
		}
	}
}
