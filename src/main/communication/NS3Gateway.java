package communication;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import org.apache.commons.lang3.ArrayUtils;

import bindings.NS3asy;

public class NS3Gateway {
	
	public static int DEFAULT_PORT = 8080;
	
	private final Map<Endpoint, List<Byte>> receivedData = new HashMap<>();
	private static NS3asy.SetOnPacketReadFtn_ftn_callback callback;
	
	public NS3Gateway() {
		callback = (receiverIp, receiverPort, senderIp, senderPort, payload, length) -> {
			final Endpoint endpoint = new Endpoint(receiverIp, receiverPort);
			final byte[] payloadAsBytes = payload.getByteArray(0, length);
			if (receivedData.containsKey(endpoint)) {
				for (byte b : payloadAsBytes) {
					receivedData.get(endpoint).add(b);
				}
			} else {
				final List<Byte> receivedBytes = new LinkedList<>();
				for (byte b : payloadAsBytes) {
					receivedBytes.add(b);
				}
				receivedData.put(endpoint, receivedBytes);
			}
		};
		NS3asy.INSTANCE.SetOnPacketReadFtn(callback);
	}
	
	public byte[] getBytes(final Endpoint endpoint) {
		if (receivedData.containsKey(endpoint)) {
			return getBytesInInterval(endpoint, 0, receivedData.get(endpoint).size());
		}
		return new byte[0];
	}
	
	public byte[] getBytesInInterval(final Endpoint endpoint, final int start, final int end) {
		final List<Byte> list = receivedData.getOrDefault(endpoint, new LinkedList<Byte>());
		if (start <= list.size() && end <= list.size()) {
			final List<Byte> subList = list.subList(start, end);
			return ArrayUtils.toPrimitive(subList.toArray(new Byte[0]));
		}
		return new byte[0];		
	}
	
	public void removeBytesInInterval(final Endpoint endpoint, final int start, final int end) {
		final List<Byte> list = receivedData.getOrDefault(endpoint, new LinkedList<Byte>());
		if (start <= list.size() && end <= list.size()) {
			list.subList(start, end).clear();
		}	
	}
	
	public static class Endpoint {
		
		private final String ip;
		private final int port;
		
		public Endpoint(String ip, int port) {
			super();
			this.ip = ip;
			this.port = port;
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
