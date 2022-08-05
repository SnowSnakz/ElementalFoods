package com.elementalwoof.foods;

import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryType.SlotType;
import org.bukkit.inventory.ItemStack;

class InventoryEvents implements Listener
{
	ElementalFoods plugin;
	InventoryEvents(ElementalFoods plugin) 
	{
		this.plugin = plugin;
	}
	
	void preventArmorEquip(ItemStack activeItem, InventoryClickEvent event)
	{
		if(plugin.isManaged(activeItem))
		{
			if(event.getSlotType() == SlotType.ARMOR)
			{
				event.setCancelled(true);
				return;
			}
			
			Material itemType = activeItem.getType();
			String typeName = itemType.name();
			if(typeName.endsWith("_BOOTS") || typeName.endsWith("_LEGGINGS") || typeName.endsWith("_CHESTPLATE") || typeName.endsWith("_HELMET") || typeName.endsWith("_ARMOR"))
			{
				if(event.isShiftClick()) 
				{
					event.setCancelled(true);
					return;
				}
			}
		}
	}
	
	void unequipArmor(Player player) 
	{
		ItemStack[] armorItems = player.getInventory().getArmorContents();
		
		for(int i = 0; i < armorItems.length; i++)
		{
			if(plugin.isManaged(armorItems[i])) 
			{
				armorItems[i] = null;
			}
		}
		
		player.getInventory().setArmorContents(armorItems);
	}
	
	ItemStack getFixedItem(ItemStack activeItem)
	{
		if(plugin.isManaged(activeItem))
		{
			CustomItem ci = plugin.getFromStack(activeItem);
			
			if(ci == null) 
			{
				ItemStack t = activeItem.clone();
				t.setAmount(0);
				return t;
			}
			
			if(!ci.referenceItem.isSimilar(activeItem)) 
			{
				ItemStack newStack = ci.referenceItem.clone();
				newStack.setAmount(activeItem.getAmount());
				return newStack;
			}
		}
		
		return null;
	}
	
	@EventHandler
	void onItemClicked(InventoryClickEvent event) 
	{
		ItemStack activeItem = event.getCurrentItem();
		if(activeItem != null)
		{
			preventArmorEquip(activeItem, event);

			ItemStack fixedItem = getFixedItem(activeItem);
			if(fixedItem != null) 
			{
				event.setCurrentItem(activeItem);
			}
		}
		
		activeItem = event.getCursor();
		if(activeItem != null)
		{
			preventArmorEquip(activeItem, event);
			
			ItemStack fixedItem = getFixedItem(activeItem);
			if(fixedItem != null) 
			{
				event.getWhoClicked().setItemOnCursor(fixedItem);
			}
		}
		
		if(event.getSlotType() == SlotType.ARMOR)
		{
			Player player = (Player)event.getWhoClicked();
			if(player != null)
			{
				unequipArmor(player);
			}
		}
	}
}
