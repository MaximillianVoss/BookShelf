package com.finenkodenis.bookshelf.ui.theme

internal fun shouldOfferLoadMore(
    itemCount: Int,
    requestedLimit: Int,
    maxLimit: Int,
    isLoadingMore: Boolean,
    previousItemCount: Int = 0
): Boolean {
    if (itemCount <= 0) return false
    if (requestedLimit >= maxLimit) return false
    return !isLoadingMore || itemCount > previousItemCount
}
