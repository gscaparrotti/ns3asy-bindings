package test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.junit.Test;

import bindings.NS3asy;
import utils.NS3StreamsUtils;


public class StreamsTests {
	
	@Test
	public void oneToOneStreamTest() {
		int nodesCount = 2;
		List<String> receivedStrings = new ArrayList<>();
		
		NS3asy.INSTANCE.SetOnPacketReadFtn((receiverIp, receiverPort, senderIp, senderPort, payload, length) ->
					receivedStrings.add(new String(payload.getByteArray(0, length))));

		NS3asy.INSTANCE.SetNodesCount(nodesCount);
		NS3asy.INSTANCE.AddLink(0, 1);
		NS3asy.INSTANCE.FinalizeSimulationSetup();
		
		//we use a Date object to demonstrate the functionality of serialization with
		//something other than a string, yet simple
		long someEpoch = 1574505936837L;
		Date dateToSend = new Date(someEpoch);
		String payload = NS3StreamsUtils.serializeToString(dateToSend);
		
		for (int i = 0; i < nodesCount; i++) {
			NS3asy.INSTANCE.SchedulePacketsSending(i, 1, payload, payload.length());
		}

		NS3asy.INSTANCE.ResumeSimulation(-1);
		
		String receivedPayload = receivedStrings.stream().reduce((s1, s2) -> s1 + s2).orElse("");
		Object receivedObject = NS3StreamsUtils.deserializeFromString(receivedPayload);
		
		if (!(receivedObject instanceof Date)) {
			fail("Received object was a " + receivedObject.getClass().getName());
		} else {
			Date receivedDate = (Date) receivedObject;
			assertEquals(someEpoch, receivedDate.getTime());
		}
	}
	
	@Test
	public void serializationTest() {		
		//we use a Date object to demonstrate the functionality of serialization with
		//something other than a string, yet simple
		long someEpoch = 1574505936837L;
		Date dateToSend = new Date(someEpoch);
		String payload = NS3StreamsUtils.serializeToString(dateToSend);

		Object receivedObject = NS3StreamsUtils.deserializeFromString(payload);
	
		if (!(receivedObject instanceof Date)) {
			fail("Received object was a " + receivedObject.getClass().getName());
		} else {
			Date receivedDate = (Date) receivedObject;
			assertEquals(someEpoch, receivedDate.getTime());
		}
	}

}
