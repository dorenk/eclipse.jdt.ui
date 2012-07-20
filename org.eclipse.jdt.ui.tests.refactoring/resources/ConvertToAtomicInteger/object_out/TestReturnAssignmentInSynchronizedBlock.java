package object_out;

import java.util.concurrent.atomic.AtomicInteger;

public class TestReturnAssignmentInSynchronizedBlock {

	public AtomicInteger i = new AtomicInteger();

	public int getI() {
		// TODO The statements in the block below are not properly synchronized.
		synchronized (this) {
			i.set(12);
			// TODO The return assignment could not be executed atomically.
			return i.get();
		}
	}
}
