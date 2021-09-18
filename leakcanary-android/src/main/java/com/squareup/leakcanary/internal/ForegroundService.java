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

import android.app.IntentService;
import android.app.Notification;
import android.content.Intent;
import android.os.IBinder;
import android.os.SystemClock;
import android.support.annotation.Nullable;

import com.squareup.leakcanary.R;

// 安卓8.0后 不允许 创建运行后台service 在onCreate时候，抵用showForegroundNotification startForeground 设置为前台服务
public abstract class ForegroundService extends IntentService {

    private final int notificationContentTitleResId;
    private final int notificationId;

    public ForegroundService(String name, int notificationContentTitleResId) {
        super(name);
        this.notificationContentTitleResId = notificationContentTitleResId;
        notificationId = (int) SystemClock.uptimeMillis();
    }

    @Override
    public void onCreate() {
        super.onCreate();
        showForegroundNotification(100, 0, true,
                getString(R.string.leak_canary_notification_foreground_text));
    }

    protected void showForegroundNotification(int max, int progress, boolean indeterminate,
                                              String contentText) {
        Notification.Builder builder = new Notification.Builder(this)
                .setContentTitle(getString(notificationContentTitleResId))
                .setContentText(contentText)
                .setProgress(max, progress, indeterminate);
        Notification notification = LeakCanaryInternals.buildNotification(this, builder);
        startForeground(notificationId, notification);
    }

    @Override
    protected void onHandleIntent(@Nullable Intent intent) {
        onHandleIntentInForeground(intent);
    }

    protected abstract void onHandleIntentInForeground(@Nullable Intent intent);

    @Override
    public void onDestroy() {
        super.onDestroy();
        stopForeground(true);
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
}
