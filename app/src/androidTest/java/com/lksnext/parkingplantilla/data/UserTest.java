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
public class UserTest {
    private DataRepository dataRepository;
    private final int TIMEOUT = 10; // segundos
    private final String testEmail = "user_test@example.com";
    private final String testPassword = "Test1234!";
    private final String testName = "User Test";

    @Before
    public void setUp() throws InterruptedException {
        dataRepository = ParkingApplication.getRepository();
        // Registrar usuario para pruebas
        CountDownLatch latch = new CountDownLatch(1);
        dataRepository.register(testName, testEmail, testPassword, new DataCallback<User>() {
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
        dataRepository.deleteUser(testEmail, new DataCallback<Boolean>() {
            @Override
            public void onSuccess(Boolean result) { latch.countDown(); }
            @Override
            public void onFailure(Exception e) { latch.countDown(); }
        });
        latch.await(TIMEOUT, TimeUnit.SECONDS);
    }

    @Test
    public void testLogout_ClearsSession() throws InterruptedException {
        // Comprobar que el usuario está logueado tras el registro
        Assert.assertTrue(dataRepository.isUserLoggedIn());
        User user = dataRepository.getCurrentUser();
        Assert.assertNotNull(user);
        Assert.assertEquals(testEmail, user.getEmail());
        // Hacer logout
        dataRepository.logout();
        // Comprobar que ya no está logueado
        Assert.assertFalse(dataRepository.isUserLoggedIn());
        Assert.assertNull(dataRepository.getCurrentUser());
    }

    @Test
    public void testLogin_AfterLogout() throws InterruptedException {
        // Hacer logout primero
        dataRepository.logout();
        Assert.assertFalse(dataRepository.isUserLoggedIn());
        // Login de nuevo
        CountDownLatch latch = new CountDownLatch(1);
        final User[] userResult = new User[1];
        final Exception[] error = new Exception[1];
        dataRepository.login(testEmail, testPassword, new DataCallback<User>() {
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
        Assert.assertNull("No debe haber error al hacer login", error[0]);
        Assert.assertNotNull("El usuario debe haberse logueado", userResult[0]);
        Assert.assertTrue(dataRepository.isUserLoggedIn());
        Assert.assertEquals(testEmail, dataRepository.getCurrentUser().getEmail());
    }

    @Test
    public void testLogin_FailsIfAlreadyLoggedIn() throws InterruptedException {
        // Asegurarse de estar logueado
        Assert.assertTrue(dataRepository.isUserLoggedIn());
        // Intentar login de nuevo sin logout
        CountDownLatch latch = new CountDownLatch(1);
        final Exception[] error = new Exception[1];
        dataRepository.login(testEmail, testPassword, new DataCallback<User>() {
            @Override
            public void onSuccess(User user) { latch.countDown(); }
            @Override
            public void onFailure(Exception e) {
                error[0] = e;
                latch.countDown();
            }
        });
        latch.await(TIMEOUT, TimeUnit.SECONDS);
        Assert.assertNotNull("Debe fallar si ya hay sesión activa", error[0]);
        Assert.assertTrue(error[0] instanceof IllegalStateException);
    }
}
