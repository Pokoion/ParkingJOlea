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
public class ChangePassViewModelInstrumentedTest {
    @Rule
    public InstantTaskExecutorRule instantTaskExecutorRule = new InstantTaskExecutorRule();

    private ChangePassViewModel viewModel;
    private DataRepository repository;

    private static final String TEST_EMAIL = "changepass_test_user@example.com";
    private static final String TEST_PASSWORD = "Test1234!";
    private static final String TEST_NAME = "ChangePass Test";
    private static final int TIMEOUT = 10;

    @Before
    public void setUp() throws Exception {
        repository = ParkingApplication.getInstance().getRepository();
        viewModel = new ChangePassViewModel(repository);
        // Registrar usuario de test
        CountDownLatch latch = new CountDownLatch(1);
        repository.register(TEST_NAME, TEST_EMAIL, TEST_PASSWORD, new LatchCallback<>(latch));
        latch.await(TIMEOUT, TimeUnit.SECONDS);
    }

    @After
    public void tearDown() throws Exception {
        // Eliminar usuario de test
        CountDownLatch latch = new CountDownLatch(1);
        repository.deleteUser(TEST_EMAIL, TEST_PASSWORD, new LatchCallback<>(latch));
        latch.await(TIMEOUT, TimeUnit.SECONDS);
    }

    @Test
    public void sendPasswordResetEmail_success() throws Exception {
        viewModel.sendPasswordResetEmail(TEST_EMAIL);
        String status = LiveDataTestUtil.getValue(viewModel.getStatusMessage());
        Boolean loading = LiveDataTestUtil.getOrAwaitValue(viewModel.getIsLoading(), Boolean.FALSE);
        assertEquals("Correo de recuperación enviado. Revisa tu bandeja de entrada.", status);
        assertNotEquals(Boolean.TRUE, loading);
    }

    @Test
    public void sendPasswordResetEmail_emailNotRegistered() throws Exception {
        String fakeEmail = "noexistechangepass@example.com";
        viewModel.sendPasswordResetEmail(fakeEmail);
        String status = LiveDataTestUtil.getValue(viewModel.getStatusMessage());
        Boolean loading = LiveDataTestUtil.getOrAwaitValue(viewModel.getIsLoading(), Boolean.FALSE);
        assertEquals("El email no está registrado.", status);
        assertNotEquals(Boolean.TRUE, loading);
    }

    @Test
    public void sendPasswordResetEmail_repositoryNull() throws Exception {
        ChangePassViewModel vm = new ChangePassViewModel(null);
        vm.sendPasswordResetEmail(TEST_EMAIL);
        String status = LiveDataTestUtil.getValue(vm.getStatusMessage());
        assertNull(status);
    }
}

