package com.snapdoc.app.di

import android.content.Context
import androidx.room.Room
import com.snapdoc.app.core.data.db.AiSummaryDao
import com.snapdoc.app.core.data.db.CategoryDao
import com.snapdoc.app.core.data.db.DocumentDao
import com.snapdoc.app.core.data.db.OcrTextDao
import com.snapdoc.app.core.data.db.PageDao
import com.snapdoc.app.core.data.db.SnapdocDatabase
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    @Provides
    @Singleton
    fun provideDatabase(@ApplicationContext context: Context): SnapdocDatabase {
        val db = Room.databaseBuilder(
            context,
            SnapdocDatabase::class.java,
            SnapdocDatabase.NAME,
        ).build()
        // Seed default categories the first time the DB is opened.
        CoroutineScope(SupervisorJob() + Dispatchers.IO).launch {
            runCatching { db.categories().seedBuiltInIfEmpty() }
        }
        return db
    }

    @Provides fun provideDocumentDao(db: SnapdocDatabase): DocumentDao = db.documents()
    @Provides fun providePageDao(db: SnapdocDatabase): PageDao = db.pages()
    @Provides fun provideOcrDao(db: SnapdocDatabase): OcrTextDao = db.ocr()
    @Provides fun provideSummaryDao(db: SnapdocDatabase): AiSummaryDao = db.summaries()
    @Provides fun provideCategoryDao(db: SnapdocDatabase): CategoryDao = db.categories()
}
