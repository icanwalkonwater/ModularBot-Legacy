import com.jesus_crie.modularbot.ModularBot;
import com.jesus_crie.modularbot.ModularBuilder;
import com.jesus_crie.modularbot.config.ConfigHandler;
import com.jesus_crie.modularbot.config.SimpleConfig;
import com.jesus_crie.modularbot.config.Version;

public class TestBot {

    public static void main(String[] args) {
        ConfigHandler config = new SimpleConfig("./config.json", Version.of(1, 0, 0, 0), "TestModular");
        print(config.getAppName());

        ModularBot bot = new ModularBuilder(args[0])
                .useStats()
                .useCustomConfigHandler(config)
                .build();
        try {
            bot.connectToDiscord();
        } catch (Exception e) {
            ModularBot.LOGGER().error("App", e);
        }
    }

    public static void print(Object o) {
        System.out.println(o);
    }
}
