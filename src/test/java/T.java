import com.jesus_crie.modularbot.template.EmbedTemplate;
import net.dv8tion.jda.core.EmbedBuilder;
import net.dv8tion.jda.core.entities.MessageEmbed;

public class T {

    public static void main(String[] args) {
        String format1 = "Yo {0}";
        String format2 = "Salut {1} !";
        String format3 = "U suck {0} and {1} !";

        String arg0 = "Pepe";
        String arg1 = "Michel";

        //print(MessageFormat.format(format1, arg0, arg1));
        //print(MessageFormat.format(format2, arg0, arg1));
        //print(MessageFormat.format(format3, arg0, arg1));

        EmbedBuilder builder = new EmbedBuilder()
                .setTitle("Hy {0} !")
                .setFooter("Created by {1} !", null);
        EmbedTemplate template = new EmbedTemplate(builder);

        MessageEmbed formatted = template.format("me", "an awesome person").build();
        print(formatted.getTitle());
        print(formatted.getFooter().getText());
    }

    public static class Test {

        private final boolean i;

        public Test(final boolean y) {
            i = y;
        }

        public boolean test(Integer i, Integer y) {
            return this.i && i == null ? false : y.equals(50);
        }
    }

    public static void print(Object o) {
        System.out.println(o);
    }
}
