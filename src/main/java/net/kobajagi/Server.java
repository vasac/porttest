package net.kobajagi;

import java.net.InetAddress;

public interface Server
        extends Runnable
    {
    InetAddress getAddress();

    int getPort();

    Boolean getSeenTraffic();

    boolean isSuccess();

    Exception getFailure();
    }
