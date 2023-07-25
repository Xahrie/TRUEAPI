package de.xahrie.trues.api.scouting.scouting;

import java.util.List;

import de.xahrie.trues.api.community.orgateam.OrgaTeam;
import de.xahrie.trues.api.community.orgateam.OrgaTeamFactory;
import de.xahrie.trues.api.coverage.participator.model.Lineup;
import de.xahrie.trues.api.coverage.participator.model.Participator;
import de.xahrie.trues.api.coverage.team.model.AbstractTeam;
import de.xahrie.trues.api.scouting.ScoutingGameType;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.interactions.callbacks.IReplyCallback;

public record CustomScouting(AbstractTeam opponent) {
  public void sendCustom(IReplyCallback event, ScoutingType type, ScoutingGameType gameType, Integer days, Integer page) {
    final EmbedBuilder builder = new EmbedBuilder()
        .setTitle(type.getTitleStart() + opponent.getName())
        .setDescription("Lineup: opgg und porofessor coming soon\nTyp: ");
    final Participator participator = determineParticipator(event);
    final List<Lineup> lineups = (participator != null) ? participator.getTeamLineup().getLineup() : null;
    new ScoutingEmbedHandler(opponent, lineups, gameType, days, page).get(type, participator).forEach(builder::addField);
    event.getHook().sendMessageEmbeds(builder.build()).queue();
  }

  private Participator determineParticipator(IReplyCallback event) {
    final OrgaTeam team = OrgaTeamFactory.getTeamFromChannel(event.getGuildChannel());
    if (team == null) return null;

    final Scouting scouting = ScoutingManager.forTeam(team);
    if (scouting == null) return null;

    final AbstractTeam t = scouting.participator().getTeam();
    if (t == null) return null;

    return t.equals(opponent) ? scouting.participator() : null;
  }
}
