/**
 * WorldGen plugin - (c) 2013 by Michael Huttinger (TheHUTMan)
 * LPGL v3.0 License
 */
package local.thehutman.worldgen;

import com.amrsatrio.server.Utils;

import java.util.logging.Logger;

/**
 * Utility class for various common routines
 *
 * @author Huttinger
 */
class Utility {

	/**
	 * Global static property holding an instance to the console logger for our
	 * plugin
	 */
	public static Logger log;

	/**
	 * Helper function to find the base package string that contains a class.
	 * Used to help abstract references to version-specific minecraft/bukkit
	 * classes.
	 *
	 * @param className Class to search for in packages
	 * @return Name of package that contains the class
	 */
	public static String getNMSPrefix(String className) {
		String ver = Utils.getVersion();
		String pref = className.startsWith("Craft") ? "org.bukkit.craftbukkit." : "net.minecraft.server.";
		return pref + ver.substring(0, ver.length() - 1);
	}

}
