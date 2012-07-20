package object_out;

import java.util.concurrent.atomic.AtomicInteger;

public class TestInfixExpressions3 {

	private AtomicInteger i = new AtomicInteger();
	int j;
	
	public void foo() {
		// TODO The operations below cannot be executed atomically.
		i.addAndGet(j + 12 + i.get());
	}
}
