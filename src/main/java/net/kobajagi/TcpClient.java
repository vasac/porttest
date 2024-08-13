package net.kobajagi;

import java.net.InetAddress;
import java.net.Socket;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;

public class TcpClient
        implements Client
    {
    private InetAddress address;

    private int port;

    public TcpClient(InetAddress address, int port)
        {
        this.address = address;
        this.port = port;
        }

    public Result call()
        {
        try (Socket clientSocket = new Socket(address, port);
             PrintWriter out = new PrintWriter(clientSocket.getOutputStream(), true);
             BufferedReader in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream())))
            {
            out.println(Main.MESSAGE);
            String received = in.readLine();
            if (!"OK".equals(received))
                {
                return new Result(Result.Protocol.TCP, port, false);
                }
            return new Result(Result.Protocol.TCP, port, true);

            }
        catch (IOException e)
            {
            System.err.printf("%s failure on %s:%d: %s%n", getClass().getSimpleName(), address, port, e.getMessage());
            return new Result(Result.Protocol.TCP, port, e);
            }

        finally
            {
            System.out.printf("TcpClient for %s:%d completed.%n", address, port);
            }
        }
    }
