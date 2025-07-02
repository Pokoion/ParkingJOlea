package com.lksnext.parkingplantilla.viewmodel;

import androidx.arch.core.executor.testing.InstantTaskExecutorRule;
import androidx.lifecycle.LiveData;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import com.lksnext.parkingplantilla.ParkingApplication;
import com.lksnext.parkingplantilla.data.DataRepository;
import com.lksnext.parkingplantilla.domain.Hora;
import com.lksnext.parkingplantilla.domain.Plaza;
import com.lksnext.parkingplantilla.domain.Reserva;
import com.lksnext.parkingplantilla.utils.DateUtils;
import com.lksnext.parkingplantilla.utils.LiveDataTestUtil;
import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.time.Duration;
import java.util.Calendar;
import java.util.List;
import java.util.concurrent.CountDownLatch;

import static org.awaitility.Awaitility.await;
import static org.junit.Assert.*;

@RunWith(AndroidJUnit4.class)
public class ReservationsViewModelGestionInstrumentedTest {
    @Rule
    public InstantTaskExecutorRule instantTaskExecutorRule = new InstantTaskExecutorRule();

    private ReservationsViewModel viewModel;
    private DataRepository repository;
    private static final String TEST_EMAIL = "test_gestion_user@example.com";
    private static final String TEST_PASSWORD = "Test1234!";
    private static final String TEST_NAME = "Test Gestion User";
    private static final String PLAZA_ID = "GESTION-1";
    private static final String PLAZA_TIPO = Plaza.TIPO_STANDARD;
    private Plaza testPlaza;

    @Before
    public void setUp() throws Exception {
        repository = ParkingApplication.getInstance().getRepository();
        viewModel = new ReservationsViewModel(repository);
        // Registrar usuario
        CountDownLatch latch = new CountDownLatch(1);
        repository.register(TEST_NAME, TEST_EMAIL, TEST_PASSWORD, new LatchCallback<>(latch));
        latch.await();
        // Login
        latch = new CountDownLatch(1);
        repository.login(TEST_EMAIL, TEST_PASSWORD, new LatchCallback<>(latch));
        latch.await();
        // Crear plaza
        testPlaza = new Plaza(PLAZA_ID, PLAZA_TIPO);
        latch = new CountDownLatch(1);
        repository.addPlaza(testPlaza, new LatchCallback<>(latch));
        latch.await();
    }

    @After
    public void tearDown() throws Exception {
        CountDownLatch latch = new CountDownLatch(1);
        repository.deleteUser(TEST_EMAIL, TEST_PASSWORD, new LatchCallback<>(latch));
        latch.await();
        latch = new CountDownLatch(1);
        repository.deletePlaza(PLAZA_ID, new LatchCallback<>(latch));
        latch.await();
    }

    /**
     * Crea una reserva de prueba y devuelve el id generado
     */
    private String crearReservaDePrueba() throws Exception {
        Calendar now = Calendar.getInstance();
        String fecha = DateUtils.formatDateForApi(now);
        long inicio = DateUtils.getCurrentTimeMs() + 10 * 60 * 1000; // en 10 min
        long fin = inicio + 60 * 60 * 1000; // +1h
        Hora hora = new Hora(inicio, fin);
        Reserva reserva = new Reserva(fecha, TEST_EMAIL, PLAZA_ID, testPlaza, hora);
        reserva.setEstado(Reserva.Estado.ACTIVA);
        boolean result = LiveDataTestUtil.getValue(viewModel.createReservation(reserva));
        assertTrue(result);
        // Buscar el id de la reserva creada
        viewModel.loadUserReservations();
        java.util.List<Reserva> reservas = LiveDataTestUtil.getValue(viewModel.getReservations());
        assertNotNull(reservas);
        for (Reserva r : reservas) {
            if (r.getPlaza().getId().equals(PLAZA_ID) && r.getFecha().equals(fecha)) {
                return r.getId();
            }
        }
        fail("No se encontró la reserva de prueba creada");
        return null;
    }

    @Test
    public void createReservation_creaReservaCorrectamente() throws Exception {
        String id = crearReservaDePrueba();
        assertNotNull(id);
        // Comprobar que la reserva aparece en el LiveData
        viewModel.loadUserReservations();
        List<Reserva> reservas = LiveDataTestUtil.getValue(viewModel.getReservations());
        boolean found = false;
        for (Reserva r : reservas) {
            if (r.getId().equals(id)) {
                found = true;
                break;
            }
        }
        assertTrue(found);
    }

    @Test
    public void updateReservation_actualizaReservaCorrectamente() throws Exception {
        // Crear una reserva de prueba
        String id = crearReservaDePrueba();
        // Modificar la hora de fin
        viewModel.loadUserReservations();
        List<Reserva> reservas = LiveDataTestUtil.getValue(viewModel.getReservations());
        Reserva reserva = null;
        for (Reserva r : reservas) {
            if (r.getId().equals(id)) {
                reserva = r;
                break;
            }
        }
        assertNotNull(reserva);
        long nuevoFin = reserva.getHora().getHoraFin() + 30 * 60 * 1000; // +30 min
        reserva.getHora().setHoraFin(nuevoFin);
        boolean updated = LiveDataTestUtil.getValue(viewModel.updateReservation(reserva));
        assertTrue(updated);
        // Comprobar que la hora de fin se ha actualizado
        viewModel.loadUserReservations();
        reservas = LiveDataTestUtil.getValue(viewModel.getReservations());
        Reserva actualizada = null;
        for (Reserva r : reservas) {
            if (r.getId().equals(id)) {
                actualizada = r;
                break;
            }
        }
        assertNotNull(actualizada);
        assertEquals(nuevoFin, actualizada.getHora().getHoraFin());
    }

    @Test
    public void deleteReservation_eliminaReservaCorrectamente() throws Exception {
        // Crear una reserva de prueba
        String id = crearReservaDePrueba();
        // Eliminar la reserva
        viewModel.deleteReservation(id);
        // Esperar a que el LiveData de reservas ya no contenga la reserva eliminada
        viewModel.loadUserReservations();
        java.util.List<Reserva> reservas = LiveDataTestUtil.getOrAwaitValue(viewModel.getReservations(), java.util.Collections.emptyList());
        assertNotNull(reservas);
        assertTrue(reservas.isEmpty());
    }

    @Test
    public void createReservation_fallaSiHoraInicioEsPasada() throws Exception {
        Calendar now = Calendar.getInstance();
        String fecha = DateUtils.formatDateForApi(now);
        long inicio = DateUtils.getCurrentTimeMs() - 2 * 60 * 1000; // hace 3 minutos
        long fin = inicio + 60 * 60 * 1000; // +1h
        Hora hora = new Hora(inicio, fin);
        Reserva reserva = new Reserva(fecha, TEST_EMAIL, PLAZA_ID, testPlaza, hora);
        LiveData<Boolean> liveResult = viewModel.createReservation(reserva);
        Boolean result = LiveDataTestUtil.getValue(liveResult);
        assertNotNull(result);
        assertFalse(result);
    }

    @Test
    public void createReservation_fallaSiYaExisteReservaEseDia() throws Exception {
        // Crear una reserva válida primero
        String id = crearReservaDePrueba();
        assertNotNull(id);
        // Intentar crear otra reserva para el mismo usuario y fecha
        Calendar now = Calendar.getInstance();
        String fecha = DateUtils.formatDateForApi(now);
        long inicio = DateUtils.getCurrentTimeMs() + 20 * 60 * 1000; // en 20 min
        long fin = inicio + 60 * 60 * 1000; // +1h
        Hora hora = new Hora(inicio, fin);
        Reserva reserva = new Reserva(fecha, TEST_EMAIL, PLAZA_ID, testPlaza, hora);
        reserva.setEstado(Reserva.Estado.ACTIVA);
        LiveData<Boolean> liveResult = viewModel.createReservation(reserva);
        Boolean result = LiveDataTestUtil.getValue(liveResult);
        assertNotNull(result);
        assertFalse(result);
    }

    @Test
    public void createReservation_fallaSiPlazaNoDisponible() throws Exception {
        // Crear una reserva válida primero
        String id = crearReservaDePrueba();
        assertNotNull(id);
        // Intentar crear otra reserva para la misma plaza y franja horaria
        Calendar now = Calendar.getInstance();
        String fecha = DateUtils.formatDateForApi(now);
        long inicio = DateUtils.getCurrentTimeMs() + 10 * 60 * 1000; // en 10 min
        long fin = inicio + 60 * 60 * 1000; // +1h
        Hora hora = new Hora(inicio, fin);
        Reserva reserva = new Reserva(fecha, TEST_EMAIL, PLAZA_ID, testPlaza, hora);
        reserva.setEstado(Reserva.Estado.ACTIVA);
        // Cambiar el usuario para que no falle por doble reserva de usuario
        reserva.setUsuario("otro_user@example.com");
        LiveData<Boolean> liveResult = viewModel.createReservation(reserva);
        Boolean result = LiveDataTestUtil.getValue(liveResult);
        assertNotNull(result);
        assertFalse(result);
    }

    @Test
    public void checkReservationAvailability_trueCuandoNoHayConflicto() throws Exception {
        // Crear una reserva de prueba
        crearReservaDePrueba();
        // Intentar reservar otra plaza diferente en el mismo horario
        Calendar now = Calendar.getInstance();
        String fecha = DateUtils.formatDateForApi(now);
        long inicio = DateUtils.getCurrentTimeMs() + 10 * 60 * 1000;
        long fin = inicio + 60 * 60 * 1000;
        Plaza otraPlaza = new Plaza("GESTION-2", Plaza.TIPO_STANDARD);
        // Añadir la plaza al sistema
        CountDownLatch latch = new CountDownLatch(1);
        repository.addPlaza(otraPlaza, new LatchCallback<>(latch));
        latch.await();
        Hora hora = new Hora(inicio, fin);
        Reserva reserva = new Reserva(fecha, TEST_EMAIL, "GESTION-2", otraPlaza, hora);
        reserva.setEstado(Reserva.Estado.ACTIVA);
        LiveData<Boolean> liveResult = viewModel.checkReservationAvailability(reserva);
        Boolean result = LiveDataTestUtil.getValue(liveResult);
        assertNotNull(result);
        assertTrue(result);
        // Limpiar plaza extra
        latch = new CountDownLatch(1);
        repository.deletePlaza("GESTION-2", new LatchCallback<>(latch));
        latch.await();
    }

    @Test
    public void checkReservationAvailability_falseCuandoConflictoSinExclude() throws Exception {
        // Crear una reserva de prueba
        crearReservaDePrueba();
        // Intentar reservar la misma plaza y horario
        Calendar now = Calendar.getInstance();
        String fecha = DateUtils.formatDateForApi(now);
        long inicio = DateUtils.getCurrentTimeMs() + 10 * 60 * 1000;
        long fin = inicio + 60 * 60 * 1000;
        Hora hora = new Hora(inicio, fin);
        Reserva reserva = new Reserva(fecha, TEST_EMAIL, PLAZA_ID, testPlaza, hora);
        reserva.setEstado(Reserva.Estado.ACTIVA);
        LiveData<Boolean> liveResult = viewModel.checkReservationAvailability(reserva);
        Boolean result = LiveDataTestUtil.getValue(liveResult);
        assertNotNull(result);
        assertFalse(result);
    }

    @Test
    public void checkReservationAvailability_trueConExcludeReservationId() throws Exception {
        // Crear una reserva de prueba
        String id = crearReservaDePrueba();
        // Intentar reservar la misma plaza y horario, pero excluyendo la propia reserva
        Calendar now = Calendar.getInstance();
        String fecha = DateUtils.formatDateForApi(now);
        long inicio = DateUtils.getCurrentTimeMs() + 10 * 60 * 1000;
        long fin = inicio + 60 * 60 * 1000;
        Hora hora = new Hora(inicio, fin);
        Reserva reserva = new Reserva(fecha, TEST_EMAIL, PLAZA_ID, testPlaza, hora);
        reserva.setEstado(Reserva.Estado.ACTIVA);
        reserva.setId(id);
        LiveData<Boolean> liveResult = viewModel.checkReservationAvailability(reserva, id);
        Boolean result = LiveDataTestUtil.getValue(liveResult);
        assertNotNull(result);
        assertTrue(result);
    }

    @Test
    public void createReservation_fallaSiMismaPlazaMismaHora() throws Exception {
        // Crear una reserva de prueba
        String id1 = crearReservaDePrueba();
        assertNotNull(id1);
        // Intentar crear otra reserva para la misma plaza y exactamente el mismo horario
        Calendar now = Calendar.getInstance();
        String fecha = DateUtils.formatDateForApi(now);
        long inicio = DateUtils.getCurrentTimeMs() + 10 * 60 * 1000;
        long fin = inicio + 60 * 60 * 1000;
        Hora hora = new Hora(inicio, fin);
        Reserva reserva2 = new Reserva(fecha, "otro_user@example.com", PLAZA_ID, testPlaza, hora);
        reserva2.setEstado(Reserva.Estado.ACTIVA);
        LiveData<Boolean> liveResult = viewModel.createReservation(reserva2);
        Boolean result = LiveDataTestUtil.getValue(liveResult);
        assertNotNull(result);
        assertFalse("No debe permitir dos reservas en la misma plaza y hora", result);
    }

    @Test
    public void createReservation_fallaSiMismaPlazaSolapada() throws Exception {
        // Crear una reserva de prueba
        String id1 = crearReservaDePrueba();
        assertNotNull(id1);
        // Intentar crear otra reserva para la misma plaza y horario solapado
        Calendar now = Calendar.getInstance();
        String fecha = DateUtils.formatDateForApi(now);
        long inicio = DateUtils.getCurrentTimeMs() + 30 * 60 * 1000; // empieza a mitad de la anterior
        long fin = inicio + 60 * 60 * 1000;
        Hora hora = new Hora(inicio, fin);
        Reserva reserva2 = new Reserva(fecha, "otro_user@example.com", PLAZA_ID, testPlaza, hora);
        reserva2.setEstado(Reserva.Estado.ACTIVA);
        LiveData<Boolean> liveResult = viewModel.createReservation(reserva2);
        Boolean result = LiveDataTestUtil.getValue(liveResult);
        assertNotNull(result);
        assertFalse("No debe permitir reservas solapadas en la misma plaza", result);
    }

    @Test
    public void checkUserHasReservationOnDate_trueCuandoExiste() throws Exception {
        // Crear una reserva de prueba para hoy
        crearReservaDePrueba();
        String fecha = DateUtils.formatDateForApi(Calendar.getInstance());
        LiveData<Boolean> liveResult = viewModel.checkUserHasReservationOnDate(fecha);
        Boolean result = LiveDataTestUtil.getValue(liveResult);
        assertNotNull(result);
        assertTrue(result);
    }

    @Test
    public void checkUserHasReservationOnDate_falseCuandoNoExiste() throws Exception {
        // No crear reserva para mañana
        Calendar tomorrow = Calendar.getInstance();
        tomorrow.add(Calendar.DATE, 1);
        String fecha = DateUtils.formatDateForApi(tomorrow);
        LiveData<Boolean> liveResult = viewModel.checkUserHasReservationOnDate(fecha);
        Boolean result = LiveDataTestUtil.getValue(liveResult);
        assertNotNull(result);
        assertFalse(result);
    }

    @Test
    public void getUserReservationDates_devuelveFechasCorrectas() throws Exception {
        // Crear una reserva de prueba para hoy
        crearReservaDePrueba();
        LiveData<List<String>> liveResult = viewModel.getUserReservationDates();
        List<String> fechas = LiveDataTestUtil.getValue(liveResult);
        assertNotNull(fechas);
        String hoy = DateUtils.formatDateForApi(Calendar.getInstance());
        assertTrue(fechas.contains(hoy));
    }

    @Test
    public void getAvailablePlazas_devuelvePlazaDisponible() throws Exception {
        // Crear una plaza extra
        Plaza otraPlaza = new Plaza("GESTION-3", Plaza.TIPO_STANDARD);
        CountDownLatch latch = new CountDownLatch(1);
        repository.addPlaza(otraPlaza, new LatchCallback<>(latch));
        latch.await();
        // Buscar plazas disponibles para ese horario
        Calendar now = Calendar.getInstance();
        String fecha = DateUtils.formatDateForApi(now);
        long inicio = DateUtils.getCurrentTimeMs() + 2 * 60 * 60 * 1000; // en 2h
        long fin = inicio + 60 * 60 * 1000;
        LiveData<List<String>> liveResult = viewModel.getAvailablePlazas(PLAZA_TIPO, fecha, inicio, fin);
        List<String> plazas = LiveDataTestUtil.getValue(liveResult);
        assertNotNull(plazas);
        assertTrue(plazas.contains("GESTION-3"));
        // Limpiar plaza extra
        latch = new CountDownLatch(1);
        repository.deletePlaza("GESTION-3", new LatchCallback<>(latch));
        latch.await();
    }

    @Test
    public void loadAvailablePlazasAndExtractRowsNumbers_extraeFilasYNumeros() throws Exception {
        // Crear plazas con filas y números personalizados para el test
        Plaza plazaT1 = new Plaza("T-1", Plaza.TIPO_STANDARD);
        Plaza plazaT2 = new Plaza("T-2", Plaza.TIPO_STANDARD);
        Plaza plazaX1 = new Plaza("X-1", Plaza.TIPO_STANDARD);
        CountDownLatch latch = new CountDownLatch(3);
        repository.addPlaza(plazaT1, new LatchCallback<>(latch));
        repository.addPlaza(plazaT2, new LatchCallback<>(latch));
        repository.addPlaza(plazaX1, new LatchCallback<>(latch));
        latch.await();
        // Consultar filas y números disponibles
        Calendar now = Calendar.getInstance();
        String fecha = DateUtils.formatDateForApi(now);
        long inicio = DateUtils.getCurrentTimeMs() + 4 * 60 * 60 * 1000; // en 4h
        long fin = inicio + 60 * 60 * 1000;
        viewModel.loadAvailablePlazasAndExtractRowsNumbers(PLAZA_TIPO, fecha, inicio, fin);
        List<String> filas = LiveDataTestUtil.getValue(viewModel.getAvailableRows());
        List<String> numeros = LiveDataTestUtil.getValue(viewModel.getAvailableNumbers());
        assertNotNull(filas);
        assertNotNull(numeros);
        assertTrue(filas.contains("T"));
        assertTrue(filas.contains("X"));
        assertTrue(numeros.contains("1"));
        assertTrue(numeros.contains("2"));
        // Limpiar plazas
        latch = new CountDownLatch(3);
        repository.deletePlaza("T-1", new LatchCallback<>(latch));
        repository.deletePlaza("T-2", new LatchCallback<>(latch));
        repository.deletePlaza("X-1", new LatchCallback<>(latch));
        latch.await();
    }

    @Test
    public void reservaCambiaAFinalizadaTrasFin() throws Exception {
        // Crear una reserva con hora de inicio = ahora y fin = ahora + 5 segundos
        Calendar now = Calendar.getInstance();
        String fecha = DateUtils.formatDateForApi(now);
        long inicio = DateUtils.getCurrentTimeMs() + 1000; // 1 segundo en el futuro
        long fin = inicio + 5000; // 5 segundos de duración
        Hora hora = new Hora(inicio, fin);
        Reserva reserva = new Reserva(fecha, TEST_EMAIL, PLAZA_ID, testPlaza, hora);
        boolean result = LiveDataTestUtil.getValue(viewModel.createReservation(reserva));
        assertTrue(result);
        String reservaId = reserva.getId();

        await().atMost(Duration.ofSeconds(70)).pollInterval(Duration.ofSeconds(5)).until(() -> {
            viewModel.loadHistoricReservations();
            List<Reserva> historicas = LiveDataTestUtil.getValue(viewModel.getHistoricReservations());
            if (historicas == null || historicas.isEmpty()) return false;
            Reserva encontrada = historicas.get(0);
            return encontrada.getId().equals(reservaId) && encontrada.getEstado() == Reserva.Estado.FINALIZADA;
        });
    }

    @Test
    public void deleteReservation_pasaACanceladaYSeMueveAHistorico() throws Exception {
        String id = crearReservaDePrueba();
        assertNotNull(id);
        // Eliminar la reserva
        viewModel.deleteReservation(id);
        // Esperar a que la reserva desaparezca de activas
        viewModel.loadUserReservations();
        List<Reserva> activas = LiveDataTestUtil.getOrAwaitValue(viewModel.getReservations(), java.util.Collections.emptyList());
        assertNotNull(activas);
        assertTrue(activas.isEmpty());
        // Esperar a que la reserva aparezca en histórico y esté CANCELADA
        viewModel.loadHistoricReservations();
        List<Reserva> historicas = LiveDataTestUtil.getValue(viewModel.getHistoricReservations());
        assertNotNull(historicas);
        boolean encontrada = false;
        for (Reserva r : historicas) {
            if (r.getId().equals(id) && r.getEstado() == Reserva.Estado.CANCELADA) {
                encontrada = true;
                break;
            }
        }
        assertTrue("La reserva cancelada debe estar en histórico", encontrada);
    }

    @Test
    public void createReservation_fallaSiHoraFinAntesDeInicio() throws Exception {
        Calendar now = Calendar.getInstance();
        String fecha = DateUtils.formatDateForApi(now);
        long inicio = DateUtils.getCurrentTimeMs() + 10 * 60 * 1000;
        long fin = inicio - 60 * 1000; // fin antes de inicio
        Hora hora = new Hora(inicio, fin);
        Reserva reserva = new Reserva(fecha, TEST_EMAIL, PLAZA_ID, testPlaza, hora);
        reserva.setEstado(Reserva.Estado.ACTIVA);
        LiveData<Boolean> liveResult = viewModel.createReservation(reserva);
        Boolean result = LiveDataTestUtil.getValue(liveResult);
        assertNotNull(result);
        assertFalse("No debe permitir hora de fin anterior a inicio", result);
    }

    @Test
    public void createReservation_fallaSiHoraFinEnPasado() throws Exception {
        Calendar now = Calendar.getInstance();
        String fecha = DateUtils.formatDateForApi(now);
        long inicio = DateUtils.getCurrentTimeMs() - 10 * 60 * 1000;
        long fin = inicio - 60 * 1000; // fin en el pasado
        Hora hora = new Hora(inicio, fin);
        Reserva reserva = new Reserva(fecha, TEST_EMAIL, PLAZA_ID, testPlaza, hora);
        reserva.setEstado(Reserva.Estado.ACTIVA);
        LiveData<Boolean> liveResult = viewModel.createReservation(reserva);
        Boolean result = LiveDataTestUtil.getValue(liveResult);
        assertNotNull(result);
        assertFalse("No debe permitir hora de fin en el pasado", result);
    }

    @Test
    public void loadCurrentReservation_devuelveReservaEnCurso() throws Exception {
        // Crear una reserva que esté en curso
        Calendar now = Calendar.getInstance();
        String fecha = DateUtils.formatDateForApi(now);
        long inicio = DateUtils.getCurrentTimeMs() - 60 * 1000; // empezó hace 1 min
        long fin = DateUtils.getCurrentTimeMs() + 10 * 60 * 1000; // termina en 10 min
        Hora hora = new Hora(inicio, fin);
        Reserva reserva = new Reserva(fecha, TEST_EMAIL, PLAZA_ID, testPlaza, hora);
        reserva.setEstado(Reserva.Estado.ACTIVA);
        boolean created = LiveDataTestUtil.getValue(viewModel.createReservation(reserva));
        assertTrue(created);
        // Cargar reserva actual
        viewModel.loadCurrentReservation();
        Reserva actual = LiveDataTestUtil.getValue(viewModel.getCurrentReservation());
        assertNotNull("Debe haber una reserva en curso", actual);
        assertEquals(reserva.getFecha(), actual.getFecha());
        assertEquals(reserva.getPlaza().getId(), actual.getPlaza().getId());
    }

    @Test
    public void loadNextReservation_devuelveReservaFutura() throws Exception {
        // Crear reserva para HOY (inicio en 1 min, fin en +1h)
        Calendar now = Calendar.getInstance();
        String fechaHoy = DateUtils.formatDateForApi(now);
        long inicioHoy = DateUtils.getCurrentTimeMs() + 5 * 60 * 1000; // en 5 min
        long finHoy = inicioHoy + 60 * 60 * 1000; // +1h
        Hora horaHoy = new Hora(inicioHoy, finHoy);
        Reserva reservaHoy = new Reserva(fechaHoy, TEST_EMAIL, PLAZA_ID, testPlaza, horaHoy);
        reservaHoy.setEstado(Reserva.Estado.ACTIVA);
        boolean createdHoy = LiveDataTestUtil.getValue(viewModel.createReservation(reservaHoy));
        assertTrue(createdHoy);
        // Crear plaza extra para la reserva futura
        Plaza otraPlaza = new Plaza("GESTION-5", Plaza.TIPO_STANDARD);
        CountDownLatch latch = new CountDownLatch(1);
        repository.addPlaza(otraPlaza, new LatchCallback<>(latch));
        latch.await();
        // Crear reserva futura para MAÑANA en la plaza extra
        Calendar manana = Calendar.getInstance();
        manana.add(Calendar.DATE, 1);
        String fechaFutura = DateUtils.formatDateForApi(manana);
        long inicio2 = manana.getTimeInMillis() + 10 * 60 * 1000; // mañana + 10 min
        long fin2 = inicio2 + 10 * 60 * 1000;
        Hora hora2 = new Hora(inicio2, fin2);
        Reserva reserva2 = new Reserva(fechaFutura, TEST_EMAIL, "GESTION-5", otraPlaza, hora2);
        reserva2.setEstado(Reserva.Estado.ACTIVA);
        boolean created2 = LiveDataTestUtil.getValue(viewModel.createReservation(reserva2));
        assertTrue(created2);
        // Cargar próxima reserva
        viewModel.loadNextReservation();
        Reserva proxima = LiveDataTestUtil.getValue(viewModel.getNextReservation());
        assertNotNull("Debe haber una reserva futura", proxima);
        assertEquals("GESTION-1", proxima.getPlaza().getId());
        // Limpiar plaza extra
        latch = new CountDownLatch(1);
        repository.deletePlaza("GESTION-5", new LatchCallback<>(latch));
        latch.await();
    }

    @Test
    public void noPermiteDosReservasMismoDia() throws Exception {
        // Crear la primera reserva para hoy
        String id1 = crearReservaDePrueba();
        assertNotNull(id1);
        // Intentar crear una segunda reserva para el mismo usuario y día, en otra plaza
        Plaza otraPlaza = new Plaza("GESTION-EXTRA", Plaza.TIPO_STANDARD);
        CountDownLatch latch = new CountDownLatch(1);
        repository.addPlaza(otraPlaza, new LatchCallback<>(latch));
        latch.await();
        Calendar now = Calendar.getInstance();
        String fecha = DateUtils.formatDateForApi(now);
        long inicio = DateUtils.getCurrentTimeMs() + 20 * 60 * 1000; // en 20 min
        long fin = inicio + 60 * 60 * 1000;
        Hora hora = new Hora(inicio, fin);
        Reserva reserva2 = new Reserva(fecha, TEST_EMAIL, "GESTION-EXTRA", otraPlaza, hora);
        reserva2.setEstado(Reserva.Estado.ACTIVA);
        boolean created2 = LiveDataTestUtil.getValue(viewModel.createReservation(reserva2));
        assertFalse("No debe permitir dos reservas activas el mismo día para el mismo usuario", created2);
        // Limpiar plaza extra
        latch = new CountDownLatch(1);
        repository.deletePlaza("GESTION-EXTRA", new LatchCallback<>(latch));
        latch.await();
    }

    @Test
    public void createReservation_fallaSiSupera9Horas() throws Exception {
        Calendar now = Calendar.getInstance();
        String fecha = DateUtils.formatDateForApi(now);
        long inicio = DateUtils.getCurrentTimeMs() + 10 * 60 * 1000; // en 10 min
        long fin = inicio + 10 * 60 * 60 * 1000; // +10 horas (supera el límite)
        Hora hora = new Hora(inicio, fin);
        Reserva reserva = new Reserva(fecha, TEST_EMAIL, PLAZA_ID, testPlaza, hora);
        reserva.setEstado(Reserva.Estado.ACTIVA);
        LiveData<Boolean> liveResult = viewModel.createReservation(reserva);
        Boolean result = LiveDataTestUtil.getValue(liveResult);
        assertNotNull(result);
        assertFalse("No debe permitir reservas de más de 9 horas", result);
    }
}
