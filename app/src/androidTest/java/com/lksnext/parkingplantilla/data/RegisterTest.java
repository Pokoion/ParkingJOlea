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
public class RegisterTest {
    private DataRepository dataRepository;
    private final int TIMEOUT = 10; // segundos
    private final String validEmail = "register_test_user@example.com";
    private final String validPassword = "Test1234!";
    private final String validName = "Register Test";

    @Before
    public void setUp() {
        dataRepository = ParkingApplication.getRepository();
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
    public void testRegister_Success() throws InterruptedException {
        CountDownLatch latch = new CountDownLatch(1);
        final User[] userResult = new User[1];
        final Exception[] error = new Exception[1];
        dataRepository.register(validName, validEmail, validPassword, new DataCallback<User>() {
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
        Assert.assertNotNull("El usuario debe haberse creado", userResult[0]);
        Assert.assertEquals(validEmail, userResult[0].getEmail());
    }

    @Test
    public void testRegister_EmailAlreadyExists() throws InterruptedException {
        // Primero registrar el usuario
        CountDownLatch latch1 = new CountDownLatch(1);
        dataRepository.register(validName, validEmail, validPassword, new DataCallback<User>() {
            @Override
            public void onSuccess(User user) { latch1.countDown(); }
            @Override
            public void onFailure(Exception e) { latch1.countDown(); }
        });
        latch1.await(TIMEOUT, TimeUnit.SECONDS);
        // Intentar registrar de nuevo
        CountDownLatch latch2 = new CountDownLatch(1);
        final Exception[] error = new Exception[1];
        dataRepository.register(validName, validEmail, validPassword, new DataCallback<User>() {
            @Override
            public void onSuccess(User user) { latch2.countDown(); }
            @Override
            public void onFailure(Exception e) {
                error[0] = e;
                latch2.countDown();
            }
        });
        latch2.await(TIMEOUT, TimeUnit.SECONDS);
        Assert.assertNotNull("Debe fallar por email existente", error[0]);
    }

    @Test
    public void testRegister_InvalidEmail() throws InterruptedException {
        String invalidEmail = "invalidemail";
        CountDownLatch latch = new CountDownLatch(1);
        final Exception[] error = new Exception[1];
        dataRepository.register(validName, invalidEmail, validPassword, new DataCallback<User>() {
            @Override
            public void onSuccess(User user) { latch.countDown(); }
            @Override
            public void onFailure(Exception e) {
                error[0] = e;
                latch.countDown();
            }
        });
        latch.await(TIMEOUT, TimeUnit.SECONDS);
        Assert.assertNotNull("Debe fallar por email inválido", error[0]);
    }

    @Test
    public void testRegister_WeakPassword() throws InterruptedException {
        String weakPassword = "123";
        CountDownLatch latch = new CountDownLatch(1);
        final Exception[] error = new Exception[1];
        dataRepository.register(validName, "weakpass_test@example.com", weakPassword, new DataCallback<User>() {
            @Override
            public void onSuccess(User user) { latch.countDown(); }
            @Override
            public void onFailure(Exception e) {
                error[0] = e;
                latch.countDown();
            }
        });
        latch.await(TIMEOUT, TimeUnit.SECONDS);
        Assert.assertNotNull("Debe fallar por contraseña débil", error[0]);
    }
}
