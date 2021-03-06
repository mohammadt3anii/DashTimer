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

package com.f2prateek.dashtimer.common.activities;

import android.content.Context;
import com.f2prateek.dashtimer.common.ForActivity;
import com.f2prateek.dashtimer.common.UiModule;
import dagger.Module;
import dagger.Provides;
import javax.inject.Singleton;

/**
 * A module for Activity dependencies. This is different from {@link UiModule} in that these will
 * only be available from the Activity Graph.
 */
@Module(library = true, complete = false, addsTo = UiModule.class)
public class ActivityModule {
  private final BaseActivity activity;

  public ActivityModule(BaseActivity activity) {
    this.activity = activity;
  }

  @Provides @Singleton @ForActivity Context provideActivityContext() {
    return activity;
  }
}
