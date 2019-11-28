package test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.junit.After;
import org.junit.Test;

import com.sun.jna.Native;
import com.sun.jna.Pointer;

import bindings.NS3asy;
import bindings.NS3asy.SetOnPacketReadFtn_ftn_callback;
import utils.NS3StreamsUtils;


public class StreamsTests {
	
	private static SetOnPacketReadFtn_ftn_callback callback;
	
	@After
	public void finish() {
		//A simulation must be cleared before starting a new one, otherwise it remains in a dangling state.
		//Note that the simulation instance is the same across all the tests
		NS3asy.INSTANCE.StopSimulation();
	}
	
	@Test
	public void oneToOneStreamTest() {
		final int nodesCount = 2;
		final List<String> receivedStrings = new ArrayList<>();
		
		callback = (receiverIp, receiverPort, senderIp, senderPort, payload, length) ->
			receivedStrings.add(new String(payload.getByteArray(0, length)));
		NS3asy.INSTANCE.SetOnPacketReadFtn(callback);

		NS3asy.INSTANCE.SetNodesCount(nodesCount);
		NS3asy.INSTANCE.AddLink(0, 1);
		NS3asy.INSTANCE.FinalizeSimulationSetup();
		
		//we use a Date object to demonstrate the functionality of serialization with
		//something other than a string, yet simple
		final long someEpoch = 1574505936837L;
		final Date dateToSend = new Date(someEpoch);
		final String stringPayload = NS3StreamsUtils.serializeToString(dateToSend);
		final int length = stringPayload.length();
		final Pointer payload = new Pointer(Native.malloc(length));
		payload.setString(0, stringPayload, "ASCII");

		NS3asy.INSTANCE.SchedulePacketsSending(0, 1, payload, length);
		//check that garbage collection doesn't claim native memory
		System.gc();
		NS3asy.INSTANCE.ResumeSimulation(-1);
		
		Native.free(Pointer.nativeValue(payload));
		
		final String receivedPayload = receivedStrings.stream().reduce((s1, s2) -> s1 + s2).orElse("");
		final Object receivedObject = NS3StreamsUtils.deserializeFromString(receivedPayload);
		
		if (!(receivedObject instanceof Date)) {
			fail("Received object was a " + receivedObject.getClass().getName());
		} else {
			final Date receivedDate = (Date) receivedObject;
			assertEquals(someEpoch, receivedDate.getTime());
			assertEquals(dateToSend, receivedDate);
		}
	}
	
	@Test
	public void serializationTest() {		
		final long someEpoch = 1574505936837L;
		final Date dateToSend = new Date(someEpoch);
		final String payload = NS3StreamsUtils.serializeToString(dateToSend);
		final Object deserializedObject = NS3StreamsUtils.deserializeFromString(payload);	
		if (!(deserializedObject instanceof Date)) {
			fail("Received object was a " + deserializedObject.getClass().getName());
		} else {
			final Date receivedDate = (Date) deserializedObject;
			assertEquals(someEpoch, receivedDate.getTime());
		}
	}

}
