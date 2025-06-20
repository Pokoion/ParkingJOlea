package com.lksnext.parkingplantilla.data;

import androidx.test.ext.junit.runners.AndroidJUnit4;
import com.lksnext.parkingplantilla.ParkingApplication;
import com.lksnext.parkingplantilla.domain.DataCallback;
import com.lksnext.parkingplantilla.domain.User;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

@RunWith(AndroidJUnit4.class)
public class PasswordResetTest {
    private DataRepository dataRepository;
    private final int TIMEOUT = 10; // segundos
    private final String validEmail = "reset_test_user@example.com";
    private final String validPassword = "Test1234!";
    private final String validName = "Reset Test";

    @Before
    public void setUp() throws InterruptedException {
        dataRepository = ParkingApplication.getRepository();
        // Registrar usuario para pruebas de reset
        CountDownLatch latch = new CountDownLatch(1);
        dataRepository.register(validName, validEmail, validPassword, new DataCallback<User>() {
            @Override
            public void onSuccess(User user) { latch.countDown(); }
            @Override
            public void onFailure(Exception e) { latch.countDown(); }
        });
        latch.await(TIMEOUT, TimeUnit.SECONDS);
    }

    @After
    public void tearDown() throws InterruptedException {
        CountDownLatch latch = new CountDownLatch(1);
        dataRepository.deleteUser(validEmail, new DataCallback<Boolean>() {
            @Override
            public void onSuccess(Boolean result) { latch.countDown(); }
            @Override
            public void onFailure(Exception e) { latch.countDown(); }
        });
        latch.await(TIMEOUT, TimeUnit.SECONDS);
    }

    @Test
    public void testSendPasswordResetEmail_Success() throws InterruptedException {
        CountDownLatch latch = new CountDownLatch(1);
        final Boolean[] result = new Boolean[1];
        final Exception[] error = new Exception[1];
        dataRepository.sendPasswordResetEmail(validEmail, new DataCallback<Boolean>() {
            @Override
            public void onSuccess(Boolean value) {
                result[0] = value;
                latch.countDown();
            }
            @Override
            public void onFailure(Exception e) {
                error[0] = e;
                latch.countDown();
            }
        });
        latch.await(TIMEOUT, TimeUnit.SECONDS);
        Assert.assertNull("No debe haber error", error[0]);
        Assert.assertTrue("El email de reseteo debe enviarse", result[0]);
    }

    @Test
    public void testSendPasswordResetEmail_NonExistentUser() throws InterruptedException {
        CountDownLatch latch = new CountDownLatch(1);
        final Boolean[] result = new Boolean[1];
        final Exception[] error = new Exception[1];
        dataRepository.sendPasswordResetEmail("noexiste@example.com", new DataCallback<Boolean>() {
            @Override
            public void onSuccess(Boolean value) {
                result[0] = value;
                latch.countDown();
            }
            @Override
            public void onFailure(Exception e) {
                error[0] = e;
                latch.countDown();
            }
        });
        latch.await(TIMEOUT, TimeUnit.SECONDS);
        Assert.assertNull("No debe haber error", error[0]);
        Assert.assertFalse("No debe enviar email si el usuario no existe", result[0]);
    }
}

