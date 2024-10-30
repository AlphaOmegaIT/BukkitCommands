package me.blvckbytes.bukkitcommands;

import me.blvckbytes.bbconfigmapper.ConfigMapper;
import me.blvckbytes.bbconfigmapper.sections.AConfigSection;
import me.blvckbytes.bukkitevaluable.CommandUpdater;
import me.blvckbytes.bukkitevaluable.ConfigKeeper;
import me.blvckbytes.bukkitevaluable.ConfigManager;
import me.blvckbytes.bukkitevaluable.section.ACommandSection;
import me.blvckbytes.bukkitevaluable.section.PermissionsSection;
import me.blvckbytes.gpeee.interpreter.EvaluationEnvironmentBuilder;
import org.bukkit.event.Event;
import org.bukkit.event.Listener;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.EventListener;
import java.util.Optional;
import java.util.logging.Level;

/**
 * Represents a factory for creating and managing command sections.
 */
public class CommandSectionFactory {

    private static final String LISTENER_FOLDER = "listener";
    private static final String COMMANDS_FOLDER = "commands";
    private static final String PERMISSIONS_SECTION = "permissions";

    private final JavaPlugin loadedPlugin;
    private final CommandUpdater commandUpdater;

    /**
     * Constructs a new CommandSectionFactory with the specified JavaPlugin.
     *
     * @param loadedPlugin The JavaPlugin instance to use.
     */
    public CommandSectionFactory(final JavaPlugin loadedPlugin) {
        this.loadedPlugin = loadedPlugin;
        this.commandUpdater = new CommandUpdater(this.loadedPlugin);
    }

    /**
     * Creates a new command based on the provided command class, command section class, and file path.
     *
     * @param commandClass        The class representing the BukkitCommand.
     * @param commandSectionClass The class representing the ACommandSection.
     * @param filePath            The file path to load the configuration from.
     * @return True if the command was successfully created, false otherwise.
     */
    public boolean createCommand(Class<? extends BukkitCommand> commandClass, Class<? extends ACommandSection> commandSectionClass, String filePath) {
        try {
            ConfigMapper cfg = new ConfigManager(this.loadedPlugin, COMMANDS_FOLDER).loadConfig(filePath);
            ACommandSection mapSection = cfg.mapSection("commands." + commandSectionClass.getDeclaredConstructor(EvaluationEnvironmentBuilder.class).newInstance(new EvaluationEnvironmentBuilder()).getDefaultCommandName(), commandSectionClass);
            BukkitCommand bukkitCommand = commandClass.getDeclaredConstructor(commandSectionClass, this.loadedPlugin.getClass()).newInstance(mapSection, this.loadedPlugin);
            this.commandUpdater.tryRegisterCommand(bukkitCommand);
            this.commandUpdater.trySyncCommands();
        } catch (Exception e) {
            this.loadedPlugin.getLogger().log(Level.WARNING, "Could not register command: " + commandClass.getSimpleName(), e);
            return false;
        }
        return true;
    }

    /**
     * Retrieves the permissions section based on the provided file path.
     *
     * @param filePath The file path to load the configuration from.
     * @return An Optional containing the PermissionsSection if found, empty otherwise.
     */
    public PermissionsSection getPermission(String filePath) {
        try {
            ConfigMapper cfg = new ConfigManager(this.loadedPlugin, COMMANDS_FOLDER).loadConfig(filePath);
            return cfg.mapSection(PERMISSIONS_SECTION, PermissionsSection.class);
        } catch (Exception e) {
            this.loadedPlugin.getLogger().log(Level.WARNING, "Could not obtain permission for path: " + filePath, e);
            return null;
        }
    }
}