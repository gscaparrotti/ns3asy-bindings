package streams;

import java.io.IOException;
import java.io.InputStream;

import communication.NS3Gateway;
import communication.NS3Gateway.Endpoint;

public class NS3asyInputStream extends InputStream {
	
	private boolean closed = false;
	private int position = 0;
	private final NS3Gateway gateway;
	private final Endpoint sender;
	private final Endpoint receiver;
	
	public NS3asyInputStream(final NS3Gateway gateway, final Endpoint sender, final Endpoint receiver) {
		this.gateway = gateway;
		this.sender = sender;
		this.receiver = receiver;
	}

	@Override
	public int read() throws IOException {
		checkClosed();
		final byte[] data = gateway.getBytesInInterval(receiver, sender, position, position + 1);
		if (data.length > 0) {
			position++;
			return data[0];
		}
		return -1;
	}
	
	@Override
	public int available() throws IOException {
		checkClosed();
		return gateway.getBytesInInterval(receiver, sender, position, -1).length;
	}
	
	@Override
	public void close() throws IOException {
		gateway.removeBytesInInterval(receiver, sender, 0, position);
		position = 0;
		closed = true;
	}
	
	private void checkClosed() throws IOException {
		if (closed) {
			throw new IOException("The stream has been closed.");
		}
	}

}
