/*
 * Copyright 2016, The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.example.android.architecture.blueprints.todoapp.data.source.local;

import static com.google.common.base.Preconditions.checkNotNull;

import android.support.annotation.NonNull;
import android.support.annotation.VisibleForTesting;

import com.example.android.architecture.blueprints.todoapp.data.Task;
import com.example.android.architecture.blueprints.todoapp.data.source.TasksDataSource;
import com.example.android.architecture.blueprints.todoapp.util.AppExecutors;

import java.util.List;


/**
 * Concrete implementation of a data source as a db.
 */
public class TasksLocalDataSource implements TasksDataSource {

    private static volatile TasksLocalDataSource INSTANCE;

    private TasksDao mTasksDao;

    private AppExecutors mAppExecutors;

    // Prevent direct instantiation.
    private TasksLocalDataSource(@NonNull AppExecutors appExecutors,
                                 @NonNull TasksDao tasksDao) {
        mAppExecutors = appExecutors;
        mTasksDao = tasksDao;
    }

    public static TasksLocalDataSource getInstance(@NonNull AppExecutors appExecutors,
                                                   @NonNull TasksDao tasksDao) {
        if (INSTANCE == null) {
            synchronized (TasksLocalDataSource.class) {
                if (INSTANCE == null) {
                    INSTANCE = new TasksLocalDataSource(appExecutors, tasksDao);
                }
            }
        }
        return INSTANCE;
    }

    /**
     * Note: {@link LoadTasksCallback#onDataNotAvailable()} is fired if the database doesn't exist
     * or the table is empty.
     */
    @Override
    public void getTasks(@NonNull LoadTasksCallback callback) {
        Runnable runnable = () -> {
            List<Task> tasks = mTasksDao.getTasks();
            mAppExecutors.mainThread().execute(() -> {
                if (tasks.isEmpty()) {
                    // This will be called if the table is new or just empty.
                    callback.onDataNotAvailable();
                } else {
                    callback.onTasksLoaded(tasks);
                }
            });
        };

        mAppExecutors.diskIO().execute(runnable);
    }

    /**
     * Note: {@link GetTaskCallback#onDataNotAvailable()} is fired if the {@link Task} isn't
     * found.
     */
    @Override
    public void getTask(@NonNull String taskId, @NonNull GetTaskCallback callback) {
        Runnable runnable = () -> {
            final Task task = mTasksDao.getTaskById(taskId);
            mAppExecutors.mainThread().execute(() -> {
                if (task != null) {
                    callback.onTaskLoaded(task);
                } else {
                    callback.onDataNotAvailable();
                }
            });
        };
        mAppExecutors.diskIO().execute(runnable);
    }

    @Override
    public void saveTask(@NonNull Task task) {
        checkNotNull(task);
        mAppExecutors.diskIO().execute(() -> mTasksDao.insertTask(task));
    }

    @Override
    public void completeTask(@NonNull Task task) {
        mAppExecutors.diskIO().execute(() -> mTasksDao.updateCompleted(task.getId(), true));
    }

    @Override
    public void completeTask(@NonNull String taskId) {
        // Not required for the local data source because the {@link TasksRepository} handles
        // converting from a {@code taskId} to a {@link task} using its cached data.
    }

    @Override
    public void activateTask(@NonNull Task task) {
        mAppExecutors.diskIO().execute(() -> mTasksDao.updateCompleted(task.getId(), false));
    }

    @Override
    public void activateTask(@NonNull String taskId) {
        // Not required for the local data source because the {@link TasksRepository} handles
        // converting from a {@code taskId} to a {@link task} using its cached data.
    }

    @Override
    public void clearCompletedTasks() {
        mAppExecutors.diskIO().execute(() -> mTasksDao.deleteCompletedTasks());
    }

    @Override
    public void refreshTasks() {
        // Not required because the {@link TasksRepository} handles the logic of refreshing the
        // tasks from all the available data sources.
    }

    @Override
    public void deleteAllTasks() {
        mAppExecutors.diskIO().execute(() -> mTasksDao.deleteTasks());
    }

    @Override
    public void deleteTask(@NonNull String taskId) {
        mAppExecutors.diskIO().execute(() -> mTasksDao.deleteTaskById(taskId));
    }

    @VisibleForTesting
    static void clearInstance() {
        INSTANCE = null;
    }
}
