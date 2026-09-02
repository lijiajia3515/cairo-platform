package io.github.lijiajia3515.cairo.core.result.error;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.List;

public class SimpleErrorUtil {

	protected static String convertTrace(Throwable error) {
		StringWriter stackTrace = new StringWriter();
		error.printStackTrace(new PrintWriter(stackTrace));
		stackTrace.flush();
		return stackTrace.toString();
	}

	protected static List<SimpleExceptionErrorMessage> convertExceptionMessage(Throwable exception) {
		List<SimpleExceptionErrorMessage> messages = new ArrayList<>(5);
		loopExceptionMessage(messages, exception, 0);
		return messages;
	}

	protected static Throwable loopExceptionMessage(List<SimpleExceptionErrorMessage> messages, Throwable ex, int depth) {
		if (ex != null && depth < 10) {
			messages.add(new SimpleExceptionErrorMessage().setBean(ex.getClass().getName()).setMessage(ex.getMessage()));
			return loopExceptionMessage(messages, ex.getCause(), depth + 1);
		}
		return null;
	}
}
