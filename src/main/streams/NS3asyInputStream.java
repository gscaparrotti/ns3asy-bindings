package streams;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;

import org.apache.commons.lang3.tuple.Pair;

import bindings.NS3asy;
import communication.NS3Gateway;
import communication.NS3Gateway.Endpoint;

public class NS3asyInputStream extends InputStream {
	
	private boolean closed = false;
	private final NS3Gateway gateway;
	private final Endpoint sender;
	private final Endpoint receiver;
	private int position = 0;
	private double firstReceiveTime = -1;
	private double lastReceiveTime = -1;
	
	public NS3asyInputStream(final NS3Gateway gateway, final Endpoint sender, final Endpoint receiver) {
		if (NS3asy.INSTANCE.isUdp()) {
			throw new IllegalStateException("Cannot create a stream over UDP");
		}
		this.gateway = gateway;
		this.sender = sender;
		this.receiver = receiver;
	}

	@Override
	public int read() throws IOException {
		checkClosed();
		final List<Pair<Byte, Double>> data = gateway.getBytesInInterval(receiver, sender, position, position + 1);
		if (data.size() > 0) {
			position++;
			if (firstReceiveTime < 0) {
				firstReceiveTime = data.get(0).getRight();
			}
			lastReceiveTime = data.get(0).getRight();
			return data.get(0).getLeft();
		}
		return -1;
	}
	
	@Override
	public int available() throws IOException {
		checkClosed();
		return gateway.getBytesInInterval(receiver, sender, position, -1).size();
	}
	
	@Override
	public void close() throws IOException {
		gateway.removeBytesInInterval(receiver, sender, 0, position);
		position = 0;
		closed = true;
	}
	
	public double getDurationAndReset() {
		final double duration = lastReceiveTime - firstReceiveTime;
		firstReceiveTime = -1;
		lastReceiveTime = -1;
		return duration;
	}
	
	private void checkClosed() throws IOException {
		if (closed) {
			throw new IOException("The stream has been closed.");
		}
	}

}
