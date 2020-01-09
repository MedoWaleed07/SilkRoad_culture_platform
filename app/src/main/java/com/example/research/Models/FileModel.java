package com.example.research.Models;

import android.net.Uri;

import java.net.URI;
import java.net.URL;

public class FileModel {
    String file_name,file_date,id;
    String fileURL,fileExtension,category;
    int dataType;

    public FileModel() {
    }

    public FileModel(String file_name, String file_date, String id, String fileURL, String fileExtension, String category, int dataType) {
        this.file_name = file_name;
        this.file_date = file_date;
        this.id = id;
        this.fileURL = fileURL;
        this.fileExtension = fileExtension;
        this.category = category;
        this.dataType = dataType;
    }

    public String getFile_name() {
        return file_name;
    }

    public void setFile_name(String file_name) {
        this.file_name = file_name;
    }

    public String getFile_date() {
        return file_date;
    }

    public void setFile_date(String file_date) {
        this.file_date = file_date;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getFileURL() {
        return fileURL;
    }

    public void setFileURL(String fileURL) {
        this.fileURL = fileURL;
    }

    public String getFileExtension() {
        return fileExtension;
    }

    public void setFileExtension(String fileExtension) {
        this.fileExtension = fileExtension;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public int getDataType() {
        return dataType;
    }

    public void setDataType(int dataType) {
        this.dataType = dataType;
    }
}
