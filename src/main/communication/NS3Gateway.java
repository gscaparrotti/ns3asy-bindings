package communication;

import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

import org.apache.commons.lang3.tuple.Pair;

import bindings.NS3asy;

public class NS3Gateway {
	
	public static int DEFAULT_PORT = 8080;
	public static int ANY_SENDER_PORT = -1;
	
	/*
	 * external map: receiver -> map<sender, list<pair<bytes, double>>>
	 * inner map: sender -> list of pair of received bytes and receive time
	 * 
	 * each list is reachable given the receiver and the sender
	 */	
	private final Map<Endpoint, Map<Endpoint, List<Pair<Byte, Double>>>> receivedData = new HashMap<>();
	private static NS3asy.SetOnPacketReadFtn_ftn_callback callback;
	
	public NS3Gateway() {
		callback = (receiverIp, receiverPort, senderIp, senderPort, payload, length, time) -> {
			final Endpoint receiver = new Endpoint(receiverIp, receiverPort);
			final Endpoint sender = new Endpoint(senderIp, senderPort);
			final byte[] payloadAsBytes = payload.getByteArray(0, length);
			if (!receivedData.containsKey(receiver)) {
				receivedData.put(receiver, new HashMap<Endpoint, List<Pair<Byte, Double>>>());
			}
			final Map<Endpoint, List<Pair<Byte, Double>>> dataBySender = receivedData.get(receiver);
			if (!dataBySender.containsKey(sender)) {
				dataBySender.put(sender, new LinkedList<>());
			}
			final List<Pair<Byte, Double>> receivedBytesFromSender = dataBySender.get(sender);
			for (byte b : payloadAsBytes) {
				receivedBytesFromSender.add(Pair.of(b, time));
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
	
	public List<Pair<Byte, Double>> getBytesInInterval(final Endpoint receiver, final Endpoint sender,
			final int start, final int end) {
		final List<Pair<Byte, Double>> list = getReceivedBytes(receiver, sender);
		final int actualEnd = end >= 0 ? end : list.size();
		if (start >= 0 && actualEnd >= 0 && start <= list.size() && actualEnd <= list.size()) {
			return list.subList(start, actualEnd);
		}
		return new LinkedList<>();		
	}
	
	public void removeBytesInInterval(final Endpoint receiver, final Endpoint sender, final int start, final int end) {
		final List<Pair<Byte, Double>> list =  getReceivedBytes(receiver, sender);
		final int actualEnd = end >= 0 ? end : list.size();
		if (start >= 0 && actualEnd >= 0 && start <= list.size() && actualEnd <= list.size()) {
			list.subList(start, actualEnd).clear();
		}	
	}
	
	private List<Pair<Byte, Double>> getReceivedBytes(final Endpoint receiver, final Endpoint sender) {
		if (receivedData.containsKey(receiver)) {
			final Map<Endpoint, List<Pair<Byte, Double>>> senders = receivedData.get(receiver);
			if (sender.port == ANY_SENDER_PORT) {
				final List<Pair<Byte, Double>> receivedBytes = senders.entrySet().stream()
						.filter(e -> e.getKey().ip.equals(sender.ip))
						.flatMap(e -> e.getValue().stream())
						.collect(Collectors.toList());
				return receivedBytes;
			} else {
				if (senders.containsKey(sender)) {
					return senders.get(sender);
				}
			}
		}
		return new LinkedList<Pair<Byte, Double>>();
	}
	
	public static byte[] convertToByteArray(List<Pair<Byte, Double>> list) {
		final byte[] byteArray = new byte[list.size()];
		for (int i = 0; i < list.size(); i++) {
			byteArray[i] = list.get(i).getLeft();
		}
		return byteArray;
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
