package net.kobajagi;

import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;

public class TcpServer
        extends AbstractServer
    {

    public TcpServer(InetAddress address, int port)
        {
        super(address, port);
        }

    public void run()
        {
        System.out.printf("%s binding to %s:%d%n", getClass().getSimpleName(), getAddress(), getPort());
        try (ServerSocket serverSocket = new ServerSocket(getPort(), 50, getAddress()))
            {
            System.out.printf("%s successfully listening on %s:%d%n", getClass().getSimpleName(), getAddress(), getPort());
            try (Socket clientSocket = serverSocket.accept();
                 PrintWriter out = new PrintWriter(clientSocket.getOutputStream(), true);
                 BufferedReader in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream())))
                {
                while (true)
                    {
                    String msg = in.readLine();
                    setSeenTraffic(true);
                    if (Main.MESSAGE.equals(msg))
                        {
                        setSuccess(true);
                        out.println("OK");
                        }
                    }
                }
            }
        catch (IOException e)
            {
            setFailure(e);
            System.err.printf("%s failure on %s:%d: %s%n", getClass().getSimpleName(), getAddress(), getPort(), e.getMessage());
            throw new RuntimeException(e);
            }
        finally
            {
            System.out.printf("%s unbound from %s:%d%n", getClass().getSimpleName(), getAddress(), getPort());
            }
        }

    }
