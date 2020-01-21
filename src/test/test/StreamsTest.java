package test;

import static org.junit.Assert.assertEquals;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.Date;

import org.junit.After;
import org.junit.Test;
import bindings.NS3asy;
import communication.NS3Gateway;
import communication.NS3Gateway.Endpoint;
import streams.NS3OutputStream;
import streams.NS3asyInputStream;

public class StreamsTest {

	@After
	public void finish() {
		NS3asy.INSTANCE.StopSimulation();
	}
	
	@Test
	public void basicStreamsTest() throws IOException, ClassNotFoundException {
		final int nodesCount = 3;
		final NS3Gateway gateway = new NS3Gateway();
		NS3asy.INSTANCE.SetNodesCount(nodesCount);
		NS3asy.INSTANCE.AddLink(0, 1);
		NS3asy.INSTANCE.AddLink(0, 2);
		NS3asy.INSTANCE.AddLink(1, 0);
		NS3asy.INSTANCE.FinalizeSimulationSetup(false, 0, 0.002, "1Mbps");
		
		final Object toSendObject = new Date();
		for (int i = 0; i < nodesCount; i++) {
			final ObjectOutputStream oos = new ObjectOutputStream(new NS3OutputStream(i, false));
			oos.writeObject(toSendObject);
			oos.flush();
			oos.close();
		}
		
		for (final Endpoint receiver : gateway.getReceivers()) {
			for (final Endpoint sender : gateway.getSenders(receiver)) {
				//flawless integration with Java idioms
				final ObjectInputStream ois = new ObjectInputStream(new NS3asyInputStream(gateway, sender, receiver));
				final Object receivedObject = ois.readObject();
				ois.close();
				assertEquals(toSendObject, receivedObject);
				//check that closing the stream actually wiped the underlying data structure
				assertEquals(0, gateway.getBytesInInterval(receiver, sender, 0, -1).size());
			}
		}
	}

}
