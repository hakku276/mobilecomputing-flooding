package com.team18.flooding;

import com.team18.flooding.ColorTerm.Color;

import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.util.LinkedList;
import java.util.List;

class Broadcast {
    private static final int STATUS_INET_ADDR_NOT_FOUND = 2;
    private static int STATUS_WIFI_NOT_FOUND = 1;
    private static int PORT = 5000 + 18;

    public static void main(String args[]) throws Exception {
        //TODO extract local address automatically with NetworkInterface
        NetworkInterface wifiInterface = NetworkUtils.findWifiInterface();
        if (wifiInterface == null) {
            System.err.println("Could not find wifi interface");
            System.exit(STATUS_WIFI_NOT_FOUND);
        }

        InetAddress myAddress = NetworkUtils.findAddressForNetworkInterface(wifiInterface);
        InetAddress broadcastAddress = NetworkUtils.findBroadcastAddressForNetworkInterface(wifiInterface, myAddress);
        if (myAddress == null || broadcastAddress == null) {
            System.err.println("Could not find local ip address");
            System.exit(STATUS_INET_ADDR_NOT_FOUND);
        }

        ColorTerm.println(Color.BLUE, "Network Address: " + myAddress);
        ColorTerm.println(Color.BLUE, "Broadcast Address: " + broadcastAddress);


        //open the port, initiate constants
        String source_In = "";
        String source_Out = myAddress.getHostAddress();
        String msg = "";
        String sentence = "Broadcasting";
        long time_Send = 0;
        long time_Rec = 0;
        long time_Dif = 0;
        //String[] check=new String[5];
        //check[0]=source_Out;
        List<String> check = new LinkedList<>();
        check.add(source_Out);

        DatagramSocket sock = new DatagramSocket(PORT);

        byte[] buf = new byte[27];

        //Construct packets to send

        DatagramPacket packet = new DatagramPacket(buf, buf.length);

        //      Random pck_number_T= new Random();

        msg = source_Out;

        msg = msg.concat(sentence);

        buf = msg.getBytes();
        System.out.println(buf.length);


        packet.setData(buf);
        packet.setAddress(broadcastAddress);
        packet.setPort(PORT);

        sock.setBroadcast(true);
        time_Send = System.currentTimeMillis();
        System.out.println(time_Send);


        for (int i = 0; i < 10; i++) {

            sock.send(packet);
            try {
                Thread.sleep(100);
            } catch (InterruptedException ex) {
                Thread.currentThread().interrupt();
            }

        }

        //Now wait for responses
        sock.setBroadcast(false);
        int node_sign = 0;
        while (true) {

            sock.receive(packet);
            String msg1 = new String(buf);
            System.out.println("Packet Recieved: " + msg1);


            source_In = msg1.substring(0, 15);
            node_sign = Integer.parseInt(msg1.substring(12, 15));
            //System.out.println(node_sign);

            if (!check.contains(source_In)) {

                time_Rec = System.currentTimeMillis();
                System.out.println(time_Rec);

                check.add(source_In);
                System.out.print("PACKET RECEIVED FROM ");
                System.out.println(source_In);
                node_sign = Integer.parseInt(msg1.substring(12, 15));
                System.out.println(node_sign);
                time_Dif = time_Rec - time_Send - 10 * 100;
                System.out.println(time_Dif);


            }
        }
    }
}


