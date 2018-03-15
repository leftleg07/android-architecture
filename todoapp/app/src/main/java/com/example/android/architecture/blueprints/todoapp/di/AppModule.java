package com.example.android.architecture.blueprints.todoapp.di;

import android.app.Application;
import android.arch.persistence.room.Room;
import android.content.Context;

import com.example.android.architecture.blueprints.todoapp.ViewModelFactory;
import com.example.android.architecture.blueprints.todoapp.data.source.TasksDataSource;
import com.example.android.architecture.blueprints.todoapp.data.source.TasksRepository;
import com.example.android.architecture.blueprints.todoapp.data.source.local.TasksLocalDataSource;
import com.example.android.architecture.blueprints.todoapp.data.source.local.ToDoDatabase;
import com.example.android.architecture.blueprints.todoapp.util.AppExecutors;

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
    ToDoDatabase provideDatabase(Application application) {
        return Room.databaseBuilder(application, ToDoDatabase.class,"Tasks.db").build();
    }

    @Singleton
    @Provides
    TasksRepository provideTasksRepository(ToDoDatabase database, TasksDataSource tasksRemoteDataSource) {
        return new TasksRepository(tasksRemoteDataSource, new TasksLocalDataSource(new AppExecutors(), database.taskDao()));
    }

    @Singleton
    @Provides
    ViewModelFactory provideViewModelFactory(Application application, TasksRepository repository) {
        return new ViewModelFactory(application, repository);
    }

}
