import com.jesus_crie.modularbot.ModularBot;
import com.jesus_crie.modularbot.ModularBuilder;
import com.jesus_crie.modularbot.command.*;
import com.jesus_crie.modularbot.config.ConfigHandler;
import com.jesus_crie.modularbot.config.SimpleConfig;
import com.jesus_crie.modularbot.config.Version;
import com.jesus_crie.modularbot.listener.CommandEvent;
import com.jesus_crie.modularbot.log.WebhookLogger;
import com.jesus_crie.modularbot.messagedecorator.ReactionDecoratorBuilder;
import com.jesus_crie.modularbot.messagedecorator.dismissible.DialogDecorator;
import com.jesus_crie.modularbot.messagedecorator.dismissible.NotificationDecorator;
import com.jesus_crie.modularbot.template.EmbedTemplate;
import com.jesus_crie.modularbot.template.MessageTemplate;
import com.jesus_crie.modularbot.template.Templates;
import com.jesus_crie.modularbot.utils.F;
import net.dv8tion.jda.core.EmbedBuilder;
import net.dv8tion.jda.core.entities.Message;

import java.awt.*;
import java.time.Instant;
import java.util.List;

@SuppressWarnings("WeakerAccess")
public class TestBot {

    public static void main(String[] args) {
        ConfigHandler config = new SimpleConfig("./config.json", Version.of(1, 0, 0, 0), "TestModular");

        ModularBot bot = new ModularBuilder(args[0])
                .useStats()
                .useWebhooks()
                .useCustomConfigHandler(config)
                .build();
        ModularBot.getCommandManager().registerCommands(
                new CommandTest(),
                new CommandStat(),
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
                            Argument.forString("embed")
                    }, this::hi),

                    new CommandPattern(new Argument[] {
                            Argument.forString("notif")
                    }, this::testNotification),

                    new CommandPattern(new Argument[] {
                            Argument.forString("dialog")
                    }, this::testDialog)
            );
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

        private void testNotification(CommandEvent event) {
            EmbedBuilder builder = new EmbedBuilder()
                    .setTitle("Imma notification !")
                    .setDescription("Click the cross to make me disappear");

            Message notif = event.getChannel().sendMessage(builder.build()).complete();
            NotificationDecorator decorator = ReactionDecoratorBuilder.newNotification()
                    .useTimeout(10000L)
                    .bindAndBuild(notif, event.getAuthor());
        }

        private void testDialog(CommandEvent event) {
            EmbedBuilder builder = new EmbedBuilder()
                    .setTitle("Do you like potatoes ?");

            Message dialog = event.getChannel().sendMessage(builder.build()).complete();
            DialogDecorator decorator = ReactionDecoratorBuilder.newDialogBox()
                    .useTimeout(10000L)
                    .bindAndBuild(dialog, event.getAuthor());

            event.fastReply("Blocking: " + decorator.get());
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

    public static class CommandStat extends Command {

        private static final MessageTemplate template = new MessageTemplate("Thread in mightyPool: {0}",
                "Thread in shardPool: {1}",
                "Decorators: {2}");

        private CommandStat() {
            super("stat", Contexts.EVERYWHERE, AccessLevel.CREATOR);
            registerPattern(new CommandPattern(null, this::onCommand));
        }

        private void onCommand(CommandEvent event) {
            Message message = template.format(ModularBot.instance().getMightyPool().getPoolSize(),
                    event.getJDA().pool.getPoolSize(),
                    ModularBot.getDecoratorManager().size());

            event.getChannel().sendMessage(message).queue();
        }
    }

    public static void print(Object o) {
        System.out.println(o);
    }
}
