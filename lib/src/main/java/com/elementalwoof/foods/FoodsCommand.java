package com.elementalwoof.foods;

import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;

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
			switch(args[0].toLowerCase())
			{
			case "reload":
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
				break;
				
			case "give":
				if(sender.hasPermission("elementalfoods.command.give")) 
				{
					sendVersionString = false;
					
					if(args.length < 3) 
					{
						sender.sendMessage("\u00a74\u00a7lUsage: \u00a74/" + label + " give <target> <food_id> [<amount>]");
					}
					else
					{
						String target = args[1];
						Player pt = Bukkit.getPlayer(target);
						
						if(pt == null) 
						{
							sender.sendMessage("\u00a7cPlayer \"" + pt + "\" does not exist, or is not online.");
						}
						else
						{
							String foodId = args[2];
							int amount = 1;
							
							if(args.length >= 4) 
							{
								try 
								{
									amount = Integer.parseInt(args[3]);
								}
								catch(NumberFormatException nfex)
								{
									sender.sendMessage("\u00a7cInvalid input for <amount>, only whole numbers are accepted.");
									break;
								}
							}
							
							if(amount < 1) 
							{
								sender.sendMessage("\u00a7cInvalid input for <amount>, the value must be at least 1.");
								break;
							}
							
							CustomItem item = plugin.customItems.get(foodId);
							if(item == null) 
							{
								sender.sendMessage("\u00a7cUnknown Food Item: \"" + foodId + "\"");
							}
							else 
							{
								sender.sendMessage("\u00a7rGiving " + pt.getDisplayName() + "\u00a7r " + item.referenceItem.getItemMeta().getDisplayName() + "\u00a7r x" + amount);
								
								PlayerInventory inventory = pt.getInventory();
								for(int i = amount; i > 0; i -= 64) 
								{
									ItemStack is = item.referenceItem.clone();
									is.setAmount(Math.min(i, 64));
									inventory.addItem(is);
								}
							}
						}
					}
				}
				break;
			}
		}
		
		if(sendVersionString) 
		{
			sender.sendMessage(plugin.pluginPrefix + "You are running version " + plugin.getDescription().getVersion());
		}
		
		return true;
	}
}
