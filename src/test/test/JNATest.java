package test;

import static org.junit.Assert.assertEquals;

import java.util.Date;

import org.junit.Test;

import com.sun.jna.Native;
import com.sun.jna.Pointer;

public class JNATest {

	@Test
	public void testDynamicAllocation() {
		final String string = "test";
		final Pointer pointer = new Pointer(Native.malloc(string.length()));
		pointer.setString(0, string, "ASCII");
		//allocate a sh*tload of objects in order to certainly trigger garbage collection 
		for (int i = 0; i < Integer.MAX_VALUE; i++) {
			final Date d = new Date();
			d.setTime(123L);
		}
		//also explicitly call gc
		System.gc();
		assertEquals(string, pointer.getString(0, "ASCII"));
		Native.free(Pointer.nativeValue(pointer));
	}
}
