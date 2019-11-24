package test;

import static org.junit.Assert.assertEquals;

import java.util.ArrayList;
import java.util.List;

import org.junit.After;
import org.junit.Test;

import com.sun.jna.Native;
import com.sun.jna.Pointer;

import bindings.NS3asy;

public class BaseTests {
	
	private static int nodesCount = 2;
	
	@After
	public void finish() {
		//A simulation must be cleared before starting a new one, otherwise it remains in a dangling state.
		//Note that the simulation instance is the same across all the tests
		NS3asy.INSTANCE.StopSimulation();
	}
	
	@Test
	public void oneToOneTest() {
		int toSendCount = 100;
		String toSendString = "test";
		List<String> receivedStrings = new ArrayList<>(toSendCount);
		NS3asy.INSTANCE.SetOnPacketReadFtn((receiverIp, receiverPort, senderIp, senderPort, payload, length) -> 
			receivedStrings.add(new String(payload.getByteArray(0, length))));
		NS3asy.INSTANCE.SetNodesCount(nodesCount);
		NS3asy.INSTANCE.AddLink(0, 1);
		NS3asy.INSTANCE.FinalizeSimulationSetup();
		for (int i = 0; i < nodesCount; i++) {
			NS3asy.INSTANCE.SchedulePacketsSending(i, toSendCount, toSendString, toSendString.length());
		}
		NS3asy.INSTANCE.ResumeSimulation(-1);
		//each sent packet contains a @ character
		assertEquals(toSendCount, countOccurences(receivedStrings, toSendString));
	}
	
	@Test
	public void addressesTest() {
		NS3asy.INSTANCE.SetNodesCount(nodesCount);
		NS3asy.INSTANCE.AddLink(0, 1);
		NS3asy.INSTANCE.FinalizeSimulationSetup();
		for (int i = 0; i < nodesCount; i++) {
			assertEquals(i, NS3asy.INSTANCE.getIndexFromIpAddress("10.1.1." + (i + 1)));
			Pointer ipPointer = NS3asy.INSTANCE.getIpAddressFromIndex(i);
			String ip = ipPointer.getString(0);
			assertEquals("10.1.1." + (i + 1), ip);
			Native.free(Pointer.nativeValue(ipPointer));
		}
	}
	
	private int countOccurences(List<String> strings, String toFind) {
		//a string could be split between two packets, so they must become a single string
		return strings.stream().reduce((s1, s2) -> s1 + s2).orElse("").split(toFind, -1).length - 1;
	}

}
