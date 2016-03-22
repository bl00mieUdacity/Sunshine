package com.example.android.sunshine.app.vo;

public class Forecast {
    private String m_dateString;
    private String m_description;
    private double m_high;
    private double m_low;

    public String getDateString() {
        return m_dateString;
    }

    public void setDateString(String dateString) {
        m_dateString = dateString;
    }

    public String getDescription() {
        return m_description;
    }

    public void setDescription(String description) {
        m_description = description;
    }

    public double getHigh() {
        return m_high;
    }

    public void setHigh(double high) {
        m_high = high;
    }

    public double getLow() {
        return m_low;
    }

    public void setLow(double low) {
        m_low = low;
    }

    private String formatHighLows() {
        long roundedHigh = Math.round(m_high);
        long roundedLow = Math.round(m_low);
        return roundedHigh + "/" + roundedLow;
    }

    public String toString() {
        return m_dateString + " - " + m_description + " - " + formatHighLows();
    }
}
