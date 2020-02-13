package fr.opsycraft.unenchanter;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

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
	private List<Recipe> recipesBackup = new ArrayList<>();
	
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
		
		//region Saving the previous recipes list
		Iterator<Recipe> a = Bukkit.getServer().recipeIterator();
		while(a.hasNext())
		{
			Recipe recipe = a.next();
			recipesBackup.add(recipe);
		}
		//endregion
		
		UnlockScroll unlockScroll = new UnlockScroll(this);
		unlockScroll.registerUnlockScrollCraft();
		new SpecialAnvil(this).registerSpecialAnvilCraft();
		
	    //Listeners
		registerEvents(this, unenchanter, unlockScroll);
		getCommand("desenchant").setExecutor(new UnEnchanterCommands(this));
	}
	
	@Override
	public void onDisable()
	{
		Bukkit.getServer().clearRecipes();
		for (Recipe r : recipesBackup)
		    getServer().addRecipe(r);
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