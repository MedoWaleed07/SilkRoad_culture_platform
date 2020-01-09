package com.example.research;

import android.app.Dialog;
import android.content.Context;
import android.view.View;
import android.view.Window;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.victor.loading.book.BookLoading;

public class CustomProgress {
    BookLoading bookLoading;

    public static CustomProgress customProgress = null;
    private Dialog mDialog;

    public static CustomProgress getInstance() {
        if (customProgress == null) {
            customProgress = new CustomProgress();
        }
        return customProgress;
    }

    public void showProgress(Context context, String message, boolean cancelable) {
        mDialog = new Dialog(context);
        // no tile for the dialog
        mDialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        mDialog.setContentView(R.layout.progress_dialog);
        bookLoading = mDialog.findViewById(R.id.book_progress);
        //  mProgressBar.getIndeterminateDrawable().setColorFilter(context.getResources()
        // .getColor(R.color.material_blue_gray_500), PorterDuff.Mode.SRC_IN);
        TextView progressText = mDialog.findViewById(R.id.progress_text);
        progressText.setText("" + message);
        progressText.setVisibility(View.VISIBLE);
        bookLoading.setVisibility(View.VISIBLE);
        // you can change or add this line according to your need
        mDialog.setCancelable(cancelable);
        mDialog.setCanceledOnTouchOutside(cancelable);
        bookLoading.start();
        mDialog.show();
    }

    public void hideProgress() {
        if (mDialog != null) {
            mDialog.dismiss();
            bookLoading.stop();
            mDialog = null;
        }
    }
}
