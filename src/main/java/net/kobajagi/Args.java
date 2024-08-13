package net.kobajagi;

import com.beust.jcommander.Parameter;

public class Args
    {
    @Parameter(names = "-s", description = "Server mode")
    public boolean server;

    @Parameter(names = "-c", description = "Client mode")
    public boolean client;

    @Parameter(names = {"--help", "-h"}, help = true)
    public boolean help;


    }
