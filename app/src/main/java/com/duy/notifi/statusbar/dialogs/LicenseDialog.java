package com.duy.notifi.statusbar.dialogs;

import android.content.Context;
import android.os.Bundle;
import android.support.v7.app.AppCompatDialog;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;

import com.duy.notifi.R;
import com.duy.notifi.statusbar.adapters.LicenseAdapter;

public class LicenseDialog extends AppCompatDialog {

    public LicenseDialog(Context context) {
        super(context, R.style.AppTheme_Dialog);
        setTitle(R.string.action_libraries);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.dialog_recycler);

        RecyclerView recyclerView = (RecyclerView) findViewById(R.id.recycler);
        recyclerView.setLayoutManager(new GridLayoutManager(getContext(), 1));
        recyclerView.setAdapter(new LicenseAdapter(getContext()));

        findViewById(R.id.dismiss).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dismiss();
            }
        });
    }
}
