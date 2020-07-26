package com.gprs.uttarpradesh;

import android.app.ActivityOptions;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ListView;
import android.widget.RadioButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;

import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;


public class fragment_todo_work extends Fragment {


    ArrayList<AssignWorkHelper> assignWorkHelpers;
    ListView list;
    CustomWorkAssignedAdapter customWorkAssignedAdapter;
    View view;
    AlertDialog alert, alert1;
    String myname = null;


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        view = inflater.inflate(R.layout.fragment_ongoing_work, container, false);
        final SharedPreferences pref = getActivity().getSharedPreferences("user", 0);

        assignWorkHelpers = new ArrayList<>();
        customWorkAssignedAdapter = new CustomWorkAssignedAdapter(getActivity(), assignWorkHelpers, false);
        list = view.findViewById(R.id.list);
        list.setAdapter(customWorkAssignedAdapter);

        list.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                show(position);
            }
        });

        FirebaseDatabase.getInstance().getReference().child("Users").child(pref.getString("user", "")).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot != null) {
                    UserRegistrationHelper u = dataSnapshot.getValue(UserRegistrationHelper.class);
                    myname = u.getFname();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

        FirebaseDatabase.getInstance().getReference().child("Work").child("volunteer").child(pref.getString("user", "")).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot != null) {
                    assignWorkHelpers.clear();
                    for (DataSnapshot d : dataSnapshot.getChildren()) {
                        AssignWorkHelper a = d.getValue(AssignWorkHelper.class);
                        if (a.getStatus().equals("In Progress"))
                            assignWorkHelpers.add(a);
                    }
                    customWorkAssignedAdapter.notifyDataSetChanged();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

        return view;
    }

    private void show(final int position) {
        final AlertDialog.Builder alertDialog = new AlertDialog.Builder(getContext(), android.R.style.Theme_Material_Light_NoActionBar_Fullscreen);
        LayoutInflater factory = LayoutInflater.from(getContext());
        final View view = factory.inflate(R.layout.alert_workview, null);
        alertDialog.setView(view);
        ListView memberlist;
        CustomWorkAssignAdapter1 c;
        TextInputLayout title, desc, startdate, duedate, contact, comments;
        ArrayList<String> name, role, phone;
        Button address;

        comments = view.findViewById(R.id.comments);
        memberlist = view.findViewById(R.id.memberlist);
        title = view.findViewById(R.id.title);
        desc = view.findViewById(R.id.desc);
        startdate = view.findViewById(R.id.startdate);
        duedate = view.findViewById(R.id.duedate);
        contact = view.findViewById(R.id.contact);
        address = view.findViewById(R.id.address);
        title.getEditText().setText(assignWorkHelpers.get(position).getTitle());
        desc.getEditText().setText(assignWorkHelpers.get(position).getDesc());
        startdate.getEditText().setText(assignWorkHelpers.get(position).getStartdate());
        duedate.getEditText().setText(assignWorkHelpers.get(position).getDuedate());
        contact.getEditText().setText(assignWorkHelpers.get(position).getContact());
        address.setText(String.valueOf(assignWorkHelpers.get(position).getLat()) + ',' + assignWorkHelpers.get(position).getLon());

        address.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getActivity(), SelectLocation.class);
                intent.putExtra("lat", assignWorkHelpers.get(position).getLat());
                intent.putExtra("lon", assignWorkHelpers.get(position).getLon());
                startActivity(intent, ActivityOptions.makeSceneTransitionAnimation(getActivity()).toBundle());
            }
        });

        if (assignWorkHelpers.get(position).getComment() != null) {
            String comment = "";
            for (int i = 0; i < assignWorkHelpers.get(position).getComment().size(); i++) {
                comment = comment + assignWorkHelpers.get(position).getCommentname().get(i) + " : " + assignWorkHelpers.get(position).getComment().get(i) + '\n';
            }
            comments.getEditText().setText(comment);
        }

        TextView title2 = view.findViewById(R.id.title2);
        title2.setText(assignWorkHelpers.get(position).getLeader());
        name = assignWorkHelpers.get(position).getName();
        role = assignWorkHelpers.get(position).getRole();
        phone = assignWorkHelpers.get(position).getPhone();
        RadioButton radioButton;
        if (assignWorkHelpers.get(position).getPriority().equals(getContext().getString(R.string.normal))) {
            radioButton = view.findViewById(R.id.radioButton1);
        } else {
            radioButton = view.findViewById(R.id.radioButton2);
        }
        radioButton.setChecked(true);
        c = new CustomWorkAssignAdapter1(getActivity(), name, role, phone, assignWorkHelpers.get(position).getLeader(), false);
        memberlist.setAdapter(c);

        view.findViewById(R.id.close).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                alert.hide();
            }
        });
        Button button = view.findViewById(R.id.stop);

        if (myname != null) {
            if (myname.equals(assignWorkHelpers.get(position).getLeader())) {
                button.setVisibility(View.VISIBLE);
            } else {
                button.setVisibility(View.VISIBLE);
                button.setText("In Progress");
                button.setEnabled(false);
                button.setClickable(false);
            }
        } else {
            button.setVisibility(View.INVISIBLE);
        }


        view.findViewById(R.id.stop).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final AlertDialog.Builder alertDialog = new AlertDialog.Builder(getContext(), android.R.style.Theme_Material_Light_NoActionBar_Fullscreen);
                alertDialog.setTitle("Confirm");
                alertDialog.setMessage("Stop Work " + assignWorkHelpers.get(position).getTitle());
                LayoutInflater factory = LayoutInflater.from(getContext());
                final View view = factory.inflate(R.layout.alert_workview_confirm, null);
                alertDialog.setView(view);
                alertDialog.setPositiveButton("YES", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        TextInputLayout comment = view.findViewById(R.id.comments);
                        if (!comment.getEditText().getText().toString().isEmpty()) {
                            if (assignWorkHelpers.get(position).getComment() == null) {
                                assignWorkHelpers.get(position).setComment(new ArrayList<String>());
                                assignWorkHelpers.get(position).setCommentname(new ArrayList<String>());
                            }
                            assignWorkHelpers.get(position).getComment().add(comment.getEditText().getText().toString());
                            assignWorkHelpers.get(position).getCommentname().add(assignWorkHelpers.get(position).getMonitorname());
                        }
                        final SharedPreferences pref = getActivity().getSharedPreferences("user", 0);
                        assignWorkHelpers.get(position).setStatus("Stopped");
                        FirebaseDatabase.getInstance().getReference().child("Work").child("monitor").child(assignWorkHelpers.get(position).getMonitorphone()).child(assignWorkHelpers.get(position).getTime()).setValue(assignWorkHelpers.get(position));
                        SimpleDateFormat dateFormat = new SimpleDateFormat("dd:MMM:hh:mm:ss");
                        String currentDateTime = dateFormat.format(new Date());
                        for (int i = 0; i < assignWorkHelpers.get(position).getPhone().size(); i++) {
                            FirebaseDatabase.getInstance().getReference().child("Work").child("volunteer").child(assignWorkHelpers.get(position).getPhone().get(i)).child(assignWorkHelpers.get(position).getTime()).setValue(assignWorkHelpers.get(position));
                            FirebaseDatabase.getInstance().getReference().child("Notification").child(assignWorkHelpers.get(position).getPhone().get(i)).child("Work").child(currentDateTime).setValue("Work submitted by leader");
                        }
                        FirebaseDatabase.getInstance().getReference().child("Notification").child(pref.getString("user", "")).child("Work").child(currentDateTime).setValue("Work submitted by you");
                        FirebaseDatabase.getInstance().getReference().child("Notification").child(assignWorkHelpers.get(position).getMonitorphone()).child("Work").child(currentDateTime).setValue("Work submitted by leader");

                        assignWorkHelpers.remove(position);
                        customWorkAssignedAdapter.notifyDataSetChanged();
                        Toast.makeText(getActivity(), "Work Stopped", Toast.LENGTH_SHORT).show();
                        alert.hide();
                    }
                });
                alertDialog.setNegativeButton("NO", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                });
                alert1 = alertDialog.create();
                alert1.setView(view);
                alert1.show();
            }
        });

        alert = alertDialog.create();
        alert.setView(view);
        alert.show();
    }
}