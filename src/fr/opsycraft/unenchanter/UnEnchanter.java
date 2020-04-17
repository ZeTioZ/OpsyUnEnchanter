package fr.opsycraft.unenchanter;

import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Random;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.block.Block;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.entity.EntityExplodeEvent;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.EnchantmentStorageMeta;
import org.bukkit.inventory.meta.ItemMeta;

import fr.opsycraft.interfacemanager.IndividualInterface;
import fr.opsycraft.itemsmanager.SpecialAnvil;
import fr.opsycraft.itemsmanager.UnlockScroll;

public class UnEnchanter implements Listener 
{
	private Main main;
	private YamlConfiguration configsFile;
	private YamlConfiguration messagesFile;
	private String prefix;
	private ItemStack specialAnvil;
	private ItemStack unlockScroll;
	private Random rand = new SecureRandom();
	
	private List<Location> placedUnEnchanters;
	private Map<Player, IndividualInterface> playersInterfaces;
	
	//region Constructeur + Getter/Setter de la liste des enclumes
	UnEnchanter(Main main)
	{
		this.main = main;
		this.configsFile = this.main.getCustomConfigs().getConfigsFile();
		this.messagesFile = this.main.getCustomConfigs().getMessagesFile();
		this.specialAnvil = new SpecialAnvil(this.main).getSpecialAnvil();
		this.unlockScroll = new UnlockScroll(this.main).getUnlockScroll();
		this.prefix = ChatColor.translateAlternateColorCodes('&', this.messagesFile.getString("prefix"));
		this.placedUnEnchanters = new ArrayList<>();
		this.playersInterfaces = new HashMap<>();
	}
	
	public List<Location> getListA()
	{
		return this.placedUnEnchanters;
	}
	
	public void setListA(List<Location> list)
	{
		this.placedUnEnchanters = list;
	}
	//endregion
	
	//region Event de placement de l'enclume spéciale
	@EventHandler
	public void onSpecialAnvilPlaced(BlockPlaceEvent e) 
	{
		ItemStack itemInHand = e.getPlayer().getInventory().getItemInMainHand();
		if(itemInHand != null && itemInHand.getType() == Material.ANVIL
			&& itemInHand.getItemMeta().getLore() != null
			&& itemInHand.getItemMeta().getLore().equals(specialAnvil.getItemMeta().getLore())
			&& e.getBlock().getType() == Material.ANVIL) 
		{
			Block underBlock = e.getBlock().getLocation().add(0, -1, 0).getBlock();
			Player p = e.getPlayer();
			if(configsFile.getStringList("block-under-place").contains(underBlock.getType().toString()))
			{
				e.setCancelled(true);
				for(String line : messagesFile.getStringList("errors.must-be-block-under"))
				{
					line = ChatColor.translateAlternateColorCodes('&', line);
					p.sendMessage(prefix + line);
				}
			}
			else if(configsFile.getStringList("block-under-empty-place").contains(underBlock.getType().toString()))
			{
				e.setCancelled(true);
				for(String line : messagesFile.getStringList("errors.cant-be-sand-or-gravel-or-plant"))
				{
					line = ChatColor.translateAlternateColorCodes('&', line);
					p.sendMessage(prefix + line);
				}
			}
			else
			{	
				if(!configsFile.getBoolean("place-permission") || p.hasPermission("opsyunenchanter.place"))
				{							
					placedUnEnchanters.add(e.getBlock().getLocation());
					main.getCustomConfigs().saveDatabaseFile();
				}
				else
				{
					for(String line : messagesFile.getStringList("errors-not-enough-permissions"))
					{
						line = ChatColor.translateAlternateColorCodes('&', line);
						p.sendMessage(prefix + line);
					}
				}
			}
		}
	}
	//endregion
	
	//region Event d'intéraction de l'enclume spéciale
	@EventHandler
	public void onSpecialAnvilRightclicked(PlayerInteractEvent e)
	{
		if(e.getClickedBlock() != null && e.getClickedBlock().getType() == Material.ANVIL) 
		{
			Location clickedAnvilLoc = e.getClickedBlock().getLocation();
			if(placedUnEnchanters.contains(clickedAnvilLoc)
				&& e.getAction() != null && e.getAction() == Action.RIGHT_CLICK_BLOCK) 
			{
				e.setCancelled(true);
				Player p = e.getPlayer();
				if(!configsFile.getBoolean("use-permission") || (configsFile.getBoolean("use-permission") && p.hasPermission("opsyunenchanter.use")))
				{		
					ItemStack itemToUnEnchant = p.getInventory().getItemInMainHand();
					if(itemToUnEnchant != null && itemToUnEnchant.getType() != Material.AIR) 
					{
						if(itemToUnEnchant.getItemMeta().getLore() != null && itemToUnEnchant.getItemMeta().getLore().contains(ChatColor.translateAlternateColorCodes('&', configsFile.getString("locked-item-lore"))))
						{
							Boolean hasUnlockScroll = false;
							int slot = 0;
							for(ItemStack itemToCheck : p.getInventory().getContents())
							{
								if(itemToCheck != null && itemToCheck.equals(unlockScroll))
								{
									itemToCheck.setAmount(itemToCheck.getAmount() - 1);
									if(itemToCheck.getAmount() == 0)
									{
										p.getInventory().setItem(slot, new ItemStack(Material.AIR));
									}
									for(String line : messagesFile.getStringList("weapon-unlocked"))
									{
										line = ChatColor.translateAlternateColorCodes('&', line);
										p.sendMessage(prefix + line);
									}
									int i = 0;
									for(String loreLine : itemToUnEnchant.getItemMeta().getLore())
									{
										if(loreLine.equals(ChatColor.translateAlternateColorCodes('&', configsFile.getString("locked-item-lore"))))
										{
											ItemMeta newItemMeta = itemToUnEnchant.getItemMeta();
											List<String> newItemLore = newItemMeta.getLore();
											newItemLore.remove(i - 1);
											newItemLore.remove(i - 1);
											newItemMeta.setLore(newItemLore);
											itemToUnEnchant.setItemMeta(newItemMeta);
											p.getInventory().setItemInMainHand(itemToUnEnchant);
										}
										i++;
									}
									hasUnlockScroll = true;
									break;
								}
								slot++;
							}
							if(!hasUnlockScroll)
							{
								for(String line : messagesFile.getStringList("errors.locked-weapon"))
								{
									e.getPlayer().sendMessage(prefix + ChatColor.translateAlternateColorCodes('&', line));
								}
								return;
							}
						}
						if(playersInterfaces.containsKey(p))
						{
							playersInterfaces.remove(p);
						}
						playersInterfaces.put(p, new IndividualInterface(p));
						IndividualInterface playerInterface = playersInterfaces.get(p);
						Map<Enchantment, Integer> itemEnchants = itemToUnEnchant.getEnchantments();
						if(!itemEnchants.keySet().isEmpty())
						{
							Bukkit.getScheduler().scheduleSyncDelayedTask(main.getPlugin(), new Runnable() {
								public void run() {
									playerInterface.setFirstInterfaceChoice(firstInterfaceChoise(playerInterface.getPlayer(), playerInterface.getItemToUnenchant()));
									playerInterface.setOpenedAnvilLoc(e.getClickedBlock().getLocation());
									playerInterface.getPlayer().getInventory().setItemInMainHand(null);
								}
							}, 2L);
						}
						else
						{
							for(String line : messagesFile.getStringList("errors.no-enchants-to-recover"))
							{
								line = ChatColor.translateAlternateColorCodes('&', line);
								e.getPlayer().sendMessage(prefix + line);
							}
						}
					}
					else
					{
						for(String line : messagesFile.getStringList("errors.no-item-hold"))
						{
							line = ChatColor.translateAlternateColorCodes('&', line);
							e.getPlayer().sendMessage(prefix + line);
						}
					}
				}
				else
				{
					for(String line : messagesFile.getStringList("errors.not-enough-permission"))
					{
						line = ChatColor.translateAlternateColorCodes('&', line);
						p.sendMessage(prefix + line);
					}
				}
			}
		}
	}
	//endregion
	
	//region Fonction de récupération de l'item après fermeture inopinée de l'inventaire
	private void giveUnEnchantedItemBack(Player p, ItemStack itemToUnEnchant, Boolean forOpti) //Fonction pour savoir si quand l'interface se ferme il faut rendre l'item ou pas
	{
		IndividualInterface playerInterface = playersInterfaces.get(p);
		if(!playerInterface.getClosedInventory())
		{
			if(!forOpti) {
				try
				{
					
					p.playSound(p.getLocation(), Sound.valueOf(configsFile.getString("sounds.anvil-close-sound").toUpperCase()), 5, 5);
				}
				catch(IllegalArgumentException ex)
				{
					for(String line : messagesFile.getStringList(""))
					{
						line = line.replace("{sound}", configsFile.getString("sounds.anvil-close-sound"));
						line = ChatColor.translateAlternateColorCodes('&', line);
						main.getLogger().warning(line);
					}
				}
			}
			if(p.getInventory().firstEmpty() != -1)
			{
				p.getInventory().addItem(itemToUnEnchant);
			}
			else
			{
				p.getWorld().dropItem(p.getLocation(), itemToUnEnchant);
			}
		}
		else
		{	
			playerInterface.setClosedInventory(false);
		}
	}
	//endregion
	
	//region Event de fermeture de l'enclume
	@EventHandler
	public void onSpecialAnvilClose(InventoryCloseEvent e) 
	{
		Player p = (Player) e.getPlayer();
		if(playersInterfaces.containsKey(p))
		{
			IndividualInterface playerInterface = playersInterfaces.get(p);
			ItemStack pItem = playerInterface.getItemToUnenchant();
			if(e.getInventory() != null && e.getInventory().equals(playerInterface.getFirstInterfaceChoice())) 
			{
				giveUnEnchantedItemBack(p, pItem, false);
			}
			
			else if((e.getInventory() != null && playerInterface.getUnEnchanterChooseInterface() != null
						&& e.getInventory().equals(playerInterface.getUnEnchanterChooseInterface()))
					|| (e.getInventory() != null && playerInterface.getUnEnchanterInterfaceRandom() != null
						&& e.getInventory().equals(playerInterface.getUnEnchanterInterfaceRandom())))
			{
				giveUnEnchantedItemBack(p, pItem, false);
				playersInterfaces.remove(p);
			}
		}
	}
	//endregion
	
	//region Première interface (Choix)
	private Inventory firstInterfaceChoise(Player player, ItemStack playerItem) // Interface 1
	{
		Inventory firstInterface =  Bukkit.createInventory(player, 27, ChatColor.translateAlternateColorCodes('&', configsFile.getString("first-interface.name")));
		ItemStack redGlassPane;
		try
		{			
			redGlassPane = new ItemStack(Material.valueOf(configsFile.getString("first-interface.refuse-decoration").toUpperCase()));
		}
		catch(IllegalArgumentException ex)
		{
			redGlassPane = new ItemStack(Material.BARRIER);
			for(String line : messagesFile.getStringList("errors.invalid-material"))
			{
				line = line.replace("{material}", configsFile.getString("first-interface.refuse-decoration"));
				line = ChatColor.translateAlternateColorCodes('&', line);
				main.getLogger().warning(line);
			}
		}
		ItemMeta redGlassPaneMeta = redGlassPane.getItemMeta();
		redGlassPaneMeta.setDisplayName(" ");
		redGlassPane.setItemMeta(redGlassPaneMeta);
		ItemStack blueGlassPane;
		try
		{
			blueGlassPane = new ItemStack(Material.valueOf(configsFile.getString("first-interface.middle-decoration").toUpperCase()));
		}
		catch(IllegalArgumentException ex)
		{
			blueGlassPane = new ItemStack(Material.BARRIER);
			for(String line : messagesFile.getStringList("errors.invalid-material"))
			{
				line = line.replace("{material}", configsFile.getString("first-interface.middle-decoration"));
				line = ChatColor.translateAlternateColorCodes('&', line);
				main.getLogger().warning(line);
			}
		}
		ItemMeta blueGlassPaneMeta = blueGlassPane.getItemMeta();
		blueGlassPaneMeta.setDisplayName(" ");
		blueGlassPane.setItemMeta(blueGlassPaneMeta);
		ItemStack greenGlassPane;
		try
		{
			greenGlassPane = new ItemStack(Material.valueOf(configsFile.getString("first-interface.accept-decoration").toUpperCase()));
		}
		catch(IllegalArgumentException ex)
		{
			greenGlassPane = new ItemStack(Material.BARRIER);
			for(String line : messagesFile.getStringList("errors.invalid-material"))
			{
				line = line.replace("{material}", configsFile.getString("first-interface.accept-decoration"));
				line = ChatColor.translateAlternateColorCodes('&', line);
				main.getLogger().warning(line);
			}
		}
		ItemMeta greenGlassPaneMeta = greenGlassPane.getItemMeta();
		greenGlassPaneMeta.setDisplayName(" ");
		greenGlassPane.setItemMeta(greenGlassPaneMeta);
		ItemStack redWool;
		try
		{			
			redWool = new ItemStack(Material.valueOf(configsFile.getString("first-interface.refuse-button-item").toUpperCase()));
		}
		catch(IllegalArgumentException ex)
		{
			redWool = new ItemStack(Material.BARRIER);
			for(String line : messagesFile.getStringList("errors.invalid-material"))
			{
				line = line.replace("{material}", configsFile.getString("first-interface.refuse-button-item"));
				line = ChatColor.translateAlternateColorCodes('&', line);
				main.getLogger().warning(line);
			}
		}
		ItemMeta redWoolMeta = redWool.getItemMeta();
		redWoolMeta.setDisplayName(ChatColor.translateAlternateColorCodes('&', configsFile.getString("first-interface.refuse-button-name")));
		redWool.setItemMeta(redWoolMeta);
		ItemStack greenWool;
		try
		{
			greenWool = new ItemStack(Material.valueOf(configsFile.getString("first-interface.accept-button-item").toUpperCase()));
		}
		catch(IllegalArgumentException ex)
		{
			greenWool = new ItemStack(Material.BARRIER);
			for(String line : messagesFile.getStringList("errors.invalid-material"))
			{
				line = line.replace("{material}", configsFile.getString("first-interface.accept-button-item"));
				line = ChatColor.translateAlternateColorCodes('&', line);
				main.getLogger().warning(line);
			}
		}
		ItemMeta greenWoolMeta = greenWool.getItemMeta();
		greenWoolMeta.setDisplayName(ChatColor.translateAlternateColorCodes('&', configsFile.getString("first-interface.accept-button-name")));
		List<String> greenWoolLore = new ArrayList<>();
		if(playerItem.getEnchantments().keySet().size() < 5) {
			for(String line : configsFile.getStringList("first-interface.less-5-button-lore"))
			{
				line = ChatColor.translateAlternateColorCodes('&', line);
				greenWoolLore.add(line);
			}
			greenWoolMeta.setLore(greenWoolLore);
		}
		else
		{
			for(String line : configsFile.getStringList("first-interface.more-5-button-lore"))
			{
				line = ChatColor.translateAlternateColorCodes('&', line);
				greenWoolLore.add(line);
			}
			greenWoolMeta.setLore(greenWoolLore);
		}
		greenWool.setItemMeta(greenWoolMeta);
		/*
		* 0 = Blue Glass Pane
		* 1 = Red Glass Pane
		* 2 = Green Glass Pane
		* 3 = Red Wool
		* 4 = Green Wool
		* 5 = AIR (Item to place)
		*/
		int[] posTable = {1, 1, 0, 1, 0, 2, 0, 2, 2, 1, 3, 1, 0, 5, 0, 2, 4, 2, 1, 1, 0, 1, 0, 2, 0, 2, 2};
		for(int i = 0; i < posTable.length; i++)
		{
			switch(posTable[i]) {
				case 0:
					firstInterface.setItem(i, blueGlassPane);
					break;
				case 1:
					firstInterface.setItem(i, redGlassPane);
					break;
				case 2:
					firstInterface.setItem(i, greenGlassPane);
					break;
				case 3:
					firstInterface.setItem(i, redWool);
					break;
				case 4:
					firstInterface.setItem(i, greenWool);
					break;
				default:
					firstInterface.setItem(i, playerItem);
					break;
			}
		}
		player.openInventory(firstInterface);
		return firstInterface;
	}
	//endregion
	
    //region Seconde interface (Choix des enchantements < 5 enchants)
	private Inventory unEnchanterChooseInterface(Player player, ItemStack playerItem) // Interface moins de 5 enchants
	{
		Inventory unEnchanterChooseInterface =  Bukkit.createInventory(player, 27, ChatColor.translateAlternateColorCodes('&', configsFile.getString("less-5-interface.name")));
		
		IndividualInterface playerInterface = playersInterfaces.get(player);
		
		ItemStack whiteGlassPane;
		try
		{
			whiteGlassPane = new ItemStack(Material.valueOf(configsFile.getString("less-5-interface.first-decoration").toUpperCase()));
		}
		catch(IllegalArgumentException ex)
		{
			whiteGlassPane = new ItemStack(Material.BARRIER);
			for(String line : messagesFile.getStringList("errors.invalid-material"))
			{
				line = line.replace("{material}", configsFile.getString("less-5-interface.first-decoration"));
				line = ChatColor.translateAlternateColorCodes('&', line);
				main.getLogger().warning(line);
			}
		}
		ItemMeta whiteGlassPaneMeta = whiteGlassPane.getItemMeta();
		whiteGlassPaneMeta.setDisplayName(" ");
		whiteGlassPane.setItemMeta(whiteGlassPaneMeta);
		ItemStack blueGlassPane;
		try
		{
			blueGlassPane = new ItemStack(Material.valueOf(configsFile.getString("less-5-interface.first-middle-decoration").toUpperCase()));
		}
		catch(IllegalArgumentException ex)
		{
			blueGlassPane = new ItemStack(Material.BARRIER);
			for(String line : messagesFile.getStringList("errors.invalid-material"))
			{
				line = line.replace("{material}", configsFile.getString("less-5-interface.first-middle-decoration"));
				line = ChatColor.translateAlternateColorCodes('&', line);
				main.getLogger().warning(line);
			}
		}
		ItemMeta blueGlassPaneMeta = blueGlassPane.getItemMeta();
		blueGlassPaneMeta.setDisplayName(" ");
		blueGlassPane.setItemMeta(blueGlassPaneMeta);
		ItemStack yellowGlassPane;
		try
		{
			yellowGlassPane = new ItemStack(Material.valueOf(configsFile.getString("less-5-interface.second-decoration").toUpperCase()));
		}
		catch(IllegalArgumentException ex)
		{
			yellowGlassPane = new ItemStack(Material.BARRIER);
			for(String line : messagesFile.getStringList("errors.invalid-material"))
			{
				line = line.replace("{material}", configsFile.getString("less-5-interface.second-decoration"));
				line = ChatColor.translateAlternateColorCodes('&', line);
				main.getLogger().warning(line);
			}
		}
		ItemMeta yellowGlassPaneMeta = yellowGlassPane.getItemMeta();
		yellowGlassPaneMeta.setDisplayName(" ");
		yellowGlassPane.setItemMeta(yellowGlassPaneMeta);
		ItemStack blackGlassPane;
		try
		{
			blackGlassPane = new ItemStack(Material.valueOf(configsFile.getString("less-5-interface.second-middle-decoration").toUpperCase()));
		}
		catch(IllegalArgumentException ex)
		{
			blackGlassPane = new ItemStack(Material.BARRIER);
			for(String line : messagesFile.getStringList("errors.invalid-material"))
			{
				line = line.replace("{material}", configsFile.getString("less-5-interface.second-middle-decoration"));
				line = ChatColor.translateAlternateColorCodes('&', line);
				main.getLogger().warning(line);
			}
		}
		ItemMeta blackGlassPaneMeta = blackGlassPane.getItemMeta();
		blackGlassPaneMeta.setDisplayName(" ");
		blackGlassPane.setItemMeta(blackGlassPaneMeta);
		/*
		* 0 = Blue Glass Pane
		* 1 = White Glass Pane
		* 2 = Yellow Glass Pane
		* 3 = Player Item
		* 4 = Enchantments Book
		* 5 = Emerald Selector
		* 6 = Black Glass Pane
		*/
		//*
		int[] posTable = {2, 1, 2, 6, 0, 6, 2, 1, 2, 1, 3, 1, 0, 0, 0, 1, 4, 1, 2, 1, 2, 6, 0, 6, 2, 1, 2};
		switch(playerItem.getEnchantments().keySet().size())
		{
			case 4:
				posTable[23] = 5;
			case 3:
				posTable[21] = 5;
			case 2:
				posTable[5] = 5;
			case 1:
				posTable[3] = 5;
				break;
			default:
				break;
		}
		ItemStack emeraldContainer = new ItemStack(Material.EMERALD);
		ItemMeta emeraldContainerMeta = emeraldContainer.getItemMeta();
		int a = 1;
		int j = playerInterface.getItemEnchants().size() - 1;
		for(Enchantment ench : playerInterface.getItemEnchantsSet()) 
		{
			int enchLevel = playerInterface.getItemEnchants().get(ench);
			emeraldContainerMeta.addEnchant(ench, enchLevel, true);
			String s = ChatColor.translateAlternateColorCodes('&', configsFile.getString("less-5-interface.enchantment-nbr-prefix"));
			emeraldContainerMeta.setDisplayName(s + a);
			emeraldContainer.setItemMeta(emeraldContainerMeta);
			playerInterface.setEmeraldMap(j, emeraldContainer);
			emeraldContainer = new ItemStack(Material.EMERALD);
			emeraldContainerMeta = emeraldContainer.getItemMeta();
			j--;
			a++;
		}
		j = playerInterface.getItemEnchants().size() - 1;
		
		for(int i = 0; i < posTable.length; i++)
		{
			switch(posTable[i]) {
				case 0:
					unEnchanterChooseInterface.setItem(i, blueGlassPane);
					break;
				case 1:
					unEnchanterChooseInterface.setItem(i, whiteGlassPane);
					break;
				case 2:
					unEnchanterChooseInterface.setItem(i, yellowGlassPane);
					break;
				case 3:
					unEnchanterChooseInterface.setItem(i, playerItem);
					break;
				case 4:
					unEnchanterChooseInterface.setItem(i, new ItemStack(Material.BOOK));
					break;
				case 5:
					unEnchanterChooseInterface.setItem(i, playerInterface.getEmeraldMap().get(j));
					j--;
					break;
				default:
					unEnchanterChooseInterface.setItem(i, blackGlassPane);
					break;
			}
		}
		player.openInventory(unEnchanterChooseInterface);
		return unEnchanterChooseInterface;
	}
	//endregion
	
	//region Troisième interface (Choix random > 5 enchants)
	private Inventory unEnchanterInterfaceRandom(Player player, ItemStack playerItem) // Interface plus ou �gal de 5 enchants
	{
		Inventory unEnchantInterfaceRandom =  Bukkit.createInventory(player, 27, ChatColor.translateAlternateColorCodes('&', configsFile.getString("more-5-interface.name")));
		
		IndividualInterface playerInterface = playersInterfaces.get(player);
		
		ItemStack whiteGlassPane;
		try
		{
			whiteGlassPane = new ItemStack(Material.valueOf(configsFile.getString("more-5-interface.first-decoration").toUpperCase()));
		}
		catch(IllegalArgumentException ex)
		{
			whiteGlassPane = new ItemStack(Material.BARRIER);
			for(String line : messagesFile.getStringList("errors.invalid-material"))
			{
				line = line.replace("{material}", configsFile.getString("more-5-interface.first-decoration"));
				line = ChatColor.translateAlternateColorCodes('&', line);
				main.getLogger().warning(line);
			}
		}
		ItemMeta whiteGlassPaneMeta = whiteGlassPane.getItemMeta();
		whiteGlassPaneMeta.setDisplayName(" ");
		whiteGlassPane.setItemMeta(whiteGlassPaneMeta);
		ItemStack blueGlassPane;
		try
		{
			blueGlassPane = new ItemStack(Material.valueOf(configsFile.getString("more-5-interface.first-middle-decoration").toUpperCase()));
		}
		catch(IllegalArgumentException ex)
		{
			blueGlassPane = new ItemStack(Material.BARRIER);
			for(String line : messagesFile.getStringList("errors.invalid-material"))
			{
				line = line.replace("{material}", configsFile.getString("more-5-interface.first-middle-decoration"));
				line = ChatColor.translateAlternateColorCodes('&', line);
				main.getLogger().warning(line);
			}
		}
		ItemMeta blueGlassPaneMeta = blueGlassPane.getItemMeta();
		blueGlassPaneMeta.setDisplayName(" ");
		blueGlassPane.setItemMeta(blueGlassPaneMeta);
		ItemStack yellowGlassPane;
		try
		{
			yellowGlassPane = new ItemStack(Material.valueOf(configsFile.getString("more-5-interface.second-decoration").toUpperCase()));
		}
		catch(IllegalArgumentException ex)
		{
			yellowGlassPane = new ItemStack(Material.BARRIER);
			for(String line : messagesFile.getStringList("errors.invalid-material"))
			{
				line = line.replace("{material}", configsFile.getString("more-5-interface.second-decoration"));
				line = ChatColor.translateAlternateColorCodes('&', line);
				main.getLogger().warning(line);
			}
		}
		ItemMeta yellowGlassPaneMeta = yellowGlassPane.getItemMeta();
		yellowGlassPaneMeta.setDisplayName(" ");
		yellowGlassPane.setItemMeta(yellowGlassPaneMeta);
		ItemStack blackGlassPane;
		try
		{
			blackGlassPane = new ItemStack(Material.valueOf(configsFile.getString("more-5-interface.second-middle-decoration").toUpperCase()));
		}
		catch(IllegalArgumentException ex)
		{
			blackGlassPane = new ItemStack(Material.BARRIER);
			for(String line : messagesFile.getStringList("errors.invalid-material"))
			{
				line = line.replace("{material}", configsFile.getString("more-5-interface.second-middle-decoration"));
				line = ChatColor.translateAlternateColorCodes('&', line);
				main.getLogger().warning(line);
			}
		}
		ItemMeta blackGlassPaneMeta = blackGlassPane.getItemMeta();
		blackGlassPaneMeta.setDisplayName(" ");
		blackGlassPane.setItemMeta(blackGlassPaneMeta);
		ItemStack emeraldCatalyzer = new ItemStack(Material.EMERALD);
		ItemMeta emeraldCatalyzerMeta = emeraldCatalyzer.getItemMeta();
		emeraldCatalyzerMeta.setDisplayName(ChatColor.translateAlternateColorCodes('&', configsFile.getString("more-5-interface.catalyzer-name")));
		List<String> emeraldCatalyzerLore = new ArrayList<>();
		emeraldCatalyzerLore.add("I");
		double baseCost = configsFile.getDouble("catalyzer-base-price");
		double levelMultiplier = configsFile.getDouble("catalyzer-level-multiplier");
		int costFormula = (int) (Math.floor(baseCost * levelMultiplier * 1));
		for(String line : configsFile.getStringList("more-5-interface.price-lore"))
		{			
			line = line.replace("{price}", String.valueOf(costFormula));
			emeraldCatalyzerLore.add(ChatColor.translateAlternateColorCodes('&', line));
		}
		emeraldCatalyzerMeta.setLore(emeraldCatalyzerLore);
		emeraldCatalyzer.setItemMeta(emeraldCatalyzerMeta);
		playerInterface.setSelectedEnchants(1);
		/*
		* 0 = Blue Glass Pane
		* 1 = White Glass Pane
		* 2 = Yellow Glass Pane
		* 3 = Player Item
		* 4 = Enchantments Book
		* 5 = Emerald Selector
		* 6 = Black Glass Pane
		*/
		//*
		int[] posTable = {2, 1, 2, 6, 0, 6, 2, 1, 2, 1, 3, 1, 0, 5, 0, 1, 4, 1, 2, 1, 2, 6, 0, 6, 2, 1, 2};
		for(int i = 0; i < posTable.length; i++)
		{
			switch(posTable[i]) {
				case 0:
					unEnchantInterfaceRandom.setItem(i, blueGlassPane);
					break;
				case 1:
					unEnchantInterfaceRandom.setItem(i, whiteGlassPane);
					break;
				case 2:
					unEnchantInterfaceRandom.setItem(i, yellowGlassPane);
					break;
				case 3:
					unEnchantInterfaceRandom.setItem(i, playerItem);
					break;
				case 4:
					unEnchantInterfaceRandom.setItem(i, new ItemStack(Material.BOOK));
					break;
				case 5:
					unEnchantInterfaceRandom.setItem(i, emeraldCatalyzer);
					break;
				default:
					unEnchantInterfaceRandom.setItem(i, blackGlassPane);
					break;
			}
		}
		player.openInventory(unEnchantInterfaceRandom);
		onRandomPick(player, playerItem.getEnchantments(), false);
		return unEnchantInterfaceRandom;
	}
	//endregion
	
	//region Select Emerald
	private void selectEmerad(IndividualInterface playerInterface, ItemStack item, int whichEmerald, Inventory inv, int whichSlot)
	{
		ItemStack itemS = new ItemStack(item);
		if(itemS.getType() ==  Material.EMERALD)
		{
			if(playerInterface.getSelectedEnchants() >= configsFile.getInt("enchants-select-max"))
			{
				for(String line : messagesFile.getStringList("errors.already-selected-max-enchants"))
				{
					playerInterface.getPlayer().sendMessage(prefix + ChatColor.translateAlternateColorCodes('&', line));
				}
				try
				{					
					playerInterface.getPlayer().playSound(playerInterface.getPlayer().getLocation(), Sound.valueOf(configsFile.getString("sounds.less-5-interface.already-max-enchants-sound").toUpperCase()), 5, 5);
				}
				catch(IllegalArgumentException ex)
				{
					for(String line : messagesFile.getStringList("errors.invalid-sound"))
					{
						line = line.replace("{sound}", configsFile.getString("sounds.less-5-interface.already-max-enchants-sound"));
						line = ChatColor.translateAlternateColorCodes('&', line);
						main.getLogger().warning(line);
					}
				}
				return;
			}
			try
			{					
				playerInterface.getPlayer().playSound(playerInterface.getPlayer().getLocation(), Sound.valueOf(configsFile.getString("sounds.less-5-interface.select-enchant-sound").toUpperCase()), 5, 5);
			}
			catch(IllegalArgumentException ex)
			{
				for(String line : messagesFile.getStringList("errors.invalid-sound"))
				{
					line = line.replace("{sound}", configsFile.getString("sounds.less-5-interface.select-enchant-sound"));
					line = ChatColor.translateAlternateColorCodes('&', line);
					main.getLogger().warning(line);
				}
			}
			playerInterface.setSelectedEnchants(playerInterface.getSelectedEnchants() + 1);
			itemS = new ItemStack(Material.PAPER);
			ItemMeta itemSMeta = itemS.getItemMeta();
			itemSMeta.setDisplayName(ChatColor.translateAlternateColorCodes('&', configsFile.getString("less-5-interface.enchantment-selected-name")));
			EnchantmentStorageMeta bookStorage = (EnchantmentStorageMeta) playerInterface.getEnchantedBook().getItemMeta();
			for(Enchantment ench : item.getEnchantments().keySet()) 
			{
				itemSMeta.addEnchant(ench, item.getEnchantments().get(ench), true);
				bookStorage.addStoredEnchant(ench, item.getEnchantments().get(ench), true);
			}
			itemS.setItemMeta(itemSMeta);
			inv.setItem(whichSlot, itemS);
			playerInterface.getEnchantedBook().setItemMeta(bookStorage);
			int emeraldLevels = 0;
			for(ItemStack emeraldSelected : inv.getContents())
			{
				if(emeraldSelected.getType() == Material.PAPER
					&& emeraldSelected.getItemMeta().getDisplayName().contains(ChatColor.translateAlternateColorCodes('&', configsFile.getString("less-5-interface.enchantment-selected-name"))))
				{
					for(Enchantment ench : emeraldSelected.getEnchantments().keySet())
					{
						emeraldLevels += emeraldSelected.getEnchantments().get(ench);
					}
				}
			}
			int enchants = playerInterface.getSelectedEnchants();
			int enchantsLevel = emeraldLevels;
			double result = Math.round(200.0 * (enchants * (1.0 + (enchantsLevel/10.0))));
			ItemStack uIEnchantedBook = playerInterface.getEnchantedBook();
			ItemMeta uIEnchantedBookMeta = uIEnchantedBook.getItemMeta();
			List<String> uIEnchantedBookLore = new ArrayList<>();
			uIEnchantedBookLore.add(" ");
			for(String line : configsFile.getStringList("less-5-interface.enchantment-price-lore"))
			{
				line = ChatColor.translateAlternateColorCodes('&', line);
				line = line.replace("{price}", String.valueOf(result));
				uIEnchantedBookLore.add(line);
			}
			uIEnchantedBookMeta.setLore(uIEnchantedBookLore);
			uIEnchantedBook.setItemMeta(uIEnchantedBookMeta);
			inv.setItem(16, uIEnchantedBook);
		}
		else if(itemS.getType() == Material.PAPER)
		{
			try
			{					
				playerInterface.getPlayer().playSound(playerInterface.getPlayer().getLocation(), Sound.valueOf(configsFile.getString("sounds.less-5-interface.unselect-enchant-sound").toUpperCase()), 5, 5);
			}
			catch(IllegalArgumentException ex)
			{
				for(String line : messagesFile.getStringList("errors.invalid-sound"))
				{
					line = line.replace("{sound}", configsFile.getString("sounds.less-5-interface.unselect-enchant-sound"));
					line = ChatColor.translateAlternateColorCodes('&', line);
					main.getLogger().warning(line);
				}
			}
			playerInterface.setSelectedEnchants(playerInterface.getSelectedEnchants() - 1);
			itemS = playerInterface.getEmeraldMap().get(playerInterface.getEmeraldMap().size() - whichEmerald);
			EnchantmentStorageMeta bookStorage = (EnchantmentStorageMeta) playerInterface.getEnchantedBook().getItemMeta();
			for(Enchantment ench : itemS.getEnchantments().keySet()) {
				bookStorage.removeStoredEnchant(ench);
			}
			inv.setItem(whichSlot, itemS);
			playerInterface.getEnchantedBook().setItemMeta(bookStorage);
			if(playerInterface.getEnchantedBookSet().isEmpty()) {
				inv.setItem(16, new ItemStack(Material.BOOK));				
			}
			else
			{
				int emeraldLevels = 0;
				for(ItemStack emeraldSelected : inv.getContents())
				{
					if(emeraldSelected.getType() == Material.PAPER
						&& emeraldSelected.getItemMeta().getDisplayName().contains(ChatColor.translateAlternateColorCodes('&', configsFile.getString("less-5-interface.enchantment-selected-name"))))
					{
						for(Enchantment ench : emeraldSelected.getEnchantments().keySet())
						{
							emeraldLevels += emeraldSelected.getEnchantments().get(ench);
						}
					}
				}
				int enchants = playerInterface.getSelectedEnchants();
				int enchantsLevel = emeraldLevels;
				double result = Math.round(200.0 * (enchants * (1.0 + (enchantsLevel/10.0))));
				ItemStack uIEnchantedBook = playerInterface.getEnchantedBook();
				ItemMeta uIEnchantedBookMeta = uIEnchantedBook.getItemMeta();
				List<String> uIEnchantedBookLore = new ArrayList<>();
				uIEnchantedBookLore.add(" ");
				for(String line : configsFile.getStringList("less-5-interface.enchantment-price-lore"))
				{
					line = ChatColor.translateAlternateColorCodes('&', line);
					line = line.replace("{price}", String.valueOf(result));
					uIEnchantedBookLore.add(line);
				}
				uIEnchantedBookMeta.setLore(uIEnchantedBookLore);
				uIEnchantedBook.setItemMeta(uIEnchantedBookMeta);
				inv.setItem(16, uIEnchantedBook);
			}
		}
	}
	//endregion
	
	//region Increment Catalyzer Level
	public void incrementCatalyzerLevel(Player p, ItemStack catalyzer)
	{
		String catalyzerName = ChatColor.translateAlternateColorCodes('&', configsFile.getString("more-5-interface.catalyzer-name"));
		ItemMeta catalyzerMeta = catalyzer.getItemMeta();
		if(catalyzerMeta.getDisplayName().equals(catalyzerName)
			&& playersInterfaces.containsKey(p))
		{
			IndividualInterface playerInterface = playersInterfaces.get(p);
			int catalyzerLevel = playerInterface.getCatalyzerLevel();
			String catalyzerLevelString = new String();
			if(catalyzerLevel >= 1 && catalyzerLevel < 4)
			{
				catalyzerLevel += 1;
				playerInterface.setCatalyzerLevel(catalyzerLevel);
				StringBuilder catalyzerLevelSTB = new StringBuilder();
				for(int i = 0; i < catalyzerLevel; i++)
				{
					catalyzerLevelSTB.append("I");
				}
				catalyzerLevelString = catalyzerLevelSTB.toString();
			}
			else if(catalyzerLevel == 4)
			{
				catalyzerLevel = 1;
				playerInterface.setCatalyzerLevel(1);
				catalyzerLevelString = "I";
			}
			double baseCost = configsFile.getDouble("catalyzer-base-price"); // Same
			double levelMultiplier = configsFile.getDouble("catalyzer-level-multiplier");
			int costFormula = (int) (Math.floor(baseCost * levelMultiplier * catalyzerLevel));
			List<String> catalyzerLore = new ArrayList<>();
			catalyzerLore.add(catalyzerLevelString);
			for(String line: configsFile.getStringList("more-5-interface.price-lore"))
			{
				line = ChatColor.translateAlternateColorCodes('&', line);
				line = line.replace("{price}", String.valueOf(costFormula));
				catalyzerLore.add(line);
			}
			catalyzerMeta.setLore(catalyzerLore);
			catalyzer.setItemMeta(catalyzerMeta);
			p.updateInventory();
		}
	}
	//endregion
	
	//region Decrement Catalyzer Level
	public void decrementCatalyzerLevel(Player p, ItemStack catalyzer)
	{
		String catalyzerName = ChatColor.translateAlternateColorCodes('&', configsFile.getString("more-5-interface.catalyzer-name"));
		ItemMeta catalyzerMeta = catalyzer.getItemMeta();
		if(catalyzerMeta.getDisplayName().equals(catalyzerName)
			&& playersInterfaces.containsKey(p))
		{
			IndividualInterface playerInterface = playersInterfaces.get(p);
			int catalyzerLevel = playerInterface.getCatalyzerLevel();
			String catalyzerLevelString = new String();
			if(catalyzerLevel >= 2 && catalyzerLevel <= 4)
			{
				catalyzerLevel -= 1;
				playerInterface.setCatalyzerLevel(catalyzerLevel);
				StringBuilder catalyzerLevelSTB = new StringBuilder();
				for(int i = 0; i < catalyzerLevel; i++)
				{
					catalyzerLevelSTB.append("I");
				}
				catalyzerLevelString = catalyzerLevelSTB.toString();
			}
			else if(catalyzerLevel == 1)
			{
				catalyzerLevel = 4;
				playerInterface.setCatalyzerLevel(4);
				catalyzerLevelString = "IIII";
			}
			double baseCost = configsFile.getDouble("catalyzer-base-price");
			double levelMultiplier = configsFile.getDouble("catalyzer-level-multiplier");
			int costFormula = (int) (Math.floor(baseCost * levelMultiplier * catalyzerLevel));
			List<String> catalyzerLore = new ArrayList<>();
			catalyzerLore.add(catalyzerLevelString);
			for(String line: configsFile.getStringList("more-5-interface.price-lore"))
			{
				line = ChatColor.translateAlternateColorCodes('&', line);
				line = line.replace("{price}", String.valueOf(costFormula));
				catalyzerLore.add(line);
			}
			catalyzerMeta.setLore(catalyzerLore);
			catalyzer.setItemMeta(catalyzerMeta);
			p.updateInventory();
		}
	}
	//endregion
	
	//region Random Picker
	private void onRandomPick(Player p, Map<Enchantment, Integer> enchantsList, Boolean decrement)
	{
		if(decrement)
		{
			if(playersInterfaces.containsKey(p))
			{
				IndividualInterface playerInterface = playersInterfaces.get(p);
				Map<Enchantment, Integer> randomEnchants = playerInterface.getRandomEnchants();
				int randomInt = rand.nextInt(randomEnchants.size());
				int i = 0;
				if(randomEnchants.size() != 1) 
				{		
					for(Enchantment enchantment : randomEnchants.keySet())
					{
						if(i == randomInt)
						{							
							randomEnchants.remove(enchantment);
							return;
						}
						i++;
					}
				}
				else
				{
					int catalyzerMaxLevel = 4;
					for(i = 0; i < catalyzerMaxLevel - 1; i++)
					{
						onRandomPick(p, enchantsList, false);
					}
				}
			}
		}
		else
		{
			int randomInt = rand.nextInt(enchantsList.size());
			if(playersInterfaces.containsKey(p))
			{
				IndividualInterface playerInterface = playersInterfaces.get(p);
				Map<Enchantment, Integer> randomEnchants = playerInterface.getRandomEnchants();
				int catalyzerMaxLevel = 4;
				if(randomEnchants.size() != catalyzerMaxLevel)
				{	
					int i = 0;
					for(Entry<Enchantment, Integer> enchantment : enchantsList.entrySet())
					{
						if(i == randomInt)
						{
							if(randomEnchants.containsKey(enchantment.getKey()))
							{
								onRandomPick(p, enchantsList, false);
								return;
							}
							else
							{
								randomEnchants.put(enchantment.getKey(), enchantsList.get(enchantment.getKey()));
								return;
							}
						}
						i++;
					}
				}
				else
				{
					for(int i = 0; i < catalyzerMaxLevel - 1; i++)
					{
						onRandomPick(p, enchantsList, true);
					}
				}
			}
		}
	}
	//endregion
	
	//region Interface Click Event
	@EventHandler
	public void onInterfaceClick(InventoryClickEvent e) 
	{
		Player p = (Player) e.getWhoClicked();
		if(playersInterfaces.containsKey(p)) 
		{
			IndividualInterface playerInterface = playersInterfaces.get(p);
			ItemStack pItem = playerInterface.getItemToUnenchant();
			//region First Interface
			if(e.getClickedInventory() != null && e.getClickedInventory().equals(playerInterface.getFirstInterfaceChoice()))
			{	
				e.setCancelled(true);
				switch(e.getSlot()) {
				case 10:
					e.getWhoClicked().closeInventory();
					for(String line : messagesFile.getStringList("errors.unenchantment-aborted"))
					{
						line = ChatColor.translateAlternateColorCodes('&', line);
						e.getWhoClicked().sendMessage(prefix + line);
					}
					try
					{					
						((Player)e.getWhoClicked()).playSound(e.getWhoClicked().getLocation(), Sound.valueOf(configsFile.getString("sounds.anvil-close-sound").toUpperCase()), 5, 5);
					}
					catch(IllegalArgumentException ex)
					{
						for(String line : messagesFile.getStringList("errors.invalid-sound"))
						{
							line = line.replace("{sound}", configsFile.getString("sounds.anvil-close-sound"));
							line = ChatColor.translateAlternateColorCodes('&', line);
							main.getLogger().warning(line);
						}
					}
					break;
				case 16:
					playerInterface.setClosedInventory(true);
					p.closeInventory();
					try
					{					
						((Player)e.getWhoClicked()).getWorld().playSound(e.getWhoClicked().getLocation(), Sound.valueOf(configsFile.getString("sounds.first-interface.accept-click-sound").toUpperCase()), 5, 5);
					}
					catch(IllegalArgumentException ex)
					{
						for(String line : messagesFile.getStringList("errors.invalid-sound"))
						{
							line = line.replace("{sound}", configsFile.getString("sounds.first-interface.accept-click-sound"));
							line = ChatColor.translateAlternateColorCodes('&', line);
							main.getLogger().warning(line);
						}
					}
					if(playerInterface.getItemEnchantsSet().size() < 5)
					{
						Bukkit.getScheduler().scheduleSyncDelayedTask(main.getPlugin(), new Runnable() {
				            public void run() {
				            	playerInterface.setUnEnchanterChooseInterface(unEnchanterChooseInterface(p, pItem));
				            }
				        }, 2L);
					}
					else if(playerInterface.getItemEnchantsSet().size() >= 5)
					{
						Bukkit.getScheduler().scheduleSyncDelayedTask(main.getPlugin(), new Runnable() {
				            public void run() {
				            	playerInterface.setUnEnchanterInterfaceRandom(unEnchanterInterfaceRandom(p, pItem));
				            }
				        }, 2L);
					}
					break;
				default:
					break;
				}
			}
			//endregion
			
			//region Second Interface (Less and more than 5 enchants)
			else if(playerInterface.getUnEnchanterChooseInterface() != null || playerInterface.getUnEnchanterInterfaceRandom() != null)
			{		
				
				e.setCancelled(true);
				switch(e.getSlot()) 
				{
					case 3:
						if(e.getClickedInventory() != null && e.getClickedInventory().equals(playerInterface.getUnEnchanterChooseInterface()))
						{
							selectEmerad(playerInterface, e.getCurrentItem(), 1, e.getClickedInventory(), 3);
						}
						break;
					case 5:
						if(e.getClickedInventory() != null && e.getClickedInventory().equals(playerInterface.getUnEnchanterChooseInterface()))
						{
							selectEmerad(playerInterface, e.getCurrentItem(), 2, e.getClickedInventory(), 5);
						}
						break;
					case 13:
						if(e.getClickedInventory() != null && e.getClickedInventory().equals(playerInterface.getUnEnchanterInterfaceRandom()))
						{
							if(e.getClick().isLeftClick())
							{
								try
								{					
									playerInterface.getPlayer().playSound(playerInterface.getPlayer().getLocation(), Sound.valueOf(configsFile.getString("sounds.more-5-interface.increment-catalyzer-level-sound").toUpperCase()), 5, 5);
								}
								catch(IllegalArgumentException ex)
								{
									for(String line : messagesFile.getStringList("errors.invalid-sound"))
									{
										line = line.replace("{sound}", configsFile.getString("sounds.more-5-interface.increment-catalyzer-level-sound"));
										line = ChatColor.translateAlternateColorCodes('&', line);
										main.getLogger().warning(line);
									}
								}
								incrementCatalyzerLevel((Player)e.getWhoClicked(), e.getCurrentItem());
								onRandomPick(p, playerInterface.getItemEnchants(), false);
							}
							else if(e.getClick().isRightClick())
							{
								try
								{					
									playerInterface.getPlayer().playSound(playerInterface.getPlayer().getLocation(), Sound.valueOf(configsFile.getString("sounds.more-5-interface.decrement-catalyzer-level-sound").toUpperCase()), 5, 5);
								}
								catch(IllegalArgumentException ex)
								{
									for(String line : messagesFile.getStringList("errors.invalid-sound"))
									{
										line = line.replace("{sound}", configsFile.getString("sounds.more-5-interface.decrement-catalyzer-level-sound"));
										line = ChatColor.translateAlternateColorCodes('&', line);
										main.getLogger().warning(line);
									}
								}
								decrementCatalyzerLevel((Player) e.getWhoClicked(), e.getCurrentItem());
								onRandomPick(p, playerInterface.getItemEnchants(), true);
							}
						}
						break;
					case 21:
						if(e.getClickedInventory() != null && e.getClickedInventory().equals(playerInterface.getUnEnchanterChooseInterface()))
						{
							selectEmerad(playerInterface, e.getCurrentItem(), 3, e.getClickedInventory(), 21);
						}
						break;
					case 23:
						if(e.getClickedInventory() != null && e.getClickedInventory().equals(playerInterface.getUnEnchanterChooseInterface()))
						{
							selectEmerad(playerInterface, e.getCurrentItem(), 4, e.getClickedInventory(), 23);
						}
						break;
					case 16:
						double result = 0;
						float pExp = ((Player) e.getWhoClicked()).getTotalExperience();
						if(e.getClickedInventory() != null && e.getClickedInventory().equals(playerInterface.getUnEnchanterChooseInterface()))
						{
							if(playerInterface.getSelectedEnchants() <= 0)
							{
								for(String line : messagesFile.getStringList("errors.no-enchants-selected"))
								{
									line = ChatColor.translateAlternateColorCodes('&', line);
									playerInterface.getPlayer().sendMessage(prefix + line);
								}
								break;
							}
							else
							{
								int emeraldLevels = 0;
								for(ItemStack emeraldSelected : e.getClickedInventory().getContents())
								{
									if(emeraldSelected.getType() == Material.PAPER
										&& emeraldSelected.getItemMeta().getDisplayName().contains(ChatColor.translateAlternateColorCodes('&', configsFile.getString("less-5-interface.enchantment-selected-name"))))
									{
										for(Enchantment ench : emeraldSelected.getEnchantments().keySet())
										{
											emeraldLevels += emeraldSelected.getEnchantments().get(ench);
										}
									}
								}
								int enchants = playerInterface.getSelectedEnchants();
								int enchantsLevel = emeraldLevels;
								result = Math.round(200.0 * (enchants * (1.0 + (enchantsLevel/10.0))));
							}
						}
						else if(e.getClickedInventory() != null && e.getClickedInventory().equals(playerInterface.getUnEnchanterInterfaceRandom()))
						{
							int catalyzerLevel = playerInterface.getCatalyzerLevel();
							double baseCost = configsFile.getInt("catalyzer-base-price");
							double levelMultiplier = configsFile.getInt("catalyzer-level-multiplier");
							result = baseCost * levelMultiplier * catalyzerLevel;
						}
						if(pExp >= result)
						{
							if(e.getClickedInventory() != null && e.getClickedInventory().equals(playerInterface.getUnEnchanterChooseInterface()))
							{
								for(Enchantment ench : playerInterface.getEnchantedBookSet()) 
								{
									playerInterface.getItemToUnenchant().removeEnchantment(ench);
								}
							}
							else if(e.getClickedInventory() != null && e.getClickedInventory().equals(playerInterface.getUnEnchanterInterfaceRandom()))
							{								
								EnchantmentStorageMeta bookStorage = (EnchantmentStorageMeta) playerInterface.getEnchantedBook().getItemMeta();
								playerInterface.getEnchantedBook().setItemMeta(bookStorage);
								for(Enchantment ench : playerInterface.getRandomEnchants().keySet())
								{
									playerInterface.getItemToUnenchant().removeEnchantment(ench);
									bookStorage.addStoredEnchant(ench, playerInterface.getRandomEnchants().get(ench), true);
									playerInterface.getEnchantedBook().setItemMeta(bookStorage);
								}
							}
							if(configsFile.getBoolean("weapon-lock"))
							{
								ItemMeta itemMeta = playerInterface.getItemToUnenchant().getItemMeta();
								List<String> itemLore = itemMeta.getLore();
								if(itemLore == null) {
									itemLore = new ArrayList<>();
								}
								itemLore.add(" ");
								itemLore.add(ChatColor.translateAlternateColorCodes('&', configsFile.getString("locked-item-lore")));
								itemMeta.setLore(itemLore);
								playerInterface.getItemToUnenchant().setItemMeta(itemMeta);
							}
							ItemMeta enchantedBookToGiveMeta = playerInterface.getEnchantedBook().getItemMeta();
							enchantedBookToGiveMeta.setLore(new ArrayList<>());
							playerInterface.getEnchantedBook().setItemMeta(enchantedBookToGiveMeta);
							playerInterface.getPlayer().setTotalExperience(0);
							playerInterface.getPlayer().setExp(0);
							playerInterface.getPlayer().setLevel(0);
							playerInterface.getPlayer().giveExp((int) Math.round(pExp - result));
							giveUnEnchantedItemBack(p, playerInterface.getEnchantedBook(), true);
							giveUnEnchantedItemBack(p, playerInterface.getItemToUnenchant(), true);
						}
						else
						{
							for(String line : messagesFile.getStringList("errors.not-enough-XP"))
							{
								line = ChatColor.translateAlternateColorCodes('&', line);
								line = line.replace("{price}", String.valueOf(Math.round((result - pExp))));
								playerInterface.getPlayer().sendMessage(prefix + line);
							}
							try
							{					
								playerInterface.getPlayer().playSound(playerInterface.getPlayer().getLocation(), Sound.valueOf(configsFile.getString("sounds.not-enough-xp-sound").toUpperCase()), 5, 5);
							}
							catch(IllegalArgumentException ex)
							{
								for(String line : messagesFile.getStringList("errors.invalid-sound"))
								{
									line = line.replace("{sound}", configsFile.getString("sounds.not-enough-xp-sound"));
									line = ChatColor.translateAlternateColorCodes('&', line);
									main.getLogger().warning(line);
								}
							}
							break;
						}
						playerInterface.setClosedInventory(true);
						try
						{					
							playerInterface.getPlayer().playSound(playerInterface.getPlayer().getLocation(), Sound.valueOf(configsFile.getString("sounds.unenchantment-done-sound").toUpperCase()), 5, 5);
						}
						catch(IllegalArgumentException ex)
						{
							for(String line : messagesFile.getStringList("errors.invalid-sound"))
							{
								line = line.replace("{sound}", configsFile.getString("sounds.unenchantment-done-sound"));
								line = ChatColor.translateAlternateColorCodes('&', line);
								main.getLogger().warning(line);
							}
						}
						playerInterface.getPlayer().closeInventory();
						break;
					default:
						break;
				}
			}
			//endregion
		}
	}
	//endregion

	//region Anvil and under block break event
	@EventHandler
	public void onSpecialAnvilDestroyed(BlockBreakEvent e)
	{
		if(e.getBlock() != null && e.getBlock().getType() == Material.ANVIL)
		{
			Location brokenAnvilLoc = e.getBlock().getLocation();
			if(placedUnEnchanters.contains(brokenAnvilLoc)) 
			{
				e.setCancelled(true);
				if(configsFile.getBoolean("break-permission") || e.getPlayer().hasPermission("opsyunenchanter.break"))
				{					
					placedUnEnchanters.remove(brokenAnvilLoc);
					main.getCustomConfigs().saveDatabaseFile();
					if(!playersInterfaces.isEmpty())
					{
						for(Entry<Player, IndividualInterface> p : playersInterfaces.entrySet())
						{
							Location anvilOpen = p.getValue().getOpenedAnvilLoc();
							if(anvilOpen != null && anvilOpen.equals(brokenAnvilLoc))
							{
								p.getKey().closeInventory();
							}
						}
					}
					try
					{					
						e.getPlayer().getWorld().playSound(e.getBlock().getLocation(), Sound.valueOf(configsFile.getString("sounds.anvil-break-sound").toUpperCase()), 5, 5);
					}
					catch(IllegalArgumentException ex)
					{
						for(String line : messagesFile.getStringList("errors.invalid-sound"))
						{
							line = line.replace("{sound}", configsFile.getString("sounds.anvil-break-sound"));
							line = ChatColor.translateAlternateColorCodes('&', line);
							main.getLogger().warning(line);
						}
					}
					e.getBlock().setType(Material.AIR);
					e.getBlock().getWorld().dropItemNaturally(e.getBlock().getLocation(), specialAnvil);
				}
				else
				{
					for(String line : messagesFile.getStringList("errors.not-enough-permissions"))
					{
						line = ChatColor.translateAlternateColorCodes('&', line);
						e.getPlayer().sendMessage(prefix + line);
					}
				}
			}
		}
		else if(e.getBlock() != null && e.getBlock().getLocation().add(0, 1, 0).getBlock().getType() == Material.ANVIL)
		{
			Location aboveAnvil = e.getBlock().getLocation().add(0, 1, 0);
			if(placedUnEnchanters.contains(aboveAnvil))
			{
				e.setCancelled(true);
				for(String line : messagesFile.getStringList("errors.cant-break-block-under"))
				{
					line = ChatColor.translateAlternateColorCodes('&', line);
					e.getPlayer().sendMessage(prefix + line);
				}
			}
		}
	}
	//endregion
	
	//region On Special Anvil Explode and under block explode
	@EventHandler(priority = EventPriority.LOWEST)
	public void onSpecialAnvilExplode(EntityExplodeEvent e)
	{
		for(Block blockToCheck : e.blockList())
		{
			if(blockToCheck != null && blockToCheck.getType() == Material.ANVIL)
			{
				Location brokenAnvilLoc = blockToCheck.getLocation();
				if(placedUnEnchanters.contains(brokenAnvilLoc)) 
				{
					e.setCancelled(true);
					if(configsFile.getBoolean("can-be-exploded"))
					{						
						placedUnEnchanters.remove(brokenAnvilLoc);
						main.getCustomConfigs().saveDatabaseFile();
						if(!playersInterfaces.isEmpty())
						{
							for(Entry<Player, IndividualInterface> p : playersInterfaces.entrySet())
							{
								Location anvilOpen = p.getValue().getOpenedAnvilLoc();
								if(anvilOpen != null && anvilOpen.equals(brokenAnvilLoc))
								{
									p.getKey().closeInventory();
								}
							}
						}
						try
						{					
							blockToCheck.getWorld().playSound(brokenAnvilLoc, Sound.valueOf(configsFile.getString("sounds.anvil-break-sound").toUpperCase()), 5, 5);
						}
						catch(IllegalArgumentException ex)
						{
							for(String line : messagesFile.getStringList("errors.invalid-sound"))
							{
								line = line.replace("{sound}", configsFile.getString("sounds.anvil-break-sound"));
								line = ChatColor.translateAlternateColorCodes('&', line);
								main.getLogger().warning(line);
							}
						}
						blockToCheck.setType(Material.AIR);
						blockToCheck.getWorld().dropItemNaturally(brokenAnvilLoc, specialAnvil);
					}
				}
			}
			else if(blockToCheck != null && blockToCheck.getLocation().add(0, 1, 0).getBlock().getType() == Material.ANVIL)
			{
				Location brokenAnvilLoc = blockToCheck.getLocation().add(0, 1, 0);
				if(placedUnEnchanters.contains(brokenAnvilLoc))
				{
					e.setCancelled(true);
				}
			}
		}
	}
	//endregion

}
