package com.team18.flooding;

import java.net.DatagramPacket;
import java.net.InetAddress;
import java.net.UnknownHostException;

public class DataPacket {
    private static final String DELIMITER = ",";
    private static final String END_DELIMITER = "#";
    private int remainingHops = 10;
    private long creationTime;
    private long receivedTime;
    private InetAddress sourceIp;
    private InetAddress destinationIp;
    private String message;

    DataPacket(InetAddress sourceIp, InetAddress destinationIp, String message) {
        this.sourceIp = sourceIp;
        this.destinationIp = destinationIp;
        this.message = message;
    }

    DataPacket(String data) {
        receivedTime = System.currentTimeMillis();
        String[] parts = data.split(DELIMITER);
        if (parts.length != 5) {
            throw new IllegalArgumentException("The data could not be decoded");
        }
        //decode the input data
        remainingHops = Integer.parseInt(parts[0]);
        creationTime = Long.parseLong(parts[1]);
        try {
            sourceIp = InetAddress.getByName(parts[2]);
            destinationIp = InetAddress.getByName(parts[3]);
        } catch (UnknownHostException e) {
            throw new IllegalArgumentException("Could not parse the source and or destination ip");
        }
        message = parts[4].split("#")[0];
        message.replace("#","");
    }

    public int getRemainingHops() {
        return remainingHops;
    }

    public void setRemainingHops(int remainingHops) {
        this.remainingHops = remainingHops;
    }

    public boolean shouldForwardPacket() {
        return remainingHops > 0;
    }

    public void appendMessage(String message) {
        this.message = this.message + "&" + message;
    }

    public DatagramPacket createPacket(int PORT) {
        creationTime = System.currentTimeMillis();
        StringBuilder builder = new StringBuilder();
        builder.append(remainingHops).append(DELIMITER);
        builder.append(creationTime).append(DELIMITER);
        builder.append(sourceIp.getHostAddress()).append(DELIMITER);
        builder.append(destinationIp.getHostAddress()).append(DELIMITER);
        builder.append(message).append(END_DELIMITER);
        DatagramPacket packet = new DatagramPacket(builder.toString().getBytes(), builder.length());
        packet.setPort(PORT);
        packet.setAddress(destinationIp);
        return packet;
    }

    public DatagramPacket createForwardPacket(int PORT) {
        StringBuilder builder = new StringBuilder();
        remainingHops--;
        builder.append(remainingHops).append(DELIMITER);
        builder.append(creationTime).append(DELIMITER);
        builder.append(sourceIp.getHostAddress()).append(DELIMITER);
        builder.append(destinationIp.getHostAddress()).append(DELIMITER);
        builder.append(message).append(END_DELIMITER);
        DatagramPacket packet = new DatagramPacket(builder.toString().getBytes(), builder.length());
        packet.setPort(PORT);
        packet.setAddress(destinationIp);
        return packet;
    }

    @Override
    public String toString() {
        return "Remaining Hops: " + remainingHops + "\n" +
                "Creation Time: " + creationTime + "\n" +
                "Received Time: " + receivedTime + "\n" +
                "Source Ip: " + sourceIp.getHostAddress() + "\n" +
                "Destination Ip: " + destinationIp.getHostAddress() + "\n" +
                "Message: " + message;
    }

    public InetAddress getSourceIp() {
        return sourceIp;
    }

    public InetAddress getDestinationIp() {
        return destinationIp;
    }

    public long getCreationTime() {
        return creationTime;
    }

    public long getReceivedTime() {
        return receivedTime;
    }

    public String getMessage() {
        return message;
    }
}
