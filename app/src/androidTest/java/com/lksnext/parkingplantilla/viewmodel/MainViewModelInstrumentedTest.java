package com.lksnext.parkingplantilla.viewmodel;

import androidx.arch.core.executor.testing.InstantTaskExecutorRule;
import androidx.test.ext.junit.runners.AndroidJUnit4;

import com.lksnext.parkingplantilla.ParkingApplication;
import com.lksnext.parkingplantilla.data.DataRepository;
import com.lksnext.parkingplantilla.domain.User;
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
public class MainViewModelInstrumentedTest {
    @Rule
    public InstantTaskExecutorRule instantTaskExecutorRule = new InstantTaskExecutorRule();

    private MainViewModel viewModel;
    private DataRepository repository;
    private static final String TEST_EMAIL = "mainvm_test_user@example.com";
    private static final String TEST_PASSWORD = "Test1234!";
    private static final String TEST_NAME = "MainVM Test";
    private static final int TIMEOUT = 10;

    @Before
    public void setUp() throws Exception {
        repository = ParkingApplication.getRepository();
        // Registrar usuario de test
        CountDownLatch latch = new CountDownLatch(1);
        repository.register(TEST_NAME, TEST_EMAIL, TEST_PASSWORD, new LatchCallback<>(latch));
        latch.await(TIMEOUT, TimeUnit.SECONDS);
        viewModel = new MainViewModel(repository);
    }

    @After
    public void tearDown() throws Exception {
        // Eliminar usuario de test
        CountDownLatch latch = new CountDownLatch(1);
        repository.deleteUser(TEST_EMAIL, TEST_PASSWORD, new LatchCallback<>(latch));
        latch.await(TIMEOUT, TimeUnit.SECONDS);
    }

    @Test
    public void getCurrentUser_returnsUser() throws Exception{
        User user = LiveDataTestUtil.getValue(viewModel.getCurrentUser());
        assertNotNull(user);
        assertEquals(TEST_EMAIL, user.getEmail());
    }

    @Test
    public void checkCurrentUserExists_true() throws Exception {
        viewModel.checkCurrentUserExists();
        Boolean exists = LiveDataTestUtil.getValue(viewModel.getUserExists());
        assertTrue(Boolean.TRUE.equals(exists));
    }

    @Test
    public void checkCurrentUserExists_falseIfNoUser() throws Exception {
        repository.logout();
        viewModel.checkCurrentUserExists();
        Boolean exists = LiveDataTestUtil.getValue(viewModel.getUserExists());
        assertFalse(Boolean.TRUE.equals(exists));
    }
}
