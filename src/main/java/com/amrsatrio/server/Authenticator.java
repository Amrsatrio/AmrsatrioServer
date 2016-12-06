package com.amrsatrio.server;

import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.plugin.java.JavaPlugin;

import java.security.NoSuchAlgorithmException;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.regex.Pattern;

public class Authenticator {
	private FileConfiguration theConfig;
	private JavaPlugin thePlugin;
	private int counter;
	private CommandSender theSender;
	private String[] pi;
	private Boolean isPlayer;
	private String curpass;
	private boolean noAG;
	private Runnable afterFunction;

	public Authenticator(CommandSender p, String[] passInput, JavaPlugin jp, boolean noAccessGrantedMessage) {
		theSender = p;
		pi = passInput;
		theConfig = jp.getConfig();
		thePlugin = jp;
		counter = theConfig.getInt("lock-counter");
		curpass = theConfig.getString("password");
		isPlayer = theSender instanceof Player;
		noAG = noAccessGrantedMessage;
	}

	public Authenticator(CommandSender p, String[] passInput, JavaPlugin jp) {
		this(p, passInput, jp, false);
	}

	public static int getPasswordStrength(String password) {
		String[] commonPasswords = {"redstonehost", "123456", "password", "12345678", "1234", "pussy", "12345", "dragon", "qwerty", "696969", "mustang", "letmein", "baseball", "master", "michael", "football", "shadow", "monkey", "abc123", "pass", "fuckme", "6969", "jordan", "harley", "ranger", "iwantu", "jennifer", "hunter", "fuck", "2000", "test", "batman", "trustno1", "thomas", "tigger", "robert", "access", "love", "buster", "1234567", "soccer", "hockey", "killer", "george", "sexy", "andrew", "charlie", "superman", "asshole", "fuckyou", "dallas", "jessica", "panties", "pepper", "1111", "austin", "william", "daniel", "golfer", "summer", "heather", "hammer", "yankees", "joshua", "maggie", "biteme", "enter", "ashley", "thunder", "cowboy", "silver", "richard", "fucker", "orange", "merlin", "michelle", "corvette", "bigdog", "cheese", "matthew", "121212", "patrick", "martin", "freedom", "ginger", "blowjob", "nicole", "sparky", "yellow", "camaro", "secret", "dick", "falcon", "taylor", "111111", "131313", "123123", "bitch", "hello", "scooter", "please", "porsche", "guitar", "chelsea", "black", "diamond", "nascar", "jackson", "cameron", "654321", "computer", "amanda", "wizard", "xxxxxxxx", "money", "phoenix", "mickey", "bailey", "knight", "iceman", "tigers", "purple", "andrea", "horny", "dakota", "aaaaaa", "player", "sunshine", "morgan", "starwars", "boomer", "cowboys", "edward", "charles", "girls", "booboo", "coffee", "xxxxxx", "bulldog", "ncc1701", "rabbit", "peanut", "john", "johnny", "gandalf", "spanky", "winter", "brandy", "compaq", "carlos", "tennis", "james", "mike", "brandon", "fender", "anthony", "blowme", "ferrari", "cookie", "chicken", "maverick", "chicago", "joseph", "diablo", "sexsex", "hardcore", "666666", "willie", "welcome", "chris", "panther", "yamaha", "justin", "banana", "driver", "marine", "angels", "fishing", "david", "maddog", "hooters", "wilson", "butthead", "dennis", "fucking", "captain", "bigdick", "chester", "smokey", "xavier", "steven", "viking", "snoopy", "blue", "eagles", "winner", "samantha", "house", "miller", "flower", "jack", "firebird", "butter", "united", "turtle", "steelers", "tiffany", "zxcvbn", "tomcat", "golf", "bond007", "bear", "tiger", "doctor", "gateway", "gators", "angel", "junior", "thx1138", "porno", "badboy", "debbie", "spider", "melissa", "booger", "1212", "flyers", "fish", "porn", "matrix", "teens", "scooby", "jason", "walter", "cumshot", "boston", "braves", "yankee", "lover", "barney", "victor", "tucker", "princess", "mercedes", "5150", "doggie", "zzzzzz", "gunner", "horney", "bubba", "2112", "fred", "johnson", "xxxxx", "tits", "member", "boobs", "donald", "bigdaddy", "bronco", "penis", "voyager", "rangers", "birdie", "trouble", "white", "topgun", "bigtits", "bitches", "green", "super", "qazwsx", "magic", "lakers", "rachel", "slayer", "scott", "2222", "asdf", "video", "london", "7777", "marlboro", "srinivas", "internet", "action", "carter", "jasper", "monster", "teresa", "jeremy", "11111111", "bill", "crystal", "peter", "pussies", "cock", "beer", "rocket", "theman", "oliver", "prince", "beach", "amateur", "7777777", "muffin", "redsox", "star", "testing", "shannon", "murphy", "frank", "hannah", "dave", "eagle1", "11111", "mother", "nathan", "raiders", "steve", "forever", "angela", "viper", "ou812", "jake", "lovers", "suckit", "gregory", "buddy", "whatever", "young", "nicholas", "lucky", "helpme", "jackie", "monica", "midnight", "college", "baby", "cunt", "brian", "mark", "startrek", "sierra", "leather", "232323", "4444", "beavis", "bigcock", "happy", "sophie", "ladies", "naughty", "giants", "booty", "blonde", "fucked", "golden", "0", "fire", "sandra", "pookie", "packers", "einstein", "dolphins", "0", "chevy", "winston", "warrior", "sammy", "slut", "8675309", "zxcvbnm", "nipples", "power", "victoria", "asdfgh", "vagina", "toyota", "travis", "hotdog", "paris", "rock", "xxxx", "extreme", "redskins", "erotic", "dirty", "ford", "freddy", "arsenal", "access14", "wolf", "nipple", "iloveyou", "alex", "florida", "eric", "legend", "movie", "success", "rosebud", "jaguar", "great", "cool", "cooper", "1313", "scorpio", "mountain", "madison", "987654", "brazil", "lauren", "japan", "naked", "squirt", "stars", "apple", "alexis", "aaaa", "bonnie", "peaches", "jasmine", "kevin", "matt", "qwertyui", "danielle", "beaver", "4321", "4128", "runner", "swimming", "dolphin", "gordon", "casper", "stupid", "shit", "saturn", "gemini", "apples", "august", "3333", "canada", "blazer", "cumming", "hunting", "kitty", "rainbow", "112233", "arthur", "cream", "calvin", "shaved", "surfer", "samson", "kelly", "paul", "mine", "king", "racing", "5555", "eagle", "hentai", "newyork", "little", "redwings", "smith", "sticky", "cocacola", "animal", "broncos", "private", "skippy", "marvin", "blondes", "enjoy", "girl", "apollo", "parker", "qwert", "time", "sydney", "women", "voodoo", "magnum", "juice", "abgrtyu", "777777", "dreams", "maxwell", "music", "rush2112", "russia", "scorpion", "rebecca", "tester", "mistress", "phantom", "billy", "6666", "albert", "foobar"};
		int grade = 0;
		grade += Pattern.compile("[A-Z]").matcher(password).find() ? 1 : 0;
		grade += Pattern.compile("[a-z]").matcher(password).find() ? 1 : 0;
		grade += Pattern.compile("[0-9]").matcher(password).find() ? 1 : 0;
		grade += Pattern.compile("[^a-zA-Z0-9]").matcher(password).find() ? 1 : 0;
		grade += password.length() > 7 ? 1 : 0;
		grade += password.length() > 9 ? 1 : 0;
		grade += password.length() > 11 ? 1 : 0;
		if (Arrays.asList(commonPasswords).contains(password.toLowerCase())) {
			grade = 0;
		}
		return grade;
	}

	public boolean auth(Runnable after) {
		try {
			if (theConfig.getLong("lock-until") >= System.currentTimeMillis()) {
				long timeleft = theConfig.getLong("lock-until") - System.currentTimeMillis();
//				msg(theSender, String.format("\u00a7cThis feature is unusable until %d minutes and %d seconds from now.",
//						new Object[]{TimeUnit.MILLISECONDS.toMinutes(timeleft),
//								TimeUnit.MILLISECONDS.toSeconds(timeleft)
//										- TimeUnit.MINUTES.toSeconds(TimeUnit.MILLISECONDS.toMinutes(timeleft))}));
				msg(theSender, "\u00a7cThe referenced command is currently unusable.");
				Utils.tripleBeepSamePitch(theSender, thePlugin);
				return false;
			}
			if (!isPlayer) {
				if (curpass.isEmpty()) {
					after.run();
					return true;
				}
				if (pi.length == 0) {
					msg(theSender, "Provide a password by typing the password between the command name and arguments.");
					return true;
				}
				if (!Utils.sha1(pi[0]).equals(curpass)) {
					msg(theSender, "\u00a7cBad password.");
					incrementPass();
					return true;
				}
				counter = 0;
				theConfig.set("lock-counter", counter);
				if (!noAG) {
					msg(theSender, "\u00a7aAccess granted.");
				}
				after.run();
				return true;
			} else {
				if (curpass.isEmpty()) {
					after.run();
					return false;
				}
				afterFunction = after;
				msg(theSender, "Type the password in the chat box. Retype the command to cancel.");
				Utils.beepOnceNormalPitch(theSender);
				return true;
			}
		} catch (Throwable e) {
			e.printStackTrace();
		}
		return true;
	}

	public void doChat(AsyncPlayerChatEvent a) throws NoSuchAlgorithmException {
		final Player asPlayer = a.getPlayer();
		if (!curpass.isEmpty() && !Utils.sha1(a.getMessage()).equals(curpass)) {
			msg(asPlayer, "\u00a7cBad password.");
			incrementPass();
			Utils.tripleBeepSamePitch(theSender, thePlugin);
			return;
		}
		counter = 0;
		theConfig.set("lock-counter", counter);
		if (!noAG) {
			msg(asPlayer, "\u00a7aAccess granted.");
			asPlayer.playSound(asPlayer.getLocation(), "minecraft:block.note.pling", 3.0f, 1.25f);
			Bukkit.getScheduler().scheduleSyncDelayedTask(thePlugin, new Runnable() {
				@Override
				public void run() {
					asPlayer.playSound(asPlayer.getLocation(), "minecraft:block.note.pling", 3.0f, 1.5f);
				}
			}, 4L);
			Bukkit.getScheduler().scheduleSyncDelayedTask(thePlugin, new Runnable() {
				@Override
				public void run() {
					asPlayer.playSound(asPlayer.getLocation(), "minecraft:block.note.pling", 3.0f, 1.75f);
				}
			}, 8L);
			Bukkit.getScheduler().scheduleSyncDelayedTask(thePlugin, new Runnable() {
				@Override
				public void run() {
					asPlayer.playSound(asPlayer.getLocation(), "minecraft:block.note.pling", 3.0f, 2f);
				}
			}, 12L);
		}
		afterFunction.run();
	}

	public void incrementPass() {
		counter++;
		theConfig.set("lock-counter", counter);
		int lt = theConfig.getInt("lock-threshold");
		if (counter % lt == 0 && counter != 0) {
			Calendar c = Calendar.getInstance();
			c.setTime(new Date(System.currentTimeMillis()));
			c.add(Calendar.MINUTE, theConfig.getInt("lock-duration"));
			Date until = c.getTime();
			theConfig.set("lock-until", until.getTime());
			thePlugin.saveConfig();
			msg(theSender, "\u00a7cToo many incorrect attempts, locking password-protected commands for " + theConfig.getInt("lock-duration") + " minutes.");
		}
	}

	public void msg(CommandSender a, String b) {
		a.sendMessage("\u00a79Authenticator> \u00a77" + b);
	}
}
