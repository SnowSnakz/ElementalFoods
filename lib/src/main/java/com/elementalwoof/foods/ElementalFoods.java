package com.elementalwoof.foods;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Random;
import java.util.Set;
import java.util.logging.Logger;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.Sound;
import org.bukkit.SoundCategory;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;

import me.clip.placeholderapi.PlaceholderAPI;
import net.md_5.bungee.api.ChatColor;

public class ElementalFoods extends JavaPlugin
{
	Map<String, CustomItem> customItems;
	Logger l;
	
	Metrics metrics;
	
	String pluginPrefix;
	boolean hadErrors;
	
	InventoryEvents inventoryEvents;
	PlayerEvents playerEvents;
	OtherEvents otherEvents;
	
	FoodsCommand command;
	
	Random rng;
	
	boolean hasPlaceholderApi;
	boolean unlockRecipesOnJoin;
	
	String setPlaceholders(String input, Player player) 
	{
		if(hasPlaceholderApi) 
		{
			return PlaceholderAPI.setPlaceholders(player, input);
		}
		else
		{
			return input.replace("%player%", player.getName());
		}
	}
	
	void ensureUnlocks(Player player) 
	{
		if(!unlockRecipesOnJoin) return;
		
		for(CustomItem ci : customItems.values()) 
		{
			if(ci.recipe != null)
			{
				// HotFix for #2
				if(ci.recipeKey == null)
					continue;

				if(ci.recipe.automaticallyUnlock) 
				{
					if(!player.hasDiscoveredRecipe(ci.recipeKey)) 
					{
						player.discoverRecipe(ci.recipeKey);
					}
				}
			}
		}
	}
	
	boolean ensureItemInMainHand(Player player, ItemStack item, boolean sendWarning)
	{
		boolean result = true;
		if(isManaged(item)) 
		{
			if(player.getInventory().getItemInOffHand().isSimilar(item))
			{
				result = false;
			}
			if(!player.getInventory().getItemInMainHand().isSimilar(item))
			{
				result = false;
			}
		}
		
		if(sendWarning && !result) 
		{
			player.sendMessage(pluginPrefix + "\u00a7cCustom food items can only be consumed while in your main hand!");
		}
		
		return result;
	}
	
	void feedPlayer(Player player, ItemStack item, CustomItem ci) 
	{
		boolean doEat = false;
		if(player.getFoodLevel() < 20)
		{
			if(ci.isEdible) 
			{
				doEat = true;
			}
		}
		else
		{
			if(ci.canEatWhenFull)
			{
				doEat = true;
			}
		}
		
		if(doEat) 
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

			if(ci.commands != null)
			{
				for(String cmd : ci.commands)
				{
					String rcmd = cmd;
					boolean asPlayer = true;
					
					if(cmd.startsWith("srv:"))
					{
						rcmd = cmd.substring(4);
						asPlayer = false;
					}
					else if(cmd.startsWith("plr:"))
					{
						rcmd = cmd.substring(4);
						asPlayer = true;
					}
					
					rcmd = setPlaceholders(rcmd, player);
					
					if(asPlayer) 
					{
						Bukkit.dispatchCommand(player, rcmd);
					}
					else 
					{
						Bukkit.dispatchCommand(Bukkit.getConsoleSender(), rcmd);
					}
				}
			}
			
			player.playSound(player, eatSound, SoundCategory.PLAYERS, 1f, ci.eatPitchBase + (rng.nextFloat() * ci.eatPitchRange) - (ci.eatPitchRange * 0.5f));
			
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
	
	public CustomItem getCustomItem(String itemId) 
	{
		return customItems.getOrDefault(itemId, null);
	}
	
	public boolean isManaged(ItemStack stack) 
	{
		if(stack == null) return false;
		
		ItemMeta meta = stack.getItemMeta();
		if(meta == null) return false;
		
		PersistentDataContainer pdc = meta.getPersistentDataContainer();
		Byte isCustomItem = pdc.get(NamespacedKey.fromString("is_custom_item", this), PersistentDataType.BYTE);
		
		if(isCustomItem != null) 
		{
			if(isCustomItem != 0) 
			{
				return true;
			}
		}
		
		return false;
	}
	
	public CustomItem getFromStack(ItemStack stack) 
	{
		ItemMeta meta = stack.getItemMeta();
	
		if(meta == null) return null;
		
		PersistentDataContainer pdc = meta.getPersistentDataContainer();
		Byte isCustomItem = pdc.get(NamespacedKey.fromString("is_custom_item", this), PersistentDataType.BYTE);
		
		if(isCustomItem != null) 
		{
			if(isCustomItem != 0) 
			{
				String itemId = pdc.get(NamespacedKey.fromString("custom_id", this), PersistentDataType.STRING);
				return customItems.getOrDefault(itemId, null);
			}
		}
		
		return null;
	}
	
	@Override
	public void onEnable()
	{
		metrics = new Metrics(this, 16831);
		
		pluginPrefix = "\u00a73\u00a7l[\u00a7bElemental\u00a7cFoods\u00a73\u00a7l]\u00a77: \u00a7f";
		
		customItems = new HashMap<>();
		l = getLogger();
		
		rng = new Random();
		
		reload();
		
		inventoryEvents = new InventoryEvents(this);
		playerEvents = new PlayerEvents(this);
		otherEvents = new OtherEvents(this);
		
		PluginManager pm = Bukkit.getPluginManager();
		pm.registerEvents(inventoryEvents, this);
		pm.registerEvents(playerEvents, this);
		pm.registerEvents(otherEvents, this);
		
		command = new FoodsCommand(this);
		Bukkit.getPluginCommand("foods").setExecutor(command);
	}
	
	public void reload() 
	{
		hadErrors = false;
		
		saveDefaultConfig();
		reloadConfig();
		
		Set<NamespacedKey> previousRecipes = new HashSet<>();
		for(CustomItem ci : customItems.values()) 
		{
			if(ci.recipe != null) 
			{
				previousRecipes.add(ci.recipeKey);
			}
		}
		
		customItems.clear();
		
		FileConfiguration cfg = getConfig();
		
		pluginPrefix = cfg.getString("chat-prefix", pluginPrefix);
		pluginPrefix = ChatColor.translateAlternateColorCodes('&', pluginPrefix);

		hasPlaceholderApi = Bukkit.getPluginManager().getPlugin("PlaceholderAPI") != null;
		
		ConfigurationSection itemsSection = cfg.getConfigurationSection("items");
		Set<String> itemKeys = itemsSection.getKeys(false);
		for(String itemKey : itemKeys) 
		{
			ConfigurationSection itemSection = itemsSection.getConfigurationSection(itemKey);
			
			CustomItem ci = null;
			try 
			{
				ci = new CustomItem(this, itemSection);
			}
			catch(NoSuchFieldException | InvalidConfigurationException ex) 
			{
				l.severe("Unable to create custom item for items:" + itemKey + " in the configuration.");
				ex.printStackTrace();
				
				hadErrors = true;
				
				continue;
			}
			
			if(ci != null) 
			{
				customItems.put(ci.itemId, ci);
			}
		}
		
		for(CustomItem ci : customItems.values()) 
		{
			try
			{
				ci.createRecipe();
			} 
			catch (InvalidConfigurationException e)
			{
				l.severe("Unable to create recipe for custom item:" + ci.itemId);
				e.printStackTrace();
				
				hadErrors = true;
				
				continue;
			}
			
			if(ci.recipe != null) 
			{
				if(ci.recipeKey == null)
				{
					if(ci.recipe.key == null)
						continue;
					
					ci.recipeKey = ci.recipe.key;
				}
				
				if(Bukkit.getRecipe(ci.recipeKey) != null) 
				{
					Bukkit.removeRecipe(ci.recipeKey);
				}
				
				Bukkit.addRecipe(ci.recipe.recipe);
			}
		}
		
		Set<NamespacedKey> removedRecipes = new HashSet<>();
		for(NamespacedKey previousRecipe : previousRecipes) 
		{
			boolean found = false;

			for(CustomItem ci : customItems.values()) 
			{
				if(ci.recipeKey.equals(previousRecipe)) 
				{
					found = true;
					break;
				}
			}
			
			if(!found) 
			{
				removedRecipes.add(previousRecipe);
				Bukkit.removeRecipe(previousRecipe);
			}
		}
		
		unlockRecipesOnJoin = true;
		if(cfg.contains("unlock-recipes-on-join"))
		{
			unlockRecipesOnJoin = cfg.getBoolean("unlock-recipes-on-join");
		}
		
		for(Player player : Bukkit.getOnlinePlayers()) 
		{
			ensureUnlocks(player);
			
			for(NamespacedKey removedRecipe : removedRecipes) 
			{
				player.undiscoverRecipe(removedRecipe);
			}
		}
	}
}
