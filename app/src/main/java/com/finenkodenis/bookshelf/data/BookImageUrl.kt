package com.finenkodenis.bookshelf.data

fun String?.toSecureImageUrl(): String? {
    val value = this?.trim().orEmpty()
    if (value.isBlank()) return null

    return if (value.startsWith("http://")) {
        value.replaceFirst("http://", "https://")
    } else {
        value
    }
}
