package com.example.research.Activites;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

import com.example.research.Activites.LoginActivity;
import com.example.research.Activites.MainActivity;
import com.example.research.R;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class StartActivity extends AppCompatActivity {

    FirebaseAuth auth;
    FirebaseUser user;

    Button admin_btn, guest_btn;

    ImageView language;

    ProgressDialog progressDialog;

    String lng_status = "en";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_start);

        initViews();
        initFireBase();

        if (user != null) {
            Intent intent = new Intent(getApplicationContext(),MainActivity.class);
            startActivity(intent);
            finish();
        }
    }

    private void initFireBase() {
        auth = FirebaseAuth.getInstance();
        user = auth.getCurrentUser();

    }

    private void initViews() {
        admin_btn = findViewById(R.id.admin_btn);
        guest_btn = findViewById(R.id.guest_btn);

        language = findViewById(R.id.language_img);

        progressDialog = new ProgressDialog(StartActivity.this);
        progressDialog.setCancelable(false);
        progressDialog.setMessage("Signing In");
        progressDialog.setTitle("");
        progressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
    }

    public void Admin(View view) {
        Intent intent = new Intent(getApplication(),LoginActivity.class);
        intent.putExtra("lng_status",lng_status);
        startActivity(intent);
    }

    public void Guest(View view) {
        progressDialog.show();
        auth.signInWithEmailAndPassword("guest@guest.com","123456")
                .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            progressDialog.dismiss();
                            Intent intent = new Intent(getApplication(),LoginActivity.class);
                            intent.putExtra("lng_status",lng_status);
                            startActivity(intent);
                            finish();
                        }else{
                            progressDialog.dismiss();
                            Toast.makeText(getApplication(), task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                            return;
                        }
                    }
                });
    }

    public void language(View view) {
        if(language.getDrawable().getConstantState() == getResources().getDrawable(R.drawable.ic_chinese_language).getConstantState()){
            language.setImageResource(R.drawable.ic_english_language);
            admin_btn.setText("管理员登录");
            guest_btn.setText("欢迎登录");
            lng_status = "ch";
        }else if(language.getDrawable().getConstantState() == getResources().getDrawable(R.drawable.ic_english_language).getConstantState()) {
            language.setImageResource(R.drawable.ic_chinese_language);
            admin_btn.setText("Login as Admin");
            guest_btn.setText("Login as Guest");
            lng_status = "en";
        }
    }
}
