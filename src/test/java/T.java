import org.apache.commons.collections4.map.UnmodifiableMap;

import java.util.HashMap;
import java.util.Map;

public class T {

    public static void main(String[] args) {
        HashMap<Integer, Integer> map = new HashMap<>();
        for (int i = 0; i < 20; i++) {
            map.put(i, 42);
        }

        HashMap<Integer, Integer> map1 = new HashMap<>();
        map1.putAll(map);

        Map<Integer, Integer> unmodifiable = UnmodifiableMap.unmodifiableMap(map1);

        unmodifiable.forEach((k, v) -> print(k + ": " + v));
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
