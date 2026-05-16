package com.snapdoc.app.navigation

/**
 * Single source of truth for navigation destinations.
 *
 * Routes are flat strings to stay compatible with Compose Navigation's
 * type-erased graph. Each screen has its own [path] constant; argument
 * routes use the `path/{arg}` convention with helpers exposed on the
 * route object itself.
 */
sealed class SnapdocRoute(val path: String) {
    object Splash : SnapdocRoute("splash")
    object Onboarding : SnapdocRoute("onboarding")
    object Home : SnapdocRoute("home")
    object Categories : SnapdocRoute("categories")
    object Search : SnapdocRoute("search")
    object Scanner : SnapdocRoute("scanner")
    object Save : SnapdocRoute("save?pdfUri={pdfUri}") {
        fun build(pdfUri: String) = "save?pdfUri=$pdfUri"
        const val ARG_PDF_URI = "pdfUri"
    }
    object Document : SnapdocRoute("document/{id}") {
        fun build(id: Long) = "document/$id"
        const val ARG_ID = "id"
    }
    object FullText : SnapdocRoute("document/{id}/text") {
        fun build(id: Long) = "document/$id/text"
    }
    object Settings : SnapdocRoute("settings")
    object ManageCategories : SnapdocRoute("settings/categories")
    object Paywall : SnapdocRoute("paywall")
    object About : SnapdocRoute("about")
    object ComponentGallery : SnapdocRoute("debug/gallery")
}
