package com.example.research.Activites;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DownloadManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.Cursor;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.example.research.CustomProgress;
import com.example.research.Models.FileModel;
import com.example.research.R;
import com.google.android.gms.tasks.Continuation;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class MainActivity extends AppCompatActivity {

    boolean internet_test;

    ImageView language;

    ArrayAdapter<CharSequence> adapter,dialog_adapter;

    RecyclerView recyclerView;
    RecyclerView.LayoutManager layoutManager;
    DividerItemDecoration dividerItemDecoration;
    DataAdapter dataAdapter;

    Spinner category_select,dialog_Spinner;

    FloatingActionButton add_btn;

    List<FileModel> files;

    Uri fileUri;
    String fileName, fileDate, id, fileURL, extension,category,getByCategory = "AllFiles";
    String deleteID;
    int fileType;

    FileModel fileToDB;

    long downloadID;

    String lng_status = "en";

    FirebaseStorage storage;
    StorageReference storageReference;

    FirebaseAuth auth;

    FirebaseUser user;

    FirebaseDatabase firebaseDatabase;
    DatabaseReference databaseReference;

    CustomProgress customProgress;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        internet_test = checkConnection(getApplicationContext());
        if (internet_test == false) {
            Toast.makeText(this, "Please check your Internet connection", Toast.LENGTH_LONG).show();
            return;
        }
        lng_status = getIntent().getStringExtra("lng_status");
        initView();
        initFireBase();
        getFiles();
        customizeDownloader();

        if(!user.getEmail().equals("admin123@admin.com")){
            add_btn.setVisibility(View.INVISIBLE);
            add_btn.setEnabled(false);
        }else{
            add_btn.setVisibility(View.VISIBLE);
            add_btn.setEnabled(true);
        }

        category_select.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                getFiles();
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });

    }
    private void customizeDownloader() {
        IntentFilter filter = new IntentFilter(DownloadManager.ACTION_DOWNLOAD_COMPLETE);
        registerReceiver(new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                long broadcastedDownloadID = intent.getLongExtra(DownloadManager.EXTRA_DOWNLOAD_ID, -1);
                if (broadcastedDownloadID == downloadID) {
                    if (getDownloadStutus() == DownloadManager.STATUS_SUCCESSFUL) {
                        Toast.makeText(context, "Download Complete.", Toast.LENGTH_SHORT).show();
                    }else{
                        Toast.makeText(context, "Download not Complete", Toast.LENGTH_SHORT).show();
                    }
                }
            }
        }, filter);
    }

    private int getDownloadStutus(){
        DownloadManager.Query query = new DownloadManager.Query();
        query.setFilterById(downloadID);

        DownloadManager downloadManager = (DownloadManager) getSystemService(DOWNLOAD_SERVICE);
        Cursor cursor = downloadManager.query(query);

        if(cursor.moveToFirst()){
            int colomnIndex = cursor.getColumnIndex(DownloadManager.COLUMN_STATUS);
            int stutus = cursor.getInt(colomnIndex);

            return stutus;
        }
        return DownloadManager.ERROR_UNKNOWN;
    }

    private void getFiles() {
        databaseReference.child("Files").child(checkCategory()).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                files.clear();
                for(DataSnapshot dataSnapshot1 : dataSnapshot.getChildren()){
                    FileModel fileModel = dataSnapshot1.getValue(FileModel.class);
                    if(fileModel.getFileExtension().equals("docx")|| fileModel.getFileExtension().equals("doc")|| fileModel.getFileExtension().equals("txt")){
                        fileModel.setDataType(R.drawable.ic_word);
                    }else if(fileModel.getFileExtension().equals("ppt")){
                        fileModel.setDataType(R.drawable.ic_powerpoint);
                    }else if(fileModel.getFileExtension().equals("pdf")){
                        fileModel.setDataType(R.drawable.ic_pdf);
                    }else if(fileModel.getFileExtension().equals("xls") || fileModel.getFileExtension().equals("xlsx")){
                        fileModel.setDataType(R.drawable.ic_excel);
                    }else if(fileModel.getFileExtension().equals("zip")){
                        fileModel.setDataType(R.drawable.ic_rar);
                    }else{
                        fileModel.setDataType(R.drawable.ic_insert_drive_file_black_24dp);
                    }
                    files.add(fileModel);
                }
                dataAdapter = new DataAdapter(files);
                recyclerView.setAdapter(dataAdapter);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

    }

    public String checkCategory(){
        if(category_select.getSelectedItem().equals("All") || category_select.getSelectedItem().equals("所有")){
            getByCategory = "AllFiles";
        }else if(category_select.getSelectedItem().equals("Chinese Literature")|| category_select.getSelectedItem().equals("中国文学")){
            getByCategory = "Chinese Literature";
        }else if(category_select.getSelectedItem().equals("Chinese linguistics")|| category_select.getSelectedItem().equals("汉语语言学")){
            getByCategory = "Chinese linguistics";
        }else if(category_select.getSelectedItem().equals("Chinese culture")|| category_select.getSelectedItem().equals("中华文化")){
            getByCategory = "Chinese culture";
        }else if(category_select.getSelectedItem().equals("Translation ch~Ar")|| category_select.getSelectedItem().equals("阿汉汉阿翻译")){
            getByCategory = "Translation ch~Ar";
        }else if(category_select.getSelectedItem().equals("News room")|| category_select.getSelectedItem().equals("头条")){
            getByCategory = "News room";
        }
        return getByCategory;
    }

    private void initFireBase() {
        storage                     = FirebaseStorage.getInstance();

        firebaseDatabase            = FirebaseDatabase.getInstance();
        databaseReference           = firebaseDatabase.getReference();

        user                        = FirebaseAuth.getInstance().getCurrentUser();

        auth                        = FirebaseAuth.getInstance();
    }

    private void initView() {
        recyclerView                            = findViewById(R.id.recyclerview);
        dividerItemDecoration                   = new DividerItemDecoration(getApplicationContext(),DividerItemDecoration.VERTICAL);
        layoutManager                           = new LinearLayoutManager(getApplicationContext(),RecyclerView.VERTICAL,false);
        recyclerView.setLayoutManager(layoutManager);
        recyclerView.addItemDecoration(dividerItemDecoration);
        files                                   = new ArrayList<>();
        add_btn                                 = findViewById(R.id.add_btn);
        language                                = findViewById(R.id.language);

        customProgress                          = CustomProgress.getInstance();

        category_select                         = findViewById(R.id.category);

        if (lng_status == null) {
            lng_status = "en";
            adapter = ArrayAdapter.createFromResource(getApplicationContext(),R.array.English_Types, android.R.layout.simple_spinner_item);
        }else if(lng_status.equals("en")){
            adapter = ArrayAdapter.createFromResource(getApplicationContext(),R.array.English_Types, android.R.layout.simple_spinner_item);
            language.setImageResource(R.drawable.ic_chinese_language);
        }else if(lng_status.equals("ch")){
            adapter = ArrayAdapter.createFromResource(getApplicationContext(),R.array.Chinese_Types, android.R.layout.simple_spinner_item);
            language.setImageResource(R.drawable.ic_english_language);
        }
        adapter.setDropDownViewResource(android.R.layout.simple_dropdown_item_1line);
        category_select.setAdapter(adapter);
    }

    public void add(View view) {
        if (internet_test == false) {
            Toast.makeText(this, "Check Your Connection First", Toast.LENGTH_LONG).show();
            return;
        }
        String[] mimeTypes =
                {"application/msword","application/vnd.openxmlformats-officedocument.wordprocessingml.document", // .doc & .docx
                        "application/vnd.ms-powerpoint","application/vnd.openxmlformats-officedocument.presentationml.presentation", // .ppt & .pptx
                        "application/vnd.ms-excel","application/vnd.openxmlformats-officedocument.spreadsheetml.sheet", // .xls & .xlsx
                        "text/plain",
                        "application/pdf",
                        "application/zip",
                        "application/rar"};

        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        intent.addCategory(Intent.CATEGORY_OPENABLE);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            intent.setType(mimeTypes.length == 1 ? mimeTypes[0] : "*/*");
            if (mimeTypes.length > 0) {
                intent.putExtra(Intent.EXTRA_MIME_TYPES, mimeTypes);
            }
        } else {
            String mimeTypesStr = "";
            for (String mimeType : mimeTypes) {
                mimeTypesStr += mimeType + "|";
            }
            intent.setType(mimeTypesStr.substring(0,mimeTypesStr.length() - 1));
        }
        startActivityForResult(Intent.createChooser(intent,"ChooseFile"), 1000);

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if(data == null){
            Toast.makeText(this, "Please Choose A file", Toast.LENGTH_SHORT).show();
            return;
        }
        switch (requestCode) {
            case 1000:
                if (resultCode == RESULT_OK) {
                    fileUri = data.getData();

                    //************Get File Name*****************

                    if (fileUri.getScheme().equals("file")) {
                        fileName = fileUri.getLastPathSegment();
                    } else {
                        Cursor cursor = null;
                        try {
                            cursor = getContentResolver().query(fileUri, new String[]{
                                    MediaStore.Images.ImageColumns.DISPLAY_NAME
                            }, null, null, null);

                            if (cursor != null && cursor.moveToFirst()) {
                                fileName = cursor.getString(cursor.getColumnIndex(MediaStore.Images.ImageColumns.DISPLAY_NAME));
                            }
                        } finally {

                            if (cursor != null) {
                                cursor.close();
                            }
                        }
                    }
                }

                //***********Get File Date****************
                fileDate = new SimpleDateFormat("dd-MM-yyyy", Locale.getDefault()).format(new Date());

                //***********Get File Extension***********
                String uri = fileName;
                extension = uri.substring(uri.lastIndexOf(".") + 1);

                final Dialog dialog = new Dialog(MainActivity.this);
                dialog.setContentView(R.layout.dialog);
                dialog.setTitle("Select Category");
                Button dialogButton = dialog.findViewById(R.id.dialog_button);
                // set the custom dialog components - text, image and button
                dialog_Spinner = dialog.findViewById(R.id.dialog_spinner);
                if(language.getDrawable().getConstantState() == getResources().getDrawable(R.drawable.ic_english_language).getConstantState()){
                    dialog_adapter = ArrayAdapter.createFromResource(getApplicationContext(),R.array.Chinese_Dialog, android.R.layout.simple_spinner_item);
                    dialogButton.setText("完");
                }else if(language.getDrawable().getConstantState() == getResources().getDrawable(R.drawable.ic_chinese_language).getConstantState()){
                    dialog_adapter = ArrayAdapter.createFromResource(getApplicationContext(),R.array.English_Dialog, android.R.layout.simple_spinner_item);
                }
                dialog_adapter.setDropDownViewResource(android.R.layout.simple_dropdown_item_1line);
                dialog_Spinner.setAdapter(dialog_adapter);

                // if button is clicked, close the custom dialog
                dialogButton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        if (dialog_Spinner.getSelectedItem().equals("Select Category")) {
                            Toast.makeText(MainActivity.this, "Select Category", Toast.LENGTH_SHORT).show();
                            return;
                        }else if(dialog_Spinner.getSelectedItem().equals("选择类别")){
                            Toast.makeText(MainActivity.this, "选择类别", Toast.LENGTH_SHORT).show();
                            return;
                        }
                        category = dialog_Spinner.getSelectedItem().toString();
                        customProgress.showProgress(MainActivity.this,"Uploading File...",false);
                        uploadFile(fileUri);
                        dialog.dismiss();

                    }
                });

                dialog.show();
                break;
        }
    }

    private void addToDB(String fileName, String fileDate, String fileURL) {
        if(dialog_Spinner.getSelectedItem().toString().equals("Chinese Literature") || dialog_Spinner.getSelectedItem().toString().equals("中国文学")){
            category = "Chinese Literature";
        }else if(dialog_Spinner.getSelectedItem().toString().equals("Chinese linguistics") || dialog_Spinner.getSelectedItem().toString().equals("汉语语言学")){
            category = "Chinese linguistics";
        }else if(dialog_Spinner.getSelectedItem().toString().equals("Chinese culture") || dialog_Spinner.getSelectedItem().toString().equals("中华文化")){
            category = "Chinese culture";
        }else if(dialog_Spinner.getSelectedItem().toString().equals("Translation ch~Ar") || dialog_Spinner.getSelectedItem().toString().equals("阿汉汉阿翻译")){
            category = "Translation ch~Ar";
        }else if(dialog_Spinner.getSelectedItem().toString().equals("News room") || dialog_Spinner.getSelectedItem().toString().equals("头条")){
            category = "News room";
        }
        id = databaseReference.child("Files").child(category).push().getKey();
        if(extension.equals("docx")|| extension.equals("doc")|| extension.equals("txt")){
            fileType = R.drawable.ic_word;
        }else if(extension.equals("ppt")){
            fileType = R.drawable.ic_powerpoint;
        }else if(extension.equals("pdf")){
            fileType = R.drawable.ic_pdf;
        }else if(extension.equals("xls") || extension.equals("xlsx")){
            fileType = R.drawable.ic_excel;
        }else if(extension.equals("zip")){
            fileType = R.drawable.ic_rar;
        }else{
            fileType = R.drawable.ic_insert_drive_file_black_24dp;
        }
        fileToDB = new FileModel(fileName,fileDate,id,fileURL,extension,category,fileType);
        databaseReference.child("Files").child(category).child(id).setValue(fileToDB);
        databaseReference.child("Files").child("AllFiles").child(id).setValue(fileToDB);
        getFiles();
        customProgress.hideProgress();
        Toast.makeText(this, "Upload Successful", Toast.LENGTH_SHORT).show();
    }

    private void uploadFile(final Uri fileUri) {
        UploadTask uploadTask;
        storageReference = FirebaseStorage.getInstance().getReference().child("Files/").child(fileName);
        uploadTask = storageReference.putFile(fileUri);

        Task<Uri> task = uploadTask.continueWithTask(new Continuation<UploadTask.TaskSnapshot, Task<Uri>>()
        {
            @Override
            public Task<Uri> then(@NonNull Task<UploadTask.TaskSnapshot> task) throws Exception
            {
                return storageReference.getDownloadUrl();
            }
        }).addOnCompleteListener(new OnCompleteListener<Uri>()
        {
            @Override
            public void onComplete(@NonNull Task<Uri> task)
            {
                if (task.isSuccessful())
                {
                    Uri file = task.getResult();
                    fileURL = file.toString();
                    addToDB(fileName,fileDate,fileURL);
                } else
                {
                    Toast.makeText(getApplicationContext(), task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    public void logout(View view) {
        auth.signOut();
        startActivity(new Intent(getApplicationContext(), StartActivity.class));
        finish();
    }

    public void change_lng(View view) {
        int selected_item = category_select.getSelectedItemPosition();
        if(language.getDrawable().getConstantState() == getResources().getDrawable(R.drawable.ic_chinese_language).getConstantState()){
            lng_status = "ch";
            dataAdapter.notifyDataSetChanged();
            adapter = ArrayAdapter.createFromResource(getApplicationContext(),R.array.Chinese_Types, android.R.layout.simple_spinner_item);
            language.setImageResource(R.drawable.ic_english_language);

        } else if (language.getDrawable().getConstantState() == getResources().getDrawable(R.drawable.ic_english_language).getConstantState()) {
            lng_status = "en";
            dataAdapter.notifyDataSetChanged();
            adapter = ArrayAdapter.createFromResource(getApplicationContext(),R.array.English_Types, android.R.layout.simple_spinner_item);
            language.setImageResource(R.drawable.ic_chinese_language);
        }
        category_select.setAdapter(adapter);
        category_select.setSelection(selected_item);
    }

    public void search(View view) {
        final Dialog search_dialog = new Dialog(MainActivity.this);
        search_dialog.setContentView(R.layout.search_dialog);
        search_dialog.setTitle("Search");

        // set the custom dialog components - text, image and button
        final EditText search_field = search_dialog.findViewById(R.id.search_field);

        Button search_btn = search_dialog.findViewById(R.id.search_button);
        // if button is clicked, close the custom dialog
        search_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                search_dialog.dismiss();
                String searchName = search_field.getText().toString();
                List<FileModel> searchResult = new ArrayList<>();
                for(int i = 0; i < files.size(); i++){
                    String searchedName = files.get(i).getFile_name().replaceFirst("[.][^.]+$", "");
                    if(searchedName.equalsIgnoreCase(searchName) || searchedName.toLowerCase().contains(searchName.toLowerCase())){
                        searchResult.add(files.get(i));
                    }
                }
                DataAdapter newAdapter = new DataAdapter(searchResult);
                recyclerView.setAdapter(newAdapter);
            }
        });

        search_dialog.show();

    }

    public class DataAdapter extends RecyclerView.Adapter<DataAdapter.DataVH>{
        List<FileModel> fileModels;

        public DataAdapter(List<FileModel> fileModels) {
            this.fileModels = fileModels;
        }

        @NonNull
        @Override
        public DataVH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(getApplicationContext()).inflate(R.layout.data_item,null);
            return new DataVH(view);
        }

        @Override
        public void onBindViewHolder(@NonNull final DataVH holder, int position) {
            final FileModel VhModel = fileModels.get(position);

            holder.data_name.setText(VhModel.getFile_name());
            holder.data_date.setText(VhModel.getFile_date());
            holder.data_pic.setImageResource(VhModel.getDataType());

            if(lng_status == null){
                lng_status = "en";
            }else if(lng_status.equals("en")){
                holder.download_btn.setText("Download");
                holder.delete_btn.setText("Delete");
            } else if (lng_status.equals("ch")) {
                holder.download_btn.setText("删除");
                holder.delete_btn.setText("下载");
            }
            holder.download_btn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    startDownload(getApplicationContext(),VhModel.getFile_name(),VhModel.getFileExtension(),Environment.DIRECTORY_DOWNLOADS,VhModel.getFileURL());
                }

                public void startDownload(Context context, String filename, String fileExtension,String destination,String url){
                    DownloadManager downloadManager = (DownloadManager) context.
                            getSystemService(Context.DOWNLOAD_SERVICE);
                    Uri uri = Uri.parse(url);
                    DownloadManager.Request request = new DownloadManager.Request(uri);

                    request.setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED);
                    request.setDestinationInExternalFilesDir(context,destination,filename);

                    downloadManager.enqueue(request);
                }
            });
            if(!user.getEmail().equals("admin123@admin.com")){
                holder.delete_btn.setVisibility(View.INVISIBLE);
                holder.delete_btn.setEnabled(false);
            }else{
                holder.delete_btn.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        new AlertDialog.Builder(MainActivity.this)
                                .setMessage("Are you Sure to delete this")
                                .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialogInterface, int i) {
                                        deleteID = dataAdapter.getTaskAt(holder.getAdapterPosition()).getId();
                                        databaseReference.child("Files").child("AllFiles").child(deleteID).removeValue();
                                        databaseReference.child("Files").child(VhModel.getCategory()).child(deleteID).removeValue();
                                        StorageReference deletedfile = storage.getReferenceFromUrl(VhModel.getFileURL());
                                        deletedfile.delete()
                                                .addOnCompleteListener(new OnCompleteListener<Void>() {
                                                    @Override
                                                    public void onComplete(@NonNull Task<Void> task) {
                                                        Toast.makeText(MainActivity.this, "Delete Successful", Toast.LENGTH_SHORT).show();
                                                        getFiles();
                                                    }
                                                });
                                    }
                                })
                                .setNegativeButton("No", new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialogInterface, int i) {
                                        getFiles();
                                    }
                                })
                                .show();
                    }
                });
            }
        }
        public FileModel getTaskAt(int position){
            return fileModels.get(position);
        }

        @Override
        public int getItemCount() {
            return fileModels.size();
        }

        public class DataVH extends RecyclerView.ViewHolder{
            ImageView data_pic;
            TextView data_name,data_date;
            Button download_btn,delete_btn;
            public DataVH(@NonNull View itemView) {
                super(itemView);

                data_name                                   = itemView.findViewById(R.id.file_name);
                data_date                                   = itemView.findViewById(R.id.file_date);
                data_pic                                    = itemView.findViewById(R.id.dataType);
                download_btn                                = itemView.findViewById(R.id.download_btn);
                delete_btn                                  = itemView.findViewById(R.id.delete_btn);
            }
        }
    }
    public static boolean checkConnection(Context context) {
        final ConnectivityManager connMgr = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);

        if (connMgr != null) {
            NetworkInfo activeNetworkInfo = connMgr.getActiveNetworkInfo();

            if (activeNetworkInfo != null) { // connected to the internet
                // connected to the mobile provider's data plan
                if (activeNetworkInfo.getType() == ConnectivityManager.TYPE_WIFI) {
                    // connected to wifi
                    return true;
                } else return activeNetworkInfo.getType() == ConnectivityManager.TYPE_MOBILE;
            }
        }
        return false;
    }
}
