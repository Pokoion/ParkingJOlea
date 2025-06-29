package com.lksnext.parkingplantilla.data.repository;

import com.lksnext.parkingplantilla.domain.DataCallback;
import com.lksnext.parkingplantilla.domain.Plaza;
import com.lksnext.parkingplantilla.domain.Reserva;
import com.lksnext.parkingplantilla.domain.User;

import java.util.List;

/**
 * Interfaz que define el contrato de acceso a datos para reservas, usuarios y plazas.
 * Implementada por fuentes de datos como FirebaseDataSource.
 */
public interface DataSource {
    /**
     * Obtiene todas las reservas activas de un usuario.
     * @param userId Email del usuario
     * @param callback Callback con la lista de reservas activas
     */
    void getReservations(String userId, DataCallback<List<Reserva>> callback);

    /**
     * Obtiene las reservas históricas (finalizadas o canceladas) de un usuario.
     * @param userId Email del usuario
     * @param callback Callback con la lista de reservas históricas
     */
    void getHistoricReservations(String userId, DataCallback<List<Reserva>> callback);

    /**
     * Obtiene la reserva actual (en curso) de un usuario.
     * @param userId Email del usuario
     * @param callback Callback con la reserva actual o null si no hay
     */
    void getCurrentReservation(String userId, DataCallback<Reserva> callback);

    /**
     * Obtiene la próxima reserva futura de un usuario.
     * @param userId Email del usuario
     * @param callback Callback con la próxima reserva futura o null si no hay
     */
    void getNextReservation(String userId, DataCallback<Reserva> callback);

    /**
     * Crea una nueva reserva si es válida (no hay otra activa ese día y la plaza está disponible).
     * @param reserva Objeto Reserva a crear
     * @param callback Callback con true si se creó, false si no
     */
    void createReservation(Reserva reserva, DataCallback<Boolean> callback);

    /**
     * Marca una reserva como cancelada por su ID.
     * @param reservationId ID de la reserva
     * @param callback Callback con true si se canceló correctamente
     */
    void deleteReservation(String reservationId, DataCallback<Boolean> callback);

    /**
     * Actualiza los datos de una reserva existente.
     * @param reserva Objeto Reserva actualizado
     * @param callback Callback con true si se actualizó correctamente
     */
    void updateReservation(Reserva reserva, DataCallback<Boolean> callback);

    /**
     * Comprueba si una plaza está disponible para una reserva (sin solapamiento de horario).
     * @param reserva Reserva a comprobar
     * @param excludeReservationId ID de reserva a excluir (puede ser null)
     * @param callback Callback con true si está disponible
     */
    void checkAvailability(Reserva reserva, String excludeReservationId, DataCallback<Boolean> callback);

    /**
     * Inicia sesión con email y contraseña.
     * @param email Email del usuario
     * @param password Contraseña
     * @param callback Callback con el usuario autenticado
     */
    void login(String email, String password, DataCallback<User> callback);

    /**
     * Registra un nuevo usuario si el email no existe.
     * @param name Nombre
     * @param email Email
     * @param password Contraseña
     * @param callback Callback con el usuario registrado
     */
    void register(String name, String email, String password, DataCallback<User> callback);

    /**
     * Comprueba si el usuario tiene alguna reserva (de cualquier estado) en una fecha concreta.
     * @param userId Email del usuario
     * @param date Fecha (yyyy-MM-dd)
     * @param callback Callback con true si tiene reserva ese día
     */
    void hasReservationOnDate(String userId, String date, DataCallback<Boolean> callback);

    /**
     * Elimina el usuario y todas sus reservas.
     * @param email Email del usuario
     * @param password Contraseña
     * @param callback Callback con true si se eliminó correctamente
     */
    void deleteUser(String email, String password, DataCallback<Boolean> callback);

    /**
     * Elimina todas las reservas de un usuario.
     * @param email Email del usuario
     * @param callback Callback con true si se eliminaron correctamente
     */
    void deleteUserReservations(String email, DataCallback<Boolean> callback);

    /**
     * Obtiene las plazas disponibles de un tipo para una fecha y franja horaria.
     * @param tipo Tipo de plaza
     * @param fecha Fecha (yyyy-MM-dd)
     * @param horaInicio Hora de inicio en ms
     * @param horaFin Hora de fin en ms
     * @param excludeReservationId ID de reserva a excluir (puede ser null)
     * @param callback Callback con la lista de IDs de plazas disponibles
     */
    void getAvailablePlazas(String tipo, String fecha, long horaInicio, long horaFin, String excludeReservationId, DataCallback<List<String>> callback);

    /**
     * Asigna una plaza aleatoria disponible para una franja horaria.
     * @param tipo Tipo de plaza
     * @param fecha Fecha (yyyy-MM-dd)
     * @param horaInicio Hora de inicio en ms
     * @param horaFin Hora de fin en ms
     * @param excludeReservationId ID de reserva a excluir (puede ser null)
     * @param callback Callback con el ID de la plaza asignada o null si no hay
     */
    void assignRandomPlaza(String tipo, String fecha, long horaInicio, long horaFin, String excludeReservationId, DataCallback<String> callback);

    /**
     * Añade una nueva plaza al sistema.
     * @param plaza Objeto Plaza
     * @param callback Callback con true si se añadió correctamente
     */
    void addPlaza(Plaza plaza, DataCallback<Boolean> callback);

    /**
     * Elimina una plaza por su ID.
     * @param plazaId ID de la plaza
     * @param callback Callback con true si se eliminó correctamente
     */
    void deletePlaza(String plazaId, DataCallback<Boolean> callback);

    /**
     * Elimina una reserva por su ID (borrado físico).
     * @param reservaId ID de la reserva
     * @param callback Callback con true si se eliminó correctamente
     */
    void deleteReserva(String reservaId, DataCallback<Boolean> callback);

    /**
     * Obtiene los números disponibles en una fila concreta para una franja horaria.
     * @param tipo Tipo de plaza
     * @param row Fila (letra)
     * @param fecha Fecha (yyyy-MM-dd)
     * @param horaInicio Hora de inicio en ms
     * @param horaFin Hora de fin en ms
     * @param excludeReservationId ID de reserva a excluir (puede ser null)
     * @param callback Callback con la lista de números disponibles
     */
    void getAvailableNumbers(String tipo, String row, String fecha, long horaInicio, long horaFin, String excludeReservationId, DataCallback<List<String>> callback);

    /**
     * Comprueba si existe un usuario por email.
     * @param email Email
     * @param callback Callback con true si existe
     */
    void checkUserExists(String email, DataCallback<Boolean> callback);

    /**
     * Envía un email para restablecer la contraseña si el usuario existe.
     * @param email Email
     * @param callback Callback con true si se envió correctamente
     */
    void sendPasswordResetEmail(String email, DataCallback<Boolean> callback);

    /**
     * Obtiene las filas (letras) disponibles para un tipo de plaza.
     * @param tipo Tipo de plaza
     * @param callback Callback con la lista de filas
     */
    void getAvailableRows(String tipo, DataCallback<List<String>> callback);
}
