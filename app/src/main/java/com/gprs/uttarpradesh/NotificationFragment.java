package com.gprs.uttarpradesh;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;

public class NotificationFragment extends Fragment {
    SharedPreferences pref;
    SharedPreferences.Editor editor;
    ArrayList<String> arrayList = new ArrayList<>();
    ListView notices;
    ArrayAdapter adapter;
    TextView notice, notice1;
    ImageView icon;
    Button clear;
    View root;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {

        root = inflater.inflate(R.layout.fragment_notification, container, false);

        notice = root.findViewById(R.id.notice);
        notices = root.findViewById(R.id.notices);
        icon = root.findViewById(R.id.icon);
        notice1 = root.findViewById(R.id.notice1);
        clear = root.findViewById(R.id.clear);

        clear.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                FirebaseDatabase.getInstance().getReference().child("Notification").child(pref.getString("user", "")).removeValue();
                arrayList.clear();
                adapter.notifyDataSetChanged();
                notice.setVisibility(View.VISIBLE);
                notice1.setVisibility(View.VISIBLE);
                icon.setVisibility(View.VISIBLE);
                clear.setVisibility(View.INVISIBLE);
            }
        });


        pref = getActivity().getApplicationContext().getSharedPreferences("user", 0); // 0 - for private mode
        adapter = new ArrayAdapter<String>(root.getContext(), R.layout.notice, R.id.notice1, arrayList);
        notices.setAdapter(adapter);
        FirebaseDatabase.getInstance().getReference().child("Notification").child(pref.getString("user", "")).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot != null && dataSnapshot.getValue() != null) {
                    notice.setVisibility(View.INVISIBLE);
                    notice1.setVisibility(View.INVISIBLE);
                    icon.setVisibility(View.INVISIBLE);
                    clear.setVisibility(View.VISIBLE);
                    for (DataSnapshot data : dataSnapshot.getChildren()) {
                        for (DataSnapshot dat : data.getChildren())
                            arrayList.add(dat.getValue(String.class) + '\n' + dat.getKey());
                    }

                    adapter.notifyDataSetChanged();
                } else {
                    notice.setVisibility(View.VISIBLE);
                    notice1.setVisibility(View.VISIBLE);
                    icon.setVisibility(View.VISIBLE);
                    clear.setVisibility(View.INVISIBLE);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });


        return root;
    }


}
