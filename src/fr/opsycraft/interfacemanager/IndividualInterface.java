package fr.opsycraft.interfacemanager;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.EnchantmentStorageMeta;

public class IndividualInterface
{
	private Player p;
	//region Player (Getter)
	public Player getPlayer()
	{
		return this.p;
	}
	//endregion
	
	private ItemStack itemToUnenchant;
	//region Item to unchenchant (Getter/Setter)
	public ItemStack getItemToUnenchant()
	{
		return this.itemToUnenchant;
	}
	public void setItemToUnEnchant(ItemStack item)
	{
		this.itemToUnenchant = item;
	}
	//endregion
	
	private Map<Enchantment, Integer> itemEnchants;
	//region Item Enchants (Getter/Setter)
	public Map<Enchantment, Integer> getItemEnchants() {
		return this.itemEnchants;
	}
	
	public Set<Enchantment> getItemEnchantsSet() {
		return this.itemEnchants.keySet();
	}


	public void setItemEnchants(Map<Enchantment, Integer> itemEnchants) {
		this.itemEnchants = itemEnchants;
	}
	//endregion
	
	//region Class Constructor
	public IndividualInterface(Player p) 
	{
		this.p = p;
		this.itemToUnenchant = p.getItemInHand();
		this.itemEnchants = this.itemToUnenchant.getEnchantments();
	}
	//endregion
	
	//region Class Backend
	private int selectedEnchants = 0;
	//region Selected Enchants (Getter/Setter)
	public int getSelectedEnchants() 
	{
		return this.selectedEnchants;
	}
	
	public void setSelectedEnchants(int i)
	{
		this.selectedEnchants = i;
	}
	//endregion
	
	private Map<Integer, ItemStack> selectedEmeralds = new HashMap<>();
	//region Selected Emeralds (Getter/Setter)
	public Map<Integer, ItemStack> getSelectedEmeralds() 
	{
		return this.selectedEmeralds;
	}
	
	public void setSelectedEmeralds(int key, ItemStack emerald)
	{
		this.selectedEmeralds.put(key, emerald);
	}
	//endregion
	
	private Map<Integer, ItemStack> emeraldMap = new HashMap<>();
	//region Emerald Map (Getter/Setter)
	public Map<Integer, ItemStack> getEmeraldMap()
	{
		return this.emeraldMap;
	}
	
	public void setEmeraldMap(int key, ItemStack emerald)
	{
		this.emeraldMap.put(key, emerald);
	}
	//endregion
	
	private int catalyzerLevel = 1;
	//region Catalyzer Level (Getter/Setter)
	public Integer getCatalyzerLevel()
	{
		return this.catalyzerLevel;
	}
	
	public void setCatalyzerLevel(Integer catalyzerLevel)
	{
		this.catalyzerLevel = catalyzerLevel;
	}
	//endregion
	
	private Map<Enchantment, Integer> randomEnchants = new HashMap<>();
	//region Random Enchants (Getter/Setter)
	public Map<Enchantment, Integer> getRandomEnchants()
	{
		return this.randomEnchants;
	}
	
	public void setRandomEnchants(Map<Enchantment, Integer> enchants)
	{
		this.randomEnchants = enchants;
	}
	//endregion
	
	private ItemStack enchantedBook = new ItemStack(Material.ENCHANTED_BOOK);
	//region Enchanted Book (Getter/Setter)
	public ItemStack getEnchantedBook()
	{
		return this.enchantedBook;
	}
	
	public void setEnchantedBook(ItemStack enchantedbook) 
	{
		this.enchantedBook = enchantedbook;
	}
	
	public Set<Enchantment> getEnchantedBookSet()
	{
		EnchantmentStorageMeta lueurdEspoir = (EnchantmentStorageMeta) enchantedBook.getItemMeta();
		return lueurdEspoir.getStoredEnchants().keySet();
	}
	//endregion

	private Location openedAnvilLoc;
	//region Opened Anvil Location (Getter/Setter)
	public Location getOpenedAnvilLoc()
	{
		return this.openedAnvilLoc;
	}
	
	public void setOpenedAnvilLoc(Location anvilLoc)
	{
		this.openedAnvilLoc = anvilLoc;
	}
	//endregion
	
	//region Inventories
	private Inventory firstInterfaceChoice;
	private Inventory unEnchanterChooseInterface;
	private Inventory unEnchanterInterfaceRandom;
	//region First Inventory Choice (Getter/Setter)
	public Inventory getFirstInterfaceChoice() 
	{
		return this.firstInterfaceChoice;
	}

	public void setFirstInterfaceChoice(Inventory firstInterfaceChoice) 
	{
		this.firstInterfaceChoice = firstInterfaceChoice;
	}
	//endregion
	
	//region UnEnchanter Choose Interface (Getter/Setter)
	public Inventory getUnEnchanterChooseInterface() 
	{
		return this.unEnchanterChooseInterface;
	}

	public void setUnEnchanterChooseInterface(Inventory unEnchanterChooseInterface) 
	{
		this.unEnchanterChooseInterface = unEnchanterChooseInterface;
	}
	//endregion
	
	//region UnEnchant More 5 Interface Random (Getter/Setter)
	public Inventory getUnEnchanterInterfaceRandom() {
		return this.unEnchanterInterfaceRandom;
	}

	public void setUnEnchanterInterfaceRandom(Inventory unEnchantInterfaceRandom) {
		this.unEnchanterInterfaceRandom = unEnchantInterfaceRandom;
	}
	//endregion
	//endregion
	
	private Boolean closedInventory = false;
	//region Closed Inventory Boolean (Getter/Setter)
	public Boolean getClosedInventory() {
		return this.closedInventory;
	}

	public void setClosedInventory(Boolean closedInventory) {
		this.closedInventory = closedInventory;
	}
	//endregion
	
	//endregion
}
