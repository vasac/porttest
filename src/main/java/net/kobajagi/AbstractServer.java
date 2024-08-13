package net.kobajagi;

import java.net.InetAddress;

import java.util.Objects;

public abstract class AbstractServer
        implements Server
    {
    private final InetAddress address;

    private final int port;

    private volatile Boolean seenTraffic;

    private volatile boolean success;

    private Exception failure;

    protected AbstractServer(InetAddress address, int port)
        {
        this.address = address;
        this.port = port;
        }

    public InetAddress getAddress()
        {
        return address;
        }

    public int getPort()
        {
        return port;
        }

    public Boolean getSeenTraffic()
        {
        return seenTraffic;
        }

    public boolean isSuccess()
        {
        return success;
        }

    void setSeenTraffic(Boolean seenTraffic)
        {
        this.seenTraffic = seenTraffic;
        }

    void setSuccess(boolean success)
        {
        this.success = success;
        }

    public Exception getFailure()
        {
        return failure;
        }

    void setFailure(Exception failure)
        {
        this.failure = failure;
        }

    public String toString()
        {
        return String.format("%s <%20s:%d\treceived: %5b, success: %5b, failure: %s>",
                             getClass().getSimpleName(),
                             getAddress(),
                             getPort(),
                             getSeenTraffic(),
                             isSuccess(),
                             Objects.isNull(getFailure()) ? "" : getFailure().getMessage());
        }
    }
