import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

public class T {

    public static void main(String[] args) {
        List<List<Integer>> big = new ArrayList<>();
        List<Integer> l1 = new ArrayList<>();
        List<Integer> l2 = new ArrayList<>();

        l1.add(1);
        l1.add(5);
        l1.add(50);
        l1.add(10);
        l1.add(4);

        l2.add(3);
        l2.add(10);
        l2.add(11);
        l2.add(15);

        big.add(l1);
        big.add(l2);

        print(l1.size() + l2.size());
        print(big.stream().flatMap(Collection::stream).count());
        print(big.stream().flatMap(Collection::stream).collect(Collectors.toList()));
        print(big.stream().flatMap(Collection::stream).distinct().count());
        print(big.stream().flatMap(Collection::stream).distinct().collect(Collectors.toList()));

        print("G0123".substring(1));
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
