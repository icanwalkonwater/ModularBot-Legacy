import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

public class T {

    public static void main(String[] args) {
        print("Begin");
        try {
            test();
            print("Next");
        } catch (RuntimeException ignore) {}
        print("Ended");

        Map<Integer, String> map = new HashMap<>();
        map = map.entrySet().stream().collect(
                Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue, (p, n) -> p, HashMap::new));
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
