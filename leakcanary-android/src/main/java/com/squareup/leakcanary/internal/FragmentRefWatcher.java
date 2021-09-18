/*
 * Copyright (C) 2018 Square, Inc.
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
package com.squareup.leakcanary.internal;

import android.app.Activity;
import android.app.Application;
import android.content.Context;
import android.os.Bundle;

import com.squareup.leakcanary.RefWatcher;

import java.lang.reflect.Constructor;
import java.util.ArrayList;
import java.util.List;

import static android.os.Build.VERSION.SDK_INT;
import static android.os.Build.VERSION_CODES.O;

/**
 * Internal class used to watch for fragments leaks.
 */
public interface FragmentRefWatcher {

    void watchFragments(Activity activity);
    // Fragment历史原因 有support包 和framework包
    // 抽象到子类自己去处理 让子类来决定

    final class Helper { // 接口中的类都是默认静态static修饰的

        private static final String SUPPORT_FRAGMENT_REF_WATCHER_CLASS_NAME =
                "com.squareup.leakcanary.internal.SupportFragmentRefWatcher";

        public static void install(Context context, RefWatcher refWatcher) {
            List<FragmentRefWatcher> fragmentRefWatchers = new ArrayList<>();

            if (SDK_INT >= O) { //android8 之后添加一个AndroidOFragmentRefWatcher
                fragmentRefWatchers.add(new AndroidOFragmentRefWatcher(refWatcher));
            }

            try { //需要引入leakCanary的support Fragment的依赖库
                Class<?> fragmentRefWatcherClass = Class.forName(SUPPORT_FRAGMENT_REF_WATCHER_CLASS_NAME);
                Constructor<?> constructor =
                        fragmentRefWatcherClass.getDeclaredConstructor(RefWatcher.class);
                FragmentRefWatcher supportFragmentRefWatcher =
                        (FragmentRefWatcher) constructor.newInstance(refWatcher);
                fragmentRefWatchers.add(supportFragmentRefWatcher);
            } catch (Exception ignored) {
            }

            if (fragmentRefWatchers.size() == 0) { // 此版本不支持androidX
                return;  // 若没哟使用8.0之前的库  有没有使用support库或没添加依赖 则直接返回，不监测Fragment了。
            }

            Helper helper = new Helper(fragmentRefWatchers);

            Application application = (Application) context.getApplicationContext();
            application.registerActivityLifecycleCallbacks(helper.activityLifecycleCallbacks);
        }

        private final Application.ActivityLifecycleCallbacks activityLifecycleCallbacks =
                new ActivityLifecycleCallbacksAdapter() {
                    @Override
                    public void onActivityCreated(Activity activity, Bundle savedInstanceState) {
                        for (FragmentRefWatcher watcher : fragmentRefWatchers) {
                            watcher.watchFragments(activity);
                        }
                    }
                };

        private final List<FragmentRefWatcher> fragmentRefWatchers;

        private Helper(List<FragmentRefWatcher> fragmentRefWatchers) {
            this.fragmentRefWatchers = fragmentRefWatchers;
        }
    }
}
