package com.example.firealarm_iot;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class control extends AppCompatActivity {

    private Button btnToggleBuzzer, btnToggleDoor;
    private DatabaseReference databaseReference;

    private boolean isBuzzerOn = false;
    private boolean isDoorOpen = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.control);

        btnToggleBuzzer = findViewById(R.id.btn_toggle_buzzer);
        btnToggleDoor = findViewById(R.id.btn_toggle_door);

        // Kết nối đến Firebase
        databaseReference = FirebaseDatabase.getInstance().getReference("state");
        // Lắng nghe trạng thái còi báo hiệu
        databaseReference.child("buzzer").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    isBuzzerOn = snapshot.getValue(Boolean.class);
                    updateBuzzerButton();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(control.this, "Không thể lấy trạng thái còi báo hiệu", Toast.LENGTH_SHORT).show();
            }
        });

        // Lắng nghe trạng thái cửa
        databaseReference.child("door").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    isDoorOpen = snapshot.getValue(Boolean.class);
                    updateDoorButton();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(control.this, "Không thể lấy trạng thái cửa", Toast.LENGTH_SHORT).show();
            }
        });

        // Xử lý sự kiện Bật/Tắt còi báo hiệu
        btnToggleBuzzer.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                isBuzzerOn = !isBuzzerOn;
                databaseReference.child("buzzer").setValue(isBuzzerOn)
                        .addOnSuccessListener(unused -> {
                            String message = isBuzzerOn ? "Còi báo hiệu bật!" : "Còi báo hiệu tắt!";
                            Toast.makeText(control.this, message, Toast.LENGTH_SHORT).show();
                        })
                        .addOnFailureListener(e -> Toast.makeText(control.this, "Không thể thay đổi trạng thái còi báo hiệu", Toast.LENGTH_SHORT).show());
            }
        });

        // Xử lý sự kiện Đóng/Mở cửa
        btnToggleDoor.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                isDoorOpen = !isDoorOpen;
                databaseReference.child("door").setValue(isDoorOpen)
                        .addOnSuccessListener(unused -> {
                            String message = isDoorOpen ? "Cửa mở!" : "Cửa đóng!";
                            Toast.makeText(control.this, message, Toast.LENGTH_SHORT).show();
                        })
                        .addOnFailureListener(e -> Toast.makeText(control.this, "Không thể thay đổi trạng thái cửa", Toast.LENGTH_SHORT).show());
            }
        });
    }

    private void updateBuzzerButton() {
        btnToggleBuzzer.setText(isBuzzerOn ? "Tắt còi báo hiệu" : "Bật còi báo hiệu");
    }

    private void updateDoorButton() {
        btnToggleDoor.setText(isDoorOpen ? "Đóng cửa" : "Mở cửa");
    }
}
