package com.example.research.Activites;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.research.CustomProgress;
import com.example.research.R;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.victor.loading.book.BookLoading;

public class LoginActivity extends AppCompatActivity {

    TextInputEditText email_field,password_field;
    String email_txt,password_txt;
    TextView welcome_msg,logback_msg,guest_txt;
    ImageView language;
    Button login_btn;
    String lng_Status = "en";

    FirebaseAuth auth;
    FirebaseUser user;

    ProgressDialog progressDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        initView();
        initFireBase();

        lng_Status = getIntent().getStringExtra("lng_status");

        if(lng_Status.equals("en")){
            language.setImageResource(R.drawable.ic_chinese_language);
            welcome_msg.setText("Admin login !");
            logback_msg.setText("Log back into your account");
            email_field.setHint("");
            email_field.setHint("Your Email");
            password_field.setHint("");
            password_field.setHint("Password");
            login_btn.setText("Log in");
            guest_txt.setText("Login As Guest");
        }else if(lng_Status.equals("ch")){
            language.setImageResource(R.drawable.ic_english_language);
            welcome_msg.setText("管理员登录 ！");
            logback_msg.setText("重新登陆您的帐户");
            email_field.setHint("");
            email_field.setHint("邮箱地址");
            password_field.setHint("");
            password_field.setHint("密码");
            login_btn.setText("登陆");
            guest_txt.setText("欢迎登陆");
        }

        if (user != null) {
            Intent intent = new Intent(getApplicationContext(),MainActivity.class);
            intent.putExtra("lng_status",lng_Status);
            startActivity(intent);
            finish();
        }

    }

    private void initFireBase() {
        auth                                         = FirebaseAuth.getInstance();
        user                                         = auth.getCurrentUser();
    }

    private void initView() {
        email_field                         = findViewById(R.id.email_field);
        password_field                      = findViewById(R.id.password_field);

        language                            = findViewById(R.id.lng_choose);
        welcome_msg                         = findViewById(R.id.welcome_txt);
        logback_msg                         = findViewById(R.id.logback_txt);
        guest_txt                           = findViewById(R.id.guest);
        login_btn                           = findViewById(R.id.login_btn);

        progressDialog = new ProgressDialog(LoginActivity.this);
        progressDialog.setCancelable(false);
        progressDialog.setMessage("Signing In");
        progressDialog.setTitle("");
        progressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
    }

    public void login(View view) {
        email_txt                               = email_field.getText().toString();
        password_txt                            = password_field.getText().toString();

        if (email_txt.isEmpty()) {
            Toast.makeText(this, "Enter your Email", Toast.LENGTH_SHORT).show();
            email_field.requestFocus();
            return;
        } else if (password_txt.isEmpty()) {
            Toast.makeText(this, "Enter Password", Toast.LENGTH_SHORT).show();
            password_field.requestFocus();
            return;
        }
        progressDialog.show();
        auth.signInWithEmailAndPassword(email_txt,password_txt)
                .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            progressDialog.dismiss();
                            Intent intent = new Intent(getApplicationContext(),MainActivity.class);
                            intent.putExtra("lng_status",lng_Status);
                            startActivity(intent);
                            finish();
                        }else{
                            progressDialog.dismiss();
                            Toast.makeText(LoginActivity.this, task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                            return;
                        }
                    }
                });
    }

    public void change_Language(View view) {
        if(language.getDrawable().getConstantState() == getResources().getDrawable(R.drawable.ic_chinese_language).getConstantState()){
            language.setImageResource(R.drawable.ic_english_language);
            welcome_msg.setText("管理员登录 ！");
            logback_msg.setText("重新登陆您的帐户");
            email_field.setHint("");
            email_field.setHint("邮箱地址");
            password_field.setHint("");
            password_field.setHint("密码");
            login_btn.setText("登陆");
            guest_txt.setText("欢迎登陆");
            lng_Status = "ch";
        }else if(language.getDrawable().getConstantState() == getResources().getDrawable(R.drawable.ic_english_language).getConstantState()){
            language.setImageResource(R.drawable.ic_chinese_language);
            welcome_msg.setText("Admin login !");
            logback_msg.setText("Log back into your account");
            email_field.setHint("");
            email_field.setHint("Your Email");
            password_field.setHint("");
            password_field.setHint("Password");
            login_btn.setText("Log in");
            guest_txt.setText("Login As Guest");
            lng_Status = "en";
        }
    }

    public void guest_login(View view) {
        progressDialog.show();
        auth.signInWithEmailAndPassword("guest@guest.com","123456")
                .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            progressDialog.dismiss();
                            Intent intent = new Intent(getApplicationContext(),MainActivity.class);
                            intent.putExtra("lng_status",lng_Status);
                            startActivity(intent);
                            finish();
                        }else{
                            Toast.makeText(LoginActivity.this, task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                            return;
                        }
                    }
                });
    }
}
