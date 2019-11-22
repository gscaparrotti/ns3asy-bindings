package test;

import static org.junit.Assert.assertEquals;

import java.util.ArrayList;
import java.util.List;

import org.junit.Test;

import com.sun.jna.Pointer;

import bindings.NS3asy;

public class BaseTests {
	
	@Test
	public void oneToOneTest() {
		int nodesCount = 2;
		int toSendCount = 100;
		String toSendString = "test";
		List<String> receivedStrings = new ArrayList<>(toSendCount);
		NS3asy.INSTANCE.SetOnPacketReadFtn(new NS3asy.SetOnPacketReadFtn_ftn_callback() {			
			@Override
			public void apply(String receiverIp, int receiverPort, String senderIp, int senderPort, 
					Pointer payload, int length) {
					receivedStrings.add(new String(payload.getByteArray(0, length)));
			}
		});
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
	
	private int countOccurences(List<String> strings, String toFind) {
		//a string could be split between two packets, so they must become a single string
		return strings.stream().reduce((s1, s2) -> s1 + s2).orElse("").split(toFind, -1).length - 1;
	}

}
