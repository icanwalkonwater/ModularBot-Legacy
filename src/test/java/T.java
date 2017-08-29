import com.jesus_crie.modularbot.stats.bundle.Bundle;
import com.jesus_crie.modularbot.stats.bundle.BundleBuilder;

public class T {

    public static void main(String[] args) {
        Bundle b = new BundleBuilder()
                .append("KEY_1", 42)
                .append("KEY_2", new Test(false))
                .append("KEY_3", "Hey you bastard !")
                .build();
        print(b.getInteger("KEY_1"));
        print(b.getObject("KEY_2"));
        int key3 = "KEY_3".hashCode();
        print(b.getString(key3));
    }

    public static void test() throws RuntimeException {
        throw new RuntimeException();
    }

    public static class Test {

        private final boolean i;

        public Test(final boolean y) {
            i = y;
        }

        @SuppressWarnings("SimplifiableConditionalExpression")
        public boolean test(Integer i, Integer y) {
            return this.i && i == null ? false : y.equals(50);
        }
    }

    public static void print(Object o) {
        System.out.println(o);
    }
}
