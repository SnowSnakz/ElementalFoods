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

		if(plugin.isManaged(item)) 
		{
			Material itemType = item.getType();
			
			if(event.getClickedBlock() != null) 
			{
				Material clickedType = event.getClickedBlock().getType();
				switch(clickedType) 
				{
				default:
					break;
					
				case LODESTONE:
					// Prevents custom compasses from being aligned to lodestones
					if(itemType == Material.COMPASS) 
					{
						event.setCancelled(true);
					}
					break;
					
				case RESPAWN_ANCHOR:
					// Prevents custom glowstone from being accepted into respawn anchors
					if(itemType == Material.GLOWSTONE) 
					{
						event.setCancelled(true);
					}
					break;
					
				case JUKEBOX:
					// Prevents custom records from being accepted into jukeboxes
					if(itemType.isRecord()) 
					{
						event.setCancelled(true);
					}
					break;
					
				// Allow blocks with a GUI to be opened
				case CRAFTING_TABLE:
				case CARTOGRAPHY_TABLE:
				case SMITHING_TABLE:
				case FLETCHING_TABLE: // Although this block does not have a GUI yet, it probably will have one eventually.
				case FURNACE:
				case BLAST_FURNACE:
				case SMOKER:
				case LOOM:
				case BREWING_STAND:
				case STONECUTTER:
				case GRINDSTONE:
				case CHEST:
				case BARREL:
				case ANVIL:
					// Preserve Interactions
					return;
					
				case CAULDRON:
					// Prevent players from filling cauldron with custom water buckets and prevents cauldron from filling custom bottles
					if(itemType == Material.WATER_BUCKET || itemType == Material.GLASS_BOTTLE) 
					{
						event.setCancelled(true);
					}
					
					// Prevent players from washing custom leather items
					if(itemType == Material.LEATHER_BOOTS || itemType == Material.LEATHER_CHESTPLATE || itemType == Material.LEATHER_HELMET || itemType == Material.LEATHER_LEGGINGS || itemType == Material.LEATHER_HORSE_ARMOR)
					{
						event.setCancelled(true);
					}
					break;
				}
			}
			
			if(!plugin.ensureItemInMainHand(player, item, true))
			{
				event.setCancelled(true);
				return;
			}
		
			CustomItem ci = plugin.getFromStack(item);
			
			if(ci == null) 
			{
				event.setCancelled(true);
				return;
			}
			
			boolean canEatRightNow = item.getType().isEdible() || item.getType() == Material.POTION;
			canEatRightNow = !(canEatRightNow && (player.getFoodLevel() < 20));
			canEatRightNow |= ci.instantEat;
			
			if(canEatRightNow)
			{
				event.setCancelled(true);
				plugin.feedPlayer(player, item, ci);
			}
		}
	}
}
