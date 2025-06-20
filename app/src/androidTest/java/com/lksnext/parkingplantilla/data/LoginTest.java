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
public class LoginTest {
    private DataRepository dataRepository;
    private final int TIMEOUT = 10; // segundos
    private final String validEmail = "login_test_user@example.com";
    private final String validPassword = "Test1234!";
    private final String validName = "Login Test";

    @Before
    public void setUp() throws InterruptedException {
        dataRepository = ParkingApplication.getRepository();
        // Registrar usuario para pruebas de login
        CountDownLatch latch = new CountDownLatch(1);
        dataRepository.register(validName, validEmail, validPassword, new DataCallback<User>() {
            @Override
            public void onSuccess(User user) { latch.countDown(); }
            @Override
            public void onFailure(Exception e) { latch.countDown(); }
        });
        latch.await(TIMEOUT, TimeUnit.SECONDS);
        dataRepository.logout();
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
    public void testLogin_Success() throws InterruptedException {
        CountDownLatch latch = new CountDownLatch(1);
        final User[] userResult = new User[1];
        final Exception[] error = new Exception[1];
        dataRepository.login(validEmail, validPassword, new DataCallback<User>() {
            @Override
            public void onSuccess(User user) {
                userResult[0] = user;
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
        Assert.assertNotNull("El usuario debe haberse logueado", userResult[0]);
        Assert.assertEquals(validEmail, userResult[0].getEmail());
    }

    @Test
    public void testLogin_WrongPassword() throws InterruptedException {
        CountDownLatch latch = new CountDownLatch(1);
        final Exception[] error = new Exception[1];
        dataRepository.login(validEmail, "WrongPassword", new DataCallback<User>() {
            @Override
            public void onSuccess(User user) { latch.countDown(); }
            @Override
            public void onFailure(Exception e) {
                error[0] = e;
                latch.countDown();
            }
        });
        latch.await(TIMEOUT, TimeUnit.SECONDS);
        Assert.assertNotNull("Debe fallar por contraseña incorrecta", error[0]);
    }

    @Test
    public void testLogin_NonExistentEmail() throws InterruptedException {
        CountDownLatch latch = new CountDownLatch(1);
        final Exception[] error = new Exception[1];
        dataRepository.login("noexiste@example.com", validPassword, new DataCallback<User>() {
            @Override
            public void onSuccess(User user) { latch.countDown(); }
            @Override
            public void onFailure(Exception e) {
                error[0] = e;
                latch.countDown();
            }
        });
        latch.await(TIMEOUT, TimeUnit.SECONDS);
        Assert.assertNotNull("Debe fallar por email inexistente", error[0]);
    }
}

