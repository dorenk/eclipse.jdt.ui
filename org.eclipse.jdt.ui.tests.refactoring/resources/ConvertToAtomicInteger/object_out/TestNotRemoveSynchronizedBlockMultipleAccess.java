package object_out;

import java.util.concurrent.atomic.AtomicInteger;

public class TestNotRemoveSynchronizedBlockMultipleAccess {

	AtomicInteger f = new AtomicInteger();

	void syncMultipleAccess() {
		synchronized(this) {
			// TODO The statements below are not properly synchronized.
			f.getAndIncrement();
			f.getAndIncrement();
			f.getAndIncrement();
		}
	}
}