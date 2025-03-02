package com.example.studentportal;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;

class CalendarAdapter extends RecyclerView.Adapter<CalendarViewHolder> {
    private final ArrayList<String> daysOfMonth;
    private final OnItemListener onItemListener;

    public CalendarAdapter(ArrayList<String> daysOfMonth, OnItemListener onItemListener) {
        this.daysOfMonth = daysOfMonth;
        this.onItemListener = onItemListener;
    }

    @NonNull
    @Override
    public CalendarViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        View view = inflater.inflate(R.layout.calendar_cell, parent, false);
        ViewGroup.LayoutParams layoutParams = view.getLayoutParams();

        // Высота ячейки (например, 40dp)
        int cellHeight = (int) (10 * parent.getContext().getResources().getDisplayMetrics().density);

        // Рассчитываем количество строк
        int rows = calculateNumberOfRows(daysOfMonth);
        layoutParams.height = cellHeight * rows;

        return new CalendarViewHolder(view, onItemListener);
    }

    @Override
    public void onBindViewHolder(@NonNull CalendarViewHolder holder, int position) {
        holder.dayOfMonth.setText(daysOfMonth.get(position));
    }

    @Override
    public int getItemCount() {
        return daysOfMonth.size();
    }

    // Метод для расчета количества строк
    private int calculateNumberOfRows(ArrayList<String> daysOfMonth) {
        int emptyCells = 0;
        for (String day : daysOfMonth) {
            if (day.isEmpty()) {
                emptyCells++;
            }
        }

        // Если пустых ячеек больше 7, значит, у нас 6 строк
        if (emptyCells > 7) {
            return 6;
        } else {
            return 5;
        }
    }

    public interface OnItemListener {
        void onItemClick(int position, String dayText);
    }
}