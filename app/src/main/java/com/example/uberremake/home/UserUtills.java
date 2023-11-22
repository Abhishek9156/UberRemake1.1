package com.example.uberremake.home;

import android.content.Context;
import android.view.View;
import android.widget.Toast;


import com.example.uberremake.Common;
import com.example.uberremake.model.TokenModel;
import com.example.uberremake.service.MyFirebaseMessagingService;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.FirebaseApp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.FirebaseDatabase;

import java.util.Map;

public class UserUtills {
    public static void updateUser(View view, Map<String, Object> updateData) {
        FirebaseDatabase.getInstance()
                .getReference(Common.DRIVER_INFO_REFERENCE)
                .child(FirebaseAuth.getInstance().getCurrentUser().getUid())
                .updateChildren(updateData)
                .addOnFailureListener(e -> {
                    Snackbar.make(view,e.getMessage(),Snackbar.LENGTH_LONG).show();
                })
                .addOnSuccessListener(runnable -> {
                    Snackbar.make(view,"Update information successful",Snackbar.LENGTH_LONG).show();
                });
    }

    public static void updateToken(Context context, String token) {
        TokenModel tokenModel=new TokenModel(token);
        FirebaseDatabase.getInstance().getReference(Common.TOKEN_REFERENCE)
                .child(FirebaseAuth.getInstance().getCurrentUser().getUid())
                .setValue(tokenModel)
                .addOnFailureListener(e -> {
                    Toast.makeText(context, e.getMessage(), Toast.LENGTH_SHORT).show();
                }).addOnSuccessListener(runnable -> {});




    }
}
