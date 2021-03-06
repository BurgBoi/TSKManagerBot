package me.david.tskmanager.commands.impl;

import me.david.tskmanager.GuildCache;
import me.david.tskmanager.Main;
import me.david.tskmanager.commands.SetterCommandModel;
import me.david.tskmanager.commands.eventlisteners.impl.AddHrRolesEventListener;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Role;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;

import java.util.List;

public class HrRolesCommand extends SetterCommandModel {

	public HrRolesCommand() {
		super("hrroles", "Sets roles as an hr role", "hrroles (add|a|remove|r) (@role)");
		setRankUse(false, true);
	}

	@Override
	public void getterCommand(MessageReceivedEvent event, List<String> args) {
		GuildCache cache = GuildCache.getCache(event.getGuild().getId());

		if (!cache.getHRRoles().isEmpty()) {
			EmbedBuilder embedBuilder = new EmbedBuilder();
			embedBuilder.setTitle("HR Roles");
			embedBuilder.setColor(Main.defaultEmbedColor);
			for (Role role : cache.getHRRoles())
				embedBuilder.addField(role.getName(), "", true);
			event.getChannel().sendMessage(embedBuilder.build()).queue();
		} else
			event.getChannel().sendMessage("You have not set any roles as a hr role yet!").queue();
	}

	@Override
	public void setterCommand(MessageReceivedEvent event, List<String> args) {
		GuildCache cache = GuildCache.getCache(event.getGuild().getId());

		if (args.get(1).startsWith("a")) {

			event.getChannel().sendMessage("Ping roles to set as a hr role. Type finish to finish").queue();
			Main.jda.addEventListener(new AddHrRolesEventListener(event.getMember(), event.getChannel()));
		} else if (args.get(1).startsWith("r") && event.getMessage().getMentionedRoles().size() == 1) {

			int index = cache.getHRRoles().indexOf(event.getMessage().getMentionedRoles().get(0));
			cache.getHRRoles().remove(index);
			event.getChannel().sendMessage("Removed the role " + event.getMessage().getMentionedRoles().get(0) + " from hr roles").queue();
			cache.serialize();
		} else
			event.getChannel().sendMessage("Usage: " + cache.getPrefix() + getUsage()).queue();
	}
}
