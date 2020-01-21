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


public class SerializationTests {
	
	private static SetOnPacketReadFtn_ftn_callback callback;
	
	@After
	public void finish() {
		//A simulation must be cleared before starting a new one, otherwise it remains in a dangling state.
		//Note that the simulation instance is the same across all the tests
		NS3asy.INSTANCE.StopSimulation();
	}
	
	@Test
	public void oneToOneSerializationTest() {
		final int nodesCount = 2;
		final List<Byte> receivedBytes = new ArrayList<>();
		
		//set callback for received packets
		callback = (receiverIp, receiverPort, senderIp, senderPort, payload, length, time) -> {
			final byte[] receivedByteArray = payload.getByteArray(0, length);
			for (byte b : receivedByteArray) {
				receivedBytes.add(b);
			}
		};
		NS3asy.INSTANCE.SetOnPacketReadFtn(callback);
		
		//configure ns3asy
		NS3asy.INSTANCE.SetNodesCount(nodesCount);
		NS3asy.INSTANCE.AddLink(0, 1);
		NS3asy.INSTANCE.FinalizeSimulationSetup(false, 0, 0, "1Mbps");
		
		//we use a Date object to demonstrate the functionality of serialization with
		//something other than a string, yet simple
		final long someEpoch = 1574505936837L;
		final Date dateToSend = new Date(someEpoch);
		
		//allocate native memory on the heap and fill it with serialized object
		final byte[] byteArrayPayload = NS3StreamsUtils.serializeToByteArray(dateToSend);
		final int length = byteArrayPayload.length;
		final Pointer payload = new Pointer(Native.malloc(length));
		for (int i = 0; i < length; i++) {
			payload.setByte(i, byteArrayPayload[i]);
		}

		//send serialized object
		NS3asy.INSTANCE.SchedulePacketsSending(0, 1, payload, length);
		//check that garbage collection doesn't claim native memory which is still needed
		//since gc is non-deterministic, there is a specific test for this in JNATest
		System.gc();
		NS3asy.INSTANCE.ResumeSimulation(-1);
		
		//free the previously allocated native memory which is no longer necessary 
		//(it's been used when calling ResumeSimulation(-1) )
		Native.free(Pointer.nativeValue(payload));
		
		//deserialized received bytes into a a Java object
		final byte[] receivedPayload = new byte[receivedBytes.size()];
		for (int i = 0; i < receivedBytes.size(); i++) {
			receivedPayload[i] = receivedBytes.get(i);
		}
		final Object receivedObject = NS3StreamsUtils.deserializeFromByteArray(receivedPayload);
		
		//check that the received object is the same as the sent one
		if (!(receivedObject instanceof Date)) {
			fail("Received object was a " + receivedObject.getClass().getName());
		} else {
			final Date receivedDate = (Date) receivedObject;
			assertEquals(someEpoch, receivedDate.getTime());
			assertEquals(dateToSend, receivedDate);
		}
	}
	
	@Test
	public void offlineSerializationTest() {		
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
