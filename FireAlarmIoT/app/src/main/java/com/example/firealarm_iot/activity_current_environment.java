package com.example.firealarm_iot;

import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class activity_current_environment extends AppCompatActivity {
    TextView temperatureValue;
    TextView humidityValue;
    TextView flameDetectionValue;
    TextView gasDetectionValue;
    private DatabaseReference databaseReference;
    private View rootView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.current_environment);

        init();

        readSensorData();
    }

    private void init() {
        temperatureValue = findViewById(R.id.temperatureValue);
        humidityValue = findViewById(R.id.humidityValue);
        flameDetectionValue = findViewById(R.id.flameDetectionValue);
        gasDetectionValue = findViewById(R.id.gasDetectionValue);
        rootView = findViewById(R.id.rootLayout); // Assuming you have a root layout with this ID
        databaseReference = FirebaseDatabase.getInstance().getReference();
    }

    private void readSensorData() {
        databaseReference.child("sensors").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                // Lấy dữ liệu từ node sensors
                boolean flame = dataSnapshot.child("flame").getValue(Boolean.class);
                int gas = dataSnapshot.child("gas").getValue(Integer.class);
                double humidity = dataSnapshot.child("humidity").getValue(Double.class);
                int temperature = dataSnapshot.child("temperature").getValue(Integer.class);

                temperatureValue.setText(temperature + " °C");
                humidityValue.setText(humidity + " %");
                flameDetectionValue.setText(flame ? "Phát hiện lửa" : "Không");
                if (gas >= 1000) {
                    gasDetectionValue.setText("Phát hiện khí Gas");
                } else {
                    gasDetectionValue.setText("Không");
                }

                // Change background color based on sensor data
                if (temperature >= 40) {
                    rootView.setBackgroundColor(Color.RED); // Temperature above 40°C -> Red
                } else if (flame) {
                    rootView.setBackgroundColor(Color.YELLOW); // Flame detected -> Yellow
                } else if (gas >= 1000) {
                    rootView.setBackgroundColor(Color.GREEN); // Gas detected -> Green
                } else {
                    rootView.setBackgroundColor(Color.WHITE); // Default color
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Log.e("FirebaseError", "Error reading sensor data: " + databaseError.getMessage());
            }
        });
    }
}
