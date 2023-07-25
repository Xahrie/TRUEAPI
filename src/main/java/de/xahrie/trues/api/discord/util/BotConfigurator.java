package de.xahrie.trues.api.discord.util;

import de.xahrie.trues.api.util.io.cfg.JSON;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.events.session.ReadyEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.utils.ChunkingFilter;
import net.dv8tion.jda.api.utils.MemberCachePolicy;
import net.dv8tion.jda.api.utils.cache.CacheFlag;
import org.jetbrains.annotations.NotNull;

public class BotConfigurator extends ListenerAdapter {
  private JDABuilder builder;

  public JDA run() {
    final var json = JSON.read("connect.json");
    final var apiKey = json.getString("discord");
    this.builder = JDABuilder.create(apiKey, ConfigLoader.getIntents());
    return this.configure();
  }

  private JDA configure() {
    final JDA discordAPI = builder.setActivity(ConfigLoader.getActivity())
        .enableCache(
            CacheFlag.ACTIVITY,
            CacheFlag.CLIENT_STATUS,
            CacheFlag.EMOJI,
            CacheFlag.FORUM_TAGS,
            CacheFlag.MEMBER_OVERRIDES,
            CacheFlag.ONLINE_STATUS,
            CacheFlag.ROLE_TAGS,
            CacheFlag.SCHEDULED_EVENTS,
            CacheFlag.STICKER,
            CacheFlag.VOICE_STATE
        )
        .setChunkingFilter(ChunkingFilter.ALL)
        .setMemberCachePolicy(MemberCachePolicy.ALL)
        .setStatus(ConfigLoader.getStatus())
        .build();
    discordAPI.addEventListener(this);
    return discordAPI;
  }

  @Override
  public void onReady(@NotNull ReadyEvent event) {
    System.out.println("Nunu ist bereit!");
  }
}
