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

import android.app.Activity;
import android.app.Fragment;
import android.content.Context;
import android.os.Bundle;
import com.f2prateek.dart.Dart;
import com.f2prateek.dashtimer.common.DashTimerApp;
import com.f2prateek.dashtimer.common.ForActivity;
import com.squareup.otto.Bus;
import dagger.ObjectGraph;
import hugo.weaving.DebugLog;
import java.util.ArrayList;
import java.util.List;
import javax.inject.Inject;

/**
 * This does not call Butterknife.inject due to different lifecycles on the phone and on mobile.
 */
public abstract class BaseActivity extends Activity {
  @Inject Bus bus;
  @Inject @ForActivity Context activityContext;

  private ObjectGraph activityGraph;

  @Override protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);

    buildActivityGraphAndInject();

    // Inject any extras
    Dart.inject(this);
  }

  @DebugLog
  private void buildActivityGraphAndInject() {
    // Create the activity graph by .plus-ing our modules onto the application graph.
    DashTimerApp app = DashTimerApp.get(this);
    activityGraph = app.getApplicationGraph().plus(getModules().toArray());

    // Inject ourselves so subclasses will have dependencies fulfilled when this method returns.
    activityGraph.inject(this);
  }

  /** Inject the given object into the activity graph. */
  public void inject(Object o) {
    activityGraph.inject(o);
  }

  /**
   * A list of modules to use for the individual activity graph. Subclasses MUST override this
   * method to provide additional modules provided they call and include the modules returned by
   * calling {@code super.getModules()}. At the very lest, this must include all the inject points.
   * todo: introduce an abstract method so subclasses are forced to implement this at compile time
   */
  protected List<Object> getModules() {
    ArrayList<Object> baseModules = new ArrayList<Object>();
    baseModules.add(new ActivityModule(this));
    return baseModules;
  }

  @Override protected void onResume() {
    super.onResume();
    bus.register(this);
  }

  @Override protected void onPause() {
    bus.unregister(this);
    super.onPause();
  }

  @Override protected void onDestroy() {
    // Eagerly clear the reference to the activity graph to allow it to be garbage collected as
    // soon as possible.
    activityGraph = null;
    super.onDestroy();
  }

  public static BaseActivity get(Fragment fragment) {
    return (BaseActivity) fragment.getActivity();
  }
}
