package test;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;

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
	public void basicInputStreamTest() throws IOException {
		final NS3Gateway gateway = new NS3Gateway();
		NS3asy.INSTANCE.SetNodesCount(2);
		NS3asy.INSTANCE.AddLink(0, 1);
		NS3asy.INSTANCE.FinalizeSimulationSetup();
		
		final String toSendString = "test";
		
		final BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(new NS3OutputStream(0, true)));
		writer.write(toSendString);
		writer.flush();
		writer.close();
		
		for (final Endpoint receiver : gateway.getReceivers()) {
			for (final Endpoint sender : gateway.getSenders(receiver)) {
				//flawless integration with Java idioms
				final BufferedReader reader = 
						new BufferedReader(new InputStreamReader(new NS3asyInputStream(gateway, sender, receiver)));
				final String receivedString = reader.readLine();
				reader.close();
				assertEquals(toSendString, receivedString);
				//check that closing the stream actually wiped the underlying data structure
				assertArrayEquals(new byte[0], gateway.getBytesInInterval(receiver, sender, 0, -1));
			}
		}
	}

}
