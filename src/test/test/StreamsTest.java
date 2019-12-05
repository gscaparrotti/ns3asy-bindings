package test;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import org.junit.After;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import com.sun.jna.Native;
import com.sun.jna.Pointer;

import bindings.NS3asy;
import communication.NS3Gateway;
import communication.NS3Gateway.Endpoint;
import streams.NS3asyInputStream;

public class StreamsTest {
	
	@Rule
	public final ExpectedException exception = ExpectedException.none();
	
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
		final Pointer toSendPointer = new Pointer(Native.malloc(toSendString.length()));
		toSendPointer.setString(0, toSendString, "ASCII");
		NS3asy.INSTANCE.SchedulePacketsSending(0, 1, toSendPointer, toSendString.length());
		NS3asy.INSTANCE.ResumeSimulation(-1);
		Native.free(Pointer.nativeValue(toSendPointer));
		
		for (final Endpoint receiver : gateway.getReceivers()) {
			for (final Endpoint sender : gateway.getSenders(receiver)) {
				//flawless integration with Java idioms
				final NS3asyInputStream inputStream = new NS3asyInputStream(gateway, sender, receiver);
				final BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(inputStream));
				final String receivedString = bufferedReader.readLine();
				bufferedReader.close();
				assertEquals(toSendString, receivedString);
				//check that closing the stream actually wiped the underlying data structure
				assertArrayEquals(new byte[0], gateway.getBytesInInterval(receiver, sender, 0, -1));
				//check that reading a closed stream throws an exception
				exception.expect(IOException.class);
				inputStream.read();
			}
		}
	}

}
