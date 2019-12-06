package test;

import static org.junit.Assert.assertArrayEquals;
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
		final NS3Gateway gateway = new NS3Gateway();
		NS3asy.INSTANCE.SetNodesCount(2);
		NS3asy.INSTANCE.AddLink(0, 1);
		NS3asy.INSTANCE.FinalizeSimulationSetup();
		
		final Object toSendObject = new Date();
		final ObjectOutputStream oos = new ObjectOutputStream(new NS3OutputStream(0, true));
		oos.writeObject(toSendObject);
		oos.flush();
		oos.close();
		
		for (final Endpoint receiver : gateway.getReceivers()) {
			for (final Endpoint sender : gateway.getSenders(receiver)) {
				//flawless integration with Java idioms
				final ObjectInputStream ois = new ObjectInputStream(new NS3asyInputStream(gateway, sender, receiver));
				final Object receivedObject = ois.readObject();
				ois.close();
				assertEquals(toSendObject, receivedObject);
				//check that closing the stream actually wiped the underlying data structure
				assertArrayEquals(new byte[0], gateway.getBytesInInterval(receiver, sender, 0, -1));
			}
		}
	}

}
