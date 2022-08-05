package com.elementalwoof.foods;

import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.SoundCategory;
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
		
		item = player.getInventory().getItemInMainHand();
		
		if(plugin.isManaged(item)) 
		{
			event.setCancelled(true);
			
			CustomItem ci = plugin.getFromStack(item);
			
			if(ci == null) 
			{
				return;
			}
			
			if(event.getPlayer().getFoodLevel() < 20)
			{
				if(ci.isEdible) 
				{
					int newFoodValue = ci.foodValue;
					float newSaturationValue = ci.saturationValue;

					player.setFoodLevel(player.getFoodLevel() + newFoodValue);
					player.setSaturation(player.getSaturation() + newSaturationValue);
					
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
						if(item.getType() == Material.POTION)
						{
							player.getInventory().setItemInMainHand(new ItemStack(Material.GLASS_BOTTLE));
						}
						else
						{
							item.setAmount(item.getAmount() - 1);
							player.getInventory().setItemInMainHand(item);
						}
					}

					Sound eatSound = Sound.ENTITY_GENERIC_EAT;
					
					if(ci.makesDrinkingNoise) 
					{
						eatSound = Sound.ENTITY_GENERIC_DRINK;
					}

					player.playSound(player, eatSound, SoundCategory.PLAYERS, 1f, ci.eatPitchBase + (plugin.rng.nextFloat() * ci.eatPitchRange) - (ci.eatPitchRange * 0.5f));
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
						if(item.getType() == Material.POTION)
						{
							player.getInventory().setItemInMainHand(new ItemStack(Material.GLASS_BOTTLE));
						}
						else
						{
							item.setAmount(item.getAmount() - 1);
							player.getInventory().setItemInMainHand(item);
						}
					}
					
					Sound eatSound = Sound.ENTITY_GENERIC_EAT;
					
					if(ci.makesDrinkingNoise) 
					{
						eatSound = Sound.ENTITY_GENERIC_DRINK;
					}

					player.playSound(player, eatSound, SoundCategory.PLAYERS, 1f, ci.eatPitchBase + (plugin.rng.nextFloat() * ci.eatPitchRange) - (ci.eatPitchRange * 0.5f));
				}
			}
		}
	}
	
	@EventHandler
	void onPlayerInteract(PlayerInteractEvent event) 
	{
		Player player = event.getPlayer();
		if(player == null) return;
		
		ItemStack item = event.getItem();
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
			CustomItem ci = plugin.getFromStack(item);
			
			boolean canEatRightNow = !item.getType().isEdible();
			canEatRightNow &= player.getFoodLevel() < 20;
			
			canEatRightNow |= item.getType() != Material.POTION;
			canEatRightNow |= ci.instantEat;
			
			if(canEatRightNow)
			{
				event.setCancelled(true);
				
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
							if(item.getType() == Material.POTION)
							{
								player.getInventory().setItemInMainHand(new ItemStack(Material.GLASS_BOTTLE));
							}
							else
							{
								item.setAmount(item.getAmount() - 1);
								player.getInventory().setItemInMainHand(item);
							}
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
							if(item.getType() == Material.POTION)
							{
								player.getInventory().setItemInMainHand(new ItemStack(Material.GLASS_BOTTLE));
							}
							else
							{
								item.setAmount(item.getAmount() - 1);
								player.getInventory().setItemInMainHand(item);
							}
						}
					}
				}
			}
		}
	}
}
