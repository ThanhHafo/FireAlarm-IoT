package com.example.firealarm_iot;

import android.app.AlertDialog;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.Spinner;

import androidx.appcompat.app.AppCompatActivity;

import com.github.mikephil.charting.charts.BarChart;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.BarData;
import com.github.mikephil.charting.data.BarDataSet;
import com.github.mikephil.charting.data.BarEntry;
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

public class DataAnalysisActivity extends AppCompatActivity {
    private BarChart barChart;
    private Button btnSelectDate, btnExportData;
    private ListView eventListView;
    private DatabaseReference databaseReference;

    private int selectedMonth, selectedYear;
    private List<String> eventList = new ArrayList<>();
    private float totalTemperature = 0, gasCount = 0, flameCount = 0;
    private int totalEvents = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.statistical);

        // Khởi tạo Firebase
        databaseReference = FirebaseDatabase.getInstance().getReference("abnormalEvents");

        // Ánh xạ View
        barChart = findViewById(R.id.barChart);
        btnSelectDate = findViewById(R.id.btnSelectDate);
        btnExportData = findViewById(R.id.btnExportData);
        eventListView = findViewById(R.id.eventListView);

        btnSelectDate.setOnClickListener(view -> showDatePickerDialog());
        btnExportData.setOnClickListener(view -> showExportDialog());
    }

    // Hiển thị dialog chọn tháng và năm
    private void showDatePickerDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        View view = LayoutInflater.from(this).inflate(R.layout.dialog_date_picker, null);
        Spinner spinnerMonth = view.findViewById(R.id.spinnerMonth);
        Spinner spinnerYear = view.findViewById(R.id.spinnerYear);
        Button btnOk = view.findViewById(R.id.btnDateOk);

        // Tạo danh sách tháng và năm
        ArrayAdapter<String> monthAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, getMonths());
        spinnerMonth.setAdapter(monthAdapter);
        spinnerMonth.setSelection(Calendar.getInstance().get(Calendar.MONTH));

        ArrayAdapter<String> yearAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, getYears());
        spinnerYear.setAdapter(yearAdapter);
        spinnerYear.setSelection(0);

        builder.setView(view);
        AlertDialog dialog = builder.create();

        btnOk.setOnClickListener(v -> {
            selectedMonth = spinnerMonth.getSelectedItemPosition() + 1;
            selectedYear = Integer.parseInt(spinnerYear.getSelectedItem().toString());
            fetchFirebaseData();
            dialog.dismiss();
        });

        dialog.show();
    }

    // Lấy dữ liệu từ Firebase
    private void fetchFirebaseData() {
        databaseReference.get().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                resetCounts();
                eventList.clear();
                for (DataSnapshot snapshot : task.getResult().getChildren()) {
                    String startTime = snapshot.child("startTime").getValue(String.class);
                    if (isDateMatch(startTime)) {
                        processEvent(snapshot);
                    }
                }
                updateBarChart();
                updateListView();
            }
        });
    }

    private void processEvent(DataSnapshot snapshot) {
        float temperature = snapshot.child("temperature").getValue(Float.class);
        boolean flameDetected = snapshot.child("flameDetected").getValue(Boolean.class);
        int gasValue = snapshot.child("gasValue").getValue(Integer.class);
        // Xác định trạng thái gasDetected
        boolean gasDetected = false;
        if (gasValue > 1000) {
            gasDetected = true;
            gasCount++; // Tăng số lần phát hiện gas
        }
        if (temperature >= 40) {
            totalTemperature++; // Đếm số lần nhiệt độ > 40°C
        }
        if (flameDetected) flameCount++;
        totalEvents++;
        // Ghi lại eventList
        eventList.add("Cảnh báo: Nhiệt độ=" + temperature
                + ", Lửa=" + flameDetected
                + ", Gas=" + gasDetected);
    }


    // Cập nhật Bar Chart
    private void updateBarChart() {
        List<BarEntry> entries = new ArrayList<>();
        if (totalEvents > 0) {
            entries.add(new BarEntry(0, totalTemperature));
            entries.add(new BarEntry(1, gasCount));                     // Số lần phát hiện gas
            entries.add(new BarEntry(2, flameCount));                   // Số lần phát hiện lửa
        }
        BarDataSet dataSet = new BarDataSet(entries, "");
        dataSet.setColors(new int[]{R.color.red, R.color.green, R.color.yellow}, this);
        dataSet.setValueTextSize(14f);
        BarData barData = new BarData(dataSet);
        barData.setBarWidth(0.9f);
        // Cấu hình BarChart
        barChart.setData(barData);
        barChart.getDescription().setEnabled(false);
        barChart.setFitBars(true);
        // Gán nhãn trục X
        String[] labels = new String[]{"Nhiệt độ trên 40 (°C)","Cảnh báo Gas", "Cảnh báo lửa"};
        XAxis xAxis = barChart.getXAxis();
        xAxis.setValueFormatter(new IndexAxisValueFormatter(labels));
        xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);
        xAxis.setGranularity(1f);
        xAxis.setGranularityEnabled(true);
        xAxis.setTextSize(14f);

        YAxis leftAxis = barChart.getAxisLeft();
        leftAxis.setTextSize(14f);
        barChart.invalidate(); // Làm mới biểu đồ
    }

    private void updateListView() {
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, eventList);
        eventListView.setAdapter(adapter);
    }

    private void resetCounts() {
        totalTemperature = 0;
        gasCount = 0;
        flameCount = 0;
        totalEvents = 0;
    }

    private boolean isDateMatch(String startTime) {
        return startTime != null && startTime.startsWith(selectedYear + "-" + String.format("%02d", selectedMonth));
    }

    private List<String> getMonths() {
        List<String> months = new ArrayList<>();
        for (int i = 1; i <= 12; i++) months.add("Month " + i);
        return months;
    }

    private List<String> getYears() {
        List<String> years = new ArrayList<>();
        int currentYear = Calendar.getInstance().get(Calendar.YEAR);
        for (int i = currentYear; i >= currentYear - 10; i--) years.add(String.valueOf(i));
        return years;
    }

    // Hiển thị Export Dialog
    private void showExportDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Export Data");
        String message = "Trong tháng " + selectedMonth + "/" + selectedYear +
                ":\n- Số lần nhiệt độ cao: " + (totalTemperature) +
                "\n- Số lần phát hiện khí Gas: " + gasCount +
                "\n- Số lần phát hiện lửa: " + flameCount;
        builder.setMessage(message);
        builder.setPositiveButton("OK", null);
        builder.show();
    }
}
