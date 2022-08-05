package com.elementalwoof.foods;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Random;
import java.util.Set;
import java.util.logging.Logger;

import org.bukkit.Bukkit;
import org.bukkit.NamespacedKey;
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

import net.md_5.bungee.api.ChatColor;

public class ElementalFoods extends JavaPlugin
{
	Map<String, CustomItem> customItems;
	Logger l;
	
	String pluginPrefix;
	boolean hadErrors;
	
	InventoryEvents inventoryEvents;
	PlayerEvents playerEvents;
	OtherEvents otherEvents;
	
	FoodsCommand command;
	
	Random rng;
	
	void ensureUnlocks(Player player) 
	{
		for(CustomItem ci : customItems.values()) 
		{
			if(ci.recipe != null)
			{
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
	
	public CustomItem getCustomItem(String itemId) 
	{
		return customItems.getOrDefault(itemId, null);
	}
	
	public boolean isManaged(ItemStack stack) 
	{
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
		
		customItems.clear();
		
		FileConfiguration cfg = getConfig();
		
		pluginPrefix = cfg.getString("chat-prefix", pluginPrefix);
		pluginPrefix = ChatColor.translateAlternateColorCodes('&', pluginPrefix);
		
		Set<NamespacedKey> previousRecipes = new HashSet<>();

		for(CustomItem ci : customItems.values()) 
		{
			if(ci.recipe != null) 
			{
				previousRecipes.add(ci.recipeKey);
			}
		}
		
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
