package me.david.tskmanager;

import net.dv8tion.jda.api.entities.MessageChannel;
import net.dv8tion.jda.api.entities.Role;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class GuildCache {

	public static final Map<String, GuildCache> CACHE_MAP = new HashMap<>();

	private String guildID;
	private String prefix = "&";
	private Role hrRole;
	private Role lrRole;
	private List<Role> defaultJoinRoles = new ArrayList<>();
	private RanksTrack ranksTrack = new RanksTrack();
	private List<Role> LRMRRoles = new ArrayList<>();
	private List<Role> HRRoles = new ArrayList<>();
	private List<MessageChannel> eventChannels = new ArrayList<>();
	private Role attendingEventRole;

	public GuildCache(String guildID) {
		this.guildID = guildID;
	}

	//get the cache and if there isn't a cache, put a cache and deserialize the data from a disk
	public static GuildCache getCache(String guildID) {
		GuildCache cache = CACHE_MAP.get(guildID);

		if (cache == null) {

			cache = new GuildCache(guildID);
			cache.deserialize();
			CACHE_MAP.put(guildID, cache);
		}

		return cache;
	}

	//serialize the cache
	public void serialize() {
		try {

			//create new file and folder if does not exist
			File file = new File("./GuildData/", guildID + ".json");
			File directory = new File("./GuildData/");

			if (!directory.exists())
				directory.mkdir();
			if (!file.exists())
				file.createNewFile();

			//putting the data into a JSONObject
			JSONObject jsonObject = new JSONObject();
			if (prefix != null)
				jsonObject.put(JsonDataKeys.PREFIX.getKey(), prefix);

			if (hrRole != null)
				jsonObject.put(JsonDataKeys.HRROLE.getKey(), hrRole.getId());

			if (lrRole != null)
				jsonObject.put(JsonDataKeys.LR_ROLE.getKey(), lrRole.getId());

			if (!defaultJoinRoles.isEmpty()) {
				JSONArray jsonArray = new JSONArray();
				for (Role role : defaultJoinRoles)
					jsonArray.add(role.getId());
				jsonObject.put(JsonDataKeys.DEFAULT_JOIN_ROLES.getKey(), jsonArray);
			}

			if (!ranksTrack.getRankTrack().isEmpty()) {
				JSONArray jsonArray = new JSONArray();
				for (Role role : ranksTrack.getRankTrack())
					jsonArray.add(role.getId());
				jsonObject.put(JsonDataKeys.RANKS_TRACK.getKey(), jsonArray);
			}

			if (!LRMRRoles.isEmpty()) {
				JSONArray jsonArray = new JSONArray();
				for (Role role : LRMRRoles)
					jsonArray.add(role.getId());
				jsonObject.put(JsonDataKeys.LRMR_ROLES.getKey(), jsonArray);
			}

			if (!HRRoles.isEmpty()) {
				JSONArray jsonArray = new JSONArray();
				for (Role role : HRRoles)
					jsonArray.add(role.getId());
				jsonObject.put(JsonDataKeys.HRROLES.getKey(), jsonArray);
			}

			if (!eventChannels.isEmpty()) {
				JSONArray jsonArray = new JSONArray();
				for (MessageChannel channel : eventChannels)
					jsonArray.add(channel.getId());
				jsonObject.put(JsonDataKeys.EVENT_CHANNELS.getKey(), jsonArray);
			}

			if (attendingEventRole != null)
				jsonObject.put(JsonDataKeys.ATTENDING_EVENT_ROLE.getKey(), attendingEventRole.getId());

			//write the JSONObject to a file
			try (FileWriter fileWriter = new FileWriter(file)) {
				fileWriter.write(jsonObject.toString());
			}

		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	//deserialize data from a file
	public void deserialize() {

		//get the file
		File file = new File("./GuildData/", guildID + ".json");

		if (file.exists()) {
			//reading the file
			try (FileReader fileReader = new FileReader(file)) {

				JSONParser jsonParser = new JSONParser();
				JSONObject jsonObject = (JSONObject) jsonParser.parse(fileReader);

				//parsing the JSONObject into variables
				if (isSet(jsonObject, JsonDataKeys.PREFIX.getKey()))
					this.prefix = (String) jsonObject.get(JsonDataKeys.PREFIX.getKey());

				if (isSet(jsonObject, JsonDataKeys.HRROLE.getKey()))
					this.hrRole = Main.jda.getGuildById(guildID).getRoleById((String) jsonObject.get(JsonDataKeys.HRROLE.getKey()));

				if (isSet(jsonObject, JsonDataKeys.LR_ROLE.getKey()))
					this.lrRole = Main.jda.getGuildById(guildID).getRoleById((String) jsonObject.get(JsonDataKeys.LR_ROLE.getKey()));

				if (isSet(jsonObject, JsonDataKeys.DEFAULT_JOIN_ROLES.getKey())) {
					JSONArray jsonArray = (JSONArray) jsonObject.get(JsonDataKeys.DEFAULT_JOIN_ROLES.getKey());
					for (int i = 0; i < jsonArray.size(); i++)
						this.defaultJoinRoles.add(Main.jda.getGuildById(guildID).getRoleById((String) jsonArray.get(i)));
				}

				if (isSet(jsonObject, JsonDataKeys.RANKS_TRACK.getKey())) {
					JSONArray jsonArray = (JSONArray) jsonObject.get(JsonDataKeys.RANKS_TRACK.getKey());
					for (int i = 0; i < jsonArray.size(); i++)
						this.ranksTrack.addRank(Main.jda.getGuildById(guildID).getRoleById((String) jsonArray.get(i)));
				}

				if (isSet(jsonObject, JsonDataKeys.LRMR_ROLES.getKey())) {
					JSONArray jsonArray = (JSONArray) jsonObject.get(JsonDataKeys.LRMR_ROLES.getKey());
					for (int i = 0; i < jsonArray.size(); i++)
						this.LRMRRoles.add(Main.jda.getGuildById(guildID).getRoleById((String) jsonArray.get(i)));
				}

				if (isSet(jsonObject, JsonDataKeys.HRROLES.getKey())) {
					JSONArray jsonArray = (JSONArray) jsonObject.get(JsonDataKeys.HRROLES.getKey());
					for (int i = 0; i < jsonArray.size(); i++)
						this.HRRoles.add(Main.jda.getGuildById(guildID).getRoleById((String) jsonArray.get(i)));
				}

				if (isSet(jsonObject, JsonDataKeys.EVENT_CHANNELS.getKey())) {
					JSONArray jsonArray = (JSONArray) jsonObject.get(JsonDataKeys.EVENT_CHANNELS.getKey());
					for (int i = 0; i < jsonArray.size(); i++)
						this.eventChannels.add(Main.jda.getGuildById(guildID).getTextChannelById((String) jsonArray.get(i)));
				}

				if (isSet(jsonObject, JsonDataKeys.ATTENDING_EVENT_ROLE.getKey()))
					this.attendingEventRole = Main.jda.getGuildById(guildID).getRoleById((String) jsonObject.get(JsonDataKeys.ATTENDING_EVENT_ROLE.getKey()));

			} catch (Exception e) {
				e.printStackTrace();
			}
		} else {
			//if the file doesn't exist create a new file for it
			serialize();
		}
	}

	//check if a key in a JSONObject has a value
	private boolean isSet(JSONObject jsonObject, String key) {
		if (jsonObject.get(key) != null)
			return true;
		else
			return false;
	}

	//create an enum of json data keys so I don't have to memorize all the data keys
	public enum JsonDataKeys {

		PREFIX("prefix"),
		HRROLE("hr-role"),
		LR_ROLE("lr-role"),
		DEFAULT_JOIN_ROLES("default-join-roles"),
		RANKS_TRACK("ranks-track"),
		LRMR_ROLES("lrmr-roles"),
		HRROLES("hr-roles"),
		ATTENDING_EVENT_ROLE("attending-event-role"),
		EVENT_CHANNELS("event-channels");

		private final String key;

		JsonDataKeys(String key) {
			this.key = key;
		}

		public String getKey() {
			return key;
		}
	}

	//getters
	public String getPrefix() {
		return prefix;
	}

	public Role getHrRole() {
		return hrRole;
	}

	public List<Role> getDefaultJoinRoles() {
		return defaultJoinRoles;
	}

	public RanksTrack getRanksTrack() {
		return ranksTrack;
	}

	public List<Role> getLRMRRoles() {
		return LRMRRoles;
	}

	public List<Role> getHRRoles() {
		return HRRoles;
	}

	public List<MessageChannel> getEventChannels() {
		return eventChannels;
	}

	public Role getAttendingEventRole() {
		return attendingEventRole;
	}

	public Role getLrRole() {
		return lrRole;
	}

	//setters
	public void setPrefix(String prefix) {
		this.prefix = prefix;
	}

	public void setHrRole(Role hrRole) {
		this.hrRole = hrRole;
	}

	public void setAttendingEventRole(Role attendingEventRole) {
		this.attendingEventRole = attendingEventRole;
	}

	public void setLrRole(Role lrRole) {
		this.lrRole = lrRole;
	}
}