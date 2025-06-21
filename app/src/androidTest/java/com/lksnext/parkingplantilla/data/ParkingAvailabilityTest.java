package com.lksnext.parkingplantilla.data;

import androidx.test.ext.junit.runners.AndroidJUnit4;
import com.lksnext.parkingplantilla.ParkingApplication;
import com.lksnext.parkingplantilla.domain.DataCallback;
import com.lksnext.parkingplantilla.domain.Hora;
import com.lksnext.parkingplantilla.domain.Plaza;
import com.lksnext.parkingplantilla.domain.Reserva;
import com.lksnext.parkingplantilla.domain.User;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.time.LocalDate;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

@RunWith(AndroidJUnit4.class)
public class ParkingAvailabilityTest {
    private DataRepository dataRepository;
    private final int TIMEOUT = 15; // segundos
    private final String testUserEmail = "parking_avail_user@example.com";
    private final String testUserPassword = "Test1234!";
    private final String testUserName = "Parking Avail Test";
    private final String testPlazaId1 = "A-1";
    private final String testPlazaId2 = "A-2";
    private final String testPlazaTipo = Plaza.TIPO_STANDARD;
    private final String testFecha = LocalDate.now().toString();
    private final long testHoraInicio = 10 * 60; // 10:00 en minutos
    private final long testHoraFin = 11 * 60; // 11:00 en minutos
    private String reservaCreadaId = null;

    @Before
    public void setUp() throws InterruptedException {
        dataRepository = ParkingApplication.getRepository();
        CountDownLatch latch = new CountDownLatch(3);
        // Registrar usuario
        dataRepository.register(testUserName, testUserEmail, testUserPassword, new DataCallback<User>() {
            @Override
            public void onSuccess(User user) { latch.countDown(); }
            @Override
            public void onFailure(Exception e) { latch.countDown(); }
        });
        // Crear plazas
        dataRepository.addPlaza(new Plaza(testPlazaId1, testPlazaTipo), new DataCallback<Boolean>() {
            @Override
            public void onSuccess(Boolean result) { latch.countDown(); }
            @Override
            public void onFailure(Exception e) { latch.countDown(); }
        });
        dataRepository.addPlaza(new Plaza(testPlazaId2, testPlazaTipo), new DataCallback<Boolean>() {
            @Override
            public void onSuccess(Boolean result) { latch.countDown(); }
            @Override
            public void onFailure(Exception e) { latch.countDown(); }
        });
        latch.await(TIMEOUT, TimeUnit.SECONDS);
    }

    @After
    public void tearDown() throws InterruptedException {
        CountDownLatch latch = new CountDownLatch(3);
        // Borrar reserva creada
        if (reservaCreadaId != null) {
            dataRepository.deleteReserva(reservaCreadaId, new DataCallback<Boolean>() {
                @Override
                public void onSuccess(Boolean result) { latch.countDown(); }
                @Override
                public void onFailure(Exception e) { latch.countDown(); }
            });
        } else {
            latch.countDown();
        }
        // Borrar usuario
        dataRepository.deleteUser(testUserEmail, new DataCallback<Boolean>() {
            @Override
            public void onSuccess(Boolean result) { latch.countDown(); }
            @Override
            public void onFailure(Exception e) { latch.countDown(); }
        });
        // Borrar plazas
        dataRepository.deletePlaza(testPlazaId1, new DataCallback<Boolean>() {
            @Override
            public void onSuccess(Boolean result) { latch.countDown(); }
            @Override
            public void onFailure(Exception e) { latch.countDown(); }
        });
        dataRepository.deletePlaza(testPlazaId2, new DataCallback<Boolean>() {
            @Override
            public void onSuccess(Boolean result) { latch.countDown(); }
            @Override
            public void onFailure(Exception e) { latch.countDown(); }
        });
        latch.await(TIMEOUT, TimeUnit.SECONDS);
    }

    @Test
    public void testAvailablePlazas_AfterReservation() throws InterruptedException {
        // Comprobar que ambas plazas están disponibles
        CountDownLatch latch1 = new CountDownLatch(1);
        dataRepository.getAvailablePlazas(testPlazaTipo, testFecha, testHoraInicio, testHoraFin, new DataCallback<List<String>>() {
            @Override
            public void onSuccess(List<String> plazas) {
                Assert.assertTrue(plazas.contains(testPlazaId1));
                Assert.assertTrue(plazas.contains(testPlazaId2));
                latch1.countDown();
            }
            @Override
            public void onFailure(Exception e) {
                Assert.fail("No debe fallar: " + e.getMessage());
                latch1.countDown();
            }
        });
        latch1.await(TIMEOUT, TimeUnit.SECONDS);

        // Crear reserva en la plaza 1
        CountDownLatch latch2 = new CountDownLatch(1);
        Reserva reserva = new Reserva();
        reserva.setUsuario(testUserEmail);
        reserva.setPlaza(new Plaza(testPlazaId1, testPlazaTipo));
        reserva.setFecha(testFecha);
        reserva.setHora(new Hora(testHoraInicio, testHoraFin));
        reserva.setEstado(Reserva.Estado.ACTIVA);
        dataRepository.createReservation(reserva, new DataCallback<Boolean>() {
            @Override
            public void onSuccess(Boolean result) {
                Assert.assertTrue(result);
                reservaCreadaId = reserva.getId();
                latch2.countDown();
            }
            @Override
            public void onFailure(Exception e) {
                Assert.fail("No debe fallar la creación de reserva: " + e.getMessage());
                latch2.countDown();
            }
        });
        latch2.await(TIMEOUT, TimeUnit.SECONDS);

        // Comprobar que solo la plaza 2 está disponible
        CountDownLatch latch3 = new CountDownLatch(1);
        dataRepository.getAvailablePlazas(testPlazaTipo, testFecha, testHoraInicio, testHoraFin, new DataCallback<List<String>>() {
            @Override
            public void onSuccess(List<String> plazas) {
                Assert.assertFalse(plazas.contains(testPlazaId1));
                Assert.assertTrue(plazas.contains(testPlazaId2));
                latch3.countDown();
            }
            @Override
            public void onFailure(Exception e) {
                Assert.fail("No debe fallar: " + e.getMessage());
                latch3.countDown();
            }
        });
        latch3.await(TIMEOUT, TimeUnit.SECONDS);
    }
}

