package com.lksnext.parkingplantilla.adapters;

import android.view.LayoutInflater;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.recyclerview.widget.RecyclerView;
import com.lksnext.parkingplantilla.databinding.CardReservationCurrentBinding;
import com.lksnext.parkingplantilla.domain.Reserva;
import java.util.ArrayList;
import java.util.List;
import android.content.Context;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.lksnext.parkingplantilla.R;
import com.lksnext.parkingplantilla.viewmodel.ReservationsViewModel;
import android.content.DialogInterface;
import androidx.fragment.app.FragmentActivity;
import com.lksnext.parkingplantilla.view.fragment.CreateReservationFragment;

public class ReservationsAdapter extends RecyclerView.Adapter<ReservationsAdapter.ReservationViewHolder> {

    private final ReservationsViewModel viewModel;

    public ReservationsAdapter(ReservationsViewModel viewModel) {
        this.viewModel = viewModel;
    }

    private List<Reserva> reservas = new ArrayList<>();

    @NonNull
    @Override
    public ReservationViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        CardReservationCurrentBinding binding = CardReservationCurrentBinding.inflate(
                LayoutInflater.from(parent.getContext()), parent, false);
        return new ReservationViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull ReservationViewHolder holder, int position) {
        holder.bind(reservas.get(position));
    }

    @Override
    public int getItemCount() {
        return reservas.size();
    }

    public void setReservas(List<Reserva> reservas) {
        this.reservas = reservas;
        notifyDataSetChanged();
    }

    class ReservationViewHolder extends RecyclerView.ViewHolder {
        private final CardReservationCurrentBinding binding;

        public ReservationViewHolder(CardReservationCurrentBinding binding) {
            super(binding.getRoot());
            this.binding = binding;

            // Boton de eliminar
            binding.deleteButton.setOnClickListener(v ->
                    showDeleteConfirmationDialog(binding.getRoot().getContext(),
                            binding.getReserva()));

            // Boton de editar
            binding.editButton.setOnClickListener(v ->
                    openEditReservationActivity(binding.getRoot().getContext(),
                            binding.getReserva()));
        }

        private void showDeleteConfirmationDialog(Context context, Reserva reserva) {
            MaterialAlertDialogBuilder builder = new MaterialAlertDialogBuilder(
                    context, R.style.ThemeOverlay_App_MaterialAlertDialog);

            builder.setTitle("Confirmar eliminación")
                    .setMessage("¿Estás seguro de que deseas eliminar esta reserva?")
                    .setPositiveButton("Eliminar", (dialog, which) -> {
                        // Llamar al ViewModel para eliminar la reserva
                        viewModel.deleteReservation(reserva.getId());
                    })
                    .setNegativeButton("Cancelar", (dialog, which) -> dialog.dismiss());

            // Crear y mostrar el diálogo, guardando la referencia como AlertDialog
            AlertDialog dialog = builder.create();
            dialog.show();

            // Botón positivo (Eliminar) en color rojo
            dialog.getButton(DialogInterface.BUTTON_POSITIVE)
                    .setTextColor(androidx.core.content.ContextCompat.getColor(context, R.color.red));
        }

        private void openEditReservationActivity(Context context, Reserva reserva) {
            // Abrir el fragmento en vez de la Activity
            FragmentActivity activity = (FragmentActivity) context;
            CreateReservationFragment fragment = new CreateReservationFragment();
            android.os.Bundle args = new android.os.Bundle();
            args.putBoolean("EDIT_MODE", true);
            args.putString("RESERVATION_ID", reserva.getId());
            args.putString("RESERVATION_TYPE", reserva.getPlaza().getTipo());
            args.putString("RESERVATION_DATE", reserva.getFecha());
            args.putLong("RESERVATION_START_TIME", reserva.getHora().getHoraInicio());
            args.putLong("RESERVATION_END_TIME", reserva.getHora().getHoraFin());
            args.putString("RESERVATION_SPOT", reserva.getPlaza().getId());
            fragment.setArguments(args);
            activity.getSupportFragmentManager()
                    .beginTransaction()
                    .replace(android.R.id.content, fragment)
                    .addToBackStack(null)
                    .commit();
        }

        public void bind(Reserva reserva) {
            binding.setReserva(reserva);
            binding.executePendingBindings();
        }
    }
}

