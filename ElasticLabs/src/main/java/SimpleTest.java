public class SimpleTest {

    public static void main(String[] args) throws Exception {
        Object lockObject = new Object();
        lockObject.wait();
    }

}
