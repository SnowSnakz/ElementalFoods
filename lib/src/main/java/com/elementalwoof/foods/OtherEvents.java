package com.elementalwoof.foods;

import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.SoundCategory;
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

		if(player.getInventory().getItemInOffHand().isSimilar(item))
		{
			if(plugin.isManaged(item)) 
			{
				player.sendMessage(plugin.pluginPrefix + "\u00a7cCustom food items can only be consumed while in your main hand!");
				event.setCancelled(true);
				return;
			}
		}
		
		if(!player.getInventory().getItemInMainHand().isSimilar(item))
		{
			if(plugin.isManaged(item)) 
			{
				player.sendMessage(plugin.pluginPrefix + "\u00a7cCustom food items can only be consumed while in your main hand!");
				event.setCancelled(true);
				return;
			}
		}
		
		if(plugin.isManaged(item)) 
		{
			event.setCancelled(true);
			
			CustomItem ci = plugin.getFromStack(item);
			
			if(event.getPlayer().getFoodLevel() < 20)
			{
				if(ci.isEdible) 
				{
					int newFoodValue = ci.foodValue;
					float newSaturationValue = ci.saturationValue;

					player.setFoodLevel(player.getFoodLevel() + newFoodValue);
					player.setSaturation(player.getSaturation() + newSaturationValue);
					
					Sound eatSound = Sound.ENTITY_GENERIC_EAT;
					
					if(ci.makesDrinkingNoise) 
					{
						eatSound = Sound.ENTITY_GENERIC_DRINK;
					}

					player.playSound(player, eatSound, SoundCategory.PLAYERS, 1f, ci.eatPitchBase + (plugin.rng.nextFloat() * ci.eatPitchRange) - (ci.eatPitchRange * 0.5f));
					
					if(item.getAmount() - 1 <= 0) 
					{
						if(item.getType() == Material.POTION)
						{
							player.getInventory().setItemInMainHand(new ItemStack(Material.GLASS_BOTTLE));
						}
						else
						{
							player.getInventory().setItemInMainHand(null);
						}
					}
					else
					{
						item.setAmount(item.getAmount() - 1);
						player.getInventory().setItemInMainHand(item);
					}

					if(item.getType() == Material.POTION)
					{
						player.getInventory().setItemInMainHand(new ItemStack(Material.GLASS_BOTTLE));
					}
				}
			}
			else
			{
				if(ci.canEatWhenFull)
				{
					int newFoodValue = ci.foodValue;
					float newSaturationValue = ci.saturationValue;

					player.setFoodLevel(player.getFoodLevel() + newFoodValue);
					player.setSaturation(player.getSaturation() + newSaturationValue);
					
					Sound eatSound = Sound.ENTITY_GENERIC_EAT;
					
					if(ci.makesDrinkingNoise) 
					{
						eatSound = Sound.ENTITY_GENERIC_DRINK;
					}

					player.playSound(player, eatSound, SoundCategory.PLAYERS, 1f, ci.eatPitchBase + (plugin.rng.nextFloat() * ci.eatPitchRange) - (ci.eatPitchRange * 0.5f));

					if(item.getAmount() - 1 <= 0) 
					{
						if(item.getType() == Material.POTION)
						{
							player.getInventory().setItemInMainHand(new ItemStack(Material.GLASS_BOTTLE));
						}
						else
						{
							player.getInventory().setItemInMainHand(null);
						}
					}
					else
					{
						item.setAmount(item.getAmount() - 1);
						player.getInventory().setItemInMainHand(item);
					}
					
					if(item.getType() == Material.POTION)
					{
						player.getInventory().setItemInMainHand(new ItemStack(Material.GLASS_BOTTLE));
					}
				}
			}
		}
	}
}
