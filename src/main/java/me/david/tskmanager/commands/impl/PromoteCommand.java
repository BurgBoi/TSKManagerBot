package me.david.tskmanager.commands.impl;

import me.david.tskmanager.GuildCache;
import me.david.tskmanager.commands.CommandModel;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Role;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;

import java.util.List;

public class PromoteCommand extends CommandModel {

	public PromoteCommand() {
		super("promote", "Promotes a faction member to the next rank", "promote {@user}");
		setHrOnly(true);
	}

	@Override
	public void onCommand(MessageReceivedEvent event, List<String> args) {

		GuildCache cache = GuildCache.getCache(event.getGuild().getId());

		if (args.size() == 2 && event.getMessage().getMentionedMembers().size() == 1) {

			Role rank = null;
			Member member = event.getMessage().getMentionedMembers().get(0);

			for (Role role : member.getRoles())
				if (cache.getRanksTrack().getRankTrack().contains(role))
					rank = role;

			if (rank != null) {
				Role nextRank = cache.getRanksTrack().getNextRank(rank);
				if (member.getRoles().contains(cache.getLrRole()) && cache.getHRRoles().contains(nextRank)) {
					event.getGuild().removeRoleFromMember(member, cache.getLrRole()).queue();
					event.getGuild().addRoleToMember(member, cache.getHrRole()).queue();
				} else if (!member.getRoles().contains(cache.getLrRole()) && !member.getRoles().contains(cache.getHrRole()) && cache.getLRMRRoles().contains(nextRank))
					event.getGuild().addRoleToMember(member, cache.getLrRole()).queue();

				event.getGuild().addRoleToMember(member, nextRank).queue();
				event.getGuild().removeRoleFromMember(member, rank).queue();
				event.getChannel().sendMessage("Successfully updated the roles for " + member.getEffectiveName()).queue();
			} else
				event.getChannel().sendMessage("That member does not have a rank role!").queue();
		} else
			event.getChannel().sendMessage("Usage: " + cache.getPrefix() + getUsage()).queue();
	}
}