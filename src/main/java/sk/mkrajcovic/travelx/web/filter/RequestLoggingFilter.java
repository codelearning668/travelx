package sk.mkrajcovic.travelx.web.filter;

import static org.springframework.http.HttpMethod.PATCH;
import static org.springframework.http.HttpMethod.POST;
import static org.springframework.http.HttpMethod.PUT;

import java.io.IOException;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.util.AntPathMatcher;
import org.springframework.web.filter.OncePerRequestFilter;

import com.fasterxml.jackson.databind.ObjectMapper;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

/**
 * Servlet filter that logs incoming JSON request payloads for selected HTTP methods.
 *
 * <p>
 * The filter intercepts {@code POST}, {@code PUT}, and {@code PATCH} requests
 * with content type {@code application/json}, buffers the request body, and
 * logs a compact single-line representation of the payload together with the
 * request method, URI, and query string.
 *
 * <p>
 * Logging can be enabled or disabled through the
 * {@code travelx.request-logging.enabled} property. Individual endpoints may be
 * excluded from logging using Ant-style path patterns configured via
 * {@code travelx.request-logging.exclude-paths}.
 *
 * <p>
 * To avoid consuming the request input stream, requests are wrapped in
 * {@link BufferedRequestWrapper}, allowing downstream components to read the
 * body normally after it has been logged.
 *
 * <p>
 * Payloads are compacted before logging to reduce log volume. Empty payloads
 * are logged as {@code <no payload>}, while invalid or non-JSON content is
 * logged as {@code <non-json payload>}.
 *
 * <p>
 * The filter is registered with high precedence to ensure request contents are
 * captured before other application filters potentially modify the request.
 */
@ConditionalOnProperty(name = "travelx.request-logging.enabled", havingValue = "true")
@Component
@Order(Ordered.HIGHEST_PRECEDENCE - 10)
class RequestLoggingFilter extends OncePerRequestFilter {

	private static final Logger LOG = LoggerFactory.getLogger(RequestLoggingFilter.class);
	private static final List<HttpMethod> SUPPORTED_HTTP_METHODS = List.of(POST, PUT, PATCH);

	private final ObjectMapper jsonMapper;
	private final List<String> excludedPaths;
	private final AntPathMatcher pathMatcher;

	RequestLoggingFilter(ObjectMapper objectMapper, @Value("${travelx.request-logging.exclude-paths:#{null}}") List<String> pathsToExclude) {
		LOG.info("Initializing request logging for HTTP methods {}", SUPPORTED_HTTP_METHODS);
		jsonMapper = objectMapper;
		excludedPaths = initExcludedPaths(pathsToExclude);
		pathMatcher = new AntPathMatcher();
	}

	private List<String> initExcludedPaths(List<String> pathsToExclude) {
		if (pathsToExclude == null || pathsToExclude.isEmpty()) {
			LOG.warn("No exclude-paths configured. All incoming JSON payloads will be logged.");
			return List.of();
		}
		List<String> excluded = List.copyOf(pathsToExclude);
		LOG.info("Excluding logging for the following paths: {}", excluded);
		return excluded;
	}

	@Override
	protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {
		if (shouldLog(request)) {
			var requestWrapper = new BufferedRequestWrapper(request);
			logIncomingRequest(requestWrapper);
			request = requestWrapper;
		}
		filterChain.doFilter(request, response);
	}

	protected boolean shouldLog(HttpServletRequest request) {
		String requestUri = request.getRequestURI()
			.substring(request.getContextPath().length());

		return SUPPORTED_HTTP_METHODS.contains(HttpMethod.valueOf(request.getMethod()))
			&& MediaType.APPLICATION_JSON_VALUE.equals(request.getContentType())
			&& excludedPaths.stream()
				.noneMatch(antPath -> pathMatcher.match(antPath, requestUri));
	}

	private void logIncomingRequest(BufferedRequestWrapper request) {
		StringBuilder message = new StringBuilder(250)
			.append(request.getMethod()).append(": ")
			.append(request.getRequestURI());

		if (!isBlank(request.getQueryString())) {
			message.append('?').append(request.getQueryString());
		}
		message.append(", ").append(compactJsonSafely(request.getRequestBody()));
		logger.info(message);
	}

    private String compactJsonSafely(String input) {
		if (input == null || input.isBlank()) {
			return "<no payload>";
		}
		try {
			Object json = jsonMapper.readValue(input, Object.class);
			return jsonMapper.writeValueAsString(json);
		} catch (Exception e) {
			return "<non-json payload>";
		}
	}

    private boolean isBlank(String text) {
    	return text == null || text.isBlank();
    }
}
