package com.pervyilokon.models;

public class RequestBody {
    private String action;
    private Object data;

    public RequestBody(String action, Object data) {
        this.action = action;
        this.data = data;
    }

    public String getAction() { return action; }
    public void setAction(String action) { this.action = action; }

    public Object getData() { return data; }
    public void setData(Object data) { this.data = data; }

    public static class StatusUpdate {
        private String id;
        private String status;

        public StatusUpdate(String id, String status) {
            this.id = id;
            this.status = status;
        }

        public String getId() { return id; }
        public String getStatus() { return status; }
    }

    public static class MasterUpdate {
        private String id;
        private String master;

        public MasterUpdate(String id, String master) {
            this.id = id;
            this.master = master;
        }

        public String getId() { return id; }
        public String getMaster() { return master; }
    }
}
