package com.lksnext.parkingplantilla.data.firebase;

import androidx.arch.core.executor.testing.InstantTaskExecutorRule;
import androidx.test.ext.junit.runners.AndroidJUnit4;

import com.lksnext.parkingplantilla.domain.DataCallback;
import com.lksnext.parkingplantilla.domain.User;

import org.junit.Assert;
import org.junit.Before;
import org.junit.After;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

@RunWith(AndroidJUnit4.class)
public class FirebaseDataSourceTest {
    private FirebaseDataSource firebaseDataSource;
    private final String testEmail = "testuser_firebase_test@example.com";
    private final String testPassword = "Test1234!";
    private final String testName = "Test User";
    private final int TIMEOUT = 10; // segundos

    @Before
    public void setUp() throws InterruptedException {
        firebaseDataSource = new FirebaseDataSource();
        CountDownLatch latch = new CountDownLatch(1);
        // Intentar crear el usuario (si ya existe, ignorar el error)
        firebaseDataSource.register(testName, testEmail, testPassword, new DataCallback<User>() {
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
        // Login para poder borrar el usuario autenticado
        firebaseDataSource.login(testEmail, testPassword, new DataCallback<User>() {
            @Override
            public void onSuccess(User user) {
                com.google.firebase.auth.FirebaseAuth.getInstance().getCurrentUser().delete()
                    .addOnCompleteListener(task -> {
                        // Además, borrar de la colección users y reservas
                        firebaseDataSource.deleteUser(testEmail, new DataCallback<Boolean>() {
                            @Override
                            public void onSuccess(Boolean result) { latch.countDown(); }
                            @Override
                            public void onFailure(Exception e) { latch.countDown(); }
                        });
                    });
            }
            @Override
            public void onFailure(Exception e) { latch.countDown(); }
        });
        latch.await(TIMEOUT, TimeUnit.SECONDS);
    }

    @Test
    public void testCheckUserExists_returnsTrue() throws InterruptedException {
        CountDownLatch latch = new CountDownLatch(1);
        final boolean[] existsResult = new boolean[1];
        final Exception[] error = new Exception[1];
        firebaseDataSource.checkUserExists(testEmail, new DataCallback<Boolean>() {
            @Override
            public void onSuccess(Boolean exists) {
                existsResult[0] = exists;
                latch.countDown();
            }
            @Override
            public void onFailure(Exception e) {
                error[0] = e;
                latch.countDown();
            }
        });
        latch.await(TIMEOUT, TimeUnit.SECONDS);
        if (error[0] != null) {
            Assert.fail("Error en el test: " + error[0].getMessage());
        }
        Assert.assertTrue(existsResult[0]);
    }
}

