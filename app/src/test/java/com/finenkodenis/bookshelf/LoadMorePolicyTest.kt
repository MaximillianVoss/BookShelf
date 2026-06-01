package com.finenkodenis.bookshelf

import com.finenkodenis.bookshelf.ui.theme.shouldOfferLoadMore
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class LoadMorePolicyTest {
    @Test
    fun visibleForInitialNonEmptyResultsBelowMaxLimit() {
        assertTrue(
            shouldOfferLoadMore(
                itemCount = 7,
                requestedLimit = 20,
                maxLimit = 100,
                isLoadingMore = false
            )
        )
    }

    @Test
    fun hiddenWhenLoadMoreDoesNotAddNewItems() {
        assertFalse(
            shouldOfferLoadMore(
                itemCount = 7,
                requestedLimit = 40,
                maxLimit = 100,
                isLoadingMore = true,
                previousItemCount = 7
            )
        )
    }

    @Test
    fun hiddenAtMaxLimitOrEmptyList() {
        assertFalse(
            shouldOfferLoadMore(
                itemCount = 20,
                requestedLimit = 100,
                maxLimit = 100,
                isLoadingMore = false
            )
        )
        assertFalse(
            shouldOfferLoadMore(
                itemCount = 0,
                requestedLimit = 20,
                maxLimit = 100,
                isLoadingMore = false
            )
        )
    }
}
