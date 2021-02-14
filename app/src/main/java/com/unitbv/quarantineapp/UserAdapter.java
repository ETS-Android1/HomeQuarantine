package com.unitbv.quarantineapp;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.google.firebase.firestore.FirebaseFirestore;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;

public class UserAdapter extends ArrayAdapter<Users> {
    Context context;
    int resource;
    ArrayList<Users> users;

    public UserAdapter(@NonNull Context context, int resource, ArrayList<Users> users) {
        super(context, resource, users);

        this.context = context;
        this.resource = resource;
        this.users = users;
    }

    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        LayoutInflater inflater = LayoutInflater.from(context);

        View view = inflater.inflate(R.layout.list_item, null);

        ImageView userIcon = view.findViewById(R.id.userIcon);
        TextView name = view.findViewById(R.id.name);
        TextView status = view.findViewById(R.id.status);

        Users user = users.get(position);

        if(!user.getProfilePic().equals("")) {
            Picasso.get().load(user.getProfilePic()).into(userIcon);
        }

        name.setText(user.getFullName());
        status.setText(user.getStatus());

        return view;
    }
}
