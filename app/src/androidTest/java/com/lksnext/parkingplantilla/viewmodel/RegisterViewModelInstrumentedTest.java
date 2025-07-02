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
public class RegisterViewModelInstrumentedTest {
    @Rule
    public InstantTaskExecutorRule instantTaskExecutorRule = new InstantTaskExecutorRule();

    private RegisterViewModel viewModel;
    private DataRepository repository;

    private static final String TEST_EMAIL = "register_test_user@example.com";
    private static final String TEST_PASSWORD = "Test1234!";
    private static final String TEST_NAME = "Register Test";
    private static final int TIMEOUT = 10;

    @Before
    public void setUp() throws Exception {
        repository = ParkingApplication.getInstance().getRepository();
        viewModel = new RegisterViewModel(repository);
        // Asegurarse de que el usuario no exista antes de cada test
        CountDownLatch latch = new CountDownLatch(1);
        repository.deleteUser(TEST_EMAIL, TEST_PASSWORD, new LatchCallback<>(latch));
        latch.await(TIMEOUT, TimeUnit.SECONDS);
    }

    @After
    public void tearDown() throws Exception {
        CountDownLatch loginLatch = new CountDownLatch(1);
        repository.login(TEST_EMAIL, TEST_PASSWORD, new LatchCallback<>(loginLatch));
        loginLatch.await(TIMEOUT, TimeUnit.SECONDS);
        CountDownLatch latch = new CountDownLatch(1);
        repository.deleteUser(TEST_EMAIL, TEST_PASSWORD, new LatchCallback<>(latch));
        latch.await(TIMEOUT, TimeUnit.SECONDS);
    }

    @Test
    public void register_success() throws Exception {
        viewModel.register(TEST_EMAIL, TEST_NAME, TEST_PASSWORD);
        Boolean success = LiveDataTestUtil.getValue(viewModel.getRegistrationSuccess());
        RegisterViewModel.RegisterError error = viewModel.getRegisterError().getValue();
        assertEquals(Boolean.TRUE, success);
        assertNull(error);
    }

    @Test
    public void register_invalidEmail() throws Exception {
        viewModel.register("invalidemail", TEST_NAME, TEST_PASSWORD);
        RegisterViewModel.RegisterError error = LiveDataTestUtil.getValue(viewModel.getRegisterError());
        Boolean success = viewModel.getRegistrationSuccess().getValue();
        assertEquals(RegisterViewModel.RegisterError.INVALID_EMAIL, error);
        assertNotEquals(Boolean.TRUE, success);
    }

    @Test
    public void register_emptyUsername() throws Exception {
        viewModel.register(TEST_EMAIL, "", TEST_PASSWORD);
        RegisterViewModel.RegisterError error = LiveDataTestUtil.getValue(viewModel.getRegisterError());
        Boolean success = viewModel.getRegistrationSuccess().getValue();
        assertEquals(RegisterViewModel.RegisterError.USERNAME_EMPTY, error);
        assertNotEquals(Boolean.TRUE, success);
    }

    @Test
    public void register_shortPassword() throws Exception {
        viewModel.register(TEST_EMAIL, TEST_NAME, "123");
        RegisterViewModel.RegisterError error = LiveDataTestUtil.getValue(viewModel.getRegisterError());
        Boolean success = viewModel.getRegistrationSuccess().getValue();
        assertEquals(RegisterViewModel.RegisterError.PASSWORD_TOO_SHORT, error);
        assertNotEquals(Boolean.TRUE, success);
    }

    @Test
    public void register_emailAlreadyExists() throws Exception {
        // Primer registro y espera a que termine
        viewModel.register(TEST_EMAIL, TEST_NAME, TEST_PASSWORD);
        Boolean success = LiveDataTestUtil.getValue(viewModel.getRegistrationSuccess());
        assertEquals(Boolean.TRUE, success);
        // Segundo registro, ahora sí debería dar error
        viewModel.register(TEST_EMAIL, TEST_NAME, TEST_PASSWORD);
        RegisterViewModel.RegisterError error = LiveDataTestUtil.getValue(viewModel.getRegisterError());
        assertEquals(RegisterViewModel.RegisterError.EMAIL_ALREADY_EXISTS, error);
    }

    @Test
    public void register_applicationError() throws Exception {
        RegisterViewModel vm = new RegisterViewModel(null);
        vm.register(TEST_EMAIL, TEST_NAME, TEST_PASSWORD);
        RegisterViewModel.RegisterError error = LiveDataTestUtil.getValue(vm.getRegisterError());
        Boolean success = vm.getRegistrationSuccess().getValue();
        assertEquals(RegisterViewModel.RegisterError.APPLICATION_ERROR, error);
        assertNotEquals(Boolean.TRUE, success);
    }

    @Test
    public void validateRegisterFields() {
        // Email inválido
        assertFalse(viewModel.validateRegisterFields("bademail", TEST_NAME, TEST_PASSWORD));
        assertEquals(RegisterViewModel.RegisterError.INVALID_EMAIL, viewModel.getRegisterError().getValue());
        // Username vacío
        assertFalse(viewModel.validateRegisterFields(TEST_EMAIL, "", TEST_PASSWORD));
        assertEquals(RegisterViewModel.RegisterError.USERNAME_EMPTY, viewModel.getRegisterError().getValue());
        // Password corta
        assertFalse(viewModel.validateRegisterFields(TEST_EMAIL, TEST_NAME, "123"));
        assertEquals(RegisterViewModel.RegisterError.PASSWORD_TOO_SHORT, viewModel.getRegisterError().getValue());

        assertTrue(viewModel.validateRegisterFields(TEST_EMAIL, TEST_NAME, TEST_PASSWORD));
        assertEquals(RegisterViewModel.RegisterError.PASSWORD_TOO_SHORT, viewModel.getRegisterError().getValue()); // No cambia a NONE hasta llamar a register
    }
}

