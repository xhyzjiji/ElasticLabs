package Serializer;

import com.fasterxml.jackson.core.type.TypeReference;

import java.util.Map;
import java.util.Optional;

public class JSONUtilTest {

    public static class CA {
        public boolean b1;
        public boolean b2;

        public boolean isB1() {
            return b1;
        }

        public void setB1(boolean b1) {
            this.b1 = b1;
        }

        public boolean isB2() {
            return b2;
        }

        public void setB2(boolean b2) {
            this.b2 = b2;
        }
    }

    public static class CAS {
        public CA def;
        public Map<String, CA> defs;

        public CA getDef() {
            return def;
        }

        public void setDef(CA def) {
            this.def = def;
        }

        public Map<String, CA> getDefs() {
            return defs;
        }

        public void setDefs(Map<String, CA> defs) {
            this.defs = defs;
        }
    }

    public static void main(String[] args) {
        String tc = "{\"defs\":{\"a\":{\"b1\":false,\"b2\":true}}}";
        Optional<CAS> cas = JSONUtil.jsonToSimpleBean(tc, CAS.class);
        if (cas.isPresent()) {
            System.out.println(JSONUtil.toJsonStringSilent(cas.get(), false));
        }
    }

}
