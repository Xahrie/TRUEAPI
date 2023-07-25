package de.xahrie.trues.api.coverage.league;

import java.io.Serializable;
import java.util.List;

import de.xahrie.trues.api.coverage.match.model.PRMMatch;
import de.xahrie.trues.api.coverage.playday.Playday;

/**
 * Created by Lara on 15.02.2023 for TRUEbot
 */
public record LeaguePlayday(Playday playday, List<PRMMatch> matches) implements Serializable { }
