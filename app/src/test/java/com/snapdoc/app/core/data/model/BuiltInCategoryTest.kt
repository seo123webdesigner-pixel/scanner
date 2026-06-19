package com.snapdoc.app.core.data.model

import com.google.common.truth.Truth.assertThat
import org.junit.Test

class BuiltInCategoryTest {

    @Test
    fun `clean replies map to the matching folder`() {
        assertThat(BuiltInCategory.fromResponse("Bills")).isEqualTo(BuiltInCategory.Bills)
        assertThat(BuiltInCategory.fromResponse("Receipts")).isEqualTo(BuiltInCategory.Receipts)
        assertThat(BuiltInCategory.fromResponse("Contracts")).isEqualTo(BuiltInCategory.Contracts)
    }

    @Test
    fun `replies with punctuation or markdown still match`() {
        assertThat(BuiltInCategory.fromResponse("**Bills**")).isEqualTo(BuiltInCategory.Bills)
        assertThat(BuiltInCategory.fromResponse("Category: Receipts.")).isEqualTo(BuiltInCategory.Receipts)
        assertThat(BuiltInCategory.fromResponse("Notes\n")).isEqualTo(BuiltInCategory.Notes)
    }

    @Test
    fun `singular and different casing still match`() {
        assertThat(BuiltInCategory.fromResponse("receipt")).isEqualTo(BuiltInCategory.Receipts)
        assertThat(BuiltInCategory.fromResponse("ID")).isEqualTo(BuiltInCategory.Ids)
        assertThat(BuiltInCategory.fromResponse("BILL")).isEqualTo(BuiltInCategory.Bills)
    }

    @Test
    fun `blank, null, or unknown falls back to Other`() {
        assertThat(BuiltInCategory.fromResponse("")).isEqualTo(BuiltInCategory.Other)
        assertThat(BuiltInCategory.fromResponse(null)).isEqualTo(BuiltInCategory.Other)
        assertThat(BuiltInCategory.fromResponse("Banana")).isEqualTo(BuiltInCategory.Other)
    }
}
