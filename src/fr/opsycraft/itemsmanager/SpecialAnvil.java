package fr.opsycraft.itemsmanager;

import java.util.ArrayList;
import java.util.List;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.ShapedRecipe;
import org.bukkit.inventory.meta.ItemMeta;

import fr.opsycraft.unenchanter.Main;

public class SpecialAnvil
{
	
	private ItemStack specialAnvilItem;
	private YamlConfiguration configsFile;
	
	
	public SpecialAnvil(Main main) 
	{
		configsFile = main.getCustomConfigs().getConfigsFile();
		
		this.specialAnvilItem = new ItemStack(Material.ANVIL);
		ItemMeta specialAnvilMeta = specialAnvilItem.getItemMeta();
		specialAnvilMeta.setDisplayName(ChatColor.translateAlternateColorCodes('&', configsFile.getString("items.special-anvil.name")));
		List<String> anvilLore = new ArrayList<>();
		for(String line : configsFile.getStringList("items.special-anvil.lore"))
		{
			anvilLore.add(ChatColor.translateAlternateColorCodes('&', line));
		}
		specialAnvilMeta.setLore(anvilLore);
		specialAnvilItem.setItemMeta(specialAnvilMeta);
	}
	
	public void setSpecialAnvil(ItemStack specialAnvil)
	{
		this.specialAnvilItem = specialAnvil;
	}
	
	public ItemStack getSpecialAnvil()
	{
		return this.specialAnvilItem;
	}
	
	public void registerSpecialAnvilCraft()
	{
		if(configsFile.getBoolean("items.craft-special-anvil"))
		{
			ShapedRecipe recipe = new ShapedRecipe(this.specialAnvilItem);
			
			recipe.shape(" C ", "BAB", "AAA");
			recipe.setIngredient('A', Material.ENCHANTMENT_TABLE);
			recipe.setIngredient('B', Material.DIAMOND);
			recipe.setIngredient('C', Material.ANVIL);
			
			Bukkit.addRecipe(recipe);
		}
	}
}
