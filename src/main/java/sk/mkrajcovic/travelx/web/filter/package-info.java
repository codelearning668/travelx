/**
 * Servlet filter layer responsible for cross-cutting HTTP request concerns.
 *
 * <p>
 * This package contains infrastructure filters that operate on all incoming
 * HTTP requests, providing functionality such as request tracing, and logging.
 *
 * <p>
 * Filters in this package are executed at a very early stage of the Spring
 * filter chain (high precedence) and are independent of business logic.
 */
package sk.mkrajcovic.travelx.web.filter;