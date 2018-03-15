package com.example.android.architecture.blueprints.todoapp;

import android.app.Activity;
import android.app.Application;
import android.app.Service;

import com.example.android.architecture.blueprints.todoapp.di.AppInjector;

import java.util.logging.Logger;

import javax.inject.Inject;

import dagger.android.DispatchingAndroidInjector;
import dagger.android.HasActivityInjector;
import dagger.android.HasServiceInjector;
import timber.log.Timber;

public class TodoApp extends Application  implements HasActivityInjector, HasServiceInjector {

    @Inject
    DispatchingAndroidInjector<Activity> dispatchingAndroidInjector;

    @Inject
    DispatchingAndroidInjector<Service> mServiceDispatchingAndroidInjector;

    @Override
    public void onCreate() {
        super.onCreate();
        if (BuildConfig.DEBUG) {
            Timber.plant(new Timber.DebugTree());
        }
        AppInjector.init(this);
    }

    @Override
    public DispatchingAndroidInjector<Activity> activityInjector() {
        return dispatchingAndroidInjector;
    }

    @Override
    public DispatchingAndroidInjector<Service> serviceInjector() {
        return mServiceDispatchingAndroidInjector;
    }
}

