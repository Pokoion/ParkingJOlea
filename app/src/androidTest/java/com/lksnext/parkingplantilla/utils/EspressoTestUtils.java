package com.lksnext.parkingplantilla.utils;

import static androidx.test.espresso.Espresso.onView;

import android.app.Activity;
import android.os.SystemClock;
import android.view.View;

import androidx.test.espresso.UiController;
import androidx.test.espresso.ViewAction;
import androidx.test.espresso.util.TreeIterables;
import androidx.test.platform.app.InstrumentationRegistry;
import androidx.test.runner.lifecycle.ActivityLifecycleMonitorRegistry;
import androidx.test.runner.lifecycle.Stage;

import org.hamcrest.Matcher;

import java.util.Collection;

public class EspressoTestUtils {
    public static ViewAction waitForView(final Matcher<View> matcher, final long millis) {
        return new ViewAction() {
            @Override
            public Matcher<View> getConstraints() {
                return androidx.test.espresso.matcher.ViewMatchers.isRoot();
            }
            @Override
            public String getDescription() {
                return "Esperar hasta que la vista esté visible: " + matcher.toString();
            }
            @Override
            public void perform(final UiController uiController, final View view) {
                final long startTime = System.currentTimeMillis();
                final long endTime = startTime + millis;
                do {
                    for (View child : TreeIterables.breadthFirstViewTraversal(view)) {
                        if (matcher.matches(child) && child.isShown()) {
                            return;
                        }
                    }
                    uiController.loopMainThreadForAtLeast(50);
                } while (System.currentTimeMillis() < endTime);
                throw new AssertionError("Vista no encontrada: " + matcher.toString());
            }
        };
    }

    public static ViewAction waitUntilEnabled(final Matcher<View> matcher, final long millis) {
        return new ViewAction() {
            @Override
            public Matcher<View> getConstraints() {
                return androidx.test.espresso.matcher.ViewMatchers.isRoot();
            }
            @Override
            public String getDescription() {
                return "Esperar hasta que la vista esté habilitada: " + matcher.toString();
            }
            @Override
            public void perform(final UiController uiController, final View view) {
                final long startTime = System.currentTimeMillis();
                final long endTime = startTime + millis;
                do {
                    for (View child : TreeIterables.breadthFirstViewTraversal(view)) {
                        if (matcher.matches(child) && child.isShown() && child.isEnabled()) {
                            return;
                        }
                    }
                    uiController.loopMainThreadForAtLeast(50);
                } while (System.currentTimeMillis() < endTime);
                throw new AssertionError("Vista no habilitada: " + matcher.toString());
            }
        };
    }

    public static void waitForActivity(final Class<? extends Activity> activityClass, final long timeoutMillis) {
        final long startTime = System.currentTimeMillis();
        final long endTime = startTime + timeoutMillis;
        do {
            final Activity[] resumedActivity = new Activity[1];
            InstrumentationRegistry.getInstrumentation().runOnMainSync(() -> {
                Collection<Activity> activities = ActivityLifecycleMonitorRegistry.getInstance()
                        .getActivitiesInStage(Stage.RESUMED);
                for (Activity activity : activities) {
                    if (activity.getClass().equals(activityClass)) {
                        resumedActivity[0] = activity;
                    }
                }
            });
            if (resumedActivity[0] != null) return;
            SystemClock.sleep(100);
        } while (System.currentTimeMillis() < endTime);
        throw new AssertionError("No se encontró la actividad: " + activityClass.getSimpleName());
    }

    public static String getText(final org.hamcrest.Matcher<android.view.View> matcher) {
        final String[] stringHolder = {null};
        onView(matcher).check((view, noViewFoundException) -> {
            if (noViewFoundException != null) {
                throw noViewFoundException;
            }
            android.widget.TextView tv = (android.widget.TextView) view;
            stringHolder[0] = tv.getText().toString();
        });
        return stringHolder[0];
    }
}

