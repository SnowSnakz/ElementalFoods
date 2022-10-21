package com.elementalwoof.foods;

import java.util.ArrayList;
import java.util.List;

import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;

class FoodsCommand implements CommandExecutor, TabCompleter
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

	@Override
	public List<String> onTabComplete(CommandSender sender, Command command, String label, String[] args) 
	{
		List<String> completions = new ArrayList<>();
		
		if(!sender.hasPermission("elementalfoods.command")) 
		{
			return completions;
		}
		
		if(args.length == 0) 
		{
			if(sender.hasPermission("elementalfoods.reload")) 
			{
				completions.add("reload");
			}
			
			if(sender.hasPermission("elementalfoods.command.give")) 
			{
				completions.add("give");
			}
		}
		else 
		{
			if(args.length == 1) 
			{
				switch(args[0].toLowerCase().charAt(0)) 
				{
				default: 
					completions.add("give");
					completions.add("reload");
					break;
				
				case 'r':
					completions.add("reload");
					break;
					
				case 'g':
					completions.add("give");
					break;
				}
			}
			else
			{
				if(args[0].equalsIgnoreCase("give"))
				{
					if(!sender.hasPermission("elementalfoods.command.give"))
					{
						return completions;
					}
					
					switch(args.length) 
					{
					default:
						break;
						
					case 2:
						if(args[1].isBlank()) 
						{
							for(Player p : Bukkit.getOnlinePlayers()) 
							{
								completions.add(p.getName());
							}
						}
						else
						{
							for(Player p : Bukkit.getOnlinePlayers()) 
							{
								if(p.getName().toLowerCase().startsWith(args[1].toLowerCase()))
								{
									completions.add(p.getName());
								}
							}
						}
						break;
						
					case 3:
						if(args[2].isBlank()) 
						{
							for(String itemId : plugin.customItems.keySet()) 
							{
								completions.add(itemId);
							}
						}
						else 
						{
							for(String itemId : plugin.customItems.keySet()) 
							{
								if(itemId.toLowerCase().startsWith(args[2].toLowerCase()))
								{
									completions.add(itemId);
								}
							}
						}
						break;
					}
				}
			}
		}
		
		return completions;
	}
}
