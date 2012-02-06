package org.apache.tapestry5.internal.services;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertTrue;

import org.apache.tapestry5.ContextAwareException;
import org.testng.annotations.Test;

@SuppressWarnings("serial")
public class DefaultRequestExceptionHandlerTest {
	private DefaultRequestExceptionHandler contextFormer = new DefaultRequestExceptionHandler(null, null, null, null, null, null,
			null, null, null, null);

	private static class MyContextAwareException extends Throwable implements ContextAwareException {
		private Object[] context;

		public MyContextAwareException(Object[] context) {
			this.context = context;
		}

		public Object[] getContext() {
			return context;
		}

	}

	@Test
	public void noContextWhenExceptionDoesntContainMessage() {
		Object[] context = contextFormer.formExceptionContext(new RuntimeException() {
		});
		assertEquals(context.length, 0);
	}

	@Test
	public void contextIsExceptionMessage() {
		Object[] context = contextFormer.formExceptionContext(new RuntimeException() {
			public String getMessage() {
				return "HelloWorld";
			}
		});
		assertEquals(context.length, 1);
		assertTrue("helloworld".equals(context[0]));
	}

	@Test
	public void contextIsExceptionType() {
		Object[] context = contextFormer.formExceptionContext(new IllegalArgumentException("Value not allowed"));
		assertEquals(context.length, 1);
		assertTrue(context[0] instanceof String);
		assertTrue("illegalargument".equals(context[0]));
	}

	@Test
	public void contextIsProvidedByContextAwareException() {
		Object[] sourceContext = new Object[] { new Integer(10), this };

		Object[] context = contextFormer.formExceptionContext(new MyContextAwareException(sourceContext) {
		});
		assertEquals(context, sourceContext);

	}
}
