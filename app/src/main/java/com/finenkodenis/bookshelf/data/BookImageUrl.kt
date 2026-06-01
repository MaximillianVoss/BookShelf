package com.finenkodenis.bookshelf.data

import java.net.URI

fun String?.toSecureImageUrl(): String? {
    val value = this?.trim().orEmpty()
    if (value.isBlank()) return null

    return if (value.startsWith("http://")) {
        if (value.isLocalHttpUrl()) value else value.replaceFirst("http://", "https://")
    } else {
        value
    }
}

private fun String.isLocalHttpUrl(): Boolean {
    val host = runCatching { URI(this).host.orEmpty().lowercase() }.getOrDefault("")
    return host == "10.0.2.2" ||
        host == "localhost" ||
        host == "127.0.0.1" ||
        host.startsWith("10.") ||
        host.startsWith("192.168.") ||
        LOCAL_172_ADDRESS_REGEX.matches(host)
}

private val LOCAL_172_ADDRESS_REGEX = Regex("""172\.(1[6-9]|2\d|3[0-1])\..+""")
