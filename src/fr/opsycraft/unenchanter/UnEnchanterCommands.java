package fr.opsycraft.unenchanter;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import fr.opsycraft.itemsmanager.SpecialAnvil;
import fr.opsycraft.itemsmanager.UnlockScroll;

public class UnEnchanterCommands implements CommandExecutor
{
	private Main main;
	private YamlConfiguration messagesFile;
	private String prefix;
	private ItemStack specialAnvil;
	private ItemStack unlockScroll;
	
	//region Class Constructor
	public UnEnchanterCommands(Main main)
	{
		this.main = main;
		this.messagesFile = main.getCustomConfigs().getMessagesFile();
		this.prefix = ChatColor.translateAlternateColorCodes('&', this.messagesFile.getString("prefix"));
		this.specialAnvil = new SpecialAnvil(this.main).getSpecialAnvil();
		this.unlockScroll = new UnlockScroll(this.main).getUnlockScroll();
	}
	
	@Override
	public boolean onCommand(CommandSender sender, Command cmd, String msg, String[] args)
	{
		if(cmd.getName().equalsIgnoreCase("desenchant"))
		{
			if(args.length == 1)
			{
				if(args[0].equalsIgnoreCase("help"))
				{
					for(String line : messagesFile.getStringList("help"))
					{
						line = ChatColor.translateAlternateColorCodes('&', line);
						if(line.contains("reload"))
						{
							if(sender.hasPermission("opsyunenchanter.reload"))
							{									
								sender.sendMessage(prefix + line);
							}
						}
						else if(line.contains("give"))
						{
							if(sender.hasPermission("opsyunenchanter.give"))
							{									
								sender.sendMessage(prefix + line);
							}
						}
						else
						{
							sender.sendMessage(prefix + line);
						}
					}
				}
				else if(args[0].equalsIgnoreCase("give"))
				{
					if(sender.hasPermission("opsyunenchanter.give"))
					{
						if(sender instanceof Player)
						{							
							((Player) sender).getWorld().dropItem(((Player) sender).getLocation(), specialAnvil);
							((Player) sender).getWorld().dropItem(((Player) sender).getLocation(), unlockScroll);
						}
						else
						{
							for(String line : messagesFile.getStringList("errors.must-be-a-player"))
							{
								line = ChatColor.translateAlternateColorCodes('&', line);
								sender.sendMessage(prefix + line);
							}
						}
					}
				}
				else if(args[0].equalsIgnoreCase("reload"))
				{
					if(sender.hasPermission("opsyunenchanter.reload"))
					{
						for(String line : messagesFile.getStringList("plugin-reloaded"))
						{
							line = ChatColor.translateAlternateColorCodes('&', line);
							sender.sendMessage(prefix + line);
						}
						Bukkit.getServer().getPluginManager().disablePlugin(main);
						Bukkit.getServer().getPluginManager().enablePlugin(main);
					}
					else
					{	
						for(String line : messagesFile.getStringList("errors.not-enough-permissions"))
						{
							line = ChatColor.translateAlternateColorCodes('&', line);
							sender.sendMessage(prefix + line);
						}
					}
				}
				else
				{
					for(String line : messagesFile.getStringList("errors.unknown-command"))
					{
						line = ChatColor.translateAlternateColorCodes('&', line);
						sender.sendMessage(prefix + line);
					}
				}
			}
			else
			{
				for(String line : messagesFile.getStringList("help"))
				{
					line = ChatColor.translateAlternateColorCodes('&', line);
					if(line.contains("reload"))
					{
						if(sender.hasPermission("opsyunenchanter.reload"))
						{									
							sender.sendMessage(prefix + line);
						}
					}
					else if(line.contains("give"))
					{
						if(sender.hasPermission("opsyunenchanter.give"))
						{									
							sender.sendMessage(prefix + line);
						}
					}
					else
					{
						sender.sendMessage(prefix + line);
					}
				}
			}
		}
		return false;
	}
}