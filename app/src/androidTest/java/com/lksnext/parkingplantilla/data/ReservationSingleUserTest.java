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
public class ReservationSingleUserTest {
    private DataRepository dataRepository;
    private final int TIMEOUT = 15; // segundos
    private final String testUserEmail = "reserva_test_user@example.com";
    private final String testUserPassword = "Test1234!";
    private final String testUserName = "Reserva Test";
    private final String testPlazaId = "A-1";
    private final String testPlazaTipo = Plaza.TIPO_STANDARD;
    private final String testFecha = LocalDate.now().toString();
    private final long testHoraInicio = 8 * 60; // 8:00 en minutos
    private final long testHoraFin = 9 * 60; // 9:00 en minutos
    private String reservaCreadaId = null;

    @Before
    public void setUp() throws InterruptedException {
        dataRepository = ParkingApplication.getRepository();
        CountDownLatch latch = new CountDownLatch(2);
        // Registrar usuario
        dataRepository.register(testUserName, testUserEmail, testUserPassword, new DataCallback<User>() {
            @Override
            public void onSuccess(User user) { latch.countDown(); }
            @Override
            public void onFailure(Exception e) { latch.countDown(); }
        });
        // Crear plaza
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
        // Borrar plaza
        dataRepository.deletePlaza(testPlazaId, new DataCallback<Boolean>() {
            @Override
            public void onSuccess(Boolean result) { latch.countDown(); }
            @Override
            public void onFailure(Exception e) { latch.countDown(); }
        });
        latch.await(TIMEOUT, TimeUnit.SECONDS);
    }

    @Test
    public void testCreateReservation_Success() throws InterruptedException {
        CountDownLatch latch = new CountDownLatch(1);
        Reserva reserva = new Reserva();
        reserva.setUsuario(testUserEmail);
        reserva.setPlaza(new Plaza(testPlazaId, testPlazaTipo));
        reserva.setFecha(testFecha);
        reserva.setHora(new Hora(testHoraInicio, testHoraFin));
        reserva.setEstado(Reserva.Estado.ACTIVA);
        dataRepository.createReservation(reserva, new DataCallback<Boolean>() {
            @Override
            public void onSuccess(Boolean result) {
                Assert.assertTrue(result);
                reservaCreadaId = reserva.getId();
                latch.countDown();
            }
            @Override
            public void onFailure(Exception e) {
                Assert.fail("No debe fallar la creación de reserva: " + e.getMessage());
                latch.countDown();
            }
        });
        latch.await(TIMEOUT, TimeUnit.SECONDS);
    }

    @Test
    public void testGetReservations_Active() throws InterruptedException {
        // Crear reserva activa
        CountDownLatch latch1 = new CountDownLatch(1);
        Reserva reserva = new Reserva();
        reserva.setUsuario(testUserEmail);
        reserva.setPlaza(new Plaza(testPlazaId, testPlazaTipo));
        reserva.setFecha(testFecha);
        reserva.setHora(new Hora(testHoraInicio, testHoraFin));
        reserva.setEstado(Reserva.Estado.ACTIVA);
        dataRepository.createReservation(reserva, new DataCallback<Boolean>() {
            @Override
            public void onSuccess(Boolean result) {
                reservaCreadaId = reserva.getId();
                latch1.countDown();
            }
            @Override
            public void onFailure(Exception e) { latch1.countDown(); }
        });
        latch1.await(TIMEOUT, TimeUnit.SECONDS);
        // Obtener reservas activas
        CountDownLatch latch2 = new CountDownLatch(1);
        dataRepository.getReservations(testUserEmail, new DataCallback<List<Reserva>>() {
            @Override
            public void onSuccess(List<Reserva> reservas) {
                Assert.assertFalse(reservas.isEmpty());
                boolean found = false;
                for (Reserva r : reservas) {
                    if (r.getPlaza().getId().equals(testPlazaId) && r.getFecha().equals(testFecha)) {
                        found = true;
                        break;
                    }
                }
                Assert.assertTrue("La reserva creada debe estar en la lista", found);
                latch2.countDown();
            }
            @Override
            public void onFailure(Exception e) {
                Assert.fail("No debe fallar: " + e.getMessage());
                latch2.countDown();
            }
        });
        latch2.await(TIMEOUT, TimeUnit.SECONDS);
    }

    @Test
    public void testGetReservations_Empty() throws InterruptedException {
        // Eliminar todas las reservas del usuario
        CountDownLatch latch1 = new CountDownLatch(1);
        dataRepository.deleteUserReservations(testUserEmail, new DataCallback<Boolean>() {
            @Override
            public void onSuccess(Boolean result) { latch1.countDown(); }
            @Override
            public void onFailure(Exception e) { latch1.countDown(); }
        });
        latch1.await(TIMEOUT, TimeUnit.SECONDS);
        // Obtener reservas activas
        CountDownLatch latch2 = new CountDownLatch(1);
        dataRepository.getReservations(testUserEmail, new DataCallback<List<Reserva>>() {
            @Override
            public void onSuccess(List<Reserva> reservas) {
                Assert.assertTrue(reservas.isEmpty());
                latch2.countDown();
            }
            @Override
            public void onFailure(Exception e) {
                Assert.fail("No debe fallar: " + e.getMessage());
                latch2.countDown();
            }
        });
        latch2.await(TIMEOUT, TimeUnit.SECONDS);
    }

    @Test
    public void testGetHistoricReservations() throws InterruptedException {
        // Crear reserva finalizada (ayer)
        String yesterday = new java.text.SimpleDateFormat("yyyy-MM-dd").format(new java.util.Date(System.currentTimeMillis() - 24*60*60*1000));
        CountDownLatch latch1 = new CountDownLatch(1);
        Reserva reserva = new Reserva();
        reserva.setUsuario(testUserEmail);
        reserva.setPlaza(new Plaza(testPlazaId, testPlazaTipo));
        reserva.setFecha(yesterday);
        reserva.setHora(new Hora(testHoraInicio, testHoraFin));
        reserva.setEstado(Reserva.Estado.FINALIZADA);
        dataRepository.createReservation(reserva, new DataCallback<Boolean>() {
            @Override
            public void onSuccess(Boolean result) {
                reservaCreadaId = reserva.getId();
                latch1.countDown();
            }
            @Override
            public void onFailure(Exception e) { latch1.countDown(); }
        });
        latch1.await(TIMEOUT, TimeUnit.SECONDS);
        // Obtener reservas históricas
        CountDownLatch latch2 = new CountDownLatch(1);
        dataRepository.getHistoricReservations(testUserEmail, new DataCallback<List<Reserva>>() {
            @Override
            public void onSuccess(List<Reserva> reservas) {
                boolean found = false;
                for (Reserva r : reservas) {
                    if (r.getPlaza().getId().equals(testPlazaId) && r.getFecha().equals(yesterday)) {
                        found = true;
                        break;
                    }
                }
                Assert.assertTrue("La reserva finalizada debe estar en el histórico", found);
                latch2.countDown();
            }
            @Override
            public void onFailure(Exception e) {
                Assert.fail("No debe fallar: " + e.getMessage());
                latch2.countDown();
            }
        });
        latch2.await(TIMEOUT, TimeUnit.SECONDS);
    }

    @Test
    public void testDeleteReservation() throws InterruptedException {
        // Crear reserva activa
        CountDownLatch latch1 = new CountDownLatch(1);
        Reserva reserva = new Reserva();
        reserva.setUsuario(testUserEmail);
        reserva.setPlaza(new Plaza(testPlazaId, testPlazaTipo));
        reserva.setFecha(testFecha);
        reserva.setHora(new Hora(testHoraInicio, testHoraFin));
        reserva.setEstado(Reserva.Estado.ACTIVA);
        dataRepository.createReservation(reserva, new DataCallback<Boolean>() {
            @Override
            public void onSuccess(Boolean result) {
                reservaCreadaId = reserva.getId();
                latch1.countDown();
            }
            @Override
            public void onFailure(Exception e) { latch1.countDown(); }
        });
        latch1.await(TIMEOUT, TimeUnit.SECONDS);
        // Cancelar la reserva
        CountDownLatch latch2 = new CountDownLatch(1);
        dataRepository.deleteReservation(reservaCreadaId, new DataCallback<Boolean>() {
            @Override
            public void onSuccess(Boolean result) {
                Assert.assertTrue(result);
                latch2.countDown();
            }
            @Override
            public void onFailure(Exception e) {
                Assert.fail("No debe fallar: " + e.getMessage());
                latch2.countDown();
            }
        });
        latch2.await(TIMEOUT, TimeUnit.SECONDS);
        // Comprobar que ya no está activa
        CountDownLatch latch3 = new CountDownLatch(1);
        dataRepository.getReservations(testUserEmail, new DataCallback<List<Reserva>>() {
            @Override
            public void onSuccess(List<Reserva> reservas) {
                boolean found = false;
                for (Reserva r : reservas) {
                    if (r.getId().equals(reservaCreadaId)) {
                        found = true;
                        break;
                    }
                }
                Assert.assertFalse("La reserva cancelada no debe estar activa", found);
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

    @Test
    public void testUpdateReservation() throws InterruptedException {
        // Crear reserva activa
        CountDownLatch latch1 = new CountDownLatch(1);
        Reserva reserva = new Reserva();
        reserva.setUsuario(testUserEmail);
        reserva.setPlaza(new Plaza(testPlazaId, testPlazaTipo));
        reserva.setFecha(testFecha);
        reserva.setHora(new Hora(testHoraInicio, testHoraFin));
        reserva.setEstado(Reserva.Estado.ACTIVA);
        dataRepository.createReservation(reserva, new DataCallback<Boolean>() {
            @Override
            public void onSuccess(Boolean result) {
                reservaCreadaId = reserva.getId();
                latch1.countDown();
            }
            @Override
            public void onFailure(Exception e) { latch1.countDown(); }
        });
        latch1.await(TIMEOUT, TimeUnit.SECONDS);
        // Actualizar la reserva (cambiar hora)
        CountDownLatch latch2 = new CountDownLatch(1);
        reserva.setHora(new Hora(testHoraInicio + 60, testHoraFin + 60)); // 9:00-10:00
        dataRepository.updateReservation(reserva, new DataCallback<Boolean>() {
            @Override
            public void onSuccess(Boolean result) {
                Assert.assertTrue(result);
                latch2.countDown();
            }
            @Override
            public void onFailure(Exception e) {
                Assert.fail("No debe fallar la actualización: " + e.getMessage());
                latch2.countDown();
            }
        });
        latch2.await(TIMEOUT, TimeUnit.SECONDS);
    }

    @Test
    public void testCheckAvailability() throws InterruptedException {
        // Crear reserva activa
        CountDownLatch latch1 = new CountDownLatch(1);
        Reserva reserva = new Reserva();
        reserva.setUsuario(testUserEmail);
        reserva.setPlaza(new Plaza(testPlazaId, testPlazaTipo));
        reserva.setFecha(testFecha);
        reserva.setHora(new Hora(testHoraInicio, testHoraFin));
        reserva.setEstado(Reserva.Estado.ACTIVA);
        dataRepository.createReservation(reserva, new DataCallback<Boolean>() {
            @Override
            public void onSuccess(Boolean result) {
                reservaCreadaId = reserva.getId();
                latch1.countDown();
            }
            @Override
            public void onFailure(Exception e) { latch1.countDown(); }
        });
        latch1.await(TIMEOUT, TimeUnit.SECONDS);
        // Comprobar disponibilidad (debe ser false)
        CountDownLatch latch2 = new CountDownLatch(1);
        dataRepository.checkAvailability(reserva, new DataCallback<Boolean>() {
            @Override
            public void onSuccess(Boolean available) {
                Assert.assertFalse(available);
                latch2.countDown();
            }
            @Override
            public void onFailure(Exception e) {
                Assert.fail("No debe fallar: " + e.getMessage());
                latch2.countDown();
            }
        });
        latch2.await(TIMEOUT, TimeUnit.SECONDS);
    }

    @Test
    public void testHasReservationOnDate() throws InterruptedException {
        // Crear reserva activa
        CountDownLatch latch1 = new CountDownLatch(1);
        Reserva reserva = new Reserva();
        reserva.setUsuario(testUserEmail);
        reserva.setPlaza(new Plaza(testPlazaId, testPlazaTipo));
        reserva.setFecha(testFecha);
        reserva.setHora(new Hora(testHoraInicio, testHoraFin));
        reserva.setEstado(Reserva.Estado.ACTIVA);
        dataRepository.createReservation(reserva, new DataCallback<Boolean>() {
            @Override
            public void onSuccess(Boolean result) {
                reservaCreadaId = reserva.getId();
                latch1.countDown();
            }
            @Override
            public void onFailure(Exception e) { latch1.countDown(); }
        });
        latch1.await(TIMEOUT, TimeUnit.SECONDS);
        // Comprobar si tiene reserva en la fecha
        CountDownLatch latch2 = new CountDownLatch(1);
        dataRepository.hasReservationOnDate(testUserEmail, testFecha, new DataCallback<Boolean>() {
            @Override
            public void onSuccess(Boolean has) {
                Assert.assertTrue(has);
                latch2.countDown();
            }
            @Override
            public void onFailure(Exception e) {
                Assert.fail("No debe fallar: " + e.getMessage());
                latch2.countDown();
            }
        });
        latch2.await(TIMEOUT, TimeUnit.SECONDS);
    }

    @Test
    public void testGetAvailablePlazas() throws InterruptedException {
        // Crear reserva activa para ocupar la plaza
        CountDownLatch latch1 = new CountDownLatch(1);
        Reserva reserva = new Reserva();
        reserva.setUsuario(testUserEmail);
        reserva.setPlaza(new Plaza(testPlazaId, testPlazaTipo));
        reserva.setFecha(testFecha);
        reserva.setHora(new Hora(testHoraInicio, testHoraFin));
        reserva.setEstado(Reserva.Estado.ACTIVA);
        dataRepository.createReservation(reserva, new DataCallback<Boolean>() {
            @Override
            public void onSuccess(Boolean result) {
                reservaCreadaId = reserva.getId();
                latch1.countDown();
            }
            @Override
            public void onFailure(Exception e) { latch1.countDown(); }
        });
        latch1.await(TIMEOUT, TimeUnit.SECONDS);
        // Comprobar plazas disponibles (debe estar vacía para ese horario y plaza)
        CountDownLatch latch2 = new CountDownLatch(1);
        dataRepository.getAvailablePlazas(testPlazaTipo, testFecha, testHoraInicio, testHoraFin, new DataCallback<List<String>>() {
            @Override
            public void onSuccess(List<String> plazas) {
                Assert.assertFalse(plazas.contains(testPlazaId));
                latch2.countDown();
            }
            @Override
            public void onFailure(Exception e) {
                Assert.fail("No debe fallar: " + e.getMessage());
                latch2.countDown();
            }
        });
        latch2.await(TIMEOUT, TimeUnit.SECONDS);
    }

    @Test
    public void testAssignRandomPlaza() throws InterruptedException {
        // Comprobar que asigna una plaza disponible
        CountDownLatch latch = new CountDownLatch(1);
        dataRepository.assignRandomPlaza(testPlazaTipo, testFecha, testHoraInicio, testHoraFin, new DataCallback<String>() {
            @Override
            public void onSuccess(String plazaId) {
                Assert.assertNotNull(plazaId);
                latch.countDown();
            }
            @Override
            public void onFailure(Exception e) {
                Assert.fail("No debe fallar: " + e.getMessage());
                latch.countDown();
            }
        });
        latch.await(TIMEOUT, TimeUnit.SECONDS);
    }

    @Test
    public void testGetAvailableNumbers() throws InterruptedException {
        // Comprobar que devuelve el número de la plaza disponible
        CountDownLatch latch = new CountDownLatch(1);
        dataRepository.getAvailableNumbers(testPlazaTipo, "A", testFecha, testHoraInicio, testHoraFin, new DataCallback<List<String>>() {
            @Override
            public void onSuccess(List<String> numeros) {
                Assert.assertTrue(numeros.contains("1"));
                latch.countDown();
            }
            @Override
            public void onFailure(Exception e) {
                Assert.fail("No debe fallar: " + e.getMessage());
                latch.countDown();
            }
        });
        latch.await(TIMEOUT, TimeUnit.SECONDS);
    }

    @Test
    public void testGetCurrentReservation() throws InterruptedException {
        // Crear reserva activa para ahora
        CountDownLatch latch1 = new CountDownLatch(1);
        Reserva reserva = new Reserva();
        reserva.setUsuario(testUserEmail);
        reserva.setPlaza(new Plaza(testPlazaId, testPlazaTipo));
        reserva.setFecha(testFecha);
        reserva.setHora(new Hora(testHoraInicio, testHoraFin));
        reserva.setEstado(Reserva.Estado.ACTIVA);
        dataRepository.createReservation(reserva, new DataCallback<Boolean>() {
            @Override
            public void onSuccess(Boolean result) {
                reservaCreadaId = reserva.getId();
                latch1.countDown();
            }
            @Override
            public void onFailure(Exception e) { latch1.countDown(); }
        });
        latch1.await(TIMEOUT, TimeUnit.SECONDS);
        // Obtener reserva actual
        CountDownLatch latch2 = new CountDownLatch(1);
        dataRepository.getCurrentReservation(testUserEmail, new DataCallback<Reserva>() {
            @Override
            public void onSuccess(Reserva r) {
                Assert.assertNotNull(r);
                Assert.assertEquals(testUserEmail, r.getUsuario());
                latch2.countDown();
            }
            @Override
            public void onFailure(Exception e) {
                Assert.fail("No debe fallar: " + e.getMessage());
                latch2.countDown();
            }
        });
        latch2.await(TIMEOUT, TimeUnit.SECONDS);
    }

    @Test
    public void testGetNextReservation() throws InterruptedException {
        // Crear dos reservas futuras
        String tomorrow = new java.text.SimpleDateFormat("yyyy-MM-dd").format(new java.util.Date(System.currentTimeMillis() + 24*60*60*1000));
        String dayAfterTomorrow = new java.text.SimpleDateFormat("yyyy-MM-dd").format(new java.util.Date(System.currentTimeMillis() + 2*24*60*60*1000));
        CountDownLatch latch1 = new CountDownLatch(2);
        Reserva reserva1 = new Reserva();
        reserva1.setUsuario(testUserEmail);
        reserva1.setPlaza(new Plaza(testPlazaId, testPlazaTipo));
        reserva1.setFecha(tomorrow);
        reserva1.setHora(new Hora(testHoraInicio, testHoraFin));
        reserva1.setEstado(Reserva.Estado.ACTIVA);
        dataRepository.createReservation(reserva1, new DataCallback<Boolean>() {
            @Override
            public void onSuccess(Boolean result) { latch1.countDown(); }
            @Override
            public void onFailure(Exception e) { latch1.countDown(); }
        });
        Reserva reserva2 = new Reserva();
        reserva2.setUsuario(testUserEmail);
        reserva2.setPlaza(new Plaza(testPlazaId, testPlazaTipo));
        reserva2.setFecha(dayAfterTomorrow);
        reserva2.setHora(new Hora(testHoraInicio, testHoraFin));
        reserva2.setEstado(Reserva.Estado.ACTIVA);
        dataRepository.createReservation(reserva2, new DataCallback<Boolean>() {
            @Override
            public void onSuccess(Boolean result) { latch1.countDown(); }
            @Override
            public void onFailure(Exception e) { latch1.countDown(); }
        });
        latch1.await(TIMEOUT, TimeUnit.SECONDS);
        // Obtener la próxima reserva
        CountDownLatch latch2 = new CountDownLatch(1);
        dataRepository.getNextReservation(testUserEmail, new DataCallback<Reserva>() {
            @Override
            public void onSuccess(Reserva r) {
                Assert.assertNotNull(r);
                Assert.assertEquals(tomorrow, r.getFecha());
                latch2.countDown();
            }
            @Override
            public void onFailure(Exception e) {
                Assert.fail("No debe fallar: " + e.getMessage());
                latch2.countDown();
            }
        });
        latch2.await(TIMEOUT, TimeUnit.SECONDS);
    }
}
