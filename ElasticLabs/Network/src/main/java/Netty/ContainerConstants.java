package Netty;

public class ContainerConstants {

	public static final int SO_BACKLOG = 16;
	public static final boolean SO_REUSEADDR = true;
	public static final boolean SO_KEEPALIVE = true;
	public static final boolean TCP_NODELAY = true;

	public static final int CHANNEL_IDLE_TIMEOUT = 8;  		// second
	public static final int CHANNEL_WRITE_IDLE_TIMEOUT = 60; 	// second
	public static final int CHANNEL_READ_IDLE_TIMEOUT = 60;		// second

	public static final String ATTR_CLIENTID = "clientId"; // for channel <-> session

	public enum Role {
		SERVER,
		CLIENT,
		;
	}
}
