package com.raulshma.dailylife.ui.components

/**
 * Centralized keys for shared element transitions to avoid stringly-typed errors.
 */
object SharedElementKeys {
    fun media(itemId: Long): String = "media-$itemId"
    fun title(itemId: Long): String = "title-$itemId"
    fun card(itemId: Long): String = "card-$itemId"
    fun typeBadge(itemId: Long): String = "type-badge-$itemId"
}
