package com.team18.flooding;

import java.io.IOException;
import java.net.*;
import java.util.HashSet;
import java.util.Set;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.Semaphore;

class Node {
    private static final int STATUS_INET_ADDR_NOT_FOUND = 2;
    private static final int STATUS_WIFI_NOT_FOUND = 1;
    private static final long LINK_DISCOVERY_REPEAT_INTERVAL = 5000L;
    private static final int PORT = 5000 + 18;
    private static final Semaphore linkDiscoverySemaphore = new Semaphore(1);

    private InetAddress myAddress;
    private InetAddress broadcastAddress;
    private DatagramSocket socket;
    private Set<InetAddress> hosts;
    private Topology topology;

    private Timer timer;
    private Thread receiverThread;
    private byte[] receiveBuffer = new byte[1024];

    private TimerTask linkDiscovery = new TimerTask() {
        @Override
        public void run() {
            DataPacket dataPacket = new DataPacket(myAddress, broadcastAddress, "Link Discovery");
            DatagramPacket packet = dataPacket.createPacket(PORT);
            try {
                socket.send(packet);
            } catch (IOException e) {
                System.err.println("Could not write to socket");
                e.printStackTrace();
            }

            try {
                linkDiscoverySemaphore.acquire();
                //print the currently discovered hosts
                ColorTerm.println(ColorTerm.Color.BLUE, "------------------------------------------");
                ColorTerm.println(ColorTerm.Color.BLUE, "Discovered Hosts");
                ColorTerm.println(ColorTerm.Color.BLUE, "------------------------------------------");
                if (hosts.size() == 0) {
                    ColorTerm.println(ColorTerm.Color.BLUE, "No Hosts Discovered");
                }
                for (InetAddress host : hosts) {
                    ColorTerm.println(ColorTerm.Color.BLUE, host.getHostAddress());
                }
                ColorTerm.println(ColorTerm.Color.BLUE, "------------------------------------------");
                linkDiscoverySemaphore.release();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    };

    private Runnable receiver = new Runnable() {
        @Override
        public void run() {
            DatagramPacket packet = new DatagramPacket(receiveBuffer, receiveBuffer.length);
            while (true) {
                try {
                    socket.receive(packet);
//                    System.out.println("New packet received");
                    DataPacket dataPacket = new DataPacket(new String(packet.getData()));
//                    System.out.println(dataPacket.toString());

                    //check if the packet if link discovery
                    if (isPacketLinkDiscovery(dataPacket)) {
                        linkDiscoverySemaphore.acquire();
//                        System.out.println("Acquired Link Discovery packet");
                        if (!isMyPacket(dataPacket)) {
                            if (!hosts.contains(dataPacket.getSourceIp())) {
                                //new host detected, send a hello message
                                DataPacket helloPacket = new DataPacket(myAddress, dataPacket.getSourceIp(), "Hello");
                                socket.send(helloPacket.createPacket(PORT));
                                topology.notifyRequest(dataPacket);
                            }
                            hosts.add(dataPacket.getSourceIp());
                        }
                        linkDiscoverySemaphore.release();
                    } else if (isPacketHello(dataPacket)) {
                        if (dataPacket.getDestinationIp().equals(myAddress)) {
                            //TODO no need to forward
                            //received a hello message send back a hello reply
                            DataPacket replyPacket = new DataPacket(myAddress, dataPacket.getSourceIp(), "Reply");
                            socket.send(replyPacket.createPacket(PORT));
                            continue;
                        } else {
                            //do nothing ... but append my ip and then broadcast again
                            dataPacket.appendMessage(myAddress.getHostAddress());
                        }
                    } else if (isReplyPacket(dataPacket)) {
                        //TODO calculate the time of flight and update the tree
                        topology.notifyResponse(dataPacket);
                    } else {
                        System.out.println("Unknown packet");
                    }
                    //forward packet if necessary
                    if (dataPacket.shouldForwardPacket()) {
//                        System.out.println("Forwarding the packet");
                        socket.send(dataPacket.createForwardPacket(PORT));
                    }
                } catch (IOException e) {
                    System.err.println("Could not read or write from Socket");
                    e.printStackTrace();
                } catch (InterruptedException e) {
                    System.err.println("Was interrupted while waiting for the semaphore");
                    e.printStackTrace();
                    break;
                }
            }
        }
    };

    Node() {
        this.hosts = new HashSet<>();
        NetworkInterface wifiInterface = NetworkUtils.findWifiInterface();
        if (wifiInterface == null) {
            throw new IllegalStateException("Could not find wifi interface");
        }
        myAddress = NetworkUtils.findAddressForNetworkInterface(wifiInterface);
        broadcastAddress = NetworkUtils.findBroadcastAddressForNetworkInterface(wifiInterface, myAddress);
        if (myAddress == null || broadcastAddress == null) {
            throw new IllegalStateException("Could not find local ip address");
        }
        try {
            socket = new DatagramSocket(PORT);
            socket.setBroadcast(true);
        } catch (SocketException e) {
            System.err.println("Could not create socket");
            throw new IllegalStateException(e);
        }
        timer = new Timer();
        topology = new Topology(myAddress);
    }

    public static void main(String[] argv) {
        ColorTerm.println(ColorTerm.Color.RED, "Starting Flooding Controller");
        Node node = new Node();
        node.startLinkDiscovery();
        node.startReceiver();
    }

    /**
     * Checks if the packet is a link discovery packet or not
     *
     * @param packet the packet
     * @return true if it is
     */
    private boolean isPacketLinkDiscovery(DataPacket packet) {
        return packet.getMessage().charAt(0) == 'L';//if the string starts with L it is LinkDiscovery packet
    }

    private boolean isReplyPacket(DataPacket packet) {
        return packet.getMessage().charAt(0) == 'R';// if the string starts with R it is a Reply message
    }

    private boolean isPacketHello(DataPacket packet) {
        return packet.getMessage().charAt(0) == 'H'; // if the string starts with H it is a Hello message
    }

    private boolean isMyPacket(DataPacket packet) {
        return packet.getSourceIp().equals(myAddress);
    }

    public void startLinkDiscovery() {
        timer.scheduleAtFixedRate(linkDiscovery, LINK_DISCOVERY_REPEAT_INTERVAL, LINK_DISCOVERY_REPEAT_INTERVAL);
    }

    public void startReceiver() {
        receiverThread = new Thread(receiver);
        receiverThread.start();
    }

    public void stopLinkDiscovery() {
        linkDiscovery.cancel();
    }
}


