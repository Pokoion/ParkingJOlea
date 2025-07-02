package com.lksnext.parkingplantilla.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.lksnext.parkingplantilla.R;
import com.lksnext.parkingplantilla.domain.Plaza;

import java.util.List;

public class ReservationTypeAdapter extends RecyclerView.Adapter<ReservationTypeAdapter.TypeViewHolder> {

    private List<String> types;
    private OnTypeSelectedListener listener;
    private int selectedPosition = -1;

    public interface OnTypeSelectedListener {
        void onTypeSelected(String type);
    }

    public ReservationTypeAdapter(List<String> types, OnTypeSelectedListener listener) {
        this.types = types;
        this.listener = listener;
    }

    @NonNull
    @Override
    public TypeViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(
                R.layout.item_reservation_type, parent, false);
        return new TypeViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull TypeViewHolder holder, int position) {
        String type = types.get(position);
        holder.typeName.setText(type);

        // Configurar el icono según el tipo de plaza
        int resourceId = holder.itemView.getContext().getResources().getIdentifier(
                getIconName(type),
                "drawable",
                holder.itemView.getContext().getPackageName());
        holder.typeIcon.setImageResource(resourceId);

        int colorPrimary = ContextCompat.getColor(holder.itemView.getContext(), R.color.colorPrimary);
        int colorContainerBackground = ContextCompat.getColor(holder.itemView.getContext(), R.color.colorContainerBackground);

        holder.cardView.setCardBackgroundColor(
                selectedPosition == position ? colorPrimary : colorContainerBackground
        );

        holder.itemView.setOnClickListener(v -> {
            int previousSelected = selectedPosition;
            selectedPosition = holder.getBindingAdapterPosition();
            notifyItemChanged(previousSelected);
            notifyItemChanged(selectedPosition);
            listener.onTypeSelected(type);
        });
    }

    private String getIconName(String type) {
        return switch (type) {
            case Plaza.TIPO_MOTORCYCLE -> "ic_motorcycle";
            case Plaza.TIPO_CV_CHARGER -> "ic_cv_charger";
            case Plaza.TIPO_DISABLED -> "ic_disabled";
            default -> "ic_car";
        };
    }

    @Override
    public int getItemCount() {
        return types.size();
    }

    static class TypeViewHolder extends RecyclerView.ViewHolder {
        ImageView typeIcon;
        TextView typeName;
        CardView cardView;

        public TypeViewHolder(@NonNull View itemView) {
            super(itemView);
            typeIcon = itemView.findViewById(R.id.typeIcon);
            typeName = itemView.findViewById(R.id.typeName);
            cardView = (CardView) itemView;
        }
    }

    public void selectType(String type) {
        // Busca el tipo en la lista de tipos disponibles
        for (int i = 0; i < types.size(); i++) {
            if (types.get(i).equals(type)) {
                // Guarda la posición anterior para actualizar ese item
                int previousSelected = selectedPosition;
                // Actualiza el índice seleccionado
                selectedPosition = i;
                // Notifica al adaptador para actualizar los items cambiados
                notifyItemChanged(previousSelected);
                notifyItemChanged(selectedPosition);
                // Notifica al listener
                if (listener != null) {
                    listener.onTypeSelected(type);
                }
                break;
            }
        }
    }
}