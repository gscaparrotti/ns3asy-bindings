package com.github.gscaparrotti.ns3asybindings.test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.Date;
import java.util.Map;

import org.junit.After;
import org.junit.Test;

import com.github.gscaparrotti.ns3asybindings.bindings.NS3asy;
import com.github.gscaparrotti.ns3asybindings.communication.NS3Gateway;
import com.github.gscaparrotti.ns3asybindings.communication.NS3Gateway.Endpoint;
import com.github.gscaparrotti.ns3asybindings.streams.NS3OutputStream;
import com.github.gscaparrotti.ns3asybindings.streams.NS3asyInputStream;

public class StreamsTest {

	@After
	public void finish() {
		NS3asy.INSTANCE.StopSimulation();
	}
	
	@Test
	public void basicStreamsTest() throws IOException, ClassNotFoundException {
		final int nodesCount = 2;
		final NS3Gateway gateway = new NS3Gateway();
		NS3asy.INSTANCE.SetNodesCount(nodesCount);
		NS3asy.INSTANCE.AddLink(0, 1);

		NS3asy.INSTANCE.FinalizeSimulationSetup(false, 0, 0.002, "1Mbps");
		
		final Object toSendObject = new Date();

		final NS3OutputStream ns3OutputStream = new NS3OutputStream(gateway, 0, false);
		final ObjectOutputStream oos = new ObjectOutputStream(ns3OutputStream);
		oos.writeObject(toSendObject);
		oos.flush();
		oos.close();	
		
		final Map<String, Double> sendTimes = ns3OutputStream.getFirstSendTimesAndReset();
		
		for (final Endpoint receiver : gateway.getReceivers()) {
			for (final Endpoint sender : gateway.getSenders(receiver)) {
				//flawless integration with Java idioms
				final NS3asyInputStream ns3asyInputStream = new NS3asyInputStream(gateway, sender, receiver);
				final ObjectInputStream ois = new ObjectInputStream(ns3asyInputStream);
				final Object receivedObject = ois.readObject();
				ois.close();
				assertEquals(toSendObject, receivedObject);
				final double sendTime = sendTimes.get(receiver.getIp());
				final double receiveTime = ns3asyInputStream.getLastReceiveTime();
				assertTrue((receiveTime - sendTime) > 0);
				//check that closing the stream actually wiped the underlying data structure
				assertEquals(0, gateway.getBytesInInterval(receiver, sender, 0, -1).size());
			}
		}
	}

}
