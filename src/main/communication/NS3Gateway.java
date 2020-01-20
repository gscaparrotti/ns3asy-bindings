package communication;

import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

import org.apache.commons.lang3.ArrayUtils;

import bindings.NS3asy;

public class NS3Gateway {
	
	public static int DEFAULT_PORT = 8080;
	public static int ANY_SENDER_PORT = -1;
	
	/*
	 * external map: receiver -> map<sender, list<bytes>>
	 * inner map: sender -> list of received bytes
	 * 
	 * each list is reachable given the receiver and the sender
	 */	
	private final Map<Endpoint, Map<Endpoint, List<Byte>>> receivedData = new HashMap<>();
	private static NS3asy.SetOnPacketReadFtn_ftn_callback callback;
	
	public NS3Gateway() {
		callback = (receiverIp, receiverPort, senderIp, senderPort, payload, length, time) -> {
			final Endpoint receiver = new Endpoint(receiverIp, receiverPort);
			final Endpoint sender = new Endpoint(senderIp, senderPort);
			final byte[] payloadAsBytes = payload.getByteArray(0, length);
			if (!receivedData.containsKey(receiver)) {
				receivedData.put(receiver, new HashMap<Endpoint, List<Byte>>());
			}
			final Map<Endpoint, List<Byte>> dataBySender = receivedData.get(receiver);
			if (!dataBySender.containsKey(sender)) {
				dataBySender.put(sender, new LinkedList<>());
			}
			final List<Byte> receivedBytesFromSender = dataBySender.get(sender);
			for (byte b : payloadAsBytes) {
				receivedBytesFromSender.add(b);
			}
		};
 		NS3asy.INSTANCE.SetOnPacketReadFtn(callback);
	}
	
	public Set<Endpoint> getReceivers() {
		return new HashSet<>(receivedData.keySet());
	}
	
	public Set<Endpoint> getSenders(final Endpoint receiver) {
		if (receivedData.containsKey(receiver)) {
			return new HashSet<>(receivedData.get(receiver).keySet());
		}
		return Collections.emptySet();
	}
	
	public byte[] getBytesInInterval(final Endpoint receiver, final Endpoint sender, final int start, final int end) {
		final List<Byte> list = getReceivedBytes(receiver, sender);
		final int actualEnd = end >= 0 ? end : list.size();
		if (start >= 0 && actualEnd >= 0 && start <= list.size() && actualEnd <= list.size()) {
			final List<Byte> subList = list.subList(start, actualEnd);
			return ArrayUtils.toPrimitive(subList.toArray(new Byte[0]));
		}
		return new byte[0];		
	}
	
	public void removeBytesInInterval(final Endpoint receiver, final Endpoint sender, final int start, final int end) {
		final List<Byte> list =  getReceivedBytes(receiver, sender);
		final int actualEnd = end >= 0 ? end : list.size();
		if (start >= 0 && actualEnd >= 0 && start <= list.size() && actualEnd <= list.size()) {
			list.subList(start, actualEnd).clear();
		}	
	}
	
	private List<Byte> getReceivedBytes(final Endpoint receiver, final Endpoint sender) {
		if (receivedData.containsKey(receiver)) {
			final Map<Endpoint, List<Byte>> senders = receivedData.get(receiver);
			if (sender.port == ANY_SENDER_PORT) {
				final List<Byte> receivedBytes = new LinkedList<>();
				senders.entrySet().stream()
					.filter(e -> e.getKey().ip.equals(sender.ip))
					.forEach(e -> receivedBytes.addAll(e.getValue()));
				return receivedBytes;
			} else {
				if (senders.containsKey(sender)) {
					return senders.get(sender);
				}
			}
		}
		return new LinkedList<Byte>();
	}
	
	public static class Endpoint {
		
		private final String ip;
		private final int port;
		
		public Endpoint(String ip, int port) {
			super();
			this.ip = ip;
			this.port = port;
		}
		
		public String getIp() {
			return ip;
		}

		public int getPort() {
			return port;
		}

		@Override
		public int hashCode() {
			return Objects.hash(ip, port);
		}
		
		@Override
		public boolean equals(Object obj) {
			if (obj instanceof Endpoint) {
				Endpoint e = (Endpoint) obj;
				return e.ip.equals(this.ip) && e.port == this.port;
			}
			return false;
		}
		
	}

}
