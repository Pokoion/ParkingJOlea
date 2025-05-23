package com.lksnext.parkingplantilla.adapters;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.viewpager2.adapter.FragmentStateAdapter;

import com.lksnext.parkingplantilla.view.fragment.CurrentReservationsFragment;
import com.lksnext.parkingplantilla.view.fragment.HistoricReservationsFragment;

public class ReservationsPagerAdapter extends FragmentStateAdapter {

    public ReservationsPagerAdapter(@NonNull Fragment fragment) {
        super(fragment);
    }

    @NonNull
    @Override
    public Fragment createFragment(int position) {
        return position == 0
                ? new CurrentReservationsFragment()
                : new HistoricReservationsFragment();
    }

    @Override
    public int getItemCount() {
        return 2;
    }
}