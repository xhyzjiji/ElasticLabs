package BehaviorPattern;

import com.google.common.collect.Lists;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

// 中介者模式，数据通过中转单播/广播到指定的接收者，发送者不需要感知其他成员
public class MediatorPattern {

    private static final AtomicInteger RECEIVER_ID_GENERATOR = new AtomicInteger(1);

    interface Receiver {
        int getId();
        void handle(String msg);
    }

    public static class ThroughPassReceiver implements Receiver {
        private final int id = RECEIVER_ID_GENERATOR.getAndIncrement();
        private MsgMediator msgMediator;
        @Override public void handle(String msg) {
            System.out.println("receiver " + id + " handle msg: " + msg);
        }

        @Override public int getId() {
            return id;
        }
    }

    public static class LevelBoardcastReceiver implements Receiver {
        private static final String MSG_PATTERN_MODE = "(?<BroadcastIds>\\d+)(?<SingleMsg>.*)";
        private static final Pattern MSG_PATTERN = Pattern.compile(MSG_PATTERN_MODE);
        private final int id = RECEIVER_ID_GENERATOR.getAndIncrement();

        private MsgMediator msgMediator;
        public LevelBoardcastReceiver(MsgMediator msgMediator) {
            this.msgMediator = msgMediator;
        }

        @Override public void handle(String msg) {
            System.out.println("receiver " + id + " handle msg: " + msg);
            Matcher matcher = MSG_PATTERN.matcher(msg);
            if (matcher.find()) {
                String broadcastIdString = matcher.group("BroadcastIds");
                String singleMsg = matcher.group("SingleMsg");
                int broadcastId = Integer.valueOf(broadcastIdString);
                msgMediator.passMsgToGreaterThan(broadcastId, singleMsg);
            }
        }

        @Override public int getId() {
            return id;
        }
    }

    // 中介负责与客户沟通，应用层只需要与中介沟通即可，客户之间也可以通过中介相互联系
    public static class MsgMediator {
        private List<Receiver> receivers = new ArrayList<>();

        public void registerReceiver(Receiver receiver) {
            receivers.add(receiver);
        }

        public void registerReceivers(Receiver... rs) {
            receivers.addAll(Lists.newArrayList(rs));
        }

        public void passMsgTo(int id, String msg) {
            Optional<Receiver> receiverOpt = receivers.stream().filter(r -> r.getId() == id).findFirst();
            if (receiverOpt.isPresent()) {
                receiverOpt.get().handle(msg);
            }
        }

        public void passMsgToGreaterThan(int id, String msg) {
            List<Receiver> receiverCandidates = receivers.stream().filter(r -> r.getId() > id).collect(Collectors.toList());
            for (Receiver r : receiverCandidates) {
                r.handle(msg);
            }
        }
    }

    public static void main(String[] args) {
        MsgMediator msgMediator = new MsgMediator();
        ThroughPassReceiver tpr1 = new ThroughPassReceiver();
        LevelBoardcastReceiver lbr1 = new LevelBoardcastReceiver(msgMediator);
        LevelBoardcastReceiver lbr2 = new LevelBoardcastReceiver(msgMediator);
        msgMediator.registerReceivers(tpr1, lbr1, lbr2);

        msgMediator.passMsgTo(1, "Hello receiver 1");
        msgMediator.passMsgToGreaterThan(1, "Hello receiver id > 1");
        msgMediator.passMsgTo(2, "2Hello receiver id > 2");
    }

}
