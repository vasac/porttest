package net.kobajagi;

import com.beust.jcommander.JCommander;
import java.net.InetAddress;
import java.net.InterfaceAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.net.UnknownHostException;

import java.io.Console;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.CompletionService;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorCompletionService;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.function.BiFunction;
import java.util.stream.Collectors;

public class Main
    {
    public static final String MESSAGE = "Even in the future nothing works.";

    private int randomPort = 50799;

    private int[] udpPorts = {randomPort, 7574};

    private int[] tcpPorts = {randomPort, 7574, randomPort + 9};

    private int[] multicastPorts = {7574};

    private List<InetAddress> multicastAddresses = List.of(InetAddress.getByName("239.192.0.0"),
                                                           InetAddress.getByName("230.0.0.0"),
                                                           InetAddress.getByName("224.0.0.0"));

    List<InterfaceAddress> networkInterfaces;

    public Main() throws UnknownHostException
        {
        }

    private Map<Server, Thread> startServers(List<InetAddress> addresses, int[] ports, BiFunction<InetAddress, Integer, Server> makeServer)
        {
        List<Server> servers = new ArrayList<>();
        for (InetAddress address : addresses)
            {
            servers.addAll(Arrays.stream(ports)
                                   .mapToObj(port -> makeServer.apply(address, port))
                                   .toList());
            }
        Map<Server, Thread> serverMap = servers.stream()
                .collect(Collectors.toMap(server -> server,
                                          server ->
                                              {
                                              Thread thread = new Thread(server);
                                              thread.setDaemon(true);
                                              thread.start();
                                              return thread;
                                              }));
        return serverMap;
        }

    private List<Future<Result>> startClients(CompletionService<Result> completionService, List<InetAddress> addresses, int[] ports, BiFunction<InetAddress, Integer, Client> makeClient)
        {
        List<Future<Result>> futures = new ArrayList<>();
        for (InetAddress address : addresses)
            {
            futures.addAll(Arrays.stream(ports)
                                   .mapToObj(port -> completionService.submit(makeClient.apply(address, port)))
                                   .toList());
            }
        return futures;
        }

    private List<InterfaceAddress> getNetworkInterfaces() throws SocketException
        {
        if (networkInterfaces != null)
            {
            return networkInterfaces;
            }

        List<InterfaceAddress> interfaces = new ArrayList<>();
        Enumeration<NetworkInterface> enumerator = NetworkInterface.getNetworkInterfaces();
        while (enumerator.hasMoreElements())
            {
            NetworkInterface networkInterface = enumerator.nextElement();
            interfaces.addAll(networkInterface.getInterfaceAddresses());
            }
        networkInterfaces = new ArrayList<>(interfaces);
        return interfaces;
        }

    private List<InetAddress> getAddresses() throws SocketException
        {
        return getNetworkInterfaces().stream()
                .map(InterfaceAddress::getAddress)
                .toList();
        }

    public static void main(String[] a)
            throws SocketException, InterruptedException, UnknownHostException,
                   ExecutionException
        {
        Args args = new Args();
        JCommander jcd = JCommander.newBuilder()
                .programName("porttest")
                .addObject(args)
                .build();
        jcd.parse(a);

        if (args.help)
            {
            jcd.usage();
            return;
            }
        Main main = new Main();
        if (args.server)
            {
            new Thread(() ->
                           {
                           try
                               {
                               Map<Server, Thread> serverMap = new HashMap<>();
                               serverMap.putAll(main.startServers(main.getAddresses(), main.udpPorts, UdpServer::new));
                               serverMap.putAll(main.startServers(main.getAddresses(), main.tcpPorts, TcpServer::new));
                               serverMap.putAll(main.startServers(main.multicastAddresses, main.multicastPorts, MulticastServer::new));
                               Thread.sleep(1000);
                               Console console = System.console();
                               boolean loop = true;
                               do
                                   {
                                   String cmd = console.readLine("Enter 's' for stats, 'q' to quit:");
                                   switch (cmd.toLowerCase())
                                       {
                                       case "s":
                                           String status = serverMap.entrySet()
                                                   .stream()
                                                   .sorted((o1, o2) ->
                                                               {
                                                               Server s1 = o1.getKey();
                                                               Server s2 = o2.getKey();
                                                               return s1.getClass().getSimpleName().compareTo(s2.getClass().getSimpleName());
                                                               })
                                                   .map(entry ->
                                                            {
                                                            Server server = entry.getKey();
                                                            return server.toString();
                                                            })
                                                   .collect(Collectors.joining("\n"));
                                           console.printf("%s%n", status);
                                           break;
                                       case "q":
                                           loop = false;
                                           break;
                                       }
                                   }
                               while (loop);
                               }
                           catch (InterruptedException | SocketException e)
                               {
                               throw new RuntimeException(e);
                               }
                           }).start();
            }
        if (args.client)
            {
            if (args.server)
                {
                Thread.sleep(5000);
                }
            ExecutorService executorService = Executors.newCachedThreadPool();
            CompletionService<Result> completionService = new ExecutorCompletionService<>(executorService);
            List<Future<Result>> udpFutures = main.startClients(completionService, main.getAddresses(), main.udpPorts, UdpClient::new);
            List<Future<Result>> tcpFutures = main.startClients(completionService, main.getAddresses(), main.tcpPorts, TcpClient::new);
            List<Future<Result>> multicastFutures = main.startClients(completionService, main.multicastAddresses, main.multicastPorts, MulticastClient::new);

            Set<Future<Result>> futures = new HashSet<>();
            futures.addAll(udpFutures);
            futures.addAll(tcpFutures);
            futures.addAll(multicastFutures);
            int totalFutures = futures.size();
            int success = 0;
            while (!futures.isEmpty())
                {
                Future<Result> future = completionService.poll(10, TimeUnit.SECONDS);
                if (future == null)
                    {
                    futures.stream()
                            .filter(resultFuture -> !resultFuture.isDone())
                            .forEach(resultFuture -> resultFuture.cancel(true));
                    break;
                    }
                Result result = future.get();
                if (result.success())
                    {
                    success++;
                    }
                else
                    {
                    System.out.println(result);
                    }
                }

            System.out.printf("Connection success: %d/%d%n", success, totalFutures);
            executorService.shutdownNow();
            }
        }
    }