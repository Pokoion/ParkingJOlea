package com.lksnext.parkingplantilla.viewmodel;

import android.content.Context;
import android.content.SharedPreferences;

import androidx.arch.core.executor.testing.InstantTaskExecutorRule;
import androidx.test.core.app.ApplicationProvider;
import androidx.test.ext.junit.runners.AndroidJUnit4;

import com.lksnext.parkingplantilla.ParkingApplication;
import com.lksnext.parkingplantilla.data.DataRepository;
import com.lksnext.parkingplantilla.utils.LiveDataTestUtil;

import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import static org.junit.Assert.*;

@RunWith(AndroidJUnit4.class)
public class UserViewModelInstrumentedTest {
    @Rule
    public InstantTaskExecutorRule instantTaskExecutorRule = new InstantTaskExecutorRule();

    private UserViewModel viewModel;
    private DataRepository repository;
    private Context context;

    private static final String TEST_EMAIL = "userviewmodel_test_user@example.com";
    private static final String TEST_PASSWORD = "Test1234!";
    private static final String TEST_NAME = "UserVM Test";
    private static final int TIMEOUT = 10;

    @Before
    public void setUp() throws Exception {
        context = ApplicationProvider.getApplicationContext();
        repository = ParkingApplication.getRepository();
        viewModel = new UserViewModel(repository);
        // Limpia preferencias de tema antes de cada test
        SharedPreferences prefs = context.getSharedPreferences(UserViewModel.PREF_NAME, 0);
        prefs.edit().clear().commit();
    }

    @After
    public void tearDown() throws Exception {
        // Limpia preferencias de tema después de cada test
        SharedPreferences prefs = context.getSharedPreferences(UserViewModel.PREF_NAME, 0);
        prefs.edit().clear().commit();
    }

    @Test
    public void themeMode_setAndGet() {
        viewModel.setThemeMode(UserViewModel.THEME_DARK);
        int mode = viewModel.getThemeMode();
        assertEquals(UserViewModel.THEME_DARK, mode);
        viewModel.setThemeMode(UserViewModel.THEME_LIGHT);
        mode = viewModel.getThemeMode();
        assertEquals(UserViewModel.THEME_LIGHT, mode);
        viewModel.setThemeMode(UserViewModel.THEME_SYSTEM);
        mode = viewModel.getThemeMode();
        assertEquals(UserViewModel.THEME_SYSTEM, mode);
    }

    @Test
    public void applyTheme_persistsPreference() {
        viewModel.applyTheme(UserViewModel.THEME_DARK);
        int mode = viewModel.getThemeMode();
        assertEquals(UserViewModel.THEME_DARK, mode);
    }

    @Test
    public void logout_signalsSuccess() throws Exception {
        // Simula login
        repository.login(TEST_EMAIL, TEST_PASSWORD, null);
        viewModel.logout();
        Boolean logoutSuccess = LiveDataTestUtil.getValue(viewModel.isLogoutSuccessful());
        assertTrue(Boolean.TRUE.equals(logoutSuccess));
    }

    @Test
    public void resetLogoutState_setsNull() throws Exception {
        viewModel.logout();
        viewModel.resetLogoutState();
        Boolean logoutSuccess = LiveDataTestUtil.getValue(viewModel.isLogoutSuccessful());
        assertNull(logoutSuccess);
    }

    @Test
    public void getCurrentUser_returnsNullIfNotLogged() {
        repository.logout();
        assertNull(viewModel.getCurrentUser());
    }
}

