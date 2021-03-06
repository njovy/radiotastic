/*
 * Copyright (c) 2015. Lorem ipsum dolor sit amet, consectetur adipiscing elit.
 * Morbi non lorem porttitor neque feugiat blandit. Ut vitae ipsum eget quam lacinia accumsan.
 * Etiam sed turpis ac ipsum condimentum fringilla. Maecenas magna.
 * Proin dapibus sapien vel ante. Aliquam erat volutpat. Pellentesque sagittis ligula eget metus.
 * Vestibulum commodo. Ut rhoncus gravida arcu.
 */

package com.udacity.study.jam.radiotastic.di.module;

import android.content.Context;
import android.os.Bundle;

import com.udacity.study.jam.radiotastic.api.ApiEndpoint;

import javax.inject.Singleton;

import dagger.Module;
import dagger.Provides;
import retrofit.Endpoint;

import static org.mockito.Mockito.mock;

@Module(includes = RadioApiModule.class)
public class DebugDataModule {
    private final boolean mockMode;
    private final Bundle extras;

    public DebugDataModule(boolean mockMode) {
        this.extras = new Bundle();
        this.mockMode = mockMode;
    }

    public DebugDataModule(boolean mockMode, Bundle extras) {
        this.extras = extras;
        this.mockMode = mockMode;
    }

    @Provides
    Bundle provideSyncExtras() {
        return extras;
    }

    @Provides
    @Singleton
    Endpoint provideApiEndpoint(Context context) {
        if (mockMode) {
            return mock(Endpoint.class);
        } else {
            return new ApiEndpoint(context);
        }
    }
}
