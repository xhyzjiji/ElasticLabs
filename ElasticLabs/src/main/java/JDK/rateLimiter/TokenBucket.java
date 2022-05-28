package JDK.rateLimiter;

/**
 * 令牌桶，桶容量为n，以r速率生产token到桶内，当token多于桶容量时，丢弃多余令牌
 * 现在假设T1时刻桶拥有令牌数为m(m<=n)，T2时刻(T2>T1)请求流量为x
 * T2时刻，桶应有令牌数为：tk = Math.min(n, m+(T2-T1)*r)
 * 当x<tk，允许通行，否则不允许放流
 *
 * 关于令牌预扣的事情，允许突发流量情况下放行一部分流量（但应该有个上限），多出的流量以sleep等待填充
 */
public class TokenBucket {

    public static void main(String[] args) {

    }

}
