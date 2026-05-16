package com.snapdoc.app.core.data.repository

import app.cash.turbine.test
import com.google.common.truth.Truth.assertThat
import com.snapdoc.app.core.data.db.DocumentDao
import com.snapdoc.app.core.data.db.DocumentEntity
import com.snapdoc.app.core.data.db.PageDao
import io.mockk.Runs
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.just
import io.mockk.mockk
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Test

class DocumentRepositoryTest {

    private lateinit var documents: DocumentDao
    private lateinit var pages: PageDao
    private lateinit var repo: DocumentRepository

    @Before
    fun setUp() {
        documents = mockk(relaxed = true)
        pages = mockk(relaxed = true)
        repo = DocumentRepositoryImpl(documents, pages)
    }

    @Test
    fun `observeAll maps entities to domain models`() = runTest {
        val now = 1_700_000_000L
        coEvery { documents.observeAll() } returns flowOf(
            listOf(
                DocumentEntity(
                    id = 1,
                    filename = "Aadhaar",
                    pdfPath = "/x.pdf",
                    pageCount = 2,
                    fileSizeBytes = 1024,
                    category = "IDs",
                    createdAt = now,
                    updatedAt = now,
                ),
            ),
        )
        repo.observeAll().test {
            val first = awaitItem()
            assertThat(first).hasSize(1)
            assertThat(first.first().filename).isEqualTo("Aadhaar")
            assertThat(first.first().category).isEqualTo("IDs")
            awaitComplete()
        }
    }

    @Test
    fun `create writes a document and pages`() = runTest {
        coEvery { documents.insert(any()) } returns 42L
        coEvery { pages.insertAll(any()) } returns listOf(1L, 2L)

        val id = repo.create(
            filename = "Bill",
            pdfPath = "/bill.pdf",
            category = "Bills",
            fileSizeBytes = 2048,
            pages = listOf(
                NewPage(imagePath = "/p0.jpg"),
                NewPage(imagePath = "/p1.jpg"),
            ),
        )

        assertThat(id).isEqualTo(42L)
        coVerify(exactly = 1) { documents.insert(any()) }
        coVerify(exactly = 1) { pages.insertAll(any()) }
    }

    @Test
    fun `search returns empty list when query is blank`() = runTest {
        assertThat(repo.search("   ")).isEmpty()
        coVerify(exactly = 0) { documents.search(any()) }
    }
}
