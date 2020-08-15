package fr.opsycraft.unenchanter;

import java.util.Iterator;

import org.bukkit.Bukkit;
import org.bukkit.event.Listener;
import org.bukkit.inventory.Recipe;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;

import fr.opsycraft.configsmanager.CustomConfigs;
import fr.opsycraft.itemsmanager.SpecialAnvil;
import fr.opsycraft.itemsmanager.UnlockScroll;

public class Main extends JavaPlugin implements Listener {
	private Plugin plugin;
	private CustomConfigs customConfigs;
	private UnEnchanter unenchanter;
	private UnlockScroll unlockScroll;
	private SpecialAnvil specialAnvil;
	
	@Override
	public void onEnable()
	{   
        //Instantiate static plugin variable
		plugin = this;

		//region Database file creation/load
		customConfigs = new CustomConfigs(this);
		customConfigs.createConfigsFile();
		customConfigs.createMessagesFile();
		unenchanter = new UnEnchanter(this);
		customConfigs.reloadDatabaseFile();
		//endregion
		
		unlockScroll = new UnlockScroll(this);
		unlockScroll.registerUnlockScrollCraft();
		specialAnvil = new SpecialAnvil(this);
		specialAnvil.registerSpecialAnvilCraft();
		
	    //Listeners
		registerEvents(this, unenchanter, unlockScroll, new UnEnchanterEnchantBlocker(this));
		getCommand("desenchant").setExecutor(new UnEnchanterCommands(this));
	}
	
	@Override
	public void onDisable()
	{
		Iterator<Recipe> serverRecipes = Bukkit.getServer().recipeIterator();
		Recipe recipe;
		while(serverRecipes.hasNext())
		{
			recipe = serverRecipes.next();
			if(recipe != null && (recipe.getResult().equals(unlockScroll.getUnlockScroll()) || recipe.getResult().equals(specialAnvil.getSpecialAnvil())))
			{
				serverRecipes.remove();
			}
		}
		plugin = null;
	}
	
    public static void registerEvents(Plugin plugin, Listener... listeners) {
        for (Listener listener : listeners)
        {
            Bukkit.getServer().getPluginManager().registerEvents(listener, plugin);
        }
    }
	
    //region Getters/Setters
	public Plugin getPlugin() {
		return plugin;
	}	
	
	public CustomConfigs getCustomConfigs()
	{
		return customConfigs;
	}
	
	public UnEnchanter getUnEnchanter()
	{
		return unenchanter;
	}
	//endregion
}