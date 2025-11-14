package com.pervyilokon.models;

import java.util.List;

public class ApiResponse {
    private boolean success;
    private String id;
    private String error;
    private List<Appointment> appointments;

    public boolean isSuccess() { return success; }
    public void setSuccess(boolean success) { this.success = success; }

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getError() { return error; }
    public void setError(String error) { this.error = error; }

    public List<Appointment> getAppointments() { return appointments; }
    public void setAppointments(List<Appointment> appointments) { this.appointments = appointments; }
}
