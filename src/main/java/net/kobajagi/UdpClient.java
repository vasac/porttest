package net.kobajagi;

import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.nio.charset.StandardCharsets;

import java.io.IOException;

public class UdpClient
        implements Client
    {

    private InetAddress address;

    private int port;

    public UdpClient(InetAddress address, int port)
        {
        this.address = address;
        this.port = port;
        }

    public Result call()
        {
        try (DatagramSocket socket = new DatagramSocket())
            {
            //System.out.printf("UdpClient for %s:%d starting.%n", address, port);
            byte[] buf = Main.MESSAGE.getBytes(StandardCharsets.UTF_8);
            DatagramPacket packet = new DatagramPacket(buf, buf.length, address, port);
            socket.send(packet);
            //System.out.printf("UdpClient for %s:%d sent.%n", address, port);
            packet = new DatagramPacket(buf, buf.length);
            //System.out.printf("UdpClient for %s:%d receiving.%n", address, port);
            socket.setSoTimeout(10000);
            socket.receive(packet);
            //System.out.printf("UdpClient for %s:%d received.%n", address, port);
            String received = new String(packet.getData(), 0, packet.getLength());
            //System.out.printf("Received '%s' from %s%n", received, packet.getSocketAddress());
            if (!"OK".equals(received))
                {
                return new Result(Result.Protocol.UDP, port, false);
                }
            return new Result(Result.Protocol.UDP, port, true);
            }
        catch (IOException e)
            {
            System.err.printf("%s failure on %s:%d: %s%n", getClass().getSimpleName(), address, port, e.getMessage());
            return new Result(Result.Protocol.UDP, port, e);
            }
        finally
            {
            System.out.printf("UdpClient for %s:%d completed.%n", address, port);
            }
        }
    }
