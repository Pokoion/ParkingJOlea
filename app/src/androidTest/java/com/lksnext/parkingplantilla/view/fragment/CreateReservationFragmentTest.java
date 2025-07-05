package com.lksnext.parkingplantilla.view.fragment;

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.action.ViewActions.closeSoftKeyboard;
import static androidx.test.espresso.action.ViewActions.typeText;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.matcher.ViewMatchers.hasDescendant;
import static androidx.test.espresso.matcher.ViewMatchers.isDisplayed;
import static androidx.test.espresso.matcher.ViewMatchers.isRoot;
import static androidx.test.espresso.matcher.ViewMatchers.withClassName;
import static androidx.test.espresso.matcher.ViewMatchers.withId;
import static androidx.test.espresso.matcher.ViewMatchers.withText;
import static com.lksnext.parkingplantilla.utils.MyViewAction.clickChildViewWithId;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.allOf;

import androidx.test.espresso.contrib.RecyclerViewActions;
import androidx.test.ext.junit.rules.ActivityScenarioRule;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.espresso.matcher.RootMatchers;
import androidx.test.espresso.contrib.PickerActions;

import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import com.lksnext.parkingplantilla.R;
import com.lksnext.parkingplantilla.view.activity.LoginActivity;
import com.lksnext.parkingplantilla.view.activity.MainActivity;
import com.lksnext.parkingplantilla.utils.EspressoTestUtils;

@RunWith(AndroidJUnit4.class)
public class CreateReservationFragmentTest {

    @Rule
    public ActivityScenarioRule<LoginActivity> activityRule = new ActivityScenarioRule<>(LoginActivity.class);

    @Before
    public void loginBeforeTest() {
        EspressoTestUtils.waitForActivity(LoginActivity.class, 5000);
        onView(withId(R.id.emailText)).perform(typeText("jonoolea@gmail.com"), closeSoftKeyboard());
        onView(withId(R.id.passwordText)).perform(typeText("123456"), closeSoftKeyboard());
        onView(withId(R.id.loginButton)).perform(click());
        EspressoTestUtils.waitForActivity(MainActivity.class, 5000);
    }

    @Test
    public void testFlujoCompletoCrearReserva() {
        // Crear reserva
        onView(isRoot()).perform(EspressoTestUtils.waitForView(withId(R.id.createReservationButton), 5000));
        onView(isRoot()).perform(EspressoTestUtils.waitUntilEnabled(withId(R.id.createReservationButton), 5000));
        onView(withId(R.id.createReservationButton)).perform(click());
        onView(isRoot()).perform(EspressoTestUtils.waitForView(withId(R.id.datePickerButton), 5000));
        onView(withId(R.id.datePickerButton)).perform(click());
        onView(withText("OK")).inRoot(RootMatchers.isDialog()).perform(click());
        // Espera a btnNextStep tras seleccionar fecha
        onView(isRoot()).perform(EspressoTestUtils.waitForView(withId(R.id.btnNextStep), 5000));
        java.util.Calendar calendar = java.util.Calendar.getInstance();
        calendar.add(java.util.Calendar.HOUR_OF_DAY, 1);
        int startHour = calendar.get(java.util.Calendar.HOUR_OF_DAY);
        int startMinute = calendar.get(java.util.Calendar.MINUTE);
        onView(withId(R.id.startTimeButton)).perform(click());
        onView(withClassName(is("android.widget.TimePicker"))).perform(PickerActions.setTime(startHour, startMinute));
        onView(withText("OK")).inRoot(RootMatchers.isDialog()).perform(click());
        onView(isRoot()).perform(EspressoTestUtils.waitForView(withId(R.id.btnNextStep), 5000));
        calendar.add(java.util.Calendar.HOUR_OF_DAY, 1);
        int endHour = calendar.get(java.util.Calendar.HOUR_OF_DAY);
        int endMinute = calendar.get(java.util.Calendar.MINUTE);
        onView(withId(R.id.endTimeButton)).perform(click());
        onView(withClassName(is("android.widget.TimePicker"))).perform(PickerActions.setTime(endHour, endMinute));
        onView(withText("OK")).inRoot(RootMatchers.isDialog()).perform(click());
        onView(isRoot()).perform(EspressoTestUtils.waitForView(withId(R.id.btnNextStep), 5000));
        onView(isRoot()).perform(EspressoTestUtils.waitUntilEnabled(withId(R.id.btnNextStep), 5000));
        java.text.SimpleDateFormat sdf = new java.text.SimpleDateFormat("yyyy-MM-dd");
        String fecha = sdf.format(calendar.getTime());
        String horaInicio = String.format("%02d:%02d", startHour, startMinute);
        String horaFin = String.format("%02d:%02d", endHour, endMinute);
        String horaCard = horaInicio + " - " + horaFin;
        onView(withId(R.id.btnNextStep)).perform(click());
        onView(isRoot()).perform(EspressoTestUtils.waitForView(withId(R.id.manualParkingRadioButton), 5000));
        onView(withId(R.id.manualParkingRadioButton)).perform(click());
        onView(isRoot()).perform(EspressoTestUtils.waitForView(withId(R.id.parkingRowSpinner), 5000));
        onView(withId(R.id.parkingRowSpinner)).perform(click());
        onView(withText("A")).perform(click());
        onView(withId(R.id.parkingNumberSpinner)).perform(click());
        onView(withText("1")).perform(click());
        onView(isRoot()).perform(EspressoTestUtils.waitForView(withId(R.id.btnSaveReservation), 5000));
        onView(withId(R.id.btnSaveReservation)).perform(click());
        onView(isRoot()).perform(EspressoTestUtils.waitForView(withId(R.id.reservations), 5000));

        // Cancelar reserva
        onView(withId(R.id.reservations)).perform(click());
        // Selecciona el tab "Actual" usando el id en vez del texto
        onView(isRoot()).perform(EspressoTestUtils.waitForView(withId(R.id.tabLayout), 5000));
        // Espera a que el RecyclerView de reservas actuales esté visible
        onView(isRoot()).perform(EspressoTestUtils.waitForView(withId(R.id.recyclerViewReservations), 5000));
        // Espera a que aparezca la card de la reserva
        onView(isRoot()).perform(EspressoTestUtils.waitForView(withText("A-1"), 5000));
        onView(allOf(withText("A-1"), isDisplayed())).check(matches(isDisplayed()));
        onView(allOf(withText(fecha), isDisplayed())).check(matches(isDisplayed()));
        onView(allOf(withText(horaCard), isDisplayed())).check(matches(isDisplayed()));
        onView(withId(R.id.recyclerViewReservations))
                .perform(RecyclerViewActions.actionOnItem(
                        hasDescendant(withText("A-1")),
                        clickChildViewWithId(R.id.deleteButton)
                ));
        onView(withText("Eliminar")).inRoot(RootMatchers.isDialog()).perform(click());
    }

    @After
    public void logoutAfterTest() {
        try {
            // Ir al apartado usuario en el navBar
            onView(isRoot()).perform(EspressoTestUtils.waitForView(withId(R.id.user), 5000));
            onView(withId(R.id.user)).perform(click());
            // Esperar a que aparezca el botón de logout
            onView(isRoot()).perform(EspressoTestUtils.waitForView(withId(R.id.logoutButton), 5000));
            onView(withId(R.id.logoutButton)).perform(click());
        } catch (Exception e) {
            // Si ya está en login o no se puede hacer logout, ignorar
        }
    }

}
