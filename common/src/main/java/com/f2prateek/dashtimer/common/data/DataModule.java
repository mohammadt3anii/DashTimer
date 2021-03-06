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

package com.f2prateek.dashtimer.common.data;

import android.app.Application;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import com.f2prateek.dashtimer.common.ForApplication;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.wearable.Wearable;
import com.google.gson.Gson;
import com.squareup.otto.Bus;
import dagger.Module;
import dagger.Provides;
import javax.inject.Singleton;

import static android.content.Context.MODE_PRIVATE;

/**
 * A module for data related dependencies. This will be available from the application graph.
 */
@Module(
    includes = {
        UserPreferencesModule.class
    },
    complete = false,
    library = true)
public final class DataModule {
  @Provides @Singleton SharedPreferences provideSharedPreferences(Application app) {
    return app.getSharedPreferences("dash_timer", MODE_PRIVATE);
  }

  @Provides @Singleton Bus provideBus() {
    return new Bus();
  }

  @Provides @Singleton PackageInfo providePackageInfo(Application application) {
    try {
      return application.getPackageManager().getPackageInfo(application.getPackageName(), 0);
    } catch (PackageManager.NameNotFoundException e) {
      throw new RuntimeException(e);
    }
  }

  @Provides @Singleton GoogleApiClient provideGoogleApiClient(@ForApplication Context context) {
    return new GoogleApiClient.Builder(context).addApi(Wearable.API).build();
  }

  @Provides @Singleton Gson provideGson() {
    return new Gson();
  }
}
