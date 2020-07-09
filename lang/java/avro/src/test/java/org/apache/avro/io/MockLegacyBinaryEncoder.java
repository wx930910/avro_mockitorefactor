package org.apache.avro.io;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.ByteBuffer;

import org.apache.avro.util.Utf8;
import org.mockito.Mockito;

public class MockLegacyBinaryEncoder {
	protected OutputStream out;

	public Encoder instance;

	private interface ByteWriter {
		void write(ByteBuffer bytes) throws IOException;
	}

	private static final class SimpleByteWriter implements ByteWriter {
		private final OutputStream out;

		public SimpleByteWriter(OutputStream out) {
			this.out = out;
		}

		@Override
		public void write(ByteBuffer bytes) throws IOException {
			encodeLong(bytes.remaining(), out);
			out.write(bytes.array(), bytes.position(), bytes.remaining());
		}
	}

	private final ByteWriter byteWriter;

	/**
	 * Create a writer that sends its output to the underlying stream
	 * <code>out</code>.
	 */
	public MockLegacyBinaryEncoder(OutputStream out) {
		this.instance = Mockito.mock(Encoder.class,
				Mockito.withSettings().verboseLogging());

		this.out = out;
		this.byteWriter = new SimpleByteWriter(out);
		try {
			Mockito.doAnswer(invocation -> {
				if (out != null) {
					out.flush();
				}
				return null;
			}).when(this.instance).flush();
			Mockito.doAnswer(invocation -> {
				boolean b = invocation.getArgument(0);
				out.write(b ? 1 : 0);
				return null;
			}).when(this.instance).writeBoolean(Mockito.anyBoolean());
			Mockito.doAnswer(invocation -> {
				int n = invocation.getArgument(0);
				encodeLong(n, out);
				return null;
			}).when(this.instance).writeInt(Mockito.anyInt());
			Mockito.doAnswer(invocation -> {
				long n = invocation.getArgument(0);
				encodeLong(n, out);
				return null;
			}).when(this.instance).writeLong(Mockito.anyLong());
			Mockito.doAnswer(invocation -> {
				float n = invocation.getArgument(0);
				encodeFloat(n, out);
				return null;
			}).when(this.instance).writeFloat(Mockito.anyFloat());
			Mockito.doAnswer(invocation -> {
				double n = invocation.getArgument(0);
				encodeDouble(n, out);
				return null;
			}).when(this.instance).writeDouble(Mockito.anyDouble());
			Mockito.doAnswer(invocation -> {
				Utf8 utf8 = invocation.getArgument(0);
				encodeString(utf8.getBytes(), 0, utf8.getByteLength());
				return null;
			}).when(this.instance).writeString(Mockito.any(Utf8.class));
			Mockito.doAnswer(invocation -> {
				String string = invocation.getArgument(0);
				byte[] bytes = Utf8.getBytesFor(string);
				encodeString(bytes, 0, bytes.length);
				return null;
			}).when(this.instance).writeString(Mockito.anyString());
			Mockito.doAnswer(invocation -> {
				ByteBuffer bytes = invocation.getArgument(0);
				byteWriter.write(bytes);
				return null;
			}).when(this.instance).writeBytes(Mockito.any(ByteBuffer.class));
			Mockito.doAnswer(invocation -> {
				byte[] bytes = invocation.getArgument(0);
				int start = invocation.getArgument(1);
				int len = invocation.getArgument(2);
				encodeLong(len, out);
				out.write(bytes, start, len);
				return null;
			}).when(this.instance).writeBytes(Mockito.any(byte[].class),
					Mockito.anyInt(), Mockito.anyInt());
			Mockito.doAnswer(invocation -> {
				byte[] bytes = invocation.getArgument(0);
				int start = invocation.getArgument(1);
				int len = invocation.getArgument(2);
				out.write(bytes, start, len);
				return null;
			}).when(this.instance).writeFixed(Mockito.any(byte[].class),
					Mockito.anyInt(), Mockito.anyInt());
			Mockito.doAnswer(invocation -> {
				encodeLong((int) invocation.getArgument(0), out);
				return null;
			}).when(this.instance).writeEnum(Mockito.anyInt());
			Mockito.doAnswer(invocation -> {
				long itemCount = invocation.getArgument(0);
				if (itemCount > 0) {
					this.instance.writeLong(itemCount);
				}
				return null;
			}).when(this.instance).setItemCount(Mockito.anyLong());
			Mockito.doAnswer(invocation -> {
				encodeLong(0, out);
				return null;
			}).when(this.instance).writeArrayEnd();
			Mockito.doAnswer(invocation -> {
				encodeLong(0, out);
				return null;
			}).when(this.instance).writeMapEnd();
			Mockito.doAnswer(invocation -> {
				encodeLong((int) invocation.getArgument(0), out);
				return null;
			}).when(this.instance).writeIndex(Mockito.anyInt());
		} catch (Exception e) {

		}
	}

	private void encodeString(byte[] bytes, int offset, int length)
			throws IOException {
		encodeLong(length, out);
		out.write(bytes, offset, length);
	}

	protected static void encodeLong(long n, OutputStream o)
			throws IOException {
		n = (n << 1) ^ (n >> 63); // move sign to low-order bit
		while ((n & ~0x7F) != 0) {
			o.write((byte) ((n & 0x7f) | 0x80));
			n >>>= 7;
		}
		o.write((byte) n);
	}

	protected static void encodeFloat(float f, OutputStream o)
			throws IOException {
		long bits = Float.floatToRawIntBits(f);
		o.write((int) (bits) & 0xFF);
		o.write((int) (bits >> 8) & 0xFF);
		o.write((int) (bits >> 16) & 0xFF);
		o.write((int) (bits >> 24) & 0xFF);
	}

	protected static void encodeDouble(double d, OutputStream o)
			throws IOException {
		long bits = Double.doubleToRawLongBits(d);
		o.write((int) (bits) & 0xFF);
		o.write((int) (bits >> 8) & 0xFF);
		o.write((int) (bits >> 16) & 0xFF);
		o.write((int) (bits >> 24) & 0xFF);
		o.write((int) (bits >> 32) & 0xFF);
		o.write((int) (bits >> 40) & 0xFF);
		o.write((int) (bits >> 48) & 0xFF);
		o.write((int) (bits >> 56) & 0xFF);
	}
}
