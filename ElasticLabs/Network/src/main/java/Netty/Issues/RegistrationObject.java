package Netty.Issues;

import java.io.Serializable;

public class RegistrationObject implements Serializable {

    private static final long serialVersionUID = -3161161058869951479L;
    private String id;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }
}
