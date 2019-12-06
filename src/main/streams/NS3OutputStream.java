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
	
	private final NS3asy sim = NS3asy.INSTANCE;
	private final int senderIndex;
	private final boolean applyBackpressure;
	private final List<Pointer> bytes = new LinkedList<>();
	
	public NS3OutputStream(final Endpoint sender, final boolean appylBackpressure) {
		this.senderIndex = sim.getIndexFromIpAddress(sender.getIp());
		this.applyBackpressure = appylBackpressure;
	}
	
	public NS3OutputStream(final int senderIndex, final boolean appylBackpressure) {
		this.senderIndex = senderIndex;
		this.applyBackpressure = appylBackpressure;
	}

	@Override
	public void write(int b) throws IOException {
		final Pointer toSendPointer = new Pointer(Native.malloc(1));
		toSendPointer.setInt(0, b);
		sim.SchedulePacketsSending(senderIndex, 1, toSendPointer, 1);
		bytes.add(toSendPointer);
		if (!applyBackpressure) {
			this.flush();
		}
	}
	
	@Override
	public void flush() throws IOException {
		sim.ResumeSimulation(-1);
		for (final Pointer p : bytes) {
			Native.free(Pointer.nativeValue(p));
		}
	}

}
