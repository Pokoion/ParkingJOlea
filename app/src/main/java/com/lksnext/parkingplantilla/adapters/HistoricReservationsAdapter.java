package com.lksnext.parkingplantilla.adapters;

import android.view.LayoutInflater;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.lksnext.parkingplantilla.databinding.CardReservationHistoricBinding;
import com.lksnext.parkingplantilla.domain.Reserva;
import java.util.ArrayList;
import java.util.List;

public class HistoricReservationsAdapter extends RecyclerView.Adapter<HistoricReservationsAdapter.ReservationViewHolder> {

    private List<Reserva> reservas = new ArrayList<>();

    @NonNull
    @Override
    public ReservationViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        CardReservationHistoricBinding binding = CardReservationHistoricBinding.inflate(
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

    static class ReservationViewHolder extends RecyclerView.ViewHolder {
        private final CardReservationHistoricBinding binding;

        public ReservationViewHolder(CardReservationHistoricBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }

        public void bind(Reserva reserva) {
            binding.setReserva(reserva);
            binding.executePendingBindings();
        }
    }
}