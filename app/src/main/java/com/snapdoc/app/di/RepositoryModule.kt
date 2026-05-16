package com.snapdoc.app.di

import com.snapdoc.app.core.data.repository.CategoryRepository
import com.snapdoc.app.core.data.repository.CategoryRepositoryImpl
import com.snapdoc.app.core.data.repository.DocumentRepository
import com.snapdoc.app.core.data.repository.DocumentRepositoryImpl
import com.snapdoc.app.core.data.repository.OcrRepository
import com.snapdoc.app.core.data.repository.OcrRepositoryImpl
import com.snapdoc.app.core.data.repository.SummaryRepository
import com.snapdoc.app.core.data.repository.SummaryRepositoryImpl
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class RepositoryModule {

    @Binds @Singleton
    abstract fun bindDocumentRepository(impl: DocumentRepositoryImpl): DocumentRepository

    @Binds @Singleton
    abstract fun bindOcrRepository(impl: OcrRepositoryImpl): OcrRepository

    @Binds @Singleton
    abstract fun bindSummaryRepository(impl: SummaryRepositoryImpl): SummaryRepository

    @Binds @Singleton
    abstract fun bindCategoryRepository(impl: CategoryRepositoryImpl): CategoryRepository
}
