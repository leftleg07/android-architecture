package com.example.android.architecture.blueprints.todoapp.data.source.local;

import com.example.android.architecture.blueprints.todoapp.BuildConfig;
import com.example.android.architecture.blueprints.todoapp.data.Task;
import com.example.android.architecture.blueprints.todoapp.data.source.TasksDataSource;
import com.example.android.architecture.blueprints.todoapp.util.SingleExecutors;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.Config;

import java.util.List;

import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Matchers.anyList;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/** Test for {@link TasksLocalDataSource}. */
@RunWith(RobolectricTestRunner.class)
@Config(constants = BuildConfig.class, manifest = Config.NONE, sdk = 21)
public class TasksLocalDataSourceTest {

    private final static String TITLE = "title";

    private final static String TITLE2 = "title2";

    private final static String TITLE3 = "title3";

    private TasksLocalDataSource mLocalDataSource;
    private TasksDao mTasksDao;

    @Before
    public void setUp() throws Exception {
        mTasksDao = mock(TasksDao.class);

        // Make sure that we're not keeping a reference to the wrong instance.
        TasksLocalDataSource.clearInstance();
        mLocalDataSource = TasksLocalDataSource.getInstance(new SingleExecutors(), mTasksDao);
    }

    @After
    public void cleanUp() {
        TasksLocalDataSource.clearInstance();
    }

    @Test
    public void testPreConditions() {
        assertNotNull(mLocalDataSource);
    }

    @Test
    public void saveTask_retrievesTask() {
        // Given a new task
        final Task newTask = mock(Task.class);

        // When saved into the persistent repository
        when(mTasksDao.getTaskById(null)).thenReturn(newTask);

        // Then the task can be retrieved from the persistent repository
        mLocalDataSource.getTask(newTask.getId(), new TasksDataSource.GetTaskCallback() {
            @Override
            public void onTaskLoaded(Task task) {
                assertThat(task, is(newTask));
            }

            @Override
            public void onDataNotAvailable() {
                fail("Callback error");
            }
        });
    }

    @Test
    public void completeTask_retrievedTaskIsComplete() {
        // Initialize mock for the callback.
        TasksDataSource.GetTaskCallback callback = mock(TasksDataSource.GetTaskCallback.class);
        // Given a new task in the persistent repository
        final Task newTask = mock(Task.class);

        // When saved into the persistent repository
        when(mTasksDao.getTaskById(null)).thenReturn(newTask);

        // When completed in the persistent repository
        when(newTask.isCompleted()).thenReturn(true);

        // Then the task can be retrieved from the persistent repository and is complete
        mLocalDataSource.getTask(newTask.getId(), new TasksDataSource.GetTaskCallback() {
            @Override
            public void onTaskLoaded(Task task) {
                assertThat(task, is(newTask));
                assertThat(task.isCompleted(), is(true));
            }

            @Override
            public void onDataNotAvailable() {
                fail("Callback error");
            }
        });
    }

    @Test
    public void activateTask_retrievedTaskIsActive() {
        // Initialize mock for the callback.
        TasksDataSource.GetTaskCallback callback = mock(TasksDataSource.GetTaskCallback.class);

        // Given a new completed task in the persistent repository
        final Task newTask = mock(Task.class);

        // When saved into the persistent repository
        when(mTasksDao.getTaskById(null)).thenReturn(newTask);

        // When completed in the persistent repository
        when(newTask.isCompleted()).thenReturn(true);

        // When activated in the persistent repository
        when(newTask.isCompleted()).thenReturn(false);

        // Then the task can be retrieved from the persistent repository and is active
        mLocalDataSource.getTask(newTask.getId(), callback);

        verify(callback, never()).onDataNotAvailable();
        verify(callback).onTaskLoaded(newTask);

        assertThat(newTask.isCompleted(), is(false));
    }

    @Test
    public void clearCompletedTask_taskNotRetrievable() {
        // Initialize mocks for the callbacks.
        TasksDataSource.GetTaskCallback callback1 = mock(TasksDataSource.GetTaskCallback.class);
        TasksDataSource.GetTaskCallback callback2 = mock(TasksDataSource.GetTaskCallback.class);
        TasksDataSource.GetTaskCallback callback3 = mock(TasksDataSource.GetTaskCallback.class);

        // Given 2 new completed tasks and 1 active task in the persistent repository
        final Task newTask1 = new Task(TITLE, "");
        mLocalDataSource.saveTask(newTask1);
        mLocalDataSource.completeTask(newTask1);
        final Task newTask2 = new Task(TITLE2, "");
        mLocalDataSource.saveTask(newTask2);
        mLocalDataSource.completeTask(newTask2);
        final Task newTask3 = new Task(TITLE3, "");
        mLocalDataSource.saveTask(newTask3);

        // When completed tasks are cleared in the repository
        mLocalDataSource.clearCompletedTasks();

        // Then the completed tasks cannot be retrieved and the active one can
        mLocalDataSource.getTask(newTask1.getId(), callback1);

        verify(callback1).onDataNotAvailable();
        verify(callback1, never()).onTaskLoaded(newTask1);

        mLocalDataSource.getTask(newTask2.getId(), callback2);

        verify(callback2).onDataNotAvailable();
        verify(callback2, never()).onTaskLoaded(newTask2);

        mLocalDataSource.getTask(newTask3.getId(), callback3);

        verify(callback3, never()).onDataNotAvailable();
        verify(callback3).onTaskLoaded(newTask3);
    }

    @Test
    public void deleteAllTasks_emptyListOfRetrievedTask() {
        // Given a new task in the persistent repository and a mocked callback
        Task newTask = new Task(TITLE, "");
        mLocalDataSource.saveTask(newTask);
        TasksDataSource.LoadTasksCallback callback = mock(TasksDataSource.LoadTasksCallback.class);

        // When all tasks are deleted
        mLocalDataSource.deleteAllTasks();

        // Then the retrieved tasks is an empty list
        mLocalDataSource.getTasks(callback);

        verify(callback).onDataNotAvailable();
        verify(callback, never()).onTasksLoaded(anyList());
    }

    @Test
    public void getTasks_retrieveSavedTasks() {
        // Given 2 new tasks in the persistent repository
        final Task newTask1 = new Task(TITLE, "");
        mLocalDataSource.saveTask(newTask1);
        final Task newTask2 = new Task(TITLE, "");
        mLocalDataSource.saveTask(newTask2);

        // Then the tasks can be retrieved from the persistent repository
        mLocalDataSource.getTasks(new TasksDataSource.LoadTasksCallback() {
            @Override
            public void onTasksLoaded(List<Task> tasks) {
                assertNotNull(tasks);
                assertTrue(tasks.size() >= 2);

                boolean newTask1IdFound = false;
                boolean newTask2IdFound = false;
                for (Task task : tasks) {
                    if (task.getId().equals(newTask1.getId())) {
                        newTask1IdFound = true;
                    }
                    if (task.getId().equals(newTask2.getId())) {
                        newTask2IdFound = true;
                    }
                }
                assertTrue(newTask1IdFound);
                assertTrue(newTask2IdFound);
            }

            @Override
            public void onDataNotAvailable() {
                fail();
            }
        });
    }
}
