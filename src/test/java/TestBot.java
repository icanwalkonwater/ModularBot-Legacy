import com.jesus_crie.modularbot.ModularBot;
import com.jesus_crie.modularbot.ModularBuilder;
import com.jesus_crie.modularbot.command.*;
import com.jesus_crie.modularbot.config.ConfigHandler;
import com.jesus_crie.modularbot.config.SimpleConfig;
import com.jesus_crie.modularbot.config.Version;
import com.jesus_crie.modularbot.listener.CommandEvent;
import com.jesus_crie.modularbot.stats.Stats;
import com.jesus_crie.modularbot.stats.bundle.Bundle;
import com.jesus_crie.modularbot.stats.bundle.Keys;
import com.jesus_crie.modularbot.template.EmbedTemplate;
import com.jesus_crie.modularbot.template.Templates;
import com.jesus_crie.modularbot.utils.F;
import com.jesus_crie.modularbot.utils.dialog.DialogBuilder;
import net.dv8tion.jda.core.EmbedBuilder;
import net.dv8tion.jda.core.entities.Message;

import java.awt.Color;
import java.lang.reflect.Field;
import java.time.Instant;
import java.util.HashMap;
import java.util.List;

@SuppressWarnings("WeakerAccess")
public class TestBot {

    public static void main(String[] args) {
        ConfigHandler config = new SimpleConfig("./config.json", Version.of(1, 0, 0, 0), "TestModular");

        ModularBot bot = new ModularBuilder(args[0])
                .useStats()
                .useCustomConfigHandler(config)
                .build();
        ModularBot.getCommandManager().registerCommands(
                new CommandTest(),
                new CommandStop()
        );
        ModularBot.getCommandManager().registerQuickCommand("ping", e -> e.fastReply("Pong !"));
        ModularBot.getCommandManager().registerCommands(
                new QuickCommand("ping", AccessLevel.EVERYONE, e -> e.fastReply("Pong !"))
        );

        try {
            bot.connectToDiscord();
        } catch (Exception e) {
            ModularBot.logger().error("App", e);
        }
    }

    public static class CommandTest extends Command {

        private CommandTest() {
            super("test",
                    Contexts.EVERYWHERE,
                    AccessLevel.ADMINISTRATOR);
            description = "Yo ceci est une description !";

            registerPatterns(
                    new CommandPattern(new Argument[] {
                            Argument.forString("yo")
                    }, this::yo),

                    new CommandPattern(new Argument[] {
                            Argument.forString("embed")
                    }, this::hi),

                    new CommandPattern(null, this::test)
            );
        }

        private void test(CommandEvent event) {
            HashMap<Integer, String> translate = new HashMap<>();
            for (Field field : Keys.class.getDeclaredFields()) {
                translate.put(field.getName().hashCode(), field.getName());
            }

            Bundle full = Stats.collectEverything();
            EmbedBuilder builder = new EmbedBuilder();
            builder.setColor(Color.WHITE);
            builder.setTitle("Full dump");
            full.getRaw().forEach((key, val) -> {
                if (!(val instanceof Bundle))
                    builder.addField(translate.getOrDefault(key, "Probably a command"), String.valueOf(val), true);
                else {
                    event.getJDA().getGuilds().forEach(g -> {
                        Bundle guildBundle = full.getSubBundle("GUILD_" + g.getIdLong());
                        StringBuilder content = new StringBuilder();
                        guildBundle.getRaw().forEach((k, v) -> content.append(translate.get(k) + ": " + v + "\n"));
                        builder.addField("GUILD_" + g.getIdLong(), content.toString(), false);

                    });
                }
            });

            event.getChannel().sendMessage(builder.build()).queue();
        }

        private void yo(CommandEvent event) {
            Message targetMessage = event.getChannel().sendMessage(Templates.SIMPLE_DIALOG.format("Vous confirmez jean-pierre ?").build()).complete();

            DialogBuilder dialog = new DialogBuilder()
                    .targetUser(event.getAuthor())
                    .useCustomEmote("\uD83E\uDD54", "\uD83C\uDF35")
                    .useTimeout(10000);
            Boolean result = dialog.bindAndRetrieve(targetMessage);
            event.fastReply(result == null ? "timeout" : result.toString());

            targetMessage = event.getAuthor().openPrivateChannel().complete().sendMessage(Templates.SIMPLE_DIALOG.format("Vous confirmez jean-pierre ?").build()).complete();
            result = dialog.bindAndRetrieve(targetMessage);
            event.fastReply(result == null ? "timeout": result.toString());
        }

        private void hi(CommandEvent event, List<Object> args) {
            EmbedBuilder base = new EmbedBuilder()
                    .setTitle("Hi {0} !")
                    .setDescription("This channel is named \"{1}\"")
                    .setFooter("Requested by {2}", null)
                    .setColor(Color.ORANGE)
                    .setImage("http://www.keepbusy.net/pics/pic-dump-97-29.jpg")
                    .setThumbnail("https://cdn.discordapp.com/attachments/302785106802638848/339750595907026954/sign-check-icon.png")
                    .setAuthor("{0}", null, "https://cdn.discordapp.com/attachments/302785106802638848/326739524975722496/cup-512.png")
                    .setTimestamp(Instant.now())
                    .setThumbnail("https://cdn.discordapp.com/attachments/302785106802638848/317074381656424459/terminal-icon.png")
                    .addField("Message id #{3}", "Server id #{4}", true);
            event.getChannel().sendMessage(base.build()).queue();

            EmbedTemplate template = new EmbedTemplate(base.build());

            EmbedBuilder response = template.format(event.getTriggerEvent().getAuthor().getName(),
                    event.getChannel().getName(),
                    F.f("%s#%s", event.getTriggerEvent().getAuthor().getName(), event.getTriggerEvent().getAuthor().getDiscriminator()),
                    event.getMessageId(),
                    event.getTriggerEvent().getGuild().getId());

            event.getChannel().sendMessage(response.build()).queue();
        }
    }

    public static class CommandStop extends Command {

        private CommandStop() {
            super("stop",
                    Contexts.EVERYWHERE,
                    AccessLevel.CREATOR);
            description = "Shutdown the bot.";

            registerPattern(new CommandPattern(null, this::stop));
        }

        private void stop(CommandEvent event) {
            event.getChannel().sendMessage(Templates.GLOBAL_SIGNED(event.getTriggerEvent().getAuthor(), "Shutting down...").build()).complete();
            ModularBot.instance().shutdown(false);
        }
    }

    public static void print(Object o) {
        System.out.println(o);
    }
}
