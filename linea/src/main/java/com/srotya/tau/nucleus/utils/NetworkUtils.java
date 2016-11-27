/**
 * Copyright 2016 Ambud Sharma
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 * 		http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.srotya.tau.nucleus.utils;

import java.net.Inet4Address;
import java.net.InterfaceAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.util.Enumeration;

/**
 * @author ambud
 */
public class NetworkUtils {

	private NetworkUtils() {
	}
	
	public static NetworkInterface selectDefaultIPAddress(boolean loopback) throws SocketException {
		Enumeration<NetworkInterface> ifaces = NetworkInterface.getNetworkInterfaces();
		while(ifaces.hasMoreElements()) {
			NetworkInterface iface = ifaces.nextElement();
			if(loopback) {
				return iface;
			}
			if(iface.isPointToPoint() || iface.isVirtual()) {
				continue;
			}
			if(iface.isUp()) {
				return iface;
			}
		}
		return null;
	}
	
	public static Inet4Address getIPv4Address(NetworkInterface iface) {
		for (InterfaceAddress interfaceAddress : iface.getInterfaceAddresses()) {
			if(interfaceAddress.getAddress() instanceof Inet4Address) {
				return (Inet4Address) interfaceAddress.getAddress();
			}
		}
		return null;
	}
}