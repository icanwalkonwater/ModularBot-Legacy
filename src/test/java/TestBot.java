import com.jesus_crie.modularbot.ModularBot;
import com.jesus_crie.modularbot.ModularBuilder;
import com.jesus_crie.modularbot.command.*;
import com.jesus_crie.modularbot.config.ConfigHandler;
import com.jesus_crie.modularbot.config.SimpleConfig;
import com.jesus_crie.modularbot.config.Version;
import com.jesus_crie.modularbot.listener.CommandEvent;
import net.dv8tion.jda.core.entities.TextChannel;
import net.dv8tion.jda.core.entities.User;

import java.util.List;

import static com.jesus_crie.modularbot.utils.F.f;

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
                            Argument.USER,
                            Argument.CHANNEL,
                            Argument.STRING.getRepeatable()
                    }, this::salut)
            );
        }

        private void yo(CommandEvent event) {
            event.fastReply("Yo !");
        }

        private void salut(CommandEvent event, List<Object> args) {
            event.fastReply(f("User: %s\nChannel: %s", ((User) args.get(0)).getName(), ((TextChannel) args.get(1)).getName()));
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
            event.fastReply("Shutting down...");
            ModularBot.instance().shutdown(false);
        }
    }

    public static void print(Object o) {
        System.out.println(o);
    }
}
