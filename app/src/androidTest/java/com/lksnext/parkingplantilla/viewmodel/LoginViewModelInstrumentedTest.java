package com.lksnext.parkingplantilla.viewmodel;

import androidx.arch.core.executor.testing.InstantTaskExecutorRule;
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

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

@RunWith(AndroidJUnit4.class)
public class LoginViewModelInstrumentedTest {
    @Rule
    public InstantTaskExecutorRule instantTaskExecutorRule = new InstantTaskExecutorRule();

    private LoginViewModel viewModel;
    private DataRepository repository;
    private static final String TEST_EMAIL = "test_user@example.com";
    private static final String TEST_PASSWORD = "123456";
    private static final int TIMEOUT = 10;

    @Before
    public void setUp() throws Exception {
        repository = ParkingApplication.getInstance().getRepository();
        viewModel = new LoginViewModel(repository);
        // Solo login, el usuario ya debe existir
        CountDownLatch latch = new CountDownLatch(1);
        repository.login(TEST_EMAIL, TEST_PASSWORD, new LatchCallback<>(latch));
        latch.await(TIMEOUT, TimeUnit.SECONDS);
        repository.logout();
    }

    @After
    public void tearDown() {
        viewModel.logout();
        // No se borra el usuario de test
    }

    @Test
    public void login_success() throws Exception {
        viewModel.loginUser(TEST_EMAIL, TEST_PASSWORD);
        Boolean isLogged = LiveDataTestUtil.getValue(viewModel.isLogged());
        String userEmail = LiveDataTestUtil.getValue(viewModel.getCurrentUserEmail());
        LoginViewModel.LoginError error = LiveDataTestUtil.getValue(viewModel.getLoginError());
        assertEquals(Boolean.TRUE, isLogged);
        assertEquals(TEST_EMAIL, userEmail);
        assertNull(error);
    }

    @Test
    public void logout_clearsSession() throws Exception {
        viewModel.loginUser(TEST_EMAIL, TEST_PASSWORD);
        viewModel.logout();
        Boolean isLogged = LiveDataTestUtil.getValue(viewModel.isLogged());
        String userEmail = LiveDataTestUtil.getValue(viewModel.getCurrentUserEmail());
        LoginViewModel.LoginError error = LiveDataTestUtil.getValue(viewModel.getLoginError());
        assertNotEquals(Boolean.TRUE, isLogged);
        assertNull(userEmail);
        assertNull(error);
    }

    @Test
    public void login_emptyFields() throws Exception {
        viewModel.loginUser("", "");
        LoginViewModel.LoginError error = LiveDataTestUtil.getValue(viewModel.getLoginError());
        Boolean isLogged = viewModel.isLogged().getValue();
        assertEquals(LoginViewModel.LoginError.EMPTY_FIELDS, error);
        assertNotEquals(Boolean.TRUE, isLogged);
    }

    @Test
    public void login_invalidCredentials() throws Exception {
        viewModel.loginUser("noexiste@ejemplo.com", "incorrecto");
        LoginViewModel.LoginError error = LiveDataTestUtil.getOrAwaitValue(
            viewModel.getLoginError(),
            LoginViewModel.LoginError.INVALID_CREDENTIALS
        );
        Boolean isLogged = LiveDataTestUtil.getValue(viewModel.isLogged());
        assertEquals(LoginViewModel.LoginError.INVALID_CREDENTIALS, error);
        assertNotEquals(Boolean.TRUE, isLogged);
    }

    @Test
    public void login_applicationError() throws Exception {
        LoginViewModel vm = new LoginViewModel(null);
        vm.loginUser("test@ejemplo.com", "1234");
        LoginViewModel.LoginError error = LiveDataTestUtil.getValue(vm.getLoginError());
        Boolean isLogged = vm.isLogged().getValue();
        assertEquals(LoginViewModel.LoginError.APPLICATION_ERROR, error);
        assertNotEquals(Boolean.TRUE, isLogged);
    }

    @Test
    public void login_twice_shouldFailSecond() throws Exception {
        viewModel.loginUser(TEST_EMAIL, TEST_PASSWORD);
        Boolean isLogged = LiveDataTestUtil.getValue(viewModel.isLogged());
        assertEquals(Boolean.TRUE, isLogged);
        viewModel.loginUser(TEST_EMAIL, TEST_PASSWORD);
        LoginViewModel.LoginError error = LiveDataTestUtil.getValue(viewModel.getLoginError());
        assertEquals(LoginViewModel.LoginError.ALREADY_LOGGED, error);
    }

    @Test
    public void logout_withoutLogin_shouldNotCrash() throws Exception {
        viewModel.logout();
        Boolean isLogged = LiveDataTestUtil.getValue(viewModel.isLogged());
        assertNotEquals(Boolean.TRUE, isLogged);
        String userEmail = LiveDataTestUtil.getValue(viewModel.getCurrentUserEmail());
        assertNull(userEmail);
        LoginViewModel.LoginError error = LiveDataTestUtil.getValue(viewModel.getLoginError());
        assertNull(error);
    }

    @Test
    public void register_and_login_flow() throws Exception {
        viewModel.logout();
        String newEmail = "nuevo_login_test@example.com";
        String newPassword = "Test5678!";
        String newName = "Nuevo Test";
        CountDownLatch latch = new CountDownLatch(1);
        repository.register(newName, newEmail, newPassword, new LatchCallback<>(latch));
        latch.await(TIMEOUT, TimeUnit.SECONDS);
        viewModel.logout();
        viewModel.loginUser(newEmail, newPassword);
        Boolean isLogged = LiveDataTestUtil.getOrAwaitValue(
            viewModel.isLogged(),
            Boolean.TRUE
        );
        String userEmail = LiveDataTestUtil.getValue(viewModel.getCurrentUserEmail());
        LoginViewModel.LoginError error = LiveDataTestUtil.getValue(viewModel.getLoginError());
        assertEquals(Boolean.TRUE, isLogged);
        assertEquals(newEmail, userEmail);
        assertNull(error);
        CountDownLatch latch2 = new CountDownLatch(1);
        repository.deleteUser(newEmail, newPassword, new LatchCallback<>(latch2));
        latch2.await(TIMEOUT, TimeUnit.SECONDS);
    }
}

