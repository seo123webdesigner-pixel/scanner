package com.snapdoc.app.feature.home.support

import androidx.compose.ui.graphics.Color
import com.snapdoc.app.core.data.model.BuiltInCategory
import com.snapdoc.app.core.ui.theme.SnapdocColors

data class CategoryAccent(val strip: Color, val chipText: Color)

/**
 * Maps a category name to its strip + chip colors per design system §2.4.
 * Unknown / custom categories fall through to the "Other" palette.
 */
fun categoryColorsFor(name: String, colors: SnapdocColors): CategoryAccent {
    val c = colors.categories
    return when (BuiltInCategory.fromName(name)) {
        BuiltInCategory.Bills -> CategoryAccent(c.billsStrip, c.billsChipText)
        BuiltInCategory.Ids -> CategoryAccent(c.idsStrip, c.idsChipText)
        BuiltInCategory.Receipts -> CategoryAccent(c.receiptsStrip, c.receiptsChipText)
        BuiltInCategory.Notes -> CategoryAccent(c.notesStrip, c.notesChipText)
        BuiltInCategory.Contracts -> CategoryAccent(c.contractsStrip, c.contractsChipText)
        BuiltInCategory.Other -> CategoryAccent(c.otherStrip, c.otherChipText)
    }
}
