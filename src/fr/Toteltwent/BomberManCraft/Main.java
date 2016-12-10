package fr.Toteltwent.BomberManCraft;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.logging.Logger;

import net.minecraft.server.v1_8_R3.PacketPlayOutTitle;
import net.minecraft.server.v1_8_R3.PlayerConnection;
import net.minecraft.server.v1_8_R3.IChatBaseComponent.ChatSerializer;
import net.minecraft.server.v1_8_R3.PacketPlayOutTitle.EnumTitleAction;

import org.bukkit.Bukkit;
import org.bukkit.Difficulty;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.craftbukkit.v1_8_R3.entity.CraftPlayer;
import org.bukkit.entity.Firework;
import org.bukkit.entity.Player;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitScheduler;
import org.bukkit.scheduler.BukkitTask;
import org.bukkit.scoreboard.Scoreboard;
import org.bukkit.scoreboard.ScoreboardManager;
import org.bukkit.scoreboard.Team;
import org.bukkit.util.Vector;

import fr.Toteltwent.BomberManCraft.Manager.BonusManager;

public class Main extends JavaPlugin {
	public final Logger logger = Logger.getLogger("Minecraft");
	public final MyListener myListener = new MyListener(this);
	public final ScoreboardManager manager = Bukkit.getScoreboardManager();
	public Scoreboard board;
	
	private BukkitTask scoreboardTimer;
	
	public final Vector spectateLocation = new Vector(0, 5, 0);
	public final Vector baseSpawnLocation = new Vector(0, 0, 0);

	private List<Bomber> bombers = new ArrayList<Bomber>();
	
	private Location arenaLocation;
	private int arenaSize;
	private int nbrPlayerMax;
	private final int blockChance = 50;
	
	private int nbrPlayer = 0;
	private int time = 0;
	private boolean started = false;
	
	private Team readyTeam;
	private int spawnUse = 0;
	private boolean root = true;
	
	@Override
    public void onEnable() {
		board = Bukkit.getScoreboardManager().getMainScoreboard();
		
		PluginManager pluginManager = this.getServer().getPluginManager();
		pluginManager.registerEvents(myListener, this);
		
		getCommand("bomber").setExecutor(new MyCommand(this));
		
		loadConfig();

        //si le monde n'existe pas on quitte
        if(arenaLocation.getWorld() == null){
            getLogger().warning("Wrong world ! Configure the config.yml and restart the server.");
    		Bukkit.getServer().getPluginManager().disablePlugin(this);
            return;
        }
		
		getArenaLocation().getWorld().setDifficulty(Difficulty.PEACEFUL);
		getArenaLocation().getWorld().setGameRuleValue("doFireTick", "false");
		
		//ajout des players en bombers
		for(Player player: getArenaLocation().getWorld().getPlayers()){
			new Bomber(this, player);
		}
		
		//ajout d'une team pour voir les joueurs ready
		if(board.getTeam("ready") == null){
			setReadyTeam(board.registerNewTeam("ready"));
			getReadyTeam().setSuffix(" - §aReady");
		}else{
			setReadyTeam(board.getTeam("ready"));
		}
		
		BonusManager.getInstance().removeAllBonus();
		mapGeneration();
	}

    @Override
    public void onDisable() {
    	for(Bomber bomber: getBombers()){
    		bomber.disableBomber();
    	}
    }
    
    private void loadConfig(){
    	//chargement du config.yml
		try {
            if(!getDataFolder().exists()){
                getDataFolder().mkdirs();
            }
            File file = new File(getDataFolder(), "config.yml");
            if (!file.exists()){
                getLogger().info("config.yml not found, creating!");
                
                //initialise les variables et les enregistre dans le config.yml
                setArenaLocation(new Location(Bukkit.getWorld("world"), 0.5, 50, 0.5));
                setArenaSize(21);
                setNbrPlayerMax(4);
                
                getConfig().set("location.world", getArenaLocation().getWorld());
                getConfig().set("location.x", getArenaLocation().getX());
                getConfig().set("location.y", getArenaLocation().getY());
                getConfig().set("location.z", getArenaLocation().getZ());
                getConfig().set("size", getArenaSize());
                getConfig().set("player", getNbrPlayerMax());
                saveDefaultConfig();
            }else{
                getLogger().info("Config.yml found, loading!");
                
                setArenaLocation(new Location(Bukkit.getWorld(getConfig().getString("location.world")), getConfig().getDouble("location.x"), getConfig().getDouble("location.y"), getConfig().getDouble("location.z")));
                setArenaSize(getConfig().getInt("size"));
                setNbrPlayerMax(4);//getConfig().getInt("player")
        	}
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    
    private void scoreboardTimer(){
    	scoreboardTimer = Bukkit.getScheduler().runTaskTimer(this, new Runnable() {
			@Override
			public void run() {
				//scoreboard
				for(Bomber bomber: getBombers()){
					bomber.getScoreboardSign().setObjectiveName("BomberCraft : " + Format(getTime()) + " ");
				}
				
				//preparation
				if(getTime() <= 4){
					PacketPlayOutTitle startTitle;
					if(getTime() == 3){
						setRoot(false);startTitle = new PacketPlayOutTitle(EnumTitleAction.TITLE, ChatSerializer.a("{\"text\":\"GO\",\"color\":\"gold\",\"bold\":true}"), 10, 10, 10);
					}else if(getTime() == 4){
						startTitle = new PacketPlayOutTitle(EnumTitleAction.TITLE, ChatSerializer.a("{\"text\":\"\",\"color\":\"gold\",\"bold\":true}"), 10, 10, 10);
					}else{
						startTitle = new PacketPlayOutTitle(EnumTitleAction.TITLE, ChatSerializer.a("{\"text\":\""+ (3 - getTime()) + "\",\"color\":\"gold\",\"bold\":true}"), 10, 10, 10);
					}
					
			    	for(Bomber bomber: getBombers()){
			    		PlayerConnection connection = ((CraftPlayer)bomber.getPlayer()).getHandle().playerConnection;
			    		connection.sendPacket(startTitle);
			    	}
				}
				

				setTime(time + 1);
			}
		}, 0, 20);
    }
    
    private void mapGeneration(){
    	Random random = new Random();
    	Location location = new Location(getArenaLocation().getWorld(), getArenaLocation().getX() - getArenaSize()/2, getArenaLocation().getY(), getArenaLocation().getZ() - getArenaSize()/2);
    	
    	for(int loopX = 0; loopX < getArenaSize(); loopX++){
    		for(int loopZ = 0; loopZ < getArenaSize(); loopZ++){
    			Location locationTemp = new Location(location.getWorld(), location.getX()+loopX, location.getY(), location.getZ()+loopZ);
    			
    			if(started){
    				//coins pour spawn
    				if(loopX == 0 && (loopZ < 2 || loopZ > getArenaSize()-3)){
    					locationTemp.getBlock().setType(Material.AIR);
    				}else if(loopX == getArenaSize()-1 && (loopZ < 2 || loopZ > getArenaSize()-3)){
    					locationTemp.getBlock().setType(Material.AIR);
    				}else if(loopZ == 0 && (loopX < 2 || loopX > getArenaSize()-3)){
    					locationTemp.getBlock().setType(Material.AIR);
    				}else if(loopZ == getArenaSize()-1 && (loopX < 2 || loopX > getArenaSize()-3)){
    					locationTemp.getBlock().setType(Material.AIR);
    				}//pour fermer les coins
    				else if(loopX == 0 && (loopZ == 2 || loopZ == getArenaSize()-3)){
        				locationTemp.getBlock().setType(Material.COBBLESTONE);
    				}else if(loopX == getArenaSize()-1 && (loopZ == 2 || loopZ == getArenaSize()-3)){
    					locationTemp.getBlock().setType(Material.COBBLESTONE);
    				}else if(loopZ == 0 && (loopX == 2 || loopX == getArenaSize()-3)){
    					locationTemp.getBlock().setType(Material.COBBLESTONE);
    				}else if(loopZ == getArenaSize()-1 && (loopX == 2 || loopX == getArenaSize()-3)){
    					locationTemp.getBlock().setType(Material.COBBLESTONE);
    				}//le reste
    				else{
		    			if(loopX%2 == 1 && loopZ%2 == 1){
		    				locationTemp.getBlock().setType(Material.OBSIDIAN);
		    			}else{
		    				//pourcent de chance d'avoir un block
		    				if(random.nextInt(100) < blockChance){
		        				locationTemp.getBlock().setType(Material.COBBLESTONE);
		    				}else{
		        				locationTemp.getBlock().setType(Material.AIR);
		    				}
		    			}
    				}
    			}else{
    				if(loopX%2 == 1 && loopZ%2 == 1){
    					locationTemp.getBlock().setType(Material.OBSIDIAN);
    				}else{
    					locationTemp.getBlock().setType(Material.AIR);
    				}
    			}
        	}
    	}
    }
    
    public Bomber playerToBomber(Player player){
    	for(Bomber bomber: getBombers()){
			if(bomber.getPlayer().getName().equalsIgnoreCase(player.getName())){
				return bomber;
			}
		}
    	return null;
	}
    
    public static String Format(int time){
		int heure = Math.abs(time / 3600);
		int minute = Math.abs((time % 3600) / 60);
		int seconde = Math.abs(((time % 3600) % 60));

		String heureStr = (heure >= 0 && heure < 10)? "0" + heure : String.valueOf(heure);
		String minuteStr = (minute >= 0 && minute < 10)? "0" + minute : String.valueOf(minute);
		String secondeStr = (seconde >= 0 && seconde < 10)? "0" + seconde : String.valueOf(seconde);

		if(heure > 0)
			return heureStr + ":" + minuteStr + ":" + secondeStr;
		else if(minute > 0)
			return minuteStr + ":" + secondeStr;
		else if(seconde > 0)
			return secondeStr;
		else
			return "00";
	}
    
    public void end(){
	    Firework f = (Firework) getArenaLocation().getWorld().spawn(getArenaLocation().clone().add(spectateLocation), Firework.class);
	    f.detonate();
    	
    	//fin du jeu 3 seconde apres
    	BukkitScheduler scheduler = Bukkit.getServer().getScheduler();
		scheduler.scheduleSyncDelayedTask(this, new Runnable() {
			@Override
			public void run() {
		    	setStarted(false);
			}
		}, 60);
    }

	public boolean isStarted() {
		return started;
	}

	public void setStarted(boolean started) {
		this.started = started;
		if(started){
			setTime(0);
			scoreboardTimer();
			resetSpawnUse();
			setRoot(true);

			for(Bomber bomber: bombers){
				bomber.reset();
			}
		}else{
			scoreboardTimer.cancel();
			setTime(0);
			BonusManager.getInstance().removeAllBonus();
			
			for(Bomber bomber: bombers){
				bomber.getScoreboardSign().setObjectiveName("BomberCraft");
				bomber.reset();
			}
			setNbrPlayer(0);
		}
		mapGeneration();
	}

	public Location getArenaLocation() {
		return arenaLocation;
	}

	public void setArenaLocation(Location arenaLocation) {
		this.arenaLocation = arenaLocation;
	}

	public int getArenaSize() {
		return arenaSize;
	}

	public void setArenaSize(int arenaSize) {
		this.arenaSize = arenaSize;
	}

	public int getTime() {
		return time;
	}

	public void setTime(int time) {
		this.time = time;
	}

	public int getNbrPlayerMax() {
		return nbrPlayerMax;
	}

	public void setNbrPlayerMax(int nbrPlayerMax) {
		this.nbrPlayerMax = nbrPlayerMax;
	}

	public int getNbrPlayer() {
		return nbrPlayer;
	}
	
	public void resetNbrPlayer(){
		nbrPlayer = 0;
	}

	public void setNbrPlayer(int nbrPlayer) {
		this.nbrPlayer = nbrPlayer;
		
		for(Bomber bomber: bombers){
			bomber.getScoreboardSign().setLine(2, "Player: " + getNbrPlayer() + "/" + getNbrPlayerMax());
		}
	}

	public List<Bomber> getBombers() {
		return bombers;
	}

	public void addBombers(Bomber bomber) {
		this.bombers.add(bomber);
	}

	public Team getReadyTeam() {
		return readyTeam;
	}

	public void setReadyTeam(Team readyTeam) {
		this.readyTeam = readyTeam;
	}

	public int getSpawnUse() {
		return spawnUse;
	}
	
	public void resetSpawnUse() {
		spawnUse = 0;
	}

	public void addSpawnUse() {
		spawnUse++;
	}

	public boolean isRoot() {
		return root;
	}

	public void setRoot(boolean root) {
		this.root = root;
	}
}
