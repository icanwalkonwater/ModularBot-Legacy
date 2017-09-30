import com.jesus_crie.modularbot.ModularBot;
import com.jesus_crie.modularbot.ModularBuilder;
import com.jesus_crie.modularbot.command.*;
import com.jesus_crie.modularbot.config.ConfigHandler;
import com.jesus_crie.modularbot.config.SimpleConfig;
import com.jesus_crie.modularbot.config.Version;
import com.jesus_crie.modularbot.listener.CommandEvent;
import com.jesus_crie.modularbot.log.WebhookLogger;
import com.jesus_crie.modularbot.template.EmbedTemplate;
import com.jesus_crie.modularbot.template.Templates;
import com.jesus_crie.modularbot.utils.F;
import com.jesus_crie.modularbot.utils.dialog.DialogBuilder;
import com.jesus_crie.modularbot.utils.menu.MenuBuilder;
import com.jesus_crie.modularbot.utils.menu.ModularMenu;
import net.dv8tion.jda.core.EmbedBuilder;
import net.dv8tion.jda.core.entities.Message;

import java.awt.Color;
import java.time.Instant;
import java.util.List;
import java.util.concurrent.ExecutionException;

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

        try {
            bot.connectToDiscord();
        } catch (Exception e) {
            ModularBot.logger().error("App", e);
        }

        ModularBot.logger().registerListener(new WebhookLogger(bot.getFirstShard().getGuildById(264001800686796800L).getWebhooks().complete().get(0)));
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
            try {
                new DialogBuilder()
                        .targetUser(event.getAuthor())
                        .useTimeout(5000)
                        .useCustomEmote("\u0031\u20E3", "347030180113547285")
                        .bind(event.fastReply("Hey")).get();
            } catch (InterruptedException | ExecutionException e) {
                e.printStackTrace();
            }
        }

        private void yo(CommandEvent event) {
            EmbedBuilder builder = new EmbedBuilder();
            builder.addField("\u0031\u20E3 Choice 1", "Hey", true);

            Message target = event.getChannel().sendMessage(builder.build()).complete();
            ModularMenu menu = new MenuBuilder(target)
                    .targetUser(event.getAuthor())
                    .useTimeout(5000)
                    .deleteAfter()
                    .build()
                    .addButtonWithListener("bite", e -> event.fastReply("Choice 1 triggered !"));

            menu.run();
            ModularBot.logger().info("App", "Menu running");
            menu.get();
            ModularBot.logger().info("App", "Menu completed");
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
