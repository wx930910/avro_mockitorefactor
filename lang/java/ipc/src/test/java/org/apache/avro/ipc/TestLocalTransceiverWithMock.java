package org.apache.avro.ipc;

import static org.junit.Assert.assertEquals;

import org.apache.avro.AvroRemoteException;
import org.apache.avro.Protocol;
import org.apache.avro.Protocol.Message;
import org.apache.avro.generic.GenericData;
import org.apache.avro.generic.GenericRecord;
import org.apache.avro.ipc.generic.GenericRequestor;
import org.apache.avro.ipc.generic.GenericResponder;
import org.apache.avro.util.Utf8;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;

public class TestLocalTransceiverWithMock {
	Protocol protocol = Protocol.parse("" + "{\"protocol\": \"Minimal\", " + "\"messages\": { \"m\": {"
			+ "   \"request\": [{\"name\": \"x\", \"type\": \"string\"}], " + "   \"response\": \"string\"} } }");

	static class TestResponder extends GenericResponder {
		public TestResponder(Protocol local) {
			super(local);
		}

		@Override
		public Object respond(Message message, Object request) throws AvroRemoteException {
			assertEquals(new Utf8("hello"), ((GenericRecord) request).get("x"));
			return new Utf8("there");
		}

	}

	@Test
	public void testSingleRpc() throws Exception {
		GenericResponder MockGenericResponder = Mockito.mock(GenericResponder.class,
				Mockito.withSettings().useConstructor(this.protocol));
		Mockito.when(MockGenericResponder.respond(Mockito.any(Message.class), Mockito.any()))
				.thenReturn(new Utf8("there"));
		ArgumentCaptor<Object> request = new Capture<>();
		Transceiver t = new LocalTransceiver(MockGenericResponder);
		GenericRecord params = new GenericData.Record(protocol.getMessages().get("m").getRequest());
		params.put("x", new Utf8("hello"));
		GenericRequestor r = new GenericRequestor(protocol, t);

		for (int x = 0; x < 5; x++) {
			assertEquals(new Utf8("there"), r.request("m", params));
			// Mockito.verify(mock)
		}

	}
}
