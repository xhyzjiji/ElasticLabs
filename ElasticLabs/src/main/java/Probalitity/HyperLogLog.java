package Probalitity;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import org.apache.commons.lang.math.RandomUtils;

public class HyperLogLog {

    public static void main(String[] args) {
        final int loop = 20000;
        final int n = 500;
        int kmaxSum = 0;
        int leftLoop = loop;
        List<Integer> kmaxs = new ArrayList<Integer>(loop);
        while (leftLoop > 0) {
            --leftLoop;
            kmaxs.add(bernoulliTest(n));
        }
        System.out.println("expected n = " + n + ", 算术平均估计值 evaluated n = " + Math.pow(2, arithmaticMean(kmaxs)));
        System.out.println("expected n = " + n + ", 调和平均估计值 evaluated n = " + Math.pow(2, harmonicMean(kmaxs)));
    }

    private static double arithmaticMean(List<Integer> kmaxs) {
        int kmaxSum = 0;
        for (int kmax : kmaxs) {
            kmaxSum += kmax;
        }
        return (double)kmaxSum / kmaxs.size();
    }

    private static double harmonicMean(List<Integer> kmaxs) {
        double kmaxSum = 0;
        for (int kmax : kmaxs) {
            kmaxSum += Math.pow(kmax, -1);
        }
        return kmaxs.size() / kmaxSum;
    }

    public static int bernoulliTest(final int n) {
        List<Integer> counter = new ArrayList<Integer>();
        int i = n;
        while (i > 0) {
            --i;
            int count = 0;
            boolean tmp = RandomUtils.nextBoolean();
            while (tmp == false) {
                tmp = RandomUtils.nextBoolean();
                count++;
            }
            counter.add(count);
        }

        Collections.sort(counter);
        int kmax = counter.get(counter.size()-1);
        return kmax;
    }

}
