package com.elementalwoof.foods;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.inventory.Recipe;
import org.bukkit.inventory.RecipeChoice;
import org.bukkit.inventory.ShapedRecipe;
import org.bukkit.inventory.ShapelessRecipe;

public class CustomRecipe 
{
	NamespacedKey key;
	boolean isShapeless;
	boolean automaticallyUnlock;
	Recipe recipe;
	
	Map<Character, RecipeChoice> recipeChoices;
	
	public CustomRecipe(ElementalFoods plugin, CustomItem item, ConfigurationSection cfg) throws NoSuchFieldException, InvalidConfigurationException 
	{
		key = NamespacedKey.fromString(item.itemId, plugin);
		isShapeless = cfg.getBoolean("is-shapeless", true);
		automaticallyUnlock = cfg.getBoolean("show-in-recipe-list", true);
		
		ConfigurationSection matSection = cfg.getConfigurationSection("materials");
		if(matSection == null)
		{
			throw new NoSuchFieldException("Crafting recipe is missing required field: 'materials'");
		}
		
		Set<String> matKeys = matSection.getKeys(false);
		if(matKeys == null) 
		{
			throw new NoSuchFieldException("Crafting recipe is missing required field: 'materials'");
		}
		
		if(matKeys.size() <= 0) 
		{
			throw new InvalidConfigurationException("Crafting recipe does not specify the required materials...");
		}
		
		recipeChoices = new HashMap<>();
		for(String matKey : matKeys) 
		{
			char keyChar = matKey.charAt(0);
			if(recipeChoices.containsKey(keyChar)) 
			{
				throw new InvalidConfigurationException("Duplicate entry '" + keyChar + "' in material definitions (please note that only the first character is used)");
			}
			
			String matId = matSection.getString(matKey);
			
			Material m = Material.getMaterial(matId);
			if(m == null) 
			{
				CustomItem craftingItem = plugin.getCustomItem(matId);
				if(craftingItem == null) 
				{
					throw new InvalidConfigurationException("Invalid material '" + matId + "' in custom recipe: " + item.itemId);
				}
				
				recipeChoices.put(keyChar, craftingItem.choice);
			}
			else 
			{
				recipeChoices.put(keyChar, new RecipeChoice.MaterialChoice(m));
			}
		}
		
		if(isShapeless)
		{
			ShapelessRecipe r = new ShapelessRecipe(key, item.referenceItem);
			recipe = r;

			Map<Character, Integer> ingredientCount = new HashMap<>();
			
			List<String> recipeShape = cfg.getStringList("shape");
			if(recipeShape != null) 
			{
				for(int i = 0; i < Math.min(recipeShape.size(), 3); i++) 
				{
					String shapeLine = recipeShape.get(i);
					
					for(int j = 0; j < Math.min(shapeLine.length(), 3); j++) 
					{
						char c = shapeLine.charAt(j);
						
						if(ingredientCount.containsKey(c)) 
						{
							ingredientCount.put(c, ingredientCount.get(c) + 1);
						}
						else 
						{
							ingredientCount.put(c, 1);
						}
					}
				}
			}
			
			recipeChoices.forEach((Character key, RecipeChoice choice) -> {
				for(int i = 0; i < ingredientCount.getOrDefault(ingredientCount, 1); i++)
				{
					r.addIngredient(choice);
				}
			});
		}
		else
		{
			ShapedRecipe r = new ShapedRecipe(key, item.referenceItem);
			recipe = r;
			
			List<String> recipeShape = cfg.getStringList("shape");
			if(recipeShape == null) 
			{
				throw new NoSuchFieldException("Crafting recipe is missing required field: 'shape'");
			}
			
			int shapeSize = Math.min(3, recipeShape.size());
			
			String[] shapeLines = new String[shapeSize];
			for(int i = 0; i < shapeSize; i++) 
			{
				String shapeLine = recipeShape.get(i);
				shapeLines[i] = shapeLine.substring(0, Math.min(shapeLine.length(), 3));
			}
			
			r.shape(shapeLines);
			
			recipeChoices.forEach((Character key, RecipeChoice choice) -> {
				r.setIngredient(key, choice);
			});
		}
	}
	
	public boolean shapeless() 
	{
		return isShapeless;
	}
	
	public NamespacedKey getKey() 
	{
		return key;
	}
	
	public Recipe getRecipe() 
	{
		return recipe;
	}
}
