package me.david.tskmanager.commands;

import me.david.tskmanager.GuildCache;
import me.david.tskmanager.MemberBypassLevel;
import me.david.tskmanager.commands.impl.HelpCommand;
import me.david.tskmanager.exceptions.TwoRankUseTypeException;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.PrivateChannel;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;

import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public abstract class CommandModel extends ListenerAdapter {

	private List<String> names = new ArrayList<>();
	private String description;
	private String usage;
	private boolean hrOnly;
	private boolean shrOnly;

	public CommandModel(String name, String description, String usage) {
		this.names = Arrays.asList(name.split("\\|"));
		this.description = description;
		this.usage = usage;
		HelpCommand.commands.add(this);
	}

	@Override
	public void onMessageReceived(@Nonnull MessageReceivedEvent event) {

		//check if the channel is a private channel
		if (event.getChannel() instanceof PrivateChannel) return;

		//get arguments
		List<String> args = new ArrayList<>() {
			{
				addAll(Arrays.asList(event.getMessage().getContentRaw().split(" ")));
			}
		};

		//check if the message starts with the bot's prefix and it is an actual command
		GuildCache cache = GuildCache.getCache(event.getGuild().getId());
		if (args.get(0).startsWith(cache.getPrefix()) && names.contains(args.get(0).replaceFirst(cache.getPrefix(), "").toLowerCase())) {
			//check if the person has permissions to do something(check if they are the HR role or not)
			if (!(hrOnly || shrOnly))
				onCommand(event, args);
			else {

				Boolean bypassType = null;
				for (MemberBypassLevel bypassLevel : cache.getMemberBypassList()) {
					if (bypassLevel.getMember().equals(event.getMember())) {
						bypassType = bypassLevel.getMemberBypassType();
					}
				}


				if (hrOnly) {
					try {
						if (event.getMember().getRoles().contains(cache.getHrRole()) || event.getMember().hasPermission(Permission.MANAGE_SERVER) || bypassType)
							onCommand(event, args);
						else
							event.getChannel().sendMessage("You must be an HR or have the permission manage server to do this!").queue();
					} catch (NullPointerException e) {
						event.getChannel().sendMessage("You must be an HR or have the permission administrator to do this!").queue();
					}
				} else if (shrOnly) {
					try {
						if (event.getMember().getRoles().contains(cache.getShrRole()) || event.getMember().hasPermission(Permission.ADMINISTRATOR) || !bypassType)
							onCommand(event, args);
						else
							event.getChannel().sendMessage("You must be an SHR or have the permission administrator to do this!").queue();
					} catch (NullPointerException e) {
						event.getChannel().sendMessage("You must be an SHR or have the permission administrator to do this!").queue();
					}
				}
			}
		}
	}

	//getters
	public String getNames() {
		return String.join("|", names);
	}

	public String getDescription() {
		return description;
	}

	public String getUsage() {
		return usage;
	}

	public boolean isHrOnly() {
		return hrOnly;
	}

	public boolean isShrOnly() {
		return shrOnly;
	}

	//setters
	public void setRankUse(boolean hrOnly, boolean shrOnly) {
		try {
			if (hrOnly == true && shrOnly == true)
				throw new TwoRankUseTypeException();
			else {
				if (hrOnly == true)
					this.hrOnly = true;
				else if (shrOnly == true)
					this.shrOnly = true;
			}
		} catch (TwoRankUseTypeException e) {
			e.printStackTrace();
		}
	}

	public abstract void onCommand(MessageReceivedEvent event, List<String> args);

}
