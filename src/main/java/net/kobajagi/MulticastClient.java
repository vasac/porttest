package net.kobajagi;

import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.nio.charset.StandardCharsets;

import java.io.IOException;

public class MulticastClient
        implements Client
    {

    private InetAddress address;

    private int port;

    public MulticastClient(InetAddress address, int port)
        {
        this.address = address;
        this.port = port;
        }

    public Result call()
        {
        try (DatagramSocket socket = new DatagramSocket())
            {
            InetAddress group = address;
            System.out.printf("MulticastClient for %s:%d starting.%n", address, port);
            byte[] buf = Main.MESSAGE.getBytes(StandardCharsets.UTF_8);
            DatagramPacket packet = new DatagramPacket(buf, buf.length, group, port);
            socket.send(packet);
            return new Result(Result.Protocol.MULTICAST, port, true);
            }
        catch (IOException e)
            {
            System.err.printf("%s failure on %s:%d: %s%n", getClass().getSimpleName(), address, port, e.getMessage());
            return new Result(Result.Protocol.MULTICAST, port, e);
            }
        finally
            {
            System.out.printf("MulticastClient for %s:%d completed.%n", address, port);
            }
        }
    }
