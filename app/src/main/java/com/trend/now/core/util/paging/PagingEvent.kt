package com.trend.now.core.util.paging

enum class PagingEvent {
    /**
     * Indicates the Paging request to fetch the whole data again.
     */
    RELOAD,

    /**
     * Indicates the Paging request to refresh the data
     * triggered from pull to refresh event.
     */
    REFRESH,

    /**
     * Indicates the Paging request to retry to fetch the data again
     * when failed to do reload, refresh, prepend, or append.
     */
    RETRY
}