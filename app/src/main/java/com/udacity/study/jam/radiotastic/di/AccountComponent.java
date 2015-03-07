/*
 * Copyright (c) 2015. Lorem ipsum dolor sit amet, consectetur adipiscing elit.
 * Morbi non lorem porttitor neque feugiat blandit. Ut vitae ipsum eget quam lacinia accumsan.
 * Etiam sed turpis ac ipsum condimentum fringilla. Maecenas magna.
 * Proin dapibus sapien vel ante. Aliquam erat volutpat. Pellentesque sagittis ligula eget metus.
 * Vestibulum commodo. Ut rhoncus gravida arcu.
 */

package com.udacity.study.jam.radiotastic.di;

import com.udacity.study.jam.radiotastic.domain.GetAccountUseCase;

import dagger.Component;

@Component(
        modules = {AccountCasesModule.class},
        dependencies = {ApplicationComponent.class, SyncComponent.class}
)
public interface AccountComponent {
    GetAccountUseCase getAccountUseCase();
}
