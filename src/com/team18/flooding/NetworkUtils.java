package com.team18.flooding;

import java.net.InetAddress;
import java.net.InterfaceAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.util.Enumeration;
import java.util.List;

class NetworkUtils {

    static NetworkInterface findWifiInterface() {
        try {
            Enumeration<NetworkInterface> interfaces = NetworkInterface.getNetworkInterfaces();
            NetworkInterface networkInterface;
            while (interfaces.hasMoreElements()) {
                networkInterface = interfaces.nextElement();
                if (networkInterface.getName().contains("wl")) {
                    return networkInterface;
                }
            }

        } catch (SocketException e) {
            System.err.println("Could not list the wifi interface");
        }
        return null;
    }

    static InetAddress findAddressForNetworkInterface(NetworkInterface networkInterface) {
        Enumeration<InetAddress> addresses = networkInterface.getInetAddresses();
        while (addresses.hasMoreElements()) {
            InetAddress address = addresses.nextElement();
            if (address.getHostAddress().contains(".")) {
                return address;
            }
        }
        return null;
    }

    static InetAddress findBroadcastAddressForNetworkInterface(NetworkInterface networkInterface, InetAddress addr) {
        List<InterfaceAddress> addresses = networkInterface.getInterfaceAddresses();
        for (InterfaceAddress interfaceAddress : addresses) {
            if (interfaceAddress.getAddress().equals(addr)) {
                return interfaceAddress.getBroadcast();
            }
        }
        return null;
    }
}
