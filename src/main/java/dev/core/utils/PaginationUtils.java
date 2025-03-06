package dev.core.utils;

import org.springframework.data.domain.Page;
import org.springframework.http.HttpHeaders;
import org.springframework.web.util.UriComponentsBuilder;

/**
 * @author Nelson Tanko
 */

public final class PaginationUtils {

    private static final String HEADER_X_TOTAL_COUNT = "X-Total-Count";

    private static final LinkHeaderUtils linkHeaderUtil = new LinkHeaderUtils();

    private PaginationUtils() {
    }

    /**
     * Generate pagination headers for a Spring Data {@link Page} object.
     *
     * @param uriBuilder The URI builder.
     * @param page       The page.
     * @param <T>        The type of object.
     * @return http header.
     */
    public static <T> HttpHeaders generatePaginationHttpHeaders(UriComponentsBuilder uriBuilder, Page<T> page) {
        HttpHeaders headers = new HttpHeaders();
        headers.add(HEADER_X_TOTAL_COUNT, Long.toString(page.getTotalElements()));
        headers.add(HttpHeaders.LINK, linkHeaderUtil.prepareLinkHeaders(uriBuilder, page));
        return headers;
    }
}
