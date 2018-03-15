package com.example.android.architecture.blueprints.todoapp.di;

import android.app.Application;
import android.arch.persistence.room.Room;
import android.content.Context;

import com.example.android.architecture.blueprints.todoapp.data.source.local.ToDoDatabase;

import javax.inject.Singleton;

import dagger.Module;
import dagger.Provides;

@Module
class AppModule {
    @Provides
    Context provideApplication(Application application) {
        return application;
    }
    @Singleton
    @Provides
    ToDoDatabase provideDb(Application app) {
        return Room.databaseBuilder(app, ToDoDatabase.class,"Tasks.db").build();
    }

}
