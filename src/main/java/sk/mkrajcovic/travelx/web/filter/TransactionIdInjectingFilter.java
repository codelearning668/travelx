package sk.mkrajcovic.travelx.web.filter;

import java.io.IOException;
import java.util.UUID;

import org.slf4j.MDC;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import jakarta.servlet.Filter;
import jakarta.servlet.FilterChain;
import jakarta.servlet.FilterConfig;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.http.HttpServletResponse;

@Component
@Order(Ordered.HIGHEST_PRECEDENCE)
class TransactionIdInjectingFilter implements Filter {

	private static final String TRANSACTION_ID = "TransactionId";

	@Override
	public void init(FilterConfig filterConfig) throws ServletException {
		// we do not want to do anything here
	}

	@Override
	public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException {
		String tid = getGuid();
		request.setAttribute(TRANSACTION_ID, tid);

		// to avoid writing this header after flushing output stream
		if (response instanceof HttpServletResponse httpServletResponse) {
			httpServletResponse.addHeader("X-TransactionId", tid);
		} else {
			throw new IllegalStateException("Unable to handle servlet response with class " + response.getClass());
		}

		try {
			MDC.put(TRANSACTION_ID, tid);
			chain.doFilter(request, response);
		} finally {
			MDC.remove(TRANSACTION_ID);
		}
	}

	private String getGuid() {
		return UUID.randomUUID().toString().replace("-", "");
	}

	@Override
	public void destroy() {
		/* intentionally empty */
	}
}
