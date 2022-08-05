package com.elementalwoof.foods;

import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerItemConsumeEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.inventory.ItemStack;

class PlayerEvents implements Listener
{
	ElementalFoods plugin;
	PlayerEvents(ElementalFoods plugin) 
	{
		this.plugin = plugin;
	}
	
	@EventHandler
	void onPlayerJoin(PlayerJoinEvent event) 
	{
		Player player = event.getPlayer();
		if(player == null) return;
		
		plugin.ensureUnlocks(player);
	}
	
	@EventHandler
	void onPlayerEatFood(PlayerItemConsumeEvent event) 
	{
		Player player = event.getPlayer();
		if(player == null) return;
		
		ItemStack item = event.getItem();
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
			if(ci == null) return;
			
			plugin.feedPlayer(player, item, ci);
		}
	}
	
	@EventHandler
	void onPlayerInteract(PlayerInteractEvent event) 
	{
		Player player = event.getPlayer();
		if(player == null) return;
		
		ItemStack item = event.getItem();
		if(item == null) return;
		
		if(!plugin.ensureItemInMainHand(player, item, true))
		{
			event.setCancelled(true);
			return;
		}
		
		if(plugin.isManaged(item)) 
		{
			CustomItem ci = plugin.getFromStack(item);
			
			boolean canEatRightNow = !item.getType().isEdible();
			canEatRightNow &= player.getFoodLevel() < 20;
			
			canEatRightNow |= item.getType() != Material.POTION;
			canEatRightNow |= ci.instantEat;
			
			if(canEatRightNow)
			{
				event.setCancelled(true);
				plugin.feedPlayer(player, item, ci);
			}
		}
	}
}
