package utils;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.Base64;

/**
 * This class provides some static utilities that allows Java object to be treated
 * as strings, in order to be able to send them through ns3asy's functions
 *
 */
public class NS3StreamsUtils {
	
	public static String serializeToString(final Serializable object) {
		final ByteArrayOutputStream baos = new ByteArrayOutputStream();
		try {
			final ObjectOutputStream oos = new ObjectOutputStream(baos);
			oos.writeObject(object);
			oos.close();
			return Base64.getEncoder().encodeToString(baos.toByteArray());
		} catch (IOException e) {
			//ByteArrayOutputStream is a "fake" stream, so this should never happen
			return null;
		}
	}
	
	public static Object deserializeFromString(final String string) {
		final ByteArrayInputStream bais = new ByteArrayInputStream(Base64.getDecoder().decode(string));
		try {
			final ObjectInputStream ois = new ObjectInputStream(bais);
			final Object readObject = ois.readObject();
			ois.close();
			return readObject;
		} catch (IOException | ClassNotFoundException e) {
			return e;
		}
	}
		

}
