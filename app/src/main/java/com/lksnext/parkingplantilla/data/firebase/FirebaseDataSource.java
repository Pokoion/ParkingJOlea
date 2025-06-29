package com.lksnext.parkingplantilla.data.firebase;

import androidx.annotation.NonNull;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.lksnext.parkingplantilla.data.repository.DataSource;
import com.lksnext.parkingplantilla.domain.DataCallback;
import com.lksnext.parkingplantilla.domain.Hora;
import com.lksnext.parkingplantilla.domain.Plaza;
import com.lksnext.parkingplantilla.domain.Reserva;
import com.lksnext.parkingplantilla.domain.User;
import com.lksnext.parkingplantilla.utils.DateUtils;

import android.util.Log;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class FirebaseDataSource implements DataSource {
    private final FirebaseAuth mAuth;
    private final FirebaseFirestore db;

    public FirebaseDataSource() {
        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();
    }

    @Override
    public void login(String email, String password, DataCallback<User> callback) {
        mAuth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        FirebaseUser firebaseUser = mAuth.getCurrentUser();
                        if (firebaseUser != null) {
                            db.collection("users").document(firebaseUser.getEmail())
                                    .get().addOnSuccessListener(documentSnapshot -> {
                                        if (documentSnapshot.exists()) {
                                            String name = documentSnapshot.getString("name");
                                            callback.onSuccess(new User(name, firebaseUser.getEmail(), null));
                                        } else {
                                            callback.onSuccess(new User(firebaseUser.getEmail(), firebaseUser.getEmail(), null));
                                        }
                                    })
                                    .addOnFailureListener(e -> {
                                        callback.onFailure(e);
                                    });
                        } else {
                            callback.onFailure(new Exception("Usuario no encontrado"));
                        }
                    } else {
                        callback.onFailure(task.getException());
                    }
                });
    }

    @Override
    public void register(String name, String email, String password, DataCallback<User> callback) {
        // Comprobar si el usuario ya existe antes de registrar
        checkUserExists(email, new DataCallback<Boolean>() {
            @Override
            public void onSuccess(Boolean exists) {
                if (exists) {
                    callback.onFailure(new Exception("EMAIL_ALREADY_EXISTS"));
                    return;
                }
                mAuth.createUserWithEmailAndPassword(email, password)
                        .addOnCompleteListener(task -> {
                            if (task.isSuccessful()) {
                                FirebaseUser firebaseUser = mAuth.getCurrentUser();
                                if (firebaseUser != null) {
                                    Map<String, Object> userMap = new HashMap<>();
                                    userMap.put("name", name);
                                    userMap.put("email", email);
                                    db.collection("users").document(email).set(userMap)
                                            .addOnSuccessListener(aVoid -> {
                                                callback.onSuccess(new User(name, email, null));
                                            })
                                            .addOnFailureListener(e -> {
                                                callback.onFailure(e);
                                            });
                                } else {
                                    callback.onFailure(new Exception("Error al crear usuario"));
                                }
                            } else {
                                callback.onFailure(task.getException());
                            }
                        });
            }
            @Override
            public void onFailure(Exception e) {
                callback.onFailure(e);
            }
        });
    }

    @Override
    public void getReservations(String userId, DataCallback<List<Reserva>> callback) {
        db.collection("reservas")
                .whereEqualTo("usuario", userId)
                .whereEqualTo("estado", Reserva.Estado.ACTIVA.name())
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    List<Reserva> reservas = new ArrayList<>();
                    for (QueryDocumentSnapshot doc : queryDocumentSnapshots) {
                        Reserva reserva = doc.toObject(Reserva.class);
                        reservas.add(reserva);
                    }
                    callback.onSuccess(reservas);
                })
                .addOnFailureListener(callback::onFailure);
    }

    @Override
    public void getHistoricReservations(String userId, DataCallback<List<Reserva>> callback) {
        db.collection("reservas")
                .whereEqualTo("usuario", userId)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    List<Reserva> historic = new ArrayList<>();
                    for (QueryDocumentSnapshot doc : queryDocumentSnapshots) {
                        Reserva reserva = doc.toObject(Reserva.class);
                        if ((reserva.getEstado() == Reserva.Estado.FINALIZADA || reserva.getEstado() == Reserva.Estado.CANCELADA)) {
                            historic.add(reserva);
                        }
                    }
                    callback.onSuccess(historic);
                })
                .addOnFailureListener(callback::onFailure);
    }

    @Override
    public void getCurrentReservation(String userId, DataCallback<Reserva> callback) {
        db.collection("reservas")
                .whereEqualTo("usuario", userId)
                .whereEqualTo("estado", Reserva.Estado.ACTIVA.name())
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    Reserva current = null;
                    for (QueryDocumentSnapshot doc : queryDocumentSnapshots) {
                        Reserva reserva = doc.toObject(Reserva.class);
                        // Aquí puedes agregar lógica para filtrar la reserva actual según la hora
                        current = reserva;
                        break;
                    }
                    callback.onSuccess(current);
                })
                .addOnFailureListener(callback::onFailure);
    }

    @Override
    public void getNextReservation(String userId, DataCallback<Reserva> callback) {
        db.collection("reservas")
                .whereEqualTo("usuario", userId)
                .whereEqualTo("estado", Reserva.Estado.ACTIVA.name())
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    Reserva next = null;
                    long minStart = Long.MAX_VALUE;
                    for (QueryDocumentSnapshot doc : queryDocumentSnapshots) {
                        Reserva reserva = doc.toObject(Reserva.class);
                        if (reserva.getHora().getHoraInicio() < minStart) {
                            minStart = reserva.getHora().getHoraInicio();
                            next = reserva;
                        }
                    }
                    callback.onSuccess(next);
                })
                .addOnFailureListener(callback::onFailure);
    }

    @Override
    public void deleteReservation(String reservationId, DataCallback<Boolean> callback) {
        db.collection("reservas").document(reservationId)
                .update("estado", Reserva.Estado.CANCELADA.name())
                .addOnSuccessListener(aVoid -> callback.onSuccess(true))
                .addOnFailureListener(callback::onFailure);
    }

    @Override
    public void updateReservation(Reserva reserva, DataCallback<Boolean> callback) {
        db.collection("reservas").document(reserva.getId())
                .set(reserva)
                .addOnSuccessListener(aVoid -> callback.onSuccess(true))
                .addOnFailureListener(callback::onFailure);
    }

    @Override
    public void createReservation(Reserva reserva, DataCallback<Boolean> callback) {
        long now = System.currentTimeMillis();
        java.util.Date reservaStart = DateUtils.getReservaDateTime(reserva);
        if (reservaStart.getTime() < now - 2 * 60 * 1000) {
            callback.onFailure(new Exception("La hora de inicio no puede ser más de 2 minutos anterior al momento actual"));
            return;
        }
        // Comprobar si ya existe una reserva activa para ese usuario y fecha
        db.collection("reservas")
                .whereEqualTo("usuario", reserva.getUsuario())
                .whereEqualTo("fecha", reserva.getFecha())
                .whereEqualTo("estado", Reserva.Estado.ACTIVA.name())
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    if (!queryDocumentSnapshots.isEmpty()) {
                        callback.onFailure(new Exception("Ya tienes una reserva activa para este día"));
                        return;
                    }
                    // Comprobar disponibilidad antes de crear la reserva (sin excluir ninguna, porque es nueva)
                    checkAvailability(reserva, null, new DataCallback<Boolean>() {
                        @Override
                        public void onSuccess(Boolean available) {
                            if (!available) {
                                callback.onSuccess(false);
                                return;
                            }
                            String id = db.collection("reservas").document().getId();
                            reserva.setId(id);
                            db.collection("reservas").document(id)
                                    .set(reserva)
                                    .addOnSuccessListener(aVoid -> callback.onSuccess(true))
                                    .addOnFailureListener(callback::onFailure);
                        }
                        @Override
                        public void onFailure(Exception e) {
                            callback.onFailure(e);
                        }
                    });
                })
                .addOnFailureListener(callback::onFailure);
    }

    @Override
    public void getAvailablePlazas(String tipo, String fecha, long horaInicio, long horaFin, String excludeReservationId, DataCallback<List<String>> callback) {
        db.collection("plazas")
                .whereEqualTo("tipo", tipo)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    List<String> disponibles = new ArrayList<>();
                    for (QueryDocumentSnapshot doc : queryDocumentSnapshots) {
                        Plaza plaza = doc.toObject(Plaza.class);
                        disponibles.add(plaza.getId());
                    }
                    // Ahora filtrar por reservas activas en ese horario
                    db.collection("reservas")
                            .whereEqualTo("fecha", fecha)
                            .whereEqualTo("estado", Reserva.Estado.ACTIVA.name())
                            .get()
                            .addOnSuccessListener(reservasSnap -> {
                                List<String> ocupadas = new ArrayList<>();
                                for (QueryDocumentSnapshot rdoc : reservasSnap) {
                                    Reserva r = rdoc.toObject(Reserva.class);
                                    if (excludeReservationId != null && !excludeReservationId.isEmpty() && excludeReservationId.equals(r.getId())) {
                                        continue;
                                    }
                                    if (r.getHora().getHoraInicio() < horaFin && r.getHora().getHoraFin() > horaInicio) {
                                        ocupadas.add(r.getPlaza().getId());
                                    }
                                }
                                disponibles.removeAll(ocupadas);
                                callback.onSuccess(disponibles);
                            })
                            .addOnFailureListener(callback::onFailure);
                })
                .addOnFailureListener(callback::onFailure);
    }

    @Override
    public void assignRandomPlaza(String tipo, String fecha, long horaInicio, long horaFin, String excludeReservationId, DataCallback<String> callback) {
        getAvailablePlazas(tipo, fecha, horaInicio, horaFin, excludeReservationId, new DataCallback<List<String>>() {
            @Override
            public void onSuccess(List<String> disponibles) {
                if (disponibles.isEmpty()) {
                    callback.onSuccess(null);
                } else {
                    int idx = (int) (Math.random() * disponibles.size());
                    callback.onSuccess(disponibles.get(idx));
                }
            }
            @Override
            public void onFailure(Exception e) {
                callback.onFailure(e);
            }
        });
    }

    @Override
    public void checkAvailability(Reserva reserva, String excludeReservationId, DataCallback<Boolean> callback) {
        db.collection("reservas")
                .whereEqualTo("plaza.id", reserva.getPlaza().getId())
                .whereEqualTo("fecha", reserva.getFecha())
                .whereEqualTo("estado", Reserva.Estado.ACTIVA.name())
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    boolean available = true;
                    for (QueryDocumentSnapshot doc : queryDocumentSnapshots) {
                        Reserva r = doc.toObject(Reserva.class);
                        if (excludeReservationId != null && !excludeReservationId.isEmpty() && excludeReservationId.equals(r.getId())) {
                            continue;
                        }
                        if (r.getHora().getHoraInicio() < reserva.getHora().getHoraFin() && r.getHora().getHoraFin() > reserva.getHora().getHoraInicio()) {
                            available = false;
                            break;
                        }
                    }
                    callback.onSuccess(available);
                })
                .addOnFailureListener(callback::onFailure);
    }

    @Override
    public void hasReservationOnDate(String userId, String date, DataCallback<Boolean> callback) {
        db.collection("reservas")
                .whereEqualTo("usuario", userId)
                .whereEqualTo("fecha", date)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    boolean has = !queryDocumentSnapshots.isEmpty();
                    callback.onSuccess(has);
                })
                .addOnFailureListener(callback::onFailure);
    }

    @Override
    public void getAvailableNumbers(String tipo, String row, String fecha, long horaInicio, long horaFin, String excludeReservationId, DataCallback<List<String>> callback) {
        db.collection("plazas")
                .whereEqualTo("tipo", tipo)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    List<String> disponibles = new ArrayList<>();
                    for (QueryDocumentSnapshot doc : queryDocumentSnapshots) {
                        Plaza plaza = doc.toObject(Plaza.class);
                        if (plaza.getId().startsWith(row + "-")) {
                            disponibles.add(plaza.getId().split("-")[1]);
                        }
                    }
                    // Filtrar por reservas activas
                    db.collection("reservas")
                            .whereEqualTo("fecha", fecha)
                            .whereEqualTo("estado", Reserva.Estado.ACTIVA.name())
                            .get()
                            .addOnSuccessListener(reservasSnap -> {
                                List<String> ocupadas = new ArrayList<>();
                                for (QueryDocumentSnapshot rdoc : reservasSnap) {
                                    Reserva r = rdoc.toObject(Reserva.class);
                                    if (excludeReservationId != null && !excludeReservationId.isEmpty() && excludeReservationId.equals(r.getId())) {
                                        continue;
                                    }
                                    if (r.getPlaza().getId().startsWith(row + "-") && r.getHora().getHoraInicio() < horaFin && r.getHora().getHoraFin() > horaInicio) {
                                        ocupadas.add(r.getPlaza().getId().split("-")[1]);
                                    }
                                }
                                disponibles.removeAll(ocupadas);
                                callback.onSuccess(disponibles);
                            })
                            .addOnFailureListener(callback::onFailure);
                })
                .addOnFailureListener(callback::onFailure);
    }

    @Override
    public void checkUserExists(String email, DataCallback<Boolean> callback) {
        db.collection("users").document(email).get()
            .addOnSuccessListener(documentSnapshot -> {
                callback.onSuccess(documentSnapshot.exists());
            })
            .addOnFailureListener(callback::onFailure);
    }

    @Override
    public void sendPasswordResetEmail(String email, DataCallback<Boolean> callback) {
        checkUserExists(email, new DataCallback<Boolean>() {
            @Override
            public void onSuccess(Boolean exists) {
                if (!exists) {
                    callback.onSuccess(false);
                    return;
                }
                mAuth.sendPasswordResetEmail(email)
                    .addOnCompleteListener(task -> {
                        if (task.isSuccessful()) {
                            callback.onSuccess(true);
                        } else {
                            callback.onFailure(task.getException());
                        }
                    });
            }
            @Override
            public void onFailure(Exception e) {
                callback.onFailure(e);
            }
        });
    }

    @Override
    public void deleteUserReservations(String email, DataCallback<Boolean> callback) {
        db.collection("reservas")
            .whereEqualTo("usuario", email)
            .get()
            .addOnSuccessListener(queryDocumentSnapshots -> {
                for (QueryDocumentSnapshot doc : queryDocumentSnapshots) {
                    doc.getReference().delete();
                }
                callback.onSuccess(true);
            })
            .addOnFailureListener(callback::onFailure);
    }

    @Override
    public void deleteUser(String email, String password, DataCallback<Boolean> callback) {
        db.collection("users").document(email).delete()
            .addOnSuccessListener(aVoid -> {
                deleteUserReservations(email, new DataCallback<Boolean>() {
                    @Override
                    public void onSuccess(Boolean result) {
                        // Buscar el usuario en Auth y eliminarlo si está logueado
                        FirebaseUser currentUser = mAuth.getCurrentUser();
                        if (currentUser != null && currentUser.getEmail() != null && currentUser.getEmail().equals(email)) {
                            currentUser.delete()
                                .addOnSuccessListener(aVoid2 -> callback.onSuccess(true))
                                .addOnFailureListener(e -> callback.onFailure(e));
                        } else {
                            // Intentar loguear para eliminar si no está logueado
                            mAuth.signInWithEmailAndPassword(email, password)
                                .addOnSuccessListener(authResult -> {
                                    FirebaseUser userToDelete = mAuth.getCurrentUser();
                                    if (userToDelete != null) {
                                        userToDelete.delete()
                                            .addOnSuccessListener(aVoid3 -> callback.onSuccess(true))
                                            .addOnFailureListener(e -> callback.onFailure(e));
                                    } else {
                                        callback.onSuccess(true);
                                    }
                                })
                                .addOnFailureListener(e -> callback.onSuccess(true)); // Si no existe en Auth, consideramos borrado
                        }
                    }
                    @Override
                    public void onFailure(Exception e) {
                        callback.onFailure(e);
                    }
                });
            })
            .addOnFailureListener(callback::onFailure);
    }

    public void addPlaza(Plaza plaza, DataCallback<Boolean> callback) {
        db.collection("plazas").document(plaza.getId()).set(plaza)
            .addOnSuccessListener(aVoid -> callback.onSuccess(true))
            .addOnFailureListener(callback::onFailure);
    }

    public void deletePlaza(String plazaId, DataCallback<Boolean> callback) {
        db.collection("plazas").document(plazaId).delete()
            .addOnSuccessListener(aVoid -> callback.onSuccess(true))
            .addOnFailureListener(callback::onFailure);
    }

    public void deleteReserva(String reservaId, DataCallback<Boolean> callback) {
        db.collection("reservas").document(reservaId).delete()
            .addOnSuccessListener(aVoid -> callback.onSuccess(true))
            .addOnFailureListener(callback::onFailure);
    }

    @Override
    public void getAvailableRows(String tipo, DataCallback<List<String>> callback) {
        db.collection("plazas")
            .whereEqualTo("tipo", tipo)
            .get()
            .addOnSuccessListener(queryDocumentSnapshots -> {
                List<String> rows = new ArrayList<>();
                for (QueryDocumentSnapshot doc : queryDocumentSnapshots) {
                    Plaza plaza = doc.toObject(Plaza.class);
                    String id = plaza.getId();
                    if (id != null && id.contains("-")) {
                        String row = id.split("-")[0];
                        if (!rows.contains(row)) {
                            rows.add(row);
                        }
                    }
                }
                callback.onSuccess(rows);
            })
            .addOnFailureListener(callback::onFailure);
    }
}
