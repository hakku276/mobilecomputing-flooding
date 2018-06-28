package com.team18.flooding;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

public class Topology {
    private Map<InetAddress, DataPacket> requestMappings;
    private Map<InetAddress, Long> timeOfFlights;

    private Vertex self;

    Topology(InetAddress myAddress) {
        requestMappings = new HashMap<>();
        timeOfFlights = new HashMap<>();
        self = new Vertex(myAddress, 0);
    }

    /**
     * @param packet
     */
    void notifyRequest(DataPacket packet) {
        this.requestMappings.put(packet.getSourceIp(), packet);
    }

    /**
     * @param packet
     */
    void notifyResponse(DataPacket packet) {
        //update the mappings (s)->(a)->(b)->(c)->(d)
        // in the reverse order
        String[] path = packet.getMessage().split("&");
        DataPacket requestPacket = requestMappings.get(packet.getSourceIp());
        try {
            // Reply & ip1 & ip2 is the format, so skip the first block which is just the text reply
            for (int i = path.length - 1; i > 1; i--) {
                InetAddress addr = InetAddress.getByName(path[i]);
                InetAddress connectedTo = InetAddress.getByName(path[i - 1]);
                long timeOfFlight = requestPacket.getCreationTime() - packet.getReceivedTime();
                if(timeOfFlights.containsKey(addr) && (timeOfFlight < timeOfFlights.get(addr))){
                    self.addConnection(addr, connectedTo, timeOfFlight);
                }
            }
        }catch (UnknownHostException e){
            System.err.println("Could not convert the path addresses");
            e.printStackTrace();
        }
    }

    class Vertex {
        private final InetAddress address;
        private final long timeOfFlight;
        private final List<Vertex> connectedVertices;

        Vertex(InetAddress address, long timeOfFlight) {
            connectedVertices = new LinkedList<>();
            this.address = address;
            this.timeOfFlight = timeOfFlight;
        }

        void addConnectedVertex(Vertex v) {
            for (Vertex vertex : connectedVertices) {
                if (vertex.address.equals(v.address)) {
                    //already present
                    return;
                }
            }
            connectedVertices.add(v);
        }

        void removeConnectedVertex(InetAddress address) {
            connectedVertices.removeIf(v -> v.address.equals(address));
        }

        void addConnection(InetAddress address, InetAddress connectedTo, long timeOfFlight) {
            if (this.address.equals(connectedTo)) {
                //TODO remove the previous connection
                //cleanup(address);
                this.addConnectedVertex(new Vertex(address, timeOfFlight));
            } else {
                //connected to maybe to something within
                for (Vertex vertex : connectedVertices) {
                    vertex.addConnection(address, connectedTo, timeOfFlight);
                }
            }
        }

        /**
         * Requires: start.address != address
         *
         * @param address
         * @return
         */
        boolean cleanup(InetAddress address) {
            if (this.address.equals(address)) {
                return true;
            } else {
                this.connectedVertices.removeIf(v -> v.cleanup(address));
                return false;
            }
        }
    }
}
