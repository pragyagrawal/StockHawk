package com.sam_chordas.android.stockhawk.rest;

/**
 * Created by PRAGYA on 4/22/2016.
 */
public class HistoricalDataDetails {
    String symbol;
    String date;
    String open;
    String close;
    String low;
    String high;
    String volume;
    String adj_close;

    public String getSymbol() {
        return symbol;
    }

    public void setSymbol(String symbol) {
        this.symbol = symbol;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public String getOpen() {
        return open;
    }

    public void setOpen(String open) {
        this.open = open;
    }

    public String getClose() {
        return close;
    }

    public void setClose(String close) {
        this.close = close;
    }

    public String getLow() {
        return low;
    }

    public void setLow(String low) {
        this.low = low;
    }

    public String getHigh() {
        return high;
    }

    public void setHigh(String high) {
        this.high = high;
    }

    public String getVolume() {
        return volume;
    }

    public void setVolume(String volume) {
        this.volume = volume;
    }

    public String getAdj_close() {
        return adj_close;
    }

    public void setAdj_close(String adj_close) {
        this.adj_close = adj_close;
    }
}
