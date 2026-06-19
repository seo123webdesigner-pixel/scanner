package com.snapdoc.app.core.data.model

/** Domain model — the rest of the app sees this, never raw entities. */
data class Document(
    val id: Long,
    val filename: String,
    val pdfPath: String,
    val pageCount: Int,
    val fileSizeBytes: Long,
    val category: String,
    val createdAt: Long,
    val updatedAt: Long,
    val favorite: Boolean,
    val passwordProtected: Boolean,
)

data class Page(
    val id: Long,
    val documentId: Long,
    val pageIndex: Int,
    val imagePath: String,
    val thumbnailPath: String?,
)

data class AiSummary(
    val tldr: String,
    val bullets: List<String>,
    val generatedAt: Long,
)

/** Built-in default categories from spec §2.4. */
enum class BuiltInCategory(val displayName: String) {
    Bills("Bills"),
    Ids("IDs"),
    Receipts("Receipts"),
    Notes("Notes"),
    Contracts("Contracts"),

    /** Catch-all / default bucket. Shown to users as "Scans". */
    Other("Scans");

    companion object {
        fun fromName(name: String?): BuiltInCategory =
            entries.firstOrNull { it.displayName.equals(name, ignoreCase = true) } ?: Other

        /**
         * Lenient parse of a Gemini classification reply. The model is asked
         * to return one bare category word, but it sometimes adds punctuation,
         * markdown, or a short phrase ("**Bills**", "Category: Receipts."). We
         * match on whole-word presence (singular or plural) so those still land
         * in the right folder instead of silently defaulting to "Other".
         */
        fun fromResponse(raw: String?): BuiltInCategory {
            if (raw.isNullOrBlank()) return Other
            val text = raw.lowercase()
            return entries.firstOrNull { category ->
                category != Other && category.matchTokens.any { token ->
                    Regex("\\b${Regex.escape(token)}\\b").containsMatchIn(text)
                }
            } ?: Other
        }
    }
}

/** Words that should map a free-text reply onto this built-in category. */
private val BuiltInCategory.matchTokens: List<String>
    get() = when (this) {
        BuiltInCategory.Bills -> listOf("bills", "bill")
        BuiltInCategory.Ids -> listOf("ids", "id")
        BuiltInCategory.Receipts -> listOf("receipts", "receipt")
        BuiltInCategory.Notes -> listOf("notes", "note")
        BuiltInCategory.Contracts -> listOf("contracts", "contract")
        BuiltInCategory.Other -> listOf("other")
    }
