package com.snapdoc.app.core.data.repository

import com.google.common.truth.Truth.assertThat
import com.snapdoc.app.core.data.db.CategoryDao
import com.snapdoc.app.core.data.db.CategoryEntity
import com.snapdoc.app.core.data.db.DocumentDao
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Test

class CategoryRepositoryTest {

    private lateinit var categories: CategoryDao
    private lateinit var documents: DocumentDao
    private lateinit var repo: CategoryRepository

    @Before
    fun setUp() {
        categories = mockk(relaxed = true)
        documents = mockk(relaxed = true)
        repo = CategoryRepositoryImpl(categories, documents)
    }

    @Test
    fun `renaming a custom folder also re-tags the documents inside it`() = runTest {
        coEvery { categories.findById(7) } returns
            CategoryEntity(id = 7, name = "Work", isBuiltIn = false, sortOrder = 1)

        repo.rename(7, "Office")

        coVerify(exactly = 1) { categories.rename(7, "Office") }
        coVerify(exactly = 1) { documents.reassignCategory("Work", "Office", any()) }
    }

    @Test
    fun `built-in folders cannot be renamed`() = runTest {
        coEvery { categories.findById(1) } returns
            CategoryEntity(id = 1, name = "Bills", isBuiltIn = true, sortOrder = 0)

        val result = runCatching { repo.rename(1, "Utilities") }

        assertThat(result.isFailure).isTrue()
        coVerify(exactly = 0) { categories.rename(any(), any()) }
        coVerify(exactly = 0) { documents.reassignCategory(any(), any(), any()) }
    }

    @Test
    fun `deleting a custom folder moves its documents to Other`() = runTest {
        coEvery { categories.findById(7) } returns
            CategoryEntity(id = 7, name = "Work", isBuiltIn = false, sortOrder = 1)

        repo.delete(7)

        coVerify(exactly = 1) { categories.delete(7) }
        coVerify(exactly = 1) { documents.reassignCategory("Work", "Other", any()) }
    }
}
