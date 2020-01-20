package streams;

import java.io.IOException;
import java.io.OutputStream;
import java.util.LinkedList;
import java.util.List;

import com.sun.jna.Native;
import com.sun.jna.Pointer;

import bindings.NS3asy;
import communication.NS3Gateway.Endpoint;

public class NS3OutputStream extends OutputStream {
	
	private final static NS3asy SIM = NS3asy.INSTANCE;
	private final int senderIndex;
	private final boolean applyBackpressure;
	private final List<Pointer> bytes = new LinkedList<>();
	private boolean initialized = false;
	
	public NS3OutputStream(final int senderIndex, final boolean applyBackpressure) throws IllegalArgumentException {
		if (SIM.isUdp()) {
			throw new IllegalStateException("Cannot create a stream over UDP");
		}
		this.senderIndex = senderIndex;
		this.applyBackpressure = applyBackpressure;
	}
	
	public NS3OutputStream(final Endpoint sender, final boolean applyBackpressure) throws IllegalArgumentException {
		this(SIM.getIndexFromIpAddress(sender.getIp()), applyBackpressure);
	}

	@Override
	public void write(int b) throws IOException {
		if (!initialized) {
			//let the nodes establish the tcp connection
			SIM.ResumeSimulation(-1);
			initialized = true;			
		}
		final Pointer toSendPointer = new Pointer(Native.malloc(1));
		toSendPointer.setInt(0, b);
		SIM.SchedulePacketsSending(senderIndex, 1, toSendPointer, 1);
		bytes.add(toSendPointer);
		if (applyBackpressure) {
			this.flush();
		}
	}
	
	@Override
	public void flush() throws IOException {
		SIM.ResumeSimulation(-1);
		for (final Pointer p : bytes) {
			Native.free(Pointer.nativeValue(p));
		}
		bytes.clear();
	}

}
