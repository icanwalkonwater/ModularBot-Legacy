public class T {

    public static void main(String[] args) {
        Test a = new Test(true);
        Test b = new Test(false);

        print(a.test(1, 20));
        print(a.test(null, 20));
        print(a.test(1, 50));
        print("");
        print(b.test(1, 20));
        print(b.test(null, 20));
        print(b.test(1, 50));
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
