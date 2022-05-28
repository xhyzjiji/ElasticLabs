package datastream;

import java.util.concurrent.atomic.AtomicInteger;
import org.apache.commons.lang3.RandomStringUtils;
import org.apache.commons.lang3.RandomUtils;
import org.apache.flink.streaming.api.datastream.DataStreamSource;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;
import org.apache.flink.streaming.api.functions.sink.SinkFunction;
import org.apache.flink.streaming.api.functions.source.SourceFunction;

public class MultiSink {

    public static class StringSource implements SourceFunction<String> {
        @Override public void run(SourceContext context) throws Exception {
            int count = 1000;
            while (count > 0) {
                context.collect(RandomStringUtils.randomAlphabetic(RandomUtils.nextInt(5, 15)));
                count--;
                Thread.sleep(200);
            }
        }

        @Override public void cancel() {
            // do nothing
        }
    }

    public static class ConsoleSink implements SinkFunction<String> {
        private static final AtomicInteger ConsoleSinkIdGenerator = new AtomicInteger(1);
        private final int id = ConsoleSinkIdGenerator.getAndIncrement();
        @Override public void invoke(String value) throws Exception {
            System.out.println(Thread.currentThread().getName() + " | console sink " + id + " receive: " + value);
        }

        @Override public void invoke(String value, Context context) throws Exception {
            System.out.println(Thread.currentThread().getName() + " | console sink " + id + " receive: " + value);
        }
    }

    public static void main(String[] args) throws Exception {
        StreamExecutionEnvironment env = StreamExecutionEnvironment.getExecutionEnvironment();

        /*env.setParallelism(1);*/
        DataStreamSource<String> dataStreamSource = env.addSource(new StringSource());
        // flink operator chain机制使得多个算子在一个线程内被执行
        dataStreamSource.disableChaining().addSink(new ConsoleSink()).name("sink1").setParallelism(1);
        dataStreamSource.disableChaining().addSink(new ConsoleSink()).name("sink2").setParallelism(1);

        env.execute();
    }

}
