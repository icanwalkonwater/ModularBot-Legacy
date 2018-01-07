import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class T {

    public static void main(String[] args) {
        TestSub test = new TestSub();

        Logger logger = LoggerFactory.getLogger(T.class);
        logger.info("Hey");
    }

    public static void test() throws RuntimeException {
        throw new RuntimeException();
    }

    public static class Test {

        private final boolean i;

        public Test(final boolean y) {
            i = y;
            print("Yo");
            throw new RuntimeException();
        }

    }

    public static class TestSub extends Test {

        public TestSub() {
            super(true);
            print("Hey");
        }
    }

    public static void print(Object o) {
        System.out.println(o);
    }
}
