package demo.model;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

/**
 * Created by ws on 25.09.15.
 */
public class Info {
    String code;
    double rate;
    LocalDate date;

    private Info(String code, double rate, LocalDate date) throws Exception {
        if (code.length() != 3) throw new Exception("Длинна кода валюты должна быть 3 символа");
        this.code = code;
        this.rate = rate;
        this.date = date;
    }

    public static Info instance(String code) throws Exception {
        return new Info(code, 0, LocalDate.now().plusDays(1));
    }

    public static Info instance(String code, String date) throws Exception {
        return new Info(code, 0, LocalDate.parse(date, DateTimeFormatter.ISO_DATE));
    }

    public String getCode() {
        return code;
    }

    public String getRate() {
        return String.valueOf(rate);
    }

    public String getDate() {
        return date.format(DateTimeFormatter.ISO_DATE);
    }
}
