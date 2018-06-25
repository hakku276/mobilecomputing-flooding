package com.team18.flooding;

import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.util.ArrayList;
import java.util.Random;

public class Receiver {

    private static final int PORT = 5000 + 18; // 5000 plus team number

    public static void main(String args[]) throws Exception {
        //TODO extract local address automatically with NetworkInterface

        String source_In = "";
        String source_Out = "192.168.210.181";
        String msg = "";
        String sentence = "";
        ArrayList<String> check = new ArrayList<String>();
        check.add(source_Out);
        long time_Send = 0;
        long time_Rec = 0;
        long time_Dif = 0;


        DatagramSocket sock = new DatagramSocket(PORT);


        byte[] buf = new byte[27];


        DatagramPacket packet = new DatagramPacket(buf, buf.length);


        //Wait for order
        sock.setBroadcast(false);

        System.out.println("Start receiving");
        sock.receive(packet);

        System.out.println("Received something");
        String msg1 = new String(buf);
        source_In = msg1.substring(0, 15);
        sentence = msg1.substring(15, 27);

        System.out.print("ORDER RECEIVED FROM ");
        System.out.println(source_In);
        check.add(source_In);


        //Create the new packet
        msg = source_Out;
        msg = msg.concat(sentence);
        buf = msg.getBytes();

        packet.setData(buf);

        packet.setAddress(InetAddress.getByName("192.168.210.255"));
        packet.setPort(PORT);

        //Send the new packet
        //System.out.println(msg);
        sock.setBroadcast(true);
        time_Send = System.currentTimeMillis();
        for (int i = 0; i < 10; i++) {

            sock.send(packet);

            try {
                Thread.sleep(140);
            } catch (InterruptedException ex) {
                Thread.currentThread().interrupt();
            }

        }

        //Wait for responses
        sock.setBroadcast(false);
        String source_chk = "";
        while (true) {

            sock.receive(packet);
            String msg2 = new String(buf);
            source_In = msg2.substring(0, 15);
            sentence = msg2.substring(15, 27);
            if (!check.contains(source_In)) {
                time_Rec = System.currentTimeMillis();
                check.add(source_In);
                System.out.print("PACKET RECEIVED FROM ");
                System.out.println(source_In);
                System.out.println(sentence);
                time_Dif = time_Rec - time_Send - 10 * 140;

                System.out.println(time_Dif);


            }


        }


    }
}
