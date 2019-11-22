package streams;

import java.io.IOException;
import java.io.InputStream;
import java.util.LinkedList;
import java.util.List;

import com.sun.jna.Pointer;

import bindings.NS3asy;
import bindings.NS3asy.SetOnPacketReadFtn_ftn_callback;

public class NS3InputStream extends InputStream implements SetOnPacketReadFtn_ftn_callback {
	
	private List<Byte> input = new LinkedList<>();
	
	public NS3InputStream() {
		NS3asy.INSTANCE.SetOnPacketReadFtn(this);
	}

	@Override
	public void apply(String receiverIp, int receiverPort, String senderIp, int senderPort, Pointer payload,
			int payloadLength) {
		for (byte b : payload.getByteArray(0, payloadLength)) {
			input.add(b);
		}
		
	}

	@Override
	public int read() throws IOException {
		return input.size() > 0 ? input.remove(0) : -1;
	}

}
