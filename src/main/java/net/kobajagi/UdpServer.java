package net.kobajagi;

import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.nio.charset.StandardCharsets;

import java.io.IOException;

public class UdpServer
        extends AbstractServer
    {
    public UdpServer(InetAddress address, int port)
        {
        super(address, port);
        }

    public void run()
        {
        byte[] buf = new byte[256];
        System.out.printf("%s binding to %s:%d%n", getClass().getSimpleName(), getAddress(), getPort());
        try (DatagramSocket socket = new DatagramSocket(getPort(), getAddress()))
            {
            System.out.printf("%s successfully listening on %s:%d%n", getClass().getSimpleName(), getAddress(), getPort());
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
                        reply(socket, packet);
                        setSuccess(true);
                        continue;
                        }
                    //System.out.printf("Err on %s:%d received message: %s%n", getAddress(), getPort(), received);
                    }
                catch (Exception e)
                    {
                    setFailure(e);
                    System.out.printf("UDP Communicating from %s:%d failed: %s%n", getAddress(), getPort(), e);
                    }
                }
            }
        catch (Exception e)
            {
            System.err.printf("%s failure on %s:%d: %s%n", getClass().getSimpleName(), getAddress(), getPort(), e.getMessage());
            setFailure(e);
            }
        finally
            {
            System.out.printf("%s unbound from %s:%d%n", getClass().getSimpleName(), getAddress(), getPort());
            }
        }

    private void reply(DatagramSocket socket, DatagramPacket packet)
            throws IOException
        {
        InetAddress address = packet.getAddress();
        int port = packet.getPort();
        byte[] buf = "OK".getBytes(StandardCharsets.UTF_8);
        packet = new DatagramPacket(buf, buf.length, address, port);
        socket.send(packet);
        }
    }

