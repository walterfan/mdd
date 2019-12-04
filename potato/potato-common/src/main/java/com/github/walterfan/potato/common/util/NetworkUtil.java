package com.github.walterfan.potato.common.util;

import java.net.Inet6Address;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.concurrent.TimeUnit;

/**
 * @Author: Walter Fan
 * modify the org.h2.util.NetUtils
 **/
public class NetworkUtil {

    private static final int CACHE_MILLIS = 1000;
    private static InetAddress cachedBindAddress;
    private static String cachedLocalAddress;
    private static long cachedLocalAddressTime;



    public static synchronized String getLocalAddress() {
        long now = System.nanoTime();
        if (cachedLocalAddress != null) {
            if (cachedLocalAddressTime + TimeUnit.MILLISECONDS.toNanos(CACHE_MILLIS) > now) {
                return cachedLocalAddress;
            }
        }

        InetAddress bind = null;

        try {
            bind = InetAddress.getLocalHost();
        } catch (UnknownHostException e) {
            return "localhost";
        }

        String address;
        if (bind == null) {
            address = "localhost";
        } else {
            address = bind.getHostAddress();
            if (bind instanceof Inet6Address) {
                if (address.indexOf('%') >= 0) {
                    address = "localhost";
                } else if (address.indexOf(':') >= 0 && !address.startsWith("[")) {
                    // adds'[' and ']' if required for
                    // Inet6Address that contain a ':'.
                    address = "[" + address + "]";
                }
            }
        }
        if (address.equals("127.0.0.1")) {
            address = "localhost";
        }
        cachedLocalAddress = address;
        cachedLocalAddressTime = now;
        return address;
    }

}
