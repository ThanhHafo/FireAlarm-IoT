package com.example.firealarm_iot;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import java.util.ArrayList;

public class EventAdapter extends BaseAdapter {

    private Context context;
    private ArrayList<String> eventDetailsList;
    private LayoutInflater inflater;

    public EventAdapter(Context context, ArrayList<String> eventDetailsList) {
        this.context = context;
        this.eventDetailsList = eventDetailsList;
        this.inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    }

    @Override
    public int getCount() {
        return eventDetailsList.size();
    }

    @Override
    public Object getItem(int position) {
        return eventDetailsList.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        // Kiểm tra nếu convertView là null, nếu có thì tạo view mới
        if (convertView == null) {
            convertView = inflater.inflate(R.layout.item_event, parent, false);
        }

        // Lấy sự kiện tại vị trí hiện tại trong danh sách
        String event = eventDetailsList.get(position);

        // Ánh xạ các view trong item layout
        TextView eventTextView = convertView.findViewById(R.id.eventTextView);

        // Thiết lập nội dung cho TextView
        eventTextView.setText(event);

        return convertView;
    }
}
