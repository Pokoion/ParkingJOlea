package com.lksnext.parkingplantilla.viewmodel;

import androidx.arch.core.executor.testing.InstantTaskExecutorRule;
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
import java.util.Calendar;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import static org.junit.Assert.*;

@RunWith(AndroidJUnit4.class)
public class ReservationsViewModelInstrumentedTest {
    @Rule
    public InstantTaskExecutorRule instantTaskExecutorRule = new InstantTaskExecutorRule();

    private ReservationsViewModel viewModel;
    private DataRepository repository;

    private static final String TEST_EMAIL = "test_load_user@example.com";
    private static final String TEST_PASSWORD = "Test1234!";
    private static final String TEST_NAME = "Test Load User";
    private static final String PLAZA_ID = "TEST-1";
    private static final String PLAZA_TIPO = Plaza.TIPO_STANDARD;
    private static final String FECHA_HOY = DateUtils.formatDateForApi(Calendar.getInstance());
    private static final String FECHA_FUTURA = calcularFechaFutura();
    private Plaza testPlaza;

    private static String calcularFechaFutura() {
        Calendar manana = Calendar.getInstance();
        manana.add(Calendar.DAY_OF_MONTH, 1);
        return DateUtils.formatDateForApi(manana);
    }

    private String crearReservaDePrueba(String email, Plaza plaza, String fecha, long inicio, long fin, Reserva.Estado estado, int reservasEsperadas) throws Exception {
        Hora hora = new Hora(inicio, fin);
        Reserva reserva = new Reserva(fecha, email, plaza.getId(), plaza, hora);
        reserva.setEstado(estado);
        boolean result = false;
        try {
            result = LiveDataTestUtil.getValue(viewModel.createReservation(reserva));
        } catch (Exception e) {
            throw e;
        }
        assertTrue(result);
        // Esperar en el LiveData correcto según el estado
        if (estado == Reserva.Estado.FINALIZADA || estado == Reserva.Estado.CANCELADA) {
            viewModel.loadHistoricReservations();
            List<Reserva> reservas = LiveDataTestUtil.getOrAwaitValue(
                viewModel.getHistoricReservations(),
                (java.util.function.Predicate<List<Reserva>>) (list -> list != null && list.size() >= reservasEsperadas)
            );
            assertNotNull(reservas);
            for (Reserva r : reservas) {
                if (r.getPlaza().getId().equals(plaza.getId()) && r.getFecha().equals(fecha)) {
                    return r.getId();
                }
            }
        } else {
            viewModel.loadUserReservations();
            List<Reserva> reservas = LiveDataTestUtil.getOrAwaitValue(
                viewModel.getReservations(),
                (java.util.function.Predicate<List<Reserva>>) (list -> list != null && list.size() >= reservasEsperadas)
            );
            assertNotNull(reservas);
            for (Reserva r : reservas) {
                if (r.getPlaza().getId().equals(plaza.getId()) && r.getFecha().equals(fecha)) {
                    return r.getId();
                }
            }
        }
        fail("No se encontró la reserva de prueba creada");
        return null;
    }

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
        // No crear reservas aquí, cada test crea las necesarias
    }

    @After
    public void tearDown() throws Exception {
        // Borrar todas las reservas del usuario antes de eliminar usuario y plaza
        CountDownLatch latch = new CountDownLatch(1);
        repository.deleteUserReservations(TEST_EMAIL, new LatchCallback<>(latch));
        latch.await();
        latch = new CountDownLatch(1);
        repository.deleteUser(TEST_EMAIL, TEST_PASSWORD, new LatchCallback<>(latch));
        latch.await();
        latch = new CountDownLatch(1);
        repository.deletePlaza(PLAZA_ID, new LatchCallback<>(latch));
        latch.await();
    }

    @Test
    public void loadCurrentReservation_cargaReservaActual() throws Exception {
        // Crear reserva actual (en curso)
        Calendar now = Calendar.getInstance();
        String fechaActual = DateUtils.formatDateForApi(now);
        long inicio = DateUtils.getCurrentTimeMs() - 60 * 1000; // hace 1 minuto
        long fin = inicio + 60 * 60 * 1000; // +1h desde inicio
        String reservaActualId = crearReservaDePrueba(TEST_EMAIL, testPlaza, fechaActual, inicio, fin, Reserva.Estado.ACTIVA, 1);
        viewModel.loadCurrentReservation();
        Reserva current = LiveDataTestUtil.getValue(viewModel.getCurrentReservation());
        assertNotNull(current);
        assertEquals(reservaActualId, current.getId());
    }

    @Test
    public void loadCurrentReservation_empty() throws Exception {
        viewModel.loadCurrentReservation();
        Reserva current = LiveDataTestUtil.getValue(viewModel.getCurrentReservation());
        assertNull(current);
    }

    @Test
    public void loadNextReservation_cargaProximaReserva() throws Exception {
        // Crear reserva futura
        String fechaFutura = DateUtils.formatDateForApi(Calendar.getInstance());
        long inicioFuturo = DateUtils.getCurrentTimeMs() + 60 * 60 * 1000; // dentro de 1h
        long finFuturo = inicioFuturo + 60 * 60 * 1000;
        String reservaFuturaId = crearReservaDePrueba(TEST_EMAIL, testPlaza, fechaFutura, inicioFuturo, finFuturo, Reserva.Estado.ACTIVA, 1);
        viewModel.loadNextReservation();
        Reserva next = LiveDataTestUtil.getValue(viewModel.getNextReservation());
        assertNotNull(next);
        assertEquals(reservaFuturaId, next.getId());
    }

    @Test
    public void loadNextReservation_empty() throws Exception {
        viewModel.loadNextReservation();
        Reserva next = LiveDataTestUtil.getValue(viewModel.getNextReservation());
        assertNull(next);
    }

    @Test
    public void loadHistoricReservations_cargaHistorico() throws Exception {
        // Crear reserva "histórica" (finalizada) con hora de inicio hace 1 minuto y fin dentro de 1h
        Calendar now = Calendar.getInstance();
        String fechaActual = DateUtils.formatDateForApi(now);
        long inicio = DateUtils.getCurrentTimeMs() - 60 * 1000; // hace 1 minuto
        long fin = inicio + 60 * 60 * 1000; // +1h desde inicio
        String reservaHistoricaId = crearReservaDePrueba(TEST_EMAIL, testPlaza, fechaActual, inicio, fin, Reserva.Estado.FINALIZADA , 1);
        viewModel.loadHistoricReservations();
        List<Reserva> historic = LiveDataTestUtil.getValue(viewModel.getHistoricReservations());
        assertNotNull(historic);
        assertFalse(historic.isEmpty());
        boolean found = false;
        for (Reserva r : historic) {
            if (reservaHistoricaId.equals(r.getId())) found = true;
        }
        assertTrue(found);
    }

    @Test
    public void loadHistoricReservations_empty() throws Exception {
        viewModel.loadHistoricReservations();
        List<Reserva> historic = LiveDataTestUtil.getValue(viewModel.getHistoricReservations());
        assertNotNull(historic);
        assertTrue(historic.isEmpty());
    }

    @Test
    public void loadUserReservations_cargaTodasDelUsuarioActual() throws Exception {
        // Crear reserva actual (hoy)
        String fechaActual = FECHA_HOY;
        long inicio = DateUtils.getCurrentTimeMs() - 60 * 1000;
        long fin = inicio + 60 * 60 * 1000;
        String reservaActualId = crearReservaDePrueba(TEST_EMAIL, testPlaza, fechaActual, inicio, fin, Reserva.Estado.ACTIVA, 1);
        // Crear reserva futura (si coincide con hoy, usar FECHA_FUTURA)
        String fechaFutura = FECHA_FUTURA.equals(fechaActual) ? calcularFechaFutura() : FECHA_FUTURA;
        long inicioFuturo = DateUtils.timeToMs(10, 0);
        long finFuturo = inicioFuturo + 60 * 60 * 1000;
        String reservaFuturaId = crearReservaDePrueba(TEST_EMAIL, testPlaza, fechaFutura, inicioFuturo, finFuturo, Reserva.Estado.ACTIVA, 2);
        viewModel.loadUserReservations();
        List<Reserva> reservas = LiveDataTestUtil.getValue(viewModel.getReservations());
        assertNotNull(reservas);
        assertEquals(2, reservas.size());
        // Comprobar que la reserva actual está en el LiveData
        boolean foundActual = false;
        boolean foundFutura = false;
        for (Reserva r : reservas) {
            if (r.getId().equals(reservaActualId)) foundActual = true;
            if (r.getId().equals(reservaFuturaId)) foundFutura = true;
        }
        assertTrue(foundActual);
        assertTrue(foundFutura);
    }

    @Test
    public void loadUserReservations_empty() throws Exception {
        viewModel.loadUserReservations();
        List<Reserva> reservas = LiveDataTestUtil.getValue(viewModel.getReservations());
        assertNotNull(reservas);
        assertTrue(reservas.isEmpty());
    }

    @Test
    public void loadUserReservationsPorId_empty() throws Exception {
        viewModel.loadUserReservations(TEST_EMAIL);
        List<Reserva> reservas = LiveDataTestUtil.getValue(viewModel.getReservations());
        assertNotNull(reservas);
        assertTrue(reservas.isEmpty());
    }
}

