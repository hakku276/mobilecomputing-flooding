package com.team18.flooding;

import java.net.InetAddress;
import java.net.InterfaceAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.util.Arrays;
import java.util.Collections;
import java.util.Enumeration;
import java.util.List;

class NetworkUtils {

    static NetworkInterface findWifiInterface() {
        NetworkInterface foundInterface = null;
        try {
            Enumeration<NetworkInterface> interfaces = NetworkInterface.getNetworkInterfaces();
            NetworkInterface networkInterface;
            while (interfaces.hasMoreElements()) {
                networkInterface = interfaces.nextElement();
                if (networkInterface.getDisplayName().contains("wl")) {
                    foundInterface = networkInterface;
                }
            }

        } catch (SocketException e) {
            System.err.println("Could not list the wifi interface");
        }
        return foundInterface;
    }

    static InetAddress findAddressForNetworkInterface(NetworkInterface networkInterface) {
        Enumeration<InetAddress> addresses = networkInterface.getInetAddresses();
        while (addresses.hasMoreElements()) {
            InetAddress address = addresses.nextElement();
            if (address.getHostAddress().contains("192.168.")) {
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

    static void displayInterfaceInformation(NetworkInterface netint) throws SocketException {
        System.out.printf("Display name: %s\n", netint.getDisplayName());
        System.out.printf("Name: %s\n", netint.getName());
        Enumeration<InetAddress> inetAddresses = netint.getInetAddresses();

        for (InetAddress inetAddress : Collections.list(inetAddresses)) {
            System.out.printf("InetAddress: %s\n", inetAddress);
        }

        System.out.printf("MTU: %s\n", netint.getMTU());
        System.out.printf("\n");
    }
}
