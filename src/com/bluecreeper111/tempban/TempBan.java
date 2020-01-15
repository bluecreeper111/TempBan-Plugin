package com.bluecreeper111.tempban;

import java.util.Date;

import org.bukkit.BanList;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.plugin.java.JavaPlugin;

public class TempBan extends JavaPlugin implements CommandExecutor {

	// Basic onEnable method
	public void onEnable() {
		// Using console sender to enable color in console
		Bukkit.getConsoleSender().sendMessage("§6TempBan plugin has been enabled.");
		// Setting the main class as our two commands' execution class
		getCommand("tempban").setExecutor(this);
		getCommand("unban").setExecutor(this);
	}

	// Basic onDisable method
	public void onDisable() {
		Bukkit.getLogger().info("TempBan plugin has been disabled.");

	}

	// A non-deprecated way to get an OfflinePlayer object from a string (username)
	private OfflinePlayer getOfflinePlayer(String name) {
		for (OfflinePlayer player : Bukkit.getOfflinePlayers()) {
			// Return the player if username matches
			if (player.getName().equals(name)) {
				return player;
			}
		}
		// Return null if no player is found
		return null;
	}

	// The method I use to determine what time the specified player is banned for
	// (will return null if invalid time is provided)
	// Returns the amount of seconds the player is banned for
	private Long determineTime(String[] args) {
		// Long to total the seconds of every provided time (for the 'for' loop)
		long total = 0;
		// Testing every provided time (5d, 6h etc.) (not including first argument;
		// player's name)
		for (int i = 1; i < args.length; i++) {
			// Making sure that the time provided is actually an integer
			String time = args[i];
			try {
				Long.parseLong(time.substring(0, time.length() - 1));
			} catch (NumberFormatException e) {
				return null;
			}
			// Converting the number in the time to a long, simply easier for conversion to
			// seconds (in 5d, taking out the '5')
			long num = Long.parseLong(time.substring(0, time.length() - 1));
			// Getting the time unit (in 5d, the 'd')
			char unit = time.charAt(time.length() - 1);
			long timeint = 0;
			// Simple switch statement to convert the unit provided to seconds
			switch (unit) {
			// seconds
			case 's':
				timeint = num;
				break;
			// hours
			case 'h':
				timeint = num * 3600;
				break;
			// days
			case 'd':
				timeint = num * 86400;
				break;
			// weeks
			case 'w':
				timeint = num * 604800;
				break;
			// months
			case 'm':
				timeint = num * 2628000;
				break;
			// years
			case 'y':
				timeint = num * 31540000;
				break;
			// if the unit is invalid
			default:
				return null;
			}
			// We now have the amount of seconds for this provided time, now we can add it
			// to the total
			total += timeint;
		}
		// Returns the total seconds.
		return total;
	}

	/*
	 * Code run when a command is submitted on the server Typically for large
	 * plugins, developers will make seperate classes for each command But on a
	 * small plugin such as this one we will just include them in our main class
	 */
	public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
		// BanList for our ban commands
		BanList banlist = Bukkit.getBanList(BanList.Type.NAME);
		if (cmd.getName().equalsIgnoreCase("tempban")) {
			// Immediately check if the player does not have permission to run this command, and if not, return it
			if (!sender.hasPermission("tempban.tempban")) {
				sender.sendMessage("§cYou do not have permission to do that!");
				return true;
			}
			/*
			 * First, we check if the user submitted the correct amount of arguments.
			 * (/tempban <user> <time>) We will make sure there is two or more arguments, so
			 * we know they specify a player and at least one time unit
			 */
			if (args.length >= 2) {
				// Now, we double check the player they specify actually exists
				OfflinePlayer target = this.getOfflinePlayer(args[0]);
				if (target != null) {
					/*
					 * The next thing we have to check is if they provided a valid time. We will use
					 * a method above to determine the time based on integers and units Example of
					 * valid time: "1w 5d" Example of invalid time: "1week 5 days"
					 */
					Long time = this.determineTime(args);
					if (time != null) {
						// Simply checking if the player is already banned
						if (!banlist.isBanned(target.getName())) {
							// Finally, we can ban!
							Date date = new Date(System.currentTimeMillis() + (time*1000));
							banlist.addBan(target.getName(), "§cYou have been temporarily banned from this server!", date, sender.getName());
							// When we ban a player they don't actually get kicked, so we have to kick the player if they are online
							if (target.isOnline()) {
								target.getPlayer().kickPlayer("§cYou have been temporarily banned from this server!");
							}
							sender.sendMessage("§aYou have temporarily banned player §2" + target.getName() + "§a!");
						} else {
							sender.sendMessage("§cThat player is already banned!");
						}
					} else {
						sender.sendMessage(
								"§cInvalid time provided! §cBe sure to follow the correct format! (e.g: '10d')"
										+ "\n§7Valid units are 's' (seconds), 'h' (hours), 'd' (days), 'w' (weeks), 'm' (months), 'y' (years)");
					}
				} else {
					sender.sendMessage("§cThat player does not exist!");
				}
			} else {
				// Send a basic error message
				sender.sendMessage("§cInvalid syntax. Try /tempban <player> [time]");
			}
		} else if (cmd.getName().equalsIgnoreCase("unban")) {
			// Many practices will be the same with this command, however I will explain the differences
			if (!sender.hasPermission("tempban.unban")) {
				sender.sendMessage("§cYou do not have permission to do that!");
				return true;
			}
			if (args.length == 1) {
				OfflinePlayer target = this.getOfflinePlayer(args[0]);
				if (target != null) {
					// Making sure the player is actually banned
					if (banlist.isBanned(target.getName())) {
						// Pardons the ban!
						banlist.pardon(target.getName());
						sender.sendMessage("§aYou have unbanned player §2" + target.getName() + "§a.");
					} else {
						sender.sendMessage("§cThat player is not banned!");
					}
				} else {
					sender.sendMessage("§cThat player does not exist!");
				}
			} else {
				sender.sendMessage("§cInvalid syntax! Try /unban <player>");
			}
		}
		// The boolean for this method really isn't necessary if you handle all
		// exceptions for the command. We can have it just return true.
		return true;
	}
}