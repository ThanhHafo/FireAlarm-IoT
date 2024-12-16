package com.example.firealarm_iot;

import android.os.Bundle;
import android.util.Log;
import android.widget.DatePicker;
import android.widget.ListView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Locale;

public class history_environment extends AppCompatActivity {

    private DatePicker datePicker;
    private ListView listView;
    private DatabaseReference databaseReference;
    private ArrayList<String> reportList;
    private ReportAdapter reportAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.history_environment);

        // Ánh xạ các view từ layout
        datePicker = findViewById(R.id.datePicker);
        listView = findViewById(R.id.listView);

        // Khởi tạo Firebase Database
        databaseReference = FirebaseDatabase.getInstance().getReference();

        // Khởi tạo danh sách báo cáo và adapter
        reportList = new ArrayList<>();
        reportAdapter = new ReportAdapter(this, reportList);
        listView.setAdapter(reportAdapter);

        // Đọc dữ liệu khi người dùng chọn ngày
        datePicker.setOnDateChangedListener((view, year, monthOfYear, dayOfMonth) -> readHistoryData(year, monthOfYear + 1, dayOfMonth));

        // Đọc dữ liệu cho ngày hiện tại khi khởi tạo activity
        int currentYear = datePicker.getYear();
        int currentMonth = datePicker.getMonth() + 1;
        int currentDay = datePicker.getDayOfMonth();
        readHistoryData(currentYear, currentMonth, currentDay);
    }

    private void readHistoryData(int year, int month, int day) {
        String selectedDate = String.format(Locale.getDefault(), "%04d-%02d-%02d", year, month, day);

        databaseReference.child("abnormalEvents").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                reportList.clear();

                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    String timestamp = snapshot.getKey();

                    // Kiểm tra xem timestamp có thuộc ngày đã chọn không
                    if (timestamp != null && timestamp.startsWith(selectedDate)) {
                        // Đọc dữ liệu từ Firebase
                        String startTime = snapshot.child("startTime").getValue(String.class);
                        String endTime = snapshot.child("endTime").getValue(String.class);
                        Long duration = snapshot.child("duration").getValue(Long.class);
                        Integer gasValue = snapshot.child("gasValue").getValue(Integer.class);
                        Boolean flameDetected = snapshot.child("flameDetected").getValue(Boolean.class);
                        Double temperature = snapshot.child("temperature").getValue(Double.class);
                        // Xác định trạng thái khí gas
                        String gasDetected = (gasValue != null && gasValue >= 1000) ? "Có" : "Không";
                        StringBuilder reportBuilder = new StringBuilder();
                        reportBuilder.append("Thời gian xảy ra: ").append(startTime).append("\n")
                                .append("Thơ gian kết thúc: ").append(endTime != null ? endTime : "Vẫn còn đang diễn ra").append("\n")
                                .append("Thời lượng: ").append(duration != null ? duration + " giây" : "N/A").append("\n");
                        reportBuilder.append("Phát hiện khí Gas: ").append(gasDetected);
                        if ("Có".equals(gasDetected) && gasValue != null) {
                            reportBuilder.append(" (Chỉ số khí gas: ").append(gasValue).append(")");
                        }
                        reportBuilder.append("\n");
                        // Thêm chi tiết về phát hiện lửa
                        if (flameDetected != null) {
                            reportBuilder.append("Phát hiện lửa: ").append(flameDetected ? "Có" : "Không").append("\n");
                        }
                        // Xử lý trường hợp phát hiện đồng thời cả lửa và khí gas
                        if (flameDetected != null && flameDetected && "Yes".equals(gasDetected)) {
                            reportBuilder.append("** Cảnh báo: Phát hiện cả 2 lửa và khí Gas cùng 1 lúc! **\n");
                        }
                        // Thêm thông tin nhiệt độ (nếu có)
                        if (temperature != null && temperature >= 40) {
                            reportBuilder.append("Nhiệt độ cao: ").append(temperature).append(" °C\n");
                        }
                        else {
                            reportBuilder.append("Nhiệt độ: ").append(temperature).append(" °C ");
                        }
                        reportList.add(reportBuilder.toString());
                    }
                }

                if (reportList.isEmpty()) {
                    Toast.makeText(history_environment.this, "No abnormal data found for the selected date.", Toast.LENGTH_SHORT).show();
                }

                reportAdapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Log.e("FirebaseError", "Error reading history data: " + databaseError.getMessage());
            }
        });
    }
}
