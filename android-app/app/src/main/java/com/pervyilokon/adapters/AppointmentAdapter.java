package com.pervyilokon.adapters;

import android.content.Intent;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.chip.Chip;
import com.pervyilokon.R;
import com.pervyilokon.models.Appointment;
import java.util.List;

public class AppointmentAdapter extends RecyclerView.Adapter<AppointmentAdapter.ViewHolder> {
    private List<Appointment> appointments;
    private OnAppointmentClickListener listener;

    public interface OnAppointmentClickListener {
        void onAppointmentClick(Appointment appointment);
        void onStatusClick(Appointment appointment);
        void onCompleteClick(Appointment appointment);
    }

    public AppointmentAdapter(List<Appointment> appointments, OnAppointmentClickListener listener) {
        this.appointments = appointments;
        this.listener = listener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_appointment, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Appointment appointment = appointments.get(position);
        holder.bind(appointment, listener);
    }

    @Override
    public int getItemCount() {
        return appointments.size();
    }

    public void updateData(List<Appointment> newAppointments) {
        appointments.clear();
        appointments.addAll(newAppointments);
        notifyDataSetChanged();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        private TextView tvClientName, tvPhone, tvService, tvTime, tvChildAge, tvPrice, tvNotes;
        private Chip chipStatus, chipMaster;
        private MaterialButton btnCall, btnComplete;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvClientName = itemView.findViewById(R.id.tvClientName);
            tvPhone = itemView.findViewById(R.id.tvPhone);
            tvService = itemView.findViewById(R.id.tvService);
            tvTime = itemView.findViewById(R.id.tvTime);
            tvChildAge = itemView.findViewById(R.id.tvChildAge);
            tvPrice = itemView.findViewById(R.id.tvPrice);
            tvNotes = itemView.findViewById(R.id.tvNotes);
            chipStatus = itemView.findViewById(R.id.chipStatus);
            chipMaster = itemViewViewById(R.id.chipMaster);
            btnCall = itemView.findViewById(R.id.btnCall);
            btnComplete = itemView.findViewById(R.id.btnComplete);
        }

        public void bind(Appointment appointment, OnAppointmentClickListener listener) {
            tvClientName.setText(appointment.getClientName());
            tvPhone.setText(appointment.getPhone());
            tvService.setText(appointment.getService());
            tvTime.setText(appointment.getTime());
            tvChildAge.setText("Возраст: " + appointment.getChildAge() + " лет");
            tvPrice.setText(appointment.getPrice() + " руб");

            if (appointment.getNotes() != null && !appointment.getNotes().isEmpty()) {
                tvNotes.setText(appointment.getNotes());
                tvNotes.setVisibility(View.VISIBLE);
            } else {
                tvNotes.setVisibility(View.GONE);
            }

            chipStatus.setText(appointment.getStatus());
            setStatusColor(appointment.getStatus());

            String master = appointment.getMaster();
            if (master != null && !master.isEmpty()) {
                chipMaster.setText(master);
            } else {
                chipMaster.setText("Не назначен");
            }

            itemView.setOnClickListener(v -> listener.onAppointmentClick(appointment));
            chipStatus.setOnClickListener(v -> listener.onStatusClick(appointment));
            btnCall.setOnClickListener(v -> {
                Intent intent = new Intent(Intent.ACTION_DIAL);
                intent.setData(Uri.parse("tel:" + appointment.getPhone()));
                itemView.getContext().startActivity(intent);
            });
            btnComplete.setOnClickListener(v -> listener.onCompleteClick(appointment));

            if ("выполнена".equals(appointment.getStatus())) {
                btnComplete.setVisibility(View.GONE);
            } else {
                btnComplete.setVisibility(View.VISIBLE);
            }
        }

        private void setStatusColor(String status) {
            int colorRes;
            switch (status) {
                case "новая":
                    colorRes = R.color.status_new;
                    break;
                case "подтверждена":
                    colorRes = R.color.status_confirmed;
                    break;
                case "выполнена":
                    colorRes = R.color.status_completed;
                    break;
                case "отменена":
                    colorRes = R.color.status_cancelled;
                    break;
                default:
                    colorRes = R.color.status_new;
            }
            chipStatus.setChipBackgroundColorResource(colorRes);
        }
    }
}
