package test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.LinkedList;
import java.util.List;

import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.Pair;
import org.junit.After;
import org.junit.Test;

import com.sun.jna.Native;
import com.sun.jna.Pointer;

import bindings.NS3asy;
import communication.NS3Gateway;
import communication.NS3Gateway.Endpoint;

public class GatewayTest {
	
	@After
	public void finish() {
		//A simulation must be cleared before starting a new one, otherwise it remains in a dangling state.
		//Note that the simulation instance is the same across all the tests
		NS3asy.INSTANCE.StopSimulation();
	}
	
	@Test
	public void manyToManyTest() {
		final List<Pair<Integer, Integer>> links = new LinkedList<>();

		final NS3Gateway gateway = new NS3Gateway();
		NS3asy.INSTANCE.SetNodesCount(3);
		links.add(new ImmutablePair<>(0, 1));
		links.add(new ImmutablePair<>(0, 2));
		links.add(new ImmutablePair<>(1, 0));
		links.add(new ImmutablePair<>(2, 0));
		for (final Pair<Integer, Integer> link : links) {
			NS3asy.INSTANCE.AddLink(link.getLeft(), link.getRight());
		}
		NS3asy.INSTANCE.FinalizeSimulationSetup();
		
		final String toSendString = "test";
		final Pointer toSendPointer = new Pointer(Native.malloc(toSendString.length()));
		toSendPointer.setString(0, toSendString, "ASCII");
		//a node sends the packet to all the nodes it has been connected to
		for (final Pair<Integer, Integer> link : links) {
			NS3asy.INSTANCE.SchedulePacketsSending(link.getLeft(), 1, toSendPointer, toSendString.length());
		}
		NS3asy.INSTANCE.ResumeSimulation(-1);
		Native.free(Pointer.nativeValue(toSendPointer));
		
		for (final Endpoint receiver : gateway.getReceivers()) {
			for (final Endpoint sender : gateway.getSenders(receiver)) {
				final byte[] receivedBytes = gateway.getBytesInInterval(receiver, sender, 0, -1);
				assertEquals(toSendString, new String(receivedBytes));
				gateway.removeBytesInInterval(receiver, sender, 0, -1);
				assertTrue(gateway.getBytesInInterval(receiver, sender, 0, -1).length == 0);
			}
		}
	}

}
