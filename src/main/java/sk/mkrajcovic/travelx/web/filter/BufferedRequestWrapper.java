package sk.mkrajcovic.travelx.web.filter;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.nio.charset.Charset;

import org.springframework.util.StreamUtils;

import jakarta.servlet.ReadListener;
import jakarta.servlet.ServletInputStream;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletRequestWrapper;

/**
 * {@link HttpServletRequest} wrapper that buffers the entire request body in
 * memory, allowing it to be read multiple times.
 *
 * <p>
 * Servlet request input streams are typically single-use and cannot be read
 * again once consumed. This wrapper captures the request body during
 * construction and serves subsequent reads from an internal byte buffer.
 *
 * <p>
 * The primary use case is request logging, where the request payload must be
 * inspected before the request continues through the filter chain while still
 * remaining available to controllers, argument resolvers, and other downstream
 * components.
 *
 * <p>
 * Each invocation of {@link #getInputStream()} returns a new
 * {@link ServletInputStream} backed by the cached request body, ensuring
 * independent reads of the same content.
 *
 * <p>
 * The request body can also be accessed directly as a {@link String} using
 * {@link #getRequestBody()} or {@link #getRequestBody(Charset)}.
 */
class BufferedRequestWrapper extends HttpServletRequestWrapper {

	private final byte[] buffer;

	BufferedRequestWrapper(HttpServletRequest request) throws IOException {
		super(request);
		buffer = StreamUtils.copyToByteArray(request.getInputStream());
	}

	@Override
	public ServletInputStream getInputStream() {
		var byteArrayInputStream = new ByteArrayInputStream(buffer);

		return new ServletInputStream() {
			@Override
			public boolean isReady() {
				return true;
			}

			@Override
			public int read() throws IOException {
				return byteArrayInputStream.read();
			}

			@Override
			public boolean isFinished() {
				return byteArrayInputStream.available() == 0;
			}

			@Override
			public void setReadListener(ReadListener listener) { // intentionally empty, we do not use this
			}
		};
	}

	String getRequestBody() {
		return getRequestBody(Charset.defaultCharset());
	}

	String getRequestBody(Charset charset) {
		return new String(buffer, charset);
	}

}
