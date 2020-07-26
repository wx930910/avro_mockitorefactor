/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *     https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.avro.ipc.stats;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import org.apache.avro.ipc.stats.Stopwatch.Ticks;
import org.junit.Test;
import org.mockito.Mockito;

public class TestStopwatch {
	@Test
	public void testNormal() {
		Ticks tick = Mockito.mock(Ticks.class);
		Mockito.when(tick.ticks()).thenReturn(10L, 30L, 70L, 150L);
		Stopwatch s = new Stopwatch(tick);
		s.start();
		assertEquals(20, s.elapsedNanos());
		s.stop();
		assertEquals(60, s.elapsedNanos());
	}

	@Test(expected = IllegalStateException.class)
	public void testNotStarted1() {
		Ticks tick = Mockito.mock(Ticks.class);
		Stopwatch s = new Stopwatch(tick);
		s.elapsedNanos();
	}

	@Test(expected = IllegalStateException.class)
	public void testNotStarted2() {
		Ticks tick = Mockito.mock(Ticks.class);
		Stopwatch s = new Stopwatch(tick);
		s.stop();
	}

	@Test(expected = IllegalStateException.class)
	public void testTwiceStarted() {
		Ticks tick = Mockito.mock(Ticks.class);
		Stopwatch s = new Stopwatch(tick);
		s.start();
		s.start();
	}

	@Test(expected = IllegalStateException.class)
	public void testTwiceStopped() {
		Ticks tick = Mockito.mock(Ticks.class);
		Stopwatch s = new Stopwatch(tick);
		s.start();
		s.stop();
		s.stop();
	}

	@Test
	public void testSystemStopwatch() {
		Stopwatch s = new Stopwatch(Stopwatch.SYSTEM_TICKS);
		s.start();
		s.stop();
		assertTrue(s.elapsedNanos() >= 0);
	}

}
