import Netty.Client;
import Netty.Server;

public class Main {

    public static void main(String[] args) throws Exception {
        Server server = new Server();
        server.start();

        Thread.sleep(100);

        Client client1 = new Client();
        client1.start();
        Client client2 = new Client();
        client2.start();

        Thread.sleep(100);

        // 发送包大小 大于 socket的RCVBUF，会迅速背压发送端的发送窗口，发送窗口满之后会背压/减缓/阻塞OutboundBuffer的Flush（确定下要flush的内容大于发送窗口时是不是切分了），
        // 最后导致OutboundBuffer的Entry堆积，超过发送端的写水位，触发WritablityChangd事件
        for (int i = 0; i < 100; i++) {
            if ((i & 0x03) == 0x03) {
                client1.send("是对是错也好不必说了,是怨是爱也好不须揭晓,何事更重要 比两心的需要,柔情蜜意怎么可缺少,是进是退也好有若狂潮,是痛是爱也好不须发表," +
                        "曾为你愿意 我梦想都不要,流言自此心知不会少,这段情 越是浪漫越美妙,离别最是吃不消,我最不忍看你 背向我转面,要走一刻请不必诸多眷恋,浮沉浪似人潮 哪会没有思念,讲不出再见," +
                        "你我伤心到讲不出再见,是进是退也好有若狂潮,是痛是爱也好不须发表,曾为你愿意 我梦想都不要,流言自此心知不会少,这段情 越是浪漫越美妙,离别最是吃不消,我最不忍看你 背向我转面," +
                        "要走一刻请不必诸多眷恋,浮沉浪似人潮 哪会没有思念,你我伤心到讲不出再见,我最不忍看你 背向我转面,要走一刻请不必诸多眷恋,浮沉浪似人潮 哪会没有思念,你我伤心到讲不出再见," +
                        "我最不忍看你 背向我转面,要走一刻请不必诸多眷恋,浮沉浪似人潮 哪会没有思念,你我伤心到讲不出再见 \n");
            } else {
                client1.send("是对是错也好不必说了,是怨是爱也好不须揭晓,何事更重要 比两心的需要,柔情蜜意怎么可缺少,是进是退也好有若狂潮,是痛是爱也好不须发表," +
                        "曾为你愿意 我梦想都不要,流言自此心知不会少,这段情 越是浪漫越美妙,离别最是吃不消,我最不忍看你 背向我转面,要走一刻请不必诸多眷恋,浮沉浪似人潮 哪会没有思念,讲不出再见," +
                        "你我伤心到讲不出再见,是进是退也好有若狂潮,是痛是爱也好不须发表,曾为你愿意 我梦想都不要,流言自此心知不会少,这段情 越是浪漫越美妙,离别最是吃不消,我最不忍看你 背向我转面," +
                        "要走一刻请不必诸多眷恋,浮沉浪似人潮 哪会没有思念,你我伤心到讲不出再见,我最不忍看你 背向我转面,要走一刻请不必诸多眷恋,浮沉浪似人潮 哪会没有思念,你我伤心到讲不出再见," +
                        "我最不忍看你 背向我转面,要走一刻请不必诸多眷恋,浮沉浪似人潮 哪会没有思念,你我伤心到讲不出再见");
            }
        }

        Thread.sleep(1000);

        server.sendAll("Short message test~\n");

        Thread.sleep(1000);

        server.sendAll("Bye, everyone~\n");

        Thread.sleep(10000);

        client1.stop();
        client2.stop();
        server.stop();
    }

}
