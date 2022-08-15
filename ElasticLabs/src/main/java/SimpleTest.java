import java.util.Map;
import java.util.function.Function;

public class SimpleTest {

//    public static class CA {
//        public boolean b1;
//        public boolean b2;
//
//        public boolean isB1() {
//            return b1;
//        }
//
//        public void setB1(boolean b1) {
//            this.b1 = b1;
//        }
//
//        public boolean isB2() {
//            return b2;
//        }
//
//        public void setB2(boolean b2) {
//            this.b2 = b2;
//        }
//    }
//
//    public static class CAS {
//        public CA def;
//        public Map<String, CA> defs;
//
//        public CA getDef() {
//            return def;
//        }
//
//        public void setDef(CA def) {
//            this.def = def;
//        }
//
//        public Map<String, CA> getDefs() {
//            return defs;
//        }
//
//        public void setDefs(Map<String, CA> defs) {
//            this.defs = defs;
//        }
//    }
//
//    public static void main(String[] args) throws Exception {
////        Object lockObject = new Object();
////        lockObject.wait();
//        String tc = "{\"defs\":{\"a\":{\"b1\":false,\"b2\":true}}}";
//
//    }

    public interface MyInterface {
        void interfaceFunc();
    }
    public enum MyEnum {
        One {public void abstractFunc() {}},
        Two {public void abstractFunc() {}},
        Three {public void abstractFunc() {}};

        public abstract void abstractFunc();
    }

    public static Function func1 = new Function() {
        @Override
        public Object apply(Object o) {
            return null;
        }
    };
    public static Function func2 = new Function() {
        @Override
        public Object apply(Object o) {
            return null;
        }
    };
    public enum MyEnum2 {
        One(func1),
        Two(func1),
        Three(func2);

        public Function func;
        MyEnum2(Function func) {
            this.func = func;
        }
    }

}
