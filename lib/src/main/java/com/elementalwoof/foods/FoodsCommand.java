package com.elementalwoof.foods;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;

class FoodsCommand implements CommandExecutor
{
	ElementalFoods plugin;
	
	FoodsCommand(ElementalFoods plugin) 
	{
		this.plugin = plugin;
	}
	
	@Override
	public boolean onCommand(CommandSender sender, Command command, String label, String[] args) 
	{
		if(!sender.hasPermission("elementalfoods.command")) 
		{
			return false;
		}
		
		boolean sendVersionString = true;
		if(args.length != 0)
		{
			if(args[0].equalsIgnoreCase("reload")) 
			{
				if(sender.hasPermission("elementalfoods.reload")) 
				{
					sendVersionString = false;
					
					sender.sendMessage("\u00a7cReloading...");
					
					plugin.reload();
					
					if(plugin.hadErrors) 
					{
						sender.sendMessage("\u00a7c\u00a7nPlugin finished reloading with errors! \u00a7cPlease review the server log for more details!");
					}
					else 
					{
						sender.sendMessage("\u00a7a\u00a7lThe plugin was reloaded successfuly!");
					}
				}
			}
		}
		
		if(sendVersionString) 
		{
			sender.sendMessage(plugin.pluginPrefix + "You are running version " + plugin.getDescription().getVersion());
		}
		
		return true;
	}
}
