package com.bookstore.dto;

import java.util.List;

public class TrackingResponseDTO {
    private String orderId;
    private String provider;
    private String trackingNumber;
    private String status;
    private List<TrackingDetail> history;

    public TrackingResponseDTO() {
    }

    public TrackingResponseDTO(String orderId, String provider, String trackingNumber, String status,
            List<TrackingDetail> history) {
        this.orderId = orderId;
        this.provider = provider;
        this.trackingNumber = trackingNumber;
        this.status = status;
        this.history = history;
    }

    public String getOrderId() {
        return orderId;
    }

    public void setOrderId(String orderId) {
        this.orderId = orderId;
    }

    public String getProvider() {
        return provider;
    }

    public void setProvider(String provider) {
        this.provider = provider;
    }

    public String getTrackingNumber() {
        return trackingNumber;
    }

    public void setTrackingNumber(String trackingNumber) {
        this.trackingNumber = trackingNumber;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public List<TrackingDetail> getHistory() {
        return history;
    }

    public void setHistory(List<TrackingDetail> history) {
        this.history = history;
    }

    public static class TrackingDetail {
        private String time;
        private String status;
        private String location;

        public TrackingDetail() {
        }

        public TrackingDetail(String time, String status, String location) {
            this.time = time;
            this.status = status;
            this.location = location;
        }

        public String getTime() {
            return time;
        }

        public void setTime(String time) {
            this.time = time;
        }

        public String getStatus() {
            return status;
        }

        public void setStatus(String status) {
            this.status = status;
        }

        public String getLocation() {
            return location;
        }

        public void setLocation(String location) {
            this.location = location;
        }
    }
}
