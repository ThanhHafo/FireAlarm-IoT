package com.example.firealarm_iot;

import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import android.widget.ArrayAdapter;
import java.util.ArrayList;

public class ReportAdapter extends ArrayAdapter<String> {

    private Context context;
    private ArrayList<String> reports;

    public ReportAdapter(@NonNull Context context, @NonNull ArrayList<String> reports) {
        super(context, R.layout.item_report, reports);
        this.context = context;
        this.reports = reports;
    }

    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        if (convertView == null) {
            convertView = LayoutInflater.from(context).inflate(R.layout.item_report, parent, false);
        }

        // Lấy phần tử báo cáo tại vị trí hiện tại
        String report = reports.get(position);

        // Ánh xạ TextView từ layout item_report
        TextView reportTextView = convertView.findViewById(R.id.reportTextView);
        reportTextView.setText(report);

        // Đặt màu nền dựa trên nội dung báo cáo
        if (report.contains("Phát hiện lửa: Có")) {
            convertView.setBackgroundColor(Color.YELLOW);
        } else if (report.contains("Phát hiện khí Gas: Có")) {
            convertView.setBackgroundColor(Color.GREEN);
        } else if (report.contains("Nhiệt độ cao:")) {
            convertView.setBackgroundColor(Color.RED);
        } else {
            convertView.setBackgroundColor(Color.WHITE); // Mặc định là màu trắng
        }

        return convertView;
    }
}
