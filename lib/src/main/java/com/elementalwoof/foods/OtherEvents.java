package com.elementalwoof.foods;

import org.bukkit.entity.HumanEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.inventory.CraftItemEvent;
import org.bukkit.inventory.CraftingInventory;
import org.bukkit.inventory.ItemStack;

class OtherEvents implements Listener
{
	ElementalFoods plugin;
	OtherEvents(ElementalFoods plugin) 
	{
		this.plugin = plugin;
	}
	
	@EventHandler
	void onItemCraftedEvent(CraftItemEvent event)
	{
		HumanEntity whoClicked = event.getWhoClicked();
		if(whoClicked == null) return;
		
		CraftingInventory craft = event.getInventory();
		if(craft == null) return;

		if(!plugin.isManaged(craft.getResult())) 
		{
			boolean containsCustomItem = false;
			for(ItemStack stack : craft.getContents()) 
			{
				if(stack == craft.getResult()) continue;
				
				if(plugin.isManaged(stack))
				{
					containsCustomItem = true;
					break;
				}
			}
			
			if(containsCustomItem) 
			{
				whoClicked.sendMessage(plugin.pluginPrefix + "\u00a7c\u00a7lThis is a custom item, it cannot be used to craft normal items!");
				event.setCancelled(true);
				craft.setResult(null);
			}
		}
	}
	
	@EventHandler
	void onBlockPlaced(BlockPlaceEvent event) 
	{
		Player player = event.getPlayer();
		if(player == null) return;
		
		ItemStack item = event.getItemInHand();
		if(item == null) return;

		if(!plugin.ensureItemInMainHand(player, item, true))
		{
			event.setCancelled(true);
			return;
		}
		
		if(plugin.isManaged(item)) 
		{
			event.setCancelled(true);
			
			CustomItem ci = plugin.getFromStack(item);
			
			plugin.feedPlayer(player, item, ci);
		}
	}
}
