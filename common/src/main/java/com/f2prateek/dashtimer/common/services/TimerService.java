/*
 * Copyright 2014 Prateek Srivastava
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.f2prateek.dashtimer.common.services;

import android.app.AlarmManager;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import com.f2prateek.dashtimer.common.R;
import com.f2prateek.dashtimer.common.data.Timer;
import javax.inject.Inject;

import static android.app.AlarmManager.RTC_WAKEUP;
import static android.app.PendingIntent.FLAG_UPDATE_CURRENT;
import static android.app.PendingIntent.getService;

public class TimerService extends BaseService {
  public static final String EXTRA_DURATION = "duration";
  public static final String EXTRA_START_TIME = "startTime";

  public static final String ACTION_START_TIMER = "START_TIMER";
  public static final String ACTION_TIMER_COMPLETED = "TIMER_COMPLETED";
  public static final String ACTION_DELETE_TIMER = "DELETE_TIMER";
  public static final String ACTION_RESTART_TIMER = "RESTART_TIMER";

  static final int NOTIFICATION_TIMER = 1;

  @Inject NotificationManager notificationManager;
  @Inject AlarmManager alarmManager;

  public TimerService() {
    super("TimerService");
  }

  @Override protected void onHandleIntent(Intent intent) {
    super.onHandleIntent(intent);
    String action = intent.getAction();
    if (ACTION_START_TIMER.equals(action)) {
      long duration = intent.getLongExtra(EXTRA_DURATION, 6000L);
      long startTime = intent.getLongExtra(EXTRA_START_TIME, System.currentTimeMillis());
      startTimer(new Timer(startTime, duration));
    } else if (ACTION_RESTART_TIMER.equals(action)) {
      long duration = intent.getLongExtra(EXTRA_DURATION, 6000L);
      restartTimer(duration);
    } else if (ACTION_TIMER_COMPLETED.equals(action)) {
      long duration = intent.getLongExtra(EXTRA_DURATION, 6000L);
      timerCompleted(duration);
    } else if (ACTION_DELETE_TIMER.equals(action)) {
      deleteTimer();
    } else {
      throw new IllegalArgumentException("Undefined action: " + action);
    }
  }

  /** Start the timer with the given start time and duration */
  protected void startTimer(Timer timer) {
    // Start time is needed so we can correctly synchronize the end times on both phone and wear
    // With just end time we lose the ability to remember the duration to restart the timer

    PendingIntent deleteTimerIntent = getDeleteTimerIntent();
    notificationManager.notify(NOTIFICATION_TIMER,
        new Notification.Builder(this).setSmallIcon(R.drawable.app_icon)
            .setContentTitle(getString(R.string.time_left))
            .setContentText(String.valueOf(timer.duration()))
            .setUsesChronometer(true)
            .setWhen(timer.endTime())
            .addAction(R.drawable.ic_action_restart, getString(R.string.restart_timer),
                getRestartTimerIntent(timer.duration()))
            .addAction(R.drawable.ic_action_cancel, getString(R.string.delete_timer),
                deleteTimerIntent)
            .setDeleteIntent(deleteTimerIntent)
            .setDefaults(Notification.DEFAULT_ALL)
            .build());
    alarmManager.setExact(RTC_WAKEUP, timer.endTime(), getCompletedTimerIntent(timer.duration()));
  }

  /** Starts the timer with the current time as the start time */
  protected void restartTimer(long duration) {
    notificationManager.cancel(NOTIFICATION_TIMER);
    startTimer(new Timer(System.currentTimeMillis(), duration));
  }

  /** Finish the timer */
  protected void timerCompleted(long duration) {
    notificationManager.cancel(NOTIFICATION_TIMER);
    notificationManager.notify(NOTIFICATION_TIMER,
        new Notification.Builder(this).setSmallIcon(R.drawable.app_icon)
            .setContentTitle(getString(R.string.timer_elapsed))
            .setContentText(getString(R.string.timer_elapsed))
            .setUsesChronometer(true)
            .setWhen(System.currentTimeMillis())
            .addAction(R.drawable.ic_action_restart, getString(R.string.restart_timer),
                getRestartTimerIntent(duration))
            .setDefaults(Notification.DEFAULT_ALL)
            .build());
  }

  protected void deleteTimer() {
    notificationManager.cancel(NOTIFICATION_TIMER);
    alarmManager.cancel(getCompletedTimerIntent(0)); // duration is not needed here
  }

  PendingIntent getDeleteTimerIntent() {
    Intent intent = new Intent(ACTION_DELETE_TIMER, null, this, getClass());
    return getService(this, 0, intent, FLAG_UPDATE_CURRENT);
  }

  PendingIntent getCompletedTimerIntent(long duration) {
    Intent intent = new Intent(ACTION_TIMER_COMPLETED, null, this, getClass());
    intent.putExtra(EXTRA_DURATION, duration);
    return getService(this, 0, intent, FLAG_UPDATE_CURRENT);
  }

  PendingIntent getRestartTimerIntent(long duration) {
    Intent intent = new Intent(ACTION_RESTART_TIMER, null, this, getClass());
    intent.putExtra(EXTRA_DURATION, duration);
    return getService(this, 0, intent, FLAG_UPDATE_CURRENT);
  }

  public static Intent makeStartTimerIntent(Context context, long startTime, long duration,
      Class<? extends TimerService> clazz) {
    Intent intent = new Intent(ACTION_START_TIMER, null, context, clazz);
    intent.putExtra(EXTRA_DURATION, duration);
    intent.putExtra(EXTRA_START_TIME, startTime);
    return intent;
  }
}
