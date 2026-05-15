package com.finenkodenis.bookshelf

import com.finenkodenis.bookshelf.data.BookSearchSource
import com.finenkodenis.bookshelf.ui.theme.shouldFallbackToOpenLibraryAfterGoogleError
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class GoogleBooksFallbackTest {

    @Test
    fun fallbackEnabledForGoogleQuotaAndTemporaryFailure() {
        assertTrue(shouldFallbackToOpenLibraryAfterGoogleError(BookSearchSource.GOOGLE, 429))
        assertTrue(shouldFallbackToOpenLibraryAfterGoogleError(BookSearchSource.GOOGLE, 503))
        assertTrue(shouldFallbackToOpenLibraryAfterGoogleError(BookSearchSource.GOOGLE))
    }

    @Test
    fun fallbackDisabledForOtherSourcesAndAccessErrors() {
        assertFalse(shouldFallbackToOpenLibraryAfterGoogleError(BookSearchSource.OPEN_LIBRARY, 503))
        assertFalse(shouldFallbackToOpenLibraryAfterGoogleError(BookSearchSource.LOCAL, 503))
        assertFalse(shouldFallbackToOpenLibraryAfterGoogleError(BookSearchSource.GOOGLE, 403))
    }
}
