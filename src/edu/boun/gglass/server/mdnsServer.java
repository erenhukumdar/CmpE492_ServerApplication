package edu.boun.gglass.server;

import java.io.IOException;
import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Random;
import java.util.logging.ConsoleHandler;
import java.util.logging.Level;
import java.util.logging.LogManager;
import java.util.logging.Logger;

import javax.jmdns.JmDNS;
import javax.jmdns.ServiceInfo;

import edu.boun.gglass.ui.uiCallbacks;

public class mdnsServer implements Runnable{
	public final static String DEVICE_NAME = "google_glass_server";
	public final static String REMOTE_TYPE = "_http._tcp.local.";

	private uiCallbacks callbackClass;
	private JmDNS jmdns;
	private ServiceInfo pairservice;

	public mdnsServer(uiCallbacks arg){
		callbackClass=arg;
	}


	public void stop() {
		try {
			System.out.println("Closing JmDNS...");
			jmdns.unregisterService(pairservice);
			jmdns.unregisterAllServices();
			jmdns.close();
			callbackClass.onMdnsServerStop();
			System.out.println("Done!");
		} catch (IOException e) {
			System.out.println("En error occured while closing jmdns service!");
			e.printStackTrace();
		}
	}

	@Override
	public void run() {
		// Activate these lines to see log messages of JmDNS
		boolean log = false;
		if (log) {
			ConsoleHandler handler = new ConsoleHandler();
			handler.setLevel(Level.FINEST);
			for (Enumeration<String> enumerator = LogManager.getLogManager().getLoggerNames(); enumerator.hasMoreElements();) {
				String loggerName = enumerator.nextElement();
				Logger logger = Logger.getLogger(loggerName);
				logger.addHandler(handler);
				logger.setLevel(Level.FINEST);
			}
		}

		try {

			InetAddress localIp = null;
			try {
				Enumeration<NetworkInterface> interfaces = NetworkInterface.getNetworkInterfaces();
				while (interfaces.hasMoreElements()) {
					NetworkInterface iface = interfaces.nextElement();
					// filters out 127.0.0.1 and inactive interfaces
					if (iface.isLoopback() || !iface.isUp())
						continue;

					Enumeration<InetAddress> addresses = iface.getInetAddresses();
					while(addresses.hasMoreElements()) {
						InetAddress addr = addresses.nextElement();
						String ip = addr.getHostAddress();
						if (addr instanceof Inet4Address && !ip.startsWith("169.")) {
							localIp = addr;
						}
					}
				}
			} catch (SocketException e) {
				throw new RuntimeException(e);
			}

			System.out.println("Opening JmDNS...");
			jmdns = JmDNS.create(localIp);
			System.out.println("Opened JmDNS!");
			Random random = new Random();

			final HashMap<String, String> values = new HashMap<String, String>();
			values.put("DvNm", "Cagatay-Server");
			values.put("RemV", "10000");
			values.put("DvTy", "Server");
			values.put("RemN", "Remote");
			values.put("txtvers", "1");
			byte[] pair = new byte[8];
			random.nextBytes(pair);
			values.put("Pair", toHex(pair));

			System.out.println("Requesting pairing for " + DEVICE_NAME);
			pairservice = ServiceInfo.create(REMOTE_TYPE, DEVICE_NAME, 1025, 0, 0, values);
			jmdns.registerService(pairservice);
			System.out.println("\nRegistered Service as" + pairservice);
			callbackClass.onMdnsServerStart();
		} catch (IOException e) {
			callbackClass.onMdnsServerStop();
			System.out.println("En error occured while registering jmdns service!");
			e.printStackTrace();
		}
	}

	private static final char[] _nibbleToHex = { '0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'a', 'b', 'c', 'd', 'e', 'f' };

	private static String toHex(byte[] code) {
		StringBuilder result = new StringBuilder(2 * code.length);

		for (int i = 0; i < code.length; i++) {
			int b = code[i] & 0xFF;
			result.append(_nibbleToHex[b / 16]);
			result.append(_nibbleToHex[b % 16]);
		}

		return result.toString();
	}

}
