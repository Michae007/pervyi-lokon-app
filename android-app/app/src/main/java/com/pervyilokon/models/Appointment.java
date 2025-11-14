package com.pervyilokon.models;

public class Appointment {
    private String id;
    private String timestamp;
    private String clientName;
    private String phone;
    private String childAge;
    private String service;
    private String price;
    private String date;
    private String time;
    private String duration;
    private String master;
    private String status;
    private String notes;
    private String source;

    public Appointment() {}

    // Геттеры и сеттеры
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getTimestamp() { return timestamp; }
    public void setTimestamp(String timestamp) { this.timestamp = timestamp; }

    public String getClientName() { return clientName; }
    public void setClientName(String clientName) { this.clientName = clientName; }

    public String getPhone() { return phone; }
    public void setPhone(String phone) { this.phone = phone; }

    public String getChildAge() { return childAge; }
    public void setChildAge(String childAge) { this.childAge = childAge; }

    public String getService() { return service; }
    public void setService(String service) { this.service = service; }

    public String getPrice() { return price; }
    public void setPrice(String price) { this.price = price; }

    public String getDate() { return date; }
    public void setDate(String date) { this.date = date; }

    public String getTime() { return time; }
    public void setTime(String time) { this.time = time; }

    public String getDuration() { return duration; }
    public void setDuration(String duration) { this.duration = duration; }

    public String getMaster() { return master; }
    public void setMaster(String master) { this.master = master; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public String getNotes() { return notes; }
    public void setNotes(String notes) { this.notes = notes; }

    public String getSource() { return source; }
    public void setSource(String source) { this.source = source; }
}
