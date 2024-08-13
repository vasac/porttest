package net.kobajagi;

import java.net.DatagramPacket;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.MulticastSocket;

import java.io.IOException;

public class MulticastServer
        extends AbstractServer
    {
    public MulticastServer(InetAddress address, int port)
        {
        super(address, port);
        }

    public void run()
        {
        byte[] buf = new byte[256];
        InetSocketAddress isa = new InetSocketAddress(getAddress(), getPort());
        MulticastSocket socket = null;
        try
            {
            socket = new MulticastSocket(isa);
            System.out.printf("%s successfully listening on %s:%d%n", getClass().getSimpleName(), getAddress(), getPort());
            socket.joinGroup(isa.getAddress());
            System.out.printf("%s successfully joined group on %s:%d%n", getClass().getSimpleName(), getAddress(), getPort());
            while (true)
                {
                DatagramPacket packet = new DatagramPacket(buf, buf.length);
                try
                    {
                    socket.receive(packet);
                    setSeenTraffic(true);
                    String received = new String(packet.getData(), 0, packet.getLength());
                    if (received.equals(Main.MESSAGE))
                        {
                        setSuccess(true);
                        }
                    }
                catch (Exception e)
                    {
                    setFailure(e);
                    System.err.printf("%s failure on %s:%d: %s%n", getClass().getSimpleName(), getAddress(), getPort(), e.getMessage());
                    }
                }
            }
        catch (IOException e)
            {
            System.err.printf("%s failure on %s:%d: %s%n", getClass().getSimpleName(), getAddress(), getPort(), e.getMessage());
            throw new RuntimeException(e);
            }
        finally
            {
            if (socket != null)
                {
                try
                    {
                    socket.leaveGroup(isa.getAddress());
                    }
                catch (IOException e)
                    {
                    }
                socket.close();
                }
            System.out.printf("%s unbound from %s:%d%n", getClass().getSimpleName(), getAddress(), getPort());
            }
        }
    }
