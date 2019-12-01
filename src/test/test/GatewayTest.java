package test;

import static org.junit.Assert.assertEquals;

import org.junit.After;
import org.junit.Test;

import com.sun.jna.Native;
import com.sun.jna.Pointer;

import bindings.NS3asy;
import communication.NS3Gateway;
import communication.NS3Gateway.Endpoint;

public class GatewayTest {
	
	private static NS3asy.SetOnAcceptFtn_ftn_callback acceptCallback;
	
	@After
	public void finish() {
		//A simulation must be cleared before starting a new one, otherwise it remains in a dangling state.
		//Note that the simulation instance is the same across all the tests
		NS3asy.INSTANCE.StopSimulation();
	}
	
	@Test
	public void manyToManyTest() {
		final int nodesCount = 3;
		final int[] connectionsPerNode = new int[nodesCount];
		
		acceptCallback = (receiverIp, receiverPort, senderIp, senderPort) -> {
			connectionsPerNode[NS3asy.INSTANCE.getIndexFromIpAddress(receiverIp)]++;
		};

		final NS3Gateway gateway = new NS3Gateway();
		NS3asy.INSTANCE.SetOnAcceptFtn(acceptCallback);
		NS3asy.INSTANCE.SetNodesCount(nodesCount);
		NS3asy.INSTANCE.AddLink(0, 1);
		NS3asy.INSTANCE.AddLink(0, 2);
		NS3asy.INSTANCE.AddLink(1, 0);
		NS3asy.INSTANCE.AddLink(2, 0);
		NS3asy.INSTANCE.FinalizeSimulationSetup();
		
		final String toSendString = "test";
		final Pointer toSendPointer = new Pointer(Native.malloc(toSendString.length()));
		toSendPointer.setString(0, toSendString, "ASCII");
		//a node sends the packet to all the nodes it has been connected to
		for (int i = 0; i < nodesCount; i++) {
			NS3asy.INSTANCE.SchedulePacketsSending(i, 1, toSendPointer, toSendString.length());
		}
		NS3asy.INSTANCE.ResumeSimulation(-1);
		Native.free(Pointer.nativeValue(toSendPointer));
		
		for (int i = 0; i < nodesCount; i++) {
			final Endpoint endpoint = new Endpoint(NS3asy.INSTANCE.getIpAddressFromIndex(i).getString(0), 
					NS3Gateway.DEFAULT_PORT);
			final byte[] receivedBytes = gateway.getBytes(endpoint);
			String expected = "";
			for (int k = 0; k < connectionsPerNode[i]; k++) {
				expected += toSendString;
			}
			assertEquals(expected, new String(receivedBytes));
		}
	}

}
