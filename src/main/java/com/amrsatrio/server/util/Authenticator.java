package com.amrsatrio.server.util;

import com.amrsatrio.server.ServerPlugin;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.plugin.Plugin;

import java.security.NoSuchAlgorithmException;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.regex.Pattern;

public class Authenticator {
	public static final String[] COMMON_PASSWORDS = new String[]{"redstonehost", "123456", "password", "12345678", "1234", "pussy", "12345", "dragon", "qwerty", "696969", "mustang", "letmein", "baseball", "master", "michael", "football", "shadow", "monkey", "abc123", "pass", "fuckme", "6969", "jordan", "harley", "ranger", "iwantu", "jennifer", "hunter", "fuck", "2000", "test", "batman", "trustno1", "thomas", "tigger", "robert", "access", "love", "buster", "1234567", "soccer", "hockey", "killer", "george", "sexy", "andrew", "charlie", "superman", "asshole", "fuckyou", "dallas", "jessica", "panties", "pepper", "1111", "austin", "william", "daniel", "golfer", "summer", "heather", "hammer", "yankees", "joshua", "maggie", "biteme", "enter", "ashley", "thunder", "cowboy", "silver", "richard", "fucker", "orange", "merlin", "michelle", "corvette", "bigdog", "cheese", "matthew", "121212", "patrick", "martin", "freedom", "ginger", "blowjob", "nicole", "sparky", "yellow", "camaro", "secret", "dick", "falcon", "taylor", "111111", "131313", "123123", "bitch", "hello", "scooter", "please", "porsche", "guitar", "chelsea", "black", "diamond", "nascar", "jackson", "cameron", "654321", "computer", "amanda", "wizard", "xxxxxxxx", "money", "phoenix", "mickey", "bailey", "knight", "iceman", "tigers", "purple", "andrea", "horny", "dakota", "aaaaaa", "player", "sunshine", "morgan", "starwars", "boomer", "cowboys", "edward", "charles", "girls", "booboo", "coffee", "xxxxxx", "bulldog", "ncc1701", "rabbit", "peanut", "john", "johnny", "gandalf", "spanky", "winter", "brandy", "compaq", "carlos", "tennis", "james", "mike", "brandon", "fender", "anthony", "blowme", "ferrari", "cookie", "chicken", "maverick", "chicago", "joseph", "diablo", "sexsex", "hardcore", "666666", "willie", "welcome", "chris", "panther", "yamaha", "justin", "banana", "driver", "marine", "angels", "fishing", "david", "maddog", "hooters", "wilson", "butthead", "dennis", "fucking", "captain", "bigdick", "chester", "smokey", "xavier", "steven", "viking", "snoopy", "blue", "eagles", "winner", "samantha", "house", "miller", "flower", "jack", "firebird", "butter", "united", "turtle", "steelers", "tiffany", "zxcvbn", "tomcat", "golf", "bond007", "bear", "tiger", "doctor", "gateway", "gators", "angel", "junior", "thx1138", "porno", "badboy", "debbie", "spider", "melissa", "booger", "1212", "flyers", "fish", "porn", "matrix", "teens", "scooby", "jason", "walter", "cumshot", "boston", "braves", "yankee", "lover", "barney", "victor", "tucker", "princess", "mercedes", "5150", "doggie", "zzzzzz", "gunner", "horney", "bubba", "2112", "fred", "johnson", "xxxxx", "tits", "member", "boobs", "donald", "bigdaddy", "bronco", "penis", "voyager", "rangers", "birdie", "trouble", "white", "topgun", "bigtits", "bitches", "green", "super", "qazwsx", "magic", "lakers", "rachel", "slayer", "scott", "2222", "asdf", "video", "london", "7777", "marlboro", "srinivas", "internet", "action", "carter", "jasper", "monster", "teresa", "jeremy", "11111111", "bill", "crystal", "peter", "pussies", "cock", "beer", "rocket", "theman", "oliver", "prince", "beach", "amateur", "7777777", "muffin", "redsox", "star", "testing", "shannon", "murphy", "frank", "hannah", "dave", "eagle1", "11111", "mother", "nathan", "raiders", "steve", "forever", "angela", "viper", "ou812", "jake", "lovers", "suckit", "gregory", "buddy", "whatever", "young", "nicholas", "lucky", "helpme", "jackie", "monica", "midnight", "college", "baby", "cunt", "brian", "mark", "startrek", "sierra", "leather", "232323", "4444", "beavis", "bigcock", "happy", "sophie", "ladies", "naughty", "giants", "booty", "blonde", "fucked", "golden", "0", "fire", "sandra", "pookie", "packers", "einstein", "dolphins", "0", "chevy", "winston", "warrior", "sammy", "slut", "8675309", "zxcvbnm", "nipples", "power", "victoria", "asdfgh", "vagina", "toyota", "travis", "hotdog", "paris", "rock", "xxxx", "extreme", "redskins", "erotic", "dirty", "ford", "freddy", "arsenal", "access14", "wolf", "nipple", "iloveyou", "alex", "florida", "eric", "legend", "movie", "success", "rosebud", "jaguar", "great", "cool", "cooper", "1313", "scorpio", "mountain", "madison", "987654", "brazil", "lauren", "japan", "naked", "squirt", "stars", "apple", "alexis", "aaaa", "bonnie", "peaches", "jasmine", "kevin", "matt", "qwertyui", "danielle", "beaver", "4321", "4128", "runner", "swimming", "dolphin", "gordon", "casper", "stupid", "shit", "saturn", "gemini", "apples", "august", "3333", "canada", "blazer", "cumming", "hunting", "kitty", "rainbow", "112233", "arthur", "cream", "calvin", "shaved", "surfer", "samson", "kelly", "paul", "mine", "king", "racing", "5555", "eagle", "hentai", "newyork", "little", "redwings", "smith", "sticky", "cocacola", "animal", "broncos", "private", "skippy", "marvin", "blondes", "enjoy", "girl", "apollo", "parker", "qwert", "time", "sydney", "women", "voodoo", "magnum", "juice", "abgrtyu", "777777", "dreams", "maxwell", "music", "rush2112", "russia", "scorpion", "rebecca", "tester", "mistress", "phantom", "billy", "6666", "albert", "foobar"};
	private FileConfiguration pluginConfig;
	private Plugin plugin;
	private int counter;
	private CommandSender sender;
	private String[] passInput;
	private Boolean isPlayer;
	private String currentPassword;
	private boolean noAccessGrantedMessage;
	private Runnable callback;

	public Authenticator(CommandSender sender, String[] passInput, Plugin plugin, boolean noAccessGrantedMessage) {
		this.sender = sender;
		this.passInput = passInput;
		pluginConfig = plugin.getConfig();
		this.plugin = plugin;
		counter = pluginConfig.getInt("lock-counter");
		currentPassword = pluginConfig.getString("password");
		isPlayer = this.sender instanceof Player;
		this.noAccessGrantedMessage = noAccessGrantedMessage;
	}

//	public Authenticator(CommandSender sender, String[] passInput, Plugin plugin) {
//		this(sender, passInput, plugin, false);
//	}

	/**
	 * @return true if the Authenticator needs further input
	 */
	public boolean authenticate(Runnable callback) {
		try {
			if (pluginConfig.getLong("lock-until") >= System.currentTimeMillis()) {
//				long timeleft = pluginConfig.getLong("lock-until") - System.currentTimeMillis();
//				msg(sender, String.format(ChatColor.RED + "This feature is unusable until %d minutes and %d seconds from now.",
//						new Object[]{TimeUnit.MILLISECONDS.toMinutes(timeleft),
//								TimeUnit.MILLISECONDS.toSeconds(timeleft)
//										- TimeUnit.MINUTES.toSeconds(TimeUnit.MILLISECONDS.toMinutes(timeleft))}));
//				msg(sender, ChatColor.RED + "The referenced command is currently unusable.");
//				Utils.tripleBeepSamePitch(sender, plugin);
				// keep it silent
				return false;
			}

			if (isPlayer) {
				if (currentPassword.isEmpty()) {
					callback.run();
					return false;
				}

				this.callback = callback;
				msg(sender, "Type the password in the chat box. Retype the command to cancel.");
				Utils.beepOnceNormalPitch(sender);
				return true;
			} else {
				if (currentPassword.isEmpty()) {
					callback.run();
					return false;
				}

				if (passInput.length == 0) {
					msg(sender, "Provide a password by typing the password between the command name and arguments");
					return false;
				}

				if (!matchPassword(passInput[0])) {
					msg(sender, ChatColor.RED + "Invalid password");
					incrementPass();
					return false;
				}

				counter = 0;
				pluginConfig.set("lock-counter", counter);

				if (!noAccessGrantedMessage) {
					msg(sender, ChatColor.GREEN + "Access granted");
				}

				callback.run();
				return false;
			}
		} catch (Throwable e) {
			e.printStackTrace();
			return false;
		}
	}

	public void handleChat(AsyncPlayerChatEvent event) {
		final Player asPlayer = event.getPlayer();

		try {
			if (!currentPassword.isEmpty() && !matchPassword(event.getMessage())) {
				msg(asPlayer, ChatColor.RED + "Invalid password");
				incrementPass();
				Utils.tripleBeepSamePitch(sender, plugin);
				return;
			}
		} catch (NoSuchAlgorithmException e) {
			msg(asPlayer, ChatColor.RED + "An error occurred");
			Utils.tripleBeepSamePitch(sender, plugin);
			return;
		}

		counter = 0;
		pluginConfig.set("lock-counter", counter);

		if (!noAccessGrantedMessage) {
			msg(asPlayer, ChatColor.GREEN + "Access granted");
			asPlayer.playSound(asPlayer.getLocation(), "minecraft:block.note.pling", 3.0f, 1.25f);
			Bukkit.getScheduler().scheduleSyncDelayedTask(plugin, () -> asPlayer.playSound(asPlayer.getLocation(), "minecraft:block.note.pling", 3.0f, 1.5f), 4L);
			Bukkit.getScheduler().scheduleSyncDelayedTask(plugin, () -> asPlayer.playSound(asPlayer.getLocation(), "minecraft:block.note.pling", 3.0f, 1.75f), 8L);
			Bukkit.getScheduler().scheduleSyncDelayedTask(plugin, () -> asPlayer.playSound(asPlayer.getLocation(), "minecraft:block.note.pling", 3.0f, 2f), 12L);
		}

		callback.run();
	}

	private boolean matchPassword(String input) throws NoSuchAlgorithmException {
		return Utils.sha1(input).equals(currentPassword);
	}

	private void incrementPass() {
		counter++;
		pluginConfig.set("lock-counter", counter);
		int lt = pluginConfig.getInt("lock-threshold");

		if (counter % lt == 0 && counter != 0) {
			Calendar c = Calendar.getInstance();
			c.setTime(new Date(System.currentTimeMillis()));
			c.add(Calendar.MINUTE, pluginConfig.getInt("lock-duration"));
			Date until = c.getTime();
			pluginConfig.set("lock-until", until.getTime());
			plugin.saveConfig();
			msg(sender, ChatColor.RED + "Too many incorrect attempts, locking password-protected commands for " + pluginConfig.getInt("lock-duration") + " minutes.");
		}
	}

	private void msg(CommandSender a, String b) {
		ServerPlugin.msg(a, b, "Authenticator");
	}

	public static int getPasswordStrength(String password) {
		int grade = 0;
		grade += Pattern.compile("[A-Z]").matcher(password).find() ? 1 : 0;
		grade += Pattern.compile("[a-z]").matcher(password).find() ? 1 : 0;
		grade += Pattern.compile("[0-9]").matcher(password).find() ? 1 : 0;
		grade += Pattern.compile("[^a-zA-Z0-9]").matcher(password).find() ? 1 : 0;
		grade += password.length() > 7 ? 1 : 0;
		grade += password.length() > 9 ? 1 : 0;
		grade += password.length() > 11 ? 1 : 0;

		if (Arrays.asList(COMMON_PASSWORDS).contains(password.toLowerCase())) {
			grade = 0;
		}

		return grade;
	}
}
