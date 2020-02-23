package com.github.gscaparrotti.ns3asybindings.streams;

import java.io.IOException;
import java.io.OutputStream;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import com.github.gscaparrotti.ns3asybindings.bindings.NS3asy;
import com.github.gscaparrotti.ns3asybindings.communication.NS3Gateway;
import com.github.gscaparrotti.ns3asybindings.communication.NS3Gateway.Endpoint;
import com.sun.jna.Native;
import com.sun.jna.Pointer;

public class NS3OutputStream extends OutputStream {
	
	private final static NS3asy SIM = NS3asy.INSTANCE;
	private final int senderIndex;
	private final boolean applyBackpressure;
	private final List<Byte> bytes = new LinkedList<>();
	private final NS3Gateway gateway;
	private boolean initialized = false;
	
	public NS3OutputStream(final NS3Gateway gateway, final int senderIndex, final boolean applyBackpressure) throws IllegalArgumentException {
		if (SIM.isUdp()) {
			throw new IllegalStateException("Cannot create a stream over UDP");
		}
		this.senderIndex = senderIndex;
		this.applyBackpressure = applyBackpressure;
		this.gateway = gateway;
	}
	
	public NS3OutputStream(final NS3Gateway gateway, final Endpoint sender, final boolean applyBackpressure) throws IllegalArgumentException {
		this(gateway, SIM.getIndexFromIpAddress(sender.getIp()), applyBackpressure);
	}

	@Override
	public void write(int b) throws IOException {
		if (!initialized) {
			//let the nodes establish the tcp connection
			SIM.ResumeSimulation(60);
			initialized = true;
		}
		bytes.add((byte) b);
		if (applyBackpressure) {
			this.flush();
		}
	}
	
	@Override
	public void flush() throws IOException {
		final Pointer toSendPointer = new Pointer(Native.malloc(bytes.size()));
		for (int i = 0; i < bytes.size(); i++) {
			toSendPointer.setByte(i, bytes.get(i));
		}
		SIM.SchedulePacketsSending(senderIndex, 1, toSendPointer, bytes.size());
		SIM.ResumeSimulation(-1);
		Native.free(Pointer.nativeValue(toSendPointer));
		bytes.clear();
	}
	
	public Map<String, Double> getFirstSendTimesAndReset() {
		final Map<String, Double> map = gateway.getFirstSendTimes();
		gateway.resetFirstSendTimes();
		return map;
	}

}
