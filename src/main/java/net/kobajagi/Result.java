package net.kobajagi;

public record Result(Protocol protocol, int port, boolean success, Exception exception)
    {
    public Result(Protocol protocol, int port, boolean success)
        {
        this(protocol, port, success, null);
        }

    public Result(Protocol protocol, int port, Exception ex)
        {
        this(protocol, port, false, ex);
        }

    public enum Protocol
        {
            TCP, UDP, MULTICAST
        }
    }
