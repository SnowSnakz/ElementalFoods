package com.elementalwoof.foods;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.List;
import java.util.UUID;
import java.util.logging.Logger;

import org.bukkit.Bukkit;
import org.bukkit.Color;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.RecipeChoice;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.LeatherArmorMeta;
import org.bukkit.inventory.meta.PotionMeta;
import org.bukkit.inventory.meta.SkullMeta;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.profile.PlayerProfile;

import net.md_5.bungee.api.ChatColor;

public class CustomItem 
{
	NamespacedKey recipeKey;
	
	String itemId;
	String headTexture;
	
	int foodValue;
	float saturationValue;
	
	boolean isEdible;
	boolean canEatWhenFull;
	
	boolean isHeadItem;
	boolean instantEat;
	boolean makesDrinkingNoise;
	
	ItemStack referenceItem;
	CustomRecipe recipe;
	
	float eatPitchRange;
	float eatPitchBase;
	
	RecipeChoice choice;
	
	ElementalFoods plugin;
	ConfigurationSection cfg;
	
	public CustomItem(ElementalFoods plugin, ConfigurationSection cfg) throws NoSuchFieldException, InvalidConfigurationException
	{
		this.plugin = plugin;
		this.cfg = cfg;
		
		Logger l = plugin.getLogger();
		
		itemId = cfg.getString("id");
		if(itemId == null) 
		{
			throw new NullPointerException("The custom item does not have the required field: 'id'");
		}
		
		String displayName = cfg.getString("display-name");
		if(displayName == null) 
		{
			throw new NullPointerException("The custom item does not have the required field: 'display-name'");
		}
		displayName = "\u00a7f" + ChatColor.translateAlternateColorCodes('&', displayName);
		
		List<String> lore = cfg.getStringList("lore");
		if(lore != null) 
		{
			for(int i = 0; i < lore.size(); i++) 
			{
				String loreLine = lore.get(i);
				loreLine = ChatColor.translateAlternateColorCodes('&', loreLine);
				lore.set(i, loreLine);
			}
		}
		
		foodValue = cfg.getInt("food-value");
		saturationValue = (float)cfg.getDouble("saturation-value"); // Why isn't there a `cfg.getFloat()`...?
		
		eatPitchBase = (float)cfg.getDouble("sound-pitch-base");
		eatPitchRange = (float)cfg.getDouble("sound-pitch-range");
		
		if(eatPitchBase < 0.5f) 
		{
			eatPitchBase = 1f;
		}
		
		if(eatPitchRange < 0) 
		{
			eatPitchRange = Math.abs(eatPitchRange);
		}
		
		if(eatPitchRange > 0.5f) 
		{
			eatPitchRange = 0.5f;
		}
		
		isEdible = cfg.getBoolean("is-edible");
		canEatWhenFull = cfg.getBoolean("is-edible-when-full");
		instantEat = cfg.getBoolean("instant-eat");
		
		makesDrinkingNoise = cfg.getBoolean("is-drink");
		
		boolean isGlowing = cfg.getBoolean("is-glowing");
		
		String itemType = cfg.getString("material");
		isHeadItem = itemType.equalsIgnoreCase("PLAYER_HEAD");
		
		if(isHeadItem) 
		{
			referenceItem = new ItemStack(Material.PLAYER_HEAD);

			SkullMeta meta = (SkullMeta)referenceItem.getItemMeta();
			
			headTexture = cfg.getString("texture");
			PlayerProfile pp = Bukkit.createPlayerProfile(UUID.randomUUID());
			try 
			{
				pp.getTextures().setSkin(new URL("https://textures.minecraft.net/texture/" + headTexture));
			} 
			catch (MalformedURLException e) 
			{
				l.warning("Unable to set head texture for item: " + itemId);
				e.printStackTrace();
			}
			
			meta.setOwnerProfile(pp);
			
			referenceItem.setItemMeta(meta);
		}
		else
		{
			Material m = Material.getMaterial(itemType.toUpperCase());
			if(m == null)
			{
				throw new InvalidConfigurationException("Invalid material '" + itemType + "' in custom item: " + itemId);
			}
			
			referenceItem = new ItemStack(m);
			
			boolean hasColor = cfg.contains("color");
			if(hasColor)
			{
				int r, g, b;
				r = cfg.getInt("color.red");
				g = cfg.getInt("color.green");
				b = cfg.getInt("color.blue");
				
				switch(m)
				{
				case POTION:
	
					PotionMeta potionMeta = (PotionMeta)referenceItem.getItemMeta();
					
					potionMeta.setColor(Color.fromBGR(b, g, r));
					referenceItem.setItemMeta(potionMeta);
					break;
					
				case LEATHER_BOOTS:
				case LEATHER_LEGGINGS:
				case LEATHER_CHESTPLATE:
				case LEATHER_HELMET:
				case LEATHER_HORSE_ARMOR:
					LeatherArmorMeta letherMeta = (LeatherArmorMeta)referenceItem.getItemMeta();
					
					letherMeta.setColor(Color.fromBGR(b, g, r));
					referenceItem.setItemMeta(letherMeta);
					break;
					
				default:
					break;
				}
			}
		}
		
		ItemMeta meta = referenceItem.getItemMeta();
		
		PersistentDataContainer pdc = meta.getPersistentDataContainer();
		pdc.set(NamespacedKey.fromString("is_custom_item", plugin), PersistentDataType.BYTE, (byte)1);
		pdc.set(NamespacedKey.fromString("custom_id", plugin), PersistentDataType.STRING, itemId);
		
		meta.setDisplayName(displayName);
		if(lore != null) 
		{
			meta.setLore(lore);
		}
		
		if(isGlowing) 
		{
			meta.addItemFlags(ItemFlag.HIDE_ENCHANTS);
			meta.addEnchant(Enchantment.PROTECTION_ENVIRONMENTAL, 1, true);
		}
		
		referenceItem.setItemMeta(meta);
		
		choice = new RecipeChoice.ExactChoice(referenceItem);
	}
	
	public RecipeChoice getAsRecipeChoice() 
	{
		return choice;
	}
	
	public NamespacedKey createRecipe() throws InvalidConfigurationException
	{
		Logger l = plugin.getLogger();
		
		if(cfg.contains("crafting-recipe"))
		{
			try 
			{
				recipe = new CustomRecipe(plugin, this, cfg.getConfigurationSection("crafting-recipe"));
			} 
			catch (NoSuchFieldException e) 
			{
				l.severe("Unable to create crafting recipe for custom item: " + itemId);
				e.printStackTrace();
				
				return null;
			}
			
			recipeKey = recipe.getKey();
		}
		
		return recipeKey;
	}
}
