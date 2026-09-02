package io.github.lijiajia3515.cairo.feign.error;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

import static feign.Util.checkNotNull;
import static java.util.Locale.US;
import static java.util.concurrent.TimeUnit.SECONDS;

/**
 * Decodes a {@link feign.Util#RETRY_AFTER} header into an absolute date, if possible. <br>
 * See <a href="https://tools.ietf.org/html/rfc2616#section-14.37">Retry-After format</a>
 */
public class RetryAfterDecoder {

	static final DateFormat RFC822_FORMAT =
		new SimpleDateFormat("EEE, dd MMM yyyy HH:mm:ss 'GMT'", US);
	private final DateFormat rfc822Format;

	RetryAfterDecoder() {
		this(RFC822_FORMAT);
	}

	RetryAfterDecoder(DateFormat rfc822Format) {
		this.rfc822Format = checkNotNull(rfc822Format, "rfc822Format");
	}

	protected long currentTimeMillis() {
		return System.currentTimeMillis();
	}

	/**
	 * returns a date that corresponds to the first time a request can be retried.
	 *
	 * @param retryAfter String in
	 *                   <a href="https://tools.ietf.org/html/rfc2616#section-14.37" >Retry-After format</a>
	 */
	public Date apply(String retryAfter) {
		if (retryAfter == null) {
			return null;
		}
		if (retryAfter.matches("^[0-9]+\\.?0*$")) {
			retryAfter = retryAfter.replaceAll("\\.0*$", "");
			long deltaMillis = SECONDS.toMillis(Long.parseLong(retryAfter));
			return new Date(currentTimeMillis() + deltaMillis);
		}
		synchronized (rfc822Format) {
			try {
				return rfc822Format.parse(retryAfter);
			} catch (ParseException ignored) {
				return null;
			}
		}
	}
}
