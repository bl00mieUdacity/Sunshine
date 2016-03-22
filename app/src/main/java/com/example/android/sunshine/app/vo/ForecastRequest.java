package com.example.android.sunshine.app.vo;

import android.net.Uri;

public class ForecastRequest {
    private static final String s_scheme = "http";
    private static final String s_authority = "api.openweathermap.org";
    private static final String[] s_path = {"data","2.5","forecast","daily"};

    private String m_city;
    private String m_units;
    private String m_mode;
    private int m_count;

    public ForecastRequest(String city, String units, String mode, int count) {
        m_city = city;
        m_units = units;
        m_mode = mode;
        m_count = count;
    }

    public String getCity() {
        return m_city;
    }

    public void setCity(String city) {
        this.m_city = city;
    }

    public String getUnits() {
        return m_units;
    }

    public void setUnits(String units) {
        this.m_units = units;
    }

    public String getMode() {
        return m_mode;
    }

    public void setMode(String mode) {
        this.m_mode = mode;
    }

    public int getCount() {
        return m_count;
    }

    public void setCount(int count) {
        this.m_count = count;
    }

    public Uri getUri(String appid) {
        Uri.Builder builder = new Uri.Builder();

        builder.scheme(s_scheme)
            .authority(s_authority);
        for (String s : s_path) {
            builder.appendPath(s);
        }
        builder.appendQueryParameter("q", m_city)
                .appendQueryParameter("mode", m_mode)
                .appendQueryParameter("units", m_units)
                .appendQueryParameter("count", ""+m_count)
                .appendQueryParameter("appid", appid);


        return builder.build();
    }

}
