package ru.oepak22.simplemusicplayer.dagger

import dagger.Module
import dagger.Provides
import io.realm.Realm
import ru.oepak22.simplemusicplayer.data.DataOperations
import javax.inject.Singleton

// класс поставщика зависимостей
@Module
class DataModule {

    // предоставление зависимости Realm
    // зависимость предоставляется один раз
    @Provides
    @Singleton
    fun provideRealm(): Realm {
        return Realm.getDefaultInstance()
    }

    // предоставление зависимости DataOperations
    // зависимость предоставляется один раз
    @Provides
    @Singleton
    fun provideDataService(): DataOperations {
        return DataOperations()
    }
}