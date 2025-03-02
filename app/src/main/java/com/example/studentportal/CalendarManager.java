package com.example.studentportal;

import android.content.Context;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.studentportal.CalendarAdapter;

import java.time.LocalDate;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Locale;

public class CalendarManager implements CalendarAdapter.OnItemListener {
    private TextView monthYearText;
    private RecyclerView calendarRecyclerView;
    private LocalDate selectedDate;

    public CalendarManager(TextView monthYearText, RecyclerView calendarRecyclerView) {
        this.monthYearText = monthYearText;
        this.calendarRecyclerView = calendarRecyclerView;
        this.selectedDate = LocalDate.now();
        setMonthView();
    }

    private void setMonthView() {
        monthYearText.setText(monthYearFromDate(selectedDate));
        ArrayList<String> daysInMonth = daysInMonthArray(selectedDate);

        CalendarAdapter calendarAdapter = new CalendarAdapter(daysInMonth, this);
        RecyclerView.LayoutManager layoutManager = new GridLayoutManager(calendarRecyclerView.getContext(), 7);
        calendarRecyclerView.setLayoutManager(layoutManager);
        calendarRecyclerView.setAdapter(calendarAdapter);
    }

    private ArrayList<String> daysInMonthArray(LocalDate date) {
        ArrayList<String> daysInMonthArray = new ArrayList<>();
        YearMonth yearMonth = YearMonth.from(date);
        int daysInMonth = yearMonth.lengthOfMonth();
        LocalDate firstOfMonth = selectedDate.withDayOfMonth(1);
        int dayOfWeek = firstOfMonth.getDayOfWeek().getValue();

        for (int i = 1; i <= 42; i++) {
            if (i <= dayOfWeek || i > daysInMonth + dayOfWeek) {
                daysInMonthArray.add("");
            } else {
                daysInMonthArray.add(String.valueOf(i - dayOfWeek));
            }
        }
        return daysInMonthArray;
    }

    private String monthYearFromDate(LocalDate date) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("MMMM yyyy", new Locale("ru"));
        String monthYear = date.format(formatter);
        monthYear = monthYear.replace("января", "январь")
                .replace("февраля", "февраль")
                .replace("марта", "март")
                .replace("апреля", "апрель")
                .replace("мая", "май")
                .replace("июня", "июнь")
                .replace("июля", "июль")
                .replace("августа", "август")
                .replace("сентября", "сентябрь")
                .replace("октября", "октябрь")
                .replace("ноября", "ноябрь")
                .replace("декабря", "декабрь");

        return Character.toUpperCase(monthYear.charAt(0)) + monthYear.substring(1);
    }

    public void previousMonth() {
        selectedDate = selectedDate.minusMonths(1);
        setMonthView();
    }

    public void nextMonth() {
        selectedDate = selectedDate.plusMonths(1);
        setMonthView();
    }

    @Override
    public void onItemClick(int position, String dayText) {
        if (!dayText.equals("")) {
            String message = "Selected Date " + dayText + " " + monthYearFromDate(selectedDate);
            Toast.makeText(monthYearText.getContext(), message, Toast.LENGTH_LONG).show();
        }
    }
}
