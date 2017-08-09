import com.jesus_crie.modularbot.ModularBot;
import com.jesus_crie.modularbot.ModularBuilder;

public class TestBot {

    public static void main(String[] args) {
        ModularBot bot = new ModularBuilder("token")
                .setName("TestBot")
                .useSharding()
                .build();
    }
}
