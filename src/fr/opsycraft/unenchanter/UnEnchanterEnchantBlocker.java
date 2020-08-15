package fr.opsycraft.unenchanter;

import org.bukkit.ChatColor;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.enchantment.EnchantItemEvent;

public class UnEnchanterEnchantBlocker implements Listener {

	private YamlConfiguration configsFile;
	private YamlConfiguration messagesFile;
	private String prefix;
	
	public UnEnchanterEnchantBlocker(Main main)
	{
		configsFile = main.getCustomConfigs().getConfigsFile();
		messagesFile = main.getCustomConfigs().getMessagesFile();
		prefix = ChatColor.translateAlternateColorCodes('&', messagesFile.getString("prefix"));
	}
	
	@EventHandler
	public void onEnchant(EnchantItemEvent e)
	{
		if(e.getItem().getItemMeta().hasLore() && e.getItem().getItemMeta().getLore().contains(ChatColor.translateAlternateColorCodes('&', configsFile.getString("locked-item-lore"))))
		{
			e.setCancelled(true);
			e.getEnchanter().closeInventory();
			for(String line : messagesFile.getStringList("errors.locked-item"))
			{
				line = ChatColor.translateAlternateColorCodes('&', line);
				e.getEnchanter().sendMessage(prefix + line);
			}
		}
	}
}
