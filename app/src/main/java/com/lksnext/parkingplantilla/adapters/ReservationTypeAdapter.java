package com.lksnext.parkingplantilla.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;

import com.lksnext.parkingplantilla.R;

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

        holder.cardView.setCardBackgroundColor(
                selectedPosition == position ?
                        holder.itemView.getContext().getResources().getColor(R.color.colorPrimary) :
                        holder.itemView.getContext().getResources().getColor(R.color.colorContainerBackground));

        holder.itemView.setOnClickListener(v -> {
            int previousSelected = selectedPosition;
            selectedPosition = holder.getAdapterPosition();
            notifyItemChanged(previousSelected);
            notifyItemChanged(selectedPosition);
            listener.onTypeSelected(type);
        });
    }

    private String getIconName(String type) {
        switch (type) {
            case "Standard": return "ic_car";
            case "Motorcycle": return "ic_motorcycle";
            case "CV Charger": return "ic_cv_charger";
            case "Disabled": return "ic_disabled";
            default: return "ic_car";
        }
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
}