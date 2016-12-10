package fr.Toteltwent.BomberManCraft;

import net.md_5.bungee.api.ChatColor;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;

public class MyCommand implements CommandExecutor {
    private final Main plugin;

    public MyCommand(Main instance){
        plugin = instance;
    }
    
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args){
        if(args.length == 0 || args.length > 1){
            return false;
        }

        if(args[0].equalsIgnoreCase("start")){
			if(plugin.isStarted() == false){
				if(plugin.getNbrPlayer() > 4 || plugin.getNbrPlayer() <= 1){
					sender.sendMessage(ChatColor.RED+"Invalid number of players !");
					return true;
				}
				
				plugin.setStarted(true);
				
				sender.sendMessage("Game started !");
			}else{
				sender.sendMessage(ChatColor.RED+"Game already started !");
			}
			return true;
		}
		else if(args[0].equalsIgnoreCase("stop")){
			if(plugin.isStarted()){
				plugin.setStarted(false);
				
				sender.sendMessage("Game stopped !");
			}else{
				sender.sendMessage(ChatColor.RED+"Game already stopped !");
			}
			return true;
		}
		return false;
    }
}