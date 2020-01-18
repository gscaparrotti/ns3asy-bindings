package streams;

import java.io.IOException;
import java.io.OutputStream;
import java.util.LinkedList;
import java.util.List;

import com.sun.jna.Native;
import com.sun.jna.Pointer;

import bindings.NS3asy;
import communication.NS3Gateway;
import communication.NS3Gateway.Endpoint;

public class NS3OutputStream extends OutputStream {
	
	private final NS3asy sim = NS3asy.INSTANCE;
	private final int senderIndex;
	private final boolean applyBackpressure;
	private final NS3Gateway gateway;
	private final List<Pointer> bytes = new LinkedList<>();
	private boolean initialized = false;
	
	public NS3OutputStream(final NS3Gateway gateway, final Endpoint sender, final boolean applyBackpressure) {
		this.senderIndex = sim.getIndexFromIpAddress(sender.getIp());
		this.gateway = gateway;
		this.applyBackpressure = applyBackpressure;
	}
	
	public NS3OutputStream(final NS3Gateway gateway, final int senderIndex, final boolean applyBackpressure) {
		this.senderIndex = senderIndex;
		this.gateway = gateway;
		this.applyBackpressure = applyBackpressure;
	}

	@Override
	public void write(int b) throws IOException {
		if (!initialized && !applyBackpressure) {
			final Pointer initDataPointer = new Pointer(Native.malloc(1));
			initDataPointer.setChar(0, '!');
			sim.SchedulePacketsSending(senderIndex, 1, initDataPointer, 1);
			sim.ResumeSimulation(-1);
			Native.free(Pointer.nativeValue(initDataPointer));
			gateway.getReceivers().forEach(r -> gateway.getSenders(r).stream()
					.filter(s -> sim.getIndexFromIpAddress(s.getIp()) == senderIndex)
					.forEach(s -> gateway.removeBytesInInterval(r, s, 0, 1)));
			initialized = true;			
		}
		final Pointer toSendPointer = new Pointer(Native.malloc(1));
		toSendPointer.setInt(0, b);
		sim.SchedulePacketsSending(senderIndex, 1, toSendPointer, 1);
		bytes.add(toSendPointer);
		if (applyBackpressure) {
			this.flush();
		}
	}
	
	@Override
	public void flush() throws IOException {
		sim.ResumeSimulation(-1);
		for (final Pointer p : bytes) {
			Native.free(Pointer.nativeValue(p));
		}
		bytes.clear();
	}

}
