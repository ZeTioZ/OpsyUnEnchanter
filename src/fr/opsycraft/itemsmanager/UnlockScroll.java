package fr.opsycraft.itemsmanager;

import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import org.apache.commons.lang3.EnumUtils;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.EntityType;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.ShapedRecipe;
import org.bukkit.inventory.meta.ItemMeta;

import fr.opsycraft.unenchanter.Main;

public class UnlockScroll implements Listener
{
	
	private ItemStack unlockScrollItem;
	private YamlConfiguration configsFile;
	private YamlConfiguration messagesFile;
	private String prefix;
	private Random rand = new SecureRandom();
	
	public UnlockScroll(Main main) 
	{
		configsFile = main.getCustomConfigs().getConfigsFile();
		messagesFile = main.getCustomConfigs().getMessagesFile();
		prefix = ChatColor.translateAlternateColorCodes('&', this.messagesFile.getString("prefix"));
		
		this.unlockScrollItem = new ItemStack(Material.valueOf(configsFile.getString("items.unlock-scroll.material")));
		ItemMeta unlockScrollMeta = unlockScrollItem.getItemMeta();
		unlockScrollMeta.setDisplayName(ChatColor.translateAlternateColorCodes('&', configsFile.getString("items.unlock-scroll.name")));
		List<String> unlockScrollLore = new ArrayList<>();
		for(String line : configsFile.getStringList("items.unlock-scroll.lore"))
		{
			unlockScrollLore.add(ChatColor.translateAlternateColorCodes('&', line));
		}
		unlockScrollMeta.setLore(unlockScrollLore);
		unlockScrollItem.setItemMeta(unlockScrollMeta);
	}
	
	public void setUnlockScroll(ItemStack unlockScrollItem)
	{
		this.unlockScrollItem = unlockScrollItem;
	}
	
	public ItemStack getUnlockScroll()
	{
		return this.unlockScrollItem;
	}
	
	public void registerUnlockScrollCraft()
	{
		if(configsFile.getBoolean("items.craft-unlock-scroll"))
		{			
			ShapedRecipe recipe = new ShapedRecipe(this.unlockScrollItem);
			
			recipe.shape("CBC", "BAB", "CBC");
			recipe.setIngredient('A', Material.PAPER);
			recipe.setIngredient('B', Material.BOOK);
			recipe.setIngredient('C', Material.EMERALD_BLOCK);
			
			Bukkit.addRecipe(recipe);
		}
	}
	
	@EventHandler
	public void onUnlockScrollDrop(EntityDeathEvent e)
	{
		if(configsFile.getBoolean("weapon-lock") && configsFile.getBoolean("mob-drop"))
		{
			for(String entityTypeString : configsFile.getStringList("drop-mobs"))
			{
				if(EnumUtils.isValidEnum(EntityType.class, entityTypeString.toUpperCase()))
				{
					if(e.getEntity().getType() == EntityType.valueOf(entityTypeString.toUpperCase()))
					{
						for(String worldToCheckString : configsFile.getStringList("activated-world-drop"))
						{
							if(Bukkit.getWorld(worldToCheckString) == null)
							{
								for(String line : messagesFile.getStringList("errors.invalid-world-listed"))
								{
									line = line.replace("{world}", worldToCheckString);
									line = ChatColor.translateAlternateColorCodes('&', line);
									Bukkit.getLogger().severe(prefix + line);
								}
								continue;
							}
							if(e.getEntity().getLocation().getWorld() == Bukkit.getWorld(worldToCheckString))
							{
								if(configsFile.getDouble("drop-rate") <= 100.0D && configsFile.getDouble("drop-rate") > 0.0D)
								{
									double dropRate = this.configsFile.getDouble("drop-rate");
					                int nombreAleatoire = this.rand.nextInt(100);
					                if (nombreAleatoire < dropRate)
									{
										e.getEntity().getWorld().dropItemNaturally(e.getEntity().getLocation(), this.unlockScrollItem);
									}
								}
								else
								{
									for(String line : messagesFile.getStringList("errors.invalid-drop-rate"))
									{
										line = ChatColor.translateAlternateColorCodes('&', line);
										Bukkit.getLogger().severe(prefix + line);
									}
								}
							}
						}
					}
				}
				else
				{
					for(String line : messagesFile.getStringList("errors.invalid-drop-mobs"))
					{
			            line = line.replace("{mob}", entityTypeString.toUpperCase());
			            line = ChatColor.translateAlternateColorCodes('&', line);
			            Bukkit.getLogger().severe(this.prefix + line);
					}
				}
			}
		}
	}
}