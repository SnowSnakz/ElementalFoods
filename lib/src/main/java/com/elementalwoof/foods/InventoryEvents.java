package com.elementalwoof.foods;

import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;

class InventoryEvents implements Listener
{
	ElementalFoods plugin;
	InventoryEvents(ElementalFoods plugin) 
	{
		this.plugin = plugin;
	}
	
	@EventHandler
	void OnItemClicked(InventoryClickEvent event) 
	{
		ItemStack activeItem = event.getCurrentItem();
		if(activeItem == null) return;
		
		if(!plugin.isManaged(activeItem)) return;
		else
		{
			CustomItem ci = plugin.getFromStack(activeItem);
			
			if(ci == null) return;
			
			if(!activeItem.isSimilar(ci.referenceItem)) 
			{
				ItemStack newStack = ci.referenceItem.clone();
				newStack.setAmount(activeItem.getAmount());
				event.setCurrentItem(newStack);
			}
		}
	}
}
