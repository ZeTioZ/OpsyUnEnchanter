package fr.opsycraft.configsmanager;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.bukkit.Location;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.Plugin;

import fr.opsycraft.unenchanter.Main;

public class CustomConfigs
{
    
	private Plugin plugin;
	
	private Main main;
	
	//region Class Constructor
	public CustomConfigs(Main main)
	{
		this.main = main;
		this.plugin = this.main.getPlugin();
	}
	//endregion
	
	//region Config File (Creator/Getter/Reloader)
    private YamlConfiguration configsFileConfig;
    
    public YamlConfiguration getConfigsFile()
    {
        return this.configsFileConfig;
    }

    public void createConfigsFile()
    {
    	File configsFile = new File(plugin.getDataFolder(), "configs.yml");
        if (!configsFile.exists())
        {
        	configsFile.getParentFile().mkdirs();
        	plugin.saveResource("configs.yml", false);
        }

        configsFileConfig = new YamlConfiguration();
        try
        {
        	configsFileConfig.load(configsFile);
        }
        catch (IOException | InvalidConfigurationException e)
        {
        	this.plugin.getLogger().severe("Error during the loading of the config file!");
        }
    }
    //endregion
    
    //region Message File (Creator/Getter/Reloader)
    private YamlConfiguration messagesFileConfig;
    
    //region Get Messages File
    public YamlConfiguration getMessagesFile()
    {
        return this.messagesFileConfig;
    }
    //endregion

    //region Create Messages File
    public void createMessagesFile()
    {
    	File messagesFile = new File(plugin.getDataFolder(), "messages.yml");
        if (!messagesFile.exists())
        {
        	messagesFile.getParentFile().mkdirs();
            plugin.saveResource("messages.yml", false);
        }

        messagesFileConfig = new YamlConfiguration();
        try
        {
        	messagesFileConfig.load(messagesFile);
        }
        catch (IOException | InvalidConfigurationException e) {
        	this.plugin.getLogger().severe("Error during loading of the messages file!");
        }
    }
    //endregion
    
    //endregion
    
    //region Database File (Creator/Getter/Saver)
    private File databaseFile;
    private YamlConfiguration databaseFileConfig;
    
    //region Get Database File
    public YamlConfiguration getDatabaseFile()
    {
        return this.databaseFileConfig;
    }
    //endregion
    
    //region Create Database File
    public void createDatabaseFile()
    {
    	File databaseFolder = new File(plugin.getDataFolder() + File.separator + "database");
    	databaseFile = new File(databaseFolder, "placed-anvils.yml");
        if (!databaseFile.exists())
        {
        	databaseFile.getParentFile().mkdirs();
        	try
        	{
				databaseFile.createNewFile();
			}
        	catch (IOException e)
        	{
        		this.plugin.getLogger().severe("Error during the database creation!");
			}
         }

        databaseFileConfig = new YamlConfiguration();
        try
        {
        	databaseFileConfig.load(databaseFile);
        }
        catch (IOException | InvalidConfigurationException e)
        {
        	this.plugin.getLogger().severe("Error during the database loading!");
        }
    }
    //endregion
    
    //region Save Database File
    public void saveDatabaseFile()
    {
    	try
    	{
    		if(main.getUnEnchanter().getListA() != null)
    		{
    				getDatabaseFile().set("Placed Anvils Location", main.getUnEnchanter().getListA());
    				this.databaseFileConfig.save(this.databaseFile);
    		}
		}
    	catch (IOException e)
    	{
    		this.plugin.getLogger().severe("An error occured while saving the database !\nPlease contact the plugin creator !");
		}
    }
    //endregion
    
    //region Load Database File
    public void loadDatabaseFile()
    {
		@SuppressWarnings("unchecked")
		List<Location> backupedAnvils = (List<Location>) getDatabaseFile().getList("Placed Anvils Location");
		if(backupedAnvils == null || backupedAnvils.isEmpty())
		{			
			main.getUnEnchanter().setListA(new ArrayList<Location>());
		}
		else
		{
			main.getUnEnchanter().setListA(backupedAnvils);			
		}
    }
    //endregion
    
    //region Reload Database File
    public void reloadDatabaseFile()
    {
    	this.createDatabaseFile();
    	this.loadDatabaseFile();
    }
    //endregion
    
    //endregion
}