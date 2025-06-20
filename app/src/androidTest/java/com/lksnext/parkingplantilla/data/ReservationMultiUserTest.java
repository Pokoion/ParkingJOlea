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
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

@RunWith(AndroidJUnit4.class)
public class ReservationMultiUserTest {
    private DataRepository dataRepository;
    private final int TIMEOUT = 15; // segundos
    private final String testUserEmail = "reserva_test_user@example.com";
    private final String testUserPassword = "Test1234!";
    private final String testUserName = "Reserva Test";
    private final String testUser2Email = "reserva_test_user2@example.com";
    private final String testUser2Password = "Test1234!";
    private final String testUser2Name = "Reserva Test 2";
    private final String testPlazaId = "A-1";
    private final String testPlazaTipo = Plaza.TIPO_STANDARD;
    private final String testFecha = LocalDate.now().toString();
    private final long testHoraInicio = 8 * 60; // 8:00 en minutos
    private final long testHoraFin = 9 * 60; // 9:00 en minutos

    @Before
    public void setUp() throws InterruptedException {
        dataRepository = ParkingApplication.getRepository();
        CountDownLatch latch = new CountDownLatch(3);
        dataRepository.register(testUserName, testUserEmail, testUserPassword, new DataCallback<User>() {
            @Override
            public void onSuccess(User user) { latch.countDown(); }
            @Override
            public void onFailure(Exception e) { latch.countDown(); }
        });
        dataRepository.register(testUser2Name, testUser2Email, testUser2Password, new DataCallback<User>() {
            @Override
            public void onSuccess(User user) { latch.countDown(); }
            @Override
            public void onFailure(Exception e) { latch.countDown(); }
        });
        dataRepository.addPlaza(new Plaza(testPlazaId, testPlazaTipo), new DataCallback<Boolean>() {
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
        dataRepository.deleteUser(testUserEmail, new DataCallback<Boolean>() {
            @Override
            public void onSuccess(Boolean result) { latch.countDown(); }
            @Override
            public void onFailure(Exception e) { latch.countDown(); }
        });
        dataRepository.deleteUser(testUser2Email, new DataCallback<Boolean>() {
            @Override
            public void onSuccess(Boolean result) { latch.countDown(); }
            @Override
            public void onFailure(Exception e) { latch.countDown(); }
        });
        dataRepository.deletePlaza(testPlazaId, new DataCallback<Boolean>() {
            @Override
            public void onSuccess(Boolean result) { latch.countDown(); }
            @Override
            public void onFailure(Exception e) { latch.countDown(); }
        });
        latch.await(TIMEOUT, TimeUnit.SECONDS);
    }

    private boolean crearReserva(String email, String plazaId, String plazaTipo, String fecha, long horaInicio, long horaFin, Reserva.Estado estado) throws InterruptedException {
        CountDownLatch latch = new CountDownLatch(1);
        final boolean[] resultHolder = {false};
        Reserva reserva = new Reserva();
        reserva.setUsuario(email);
        reserva.setPlaza(new Plaza(plazaId, plazaTipo));
        reserva.setFecha(fecha);
        reserva.setHora(new Hora(horaInicio, horaFin));
        reserva.setEstado(estado);
        dataRepository.createReservation(reserva, new DataCallback<Boolean>() {
            @Override
            public void onSuccess(Boolean result) {
                resultHolder[0] = result;
                latch.countDown();
            }
            @Override
            public void onFailure(Exception e) {
                latch.countDown();
            }
        });
        latch.await(TIMEOUT, TimeUnit.SECONDS);
        return resultHolder[0];
    }

    @Test
    public void testTwoUsers_ConflictSameSlot() throws InterruptedException {
        boolean ok1 = crearReserva(testUserEmail, testPlazaId, testPlazaTipo, testFecha, testHoraInicio, testHoraFin, Reserva.Estado.ACTIVA);
        Assert.assertTrue("El primer usuario debe poder reservar", ok1);
        boolean ok2 = crearReserva(testUser2Email, testPlazaId, testPlazaTipo, testFecha, testHoraInicio, testHoraFin, Reserva.Estado.ACTIVA);
        Assert.assertFalse("No debería permitir reservar la misma plaza y hora a dos usuarios", ok2);
    }

    @Test
    public void testTwoUsers_NoConflictDifferentSlots() throws InterruptedException {
        boolean ok1 = crearReserva(testUserEmail, testPlazaId, testPlazaTipo, testFecha, testHoraInicio, testHoraFin, Reserva.Estado.ACTIVA);
        Assert.assertTrue("El primer usuario debe poder reservar", ok1);
        boolean ok2 = crearReserva(testUser2Email, testPlazaId, testPlazaTipo, testFecha, testHoraFin, testHoraFin + 60, Reserva.Estado.ACTIVA);
        Assert.assertTrue("El segundo usuario debe poder reservar en horario no solapado", ok2);
    }

    @Test
    public void testTwoUsers_ConflictPartialOverlap() throws InterruptedException {
        boolean ok1 = crearReserva(testUserEmail, testPlazaId, testPlazaTipo, testFecha, testHoraInicio, testHoraFin, Reserva.Estado.ACTIVA);
        Assert.assertTrue("El primer usuario debe poder reservar", ok1);
        boolean ok2 = crearReserva(testUser2Email, testPlazaId, testPlazaTipo, testFecha, testHoraInicio + 30, testHoraFin + 30, Reserva.Estado.ACTIVA);
        Assert.assertFalse("No debería permitir reservar con solapamiento parcial", ok2);
    }
}
