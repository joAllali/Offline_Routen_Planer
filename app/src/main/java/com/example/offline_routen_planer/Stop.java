package com.example.offline_routen_planer;

public class Stop {
    int stop_id,arrivalTime;
    String stop_name;
    int conn_last;

    public Stop(int stop_id, int arrivalTime, String stop_name, int conn_last) {
        this.stop_id = stop_id;
        this.arrivalTime = arrivalTime;
        this.stop_name = stop_name;
        this.conn_last = conn_last;

    }

    public int getStop_id() {
        return stop_id;
    }

    public void setStop_id(int stop_id) {
        this.stop_id = stop_id;
    }

    public int getArrivalTime() {
        return arrivalTime;
    }

    public void setArrivalTime(int arrivalTime) {
        this.arrivalTime = arrivalTime;
    }

    public String getStop_name() {
        return stop_name;
    }

    public void setStop_name(String stop_name) {
        this.stop_name = stop_name;
    }

    public int getConn_last() {
        return conn_last;
    }

    public void setConn_last(int conn_last) {
        this.conn_last = conn_last;
    }

}

