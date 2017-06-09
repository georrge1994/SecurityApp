package com.georrge.securityapps.activity;

import android.content.Context;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.georrge.securityapps.R;

import java.util.List;

/* Class adapter for create application list in main activity. */
class AppListAdapter extends BaseAdapter {

    private List<ResolveInfo> packages;
    private LayoutInflater inflater;
    private PackageManager pm;

    public AppListAdapter(Context context, PackageManager pm, List<ResolveInfo> packages) {
        this.packages = packages;
        this.pm = pm;

        inflater = (LayoutInflater) context
                .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    }

    @Override
    public int getCount() {

        return packages.size();
    }

    @Override
    public ResolveInfo getItem(int p1) {

        return packages.get(p1);
    }

    @Override
    public long getItemId(int p1) {

        return p1;
    }

    @Override
    public View getView(int position, View v, ViewGroup parent) {

        View view = null;
        ViewHolder viewHolder;

        if (v == null) {
            viewHolder = new ViewHolder();
            view = inflater.inflate(R.layout.app_list_item,parent, false);
            viewHolder.tvAppLabel = (TextView) view.findViewById(R.id.list_item_text);
            viewHolder.ivAppIcon = (ImageView) view.findViewById(R.id.list_item_image);
            viewHolder.startApp = (Button) view.findViewById(R.id.list_startApp_button);
            viewHolder.changePass = (Button) view.findViewById(R.id.list_changePass_button);
            viewHolder.addPass = (Button) view.findViewById(R.id.list_addPass_button);
            view.setTag(viewHolder);
        } else {
            view = v;
            viewHolder = (ViewHolder) view.getTag();
        }

        ResolveInfo app = packages.get(position);
        viewHolder.tvAppLabel.setText(app.loadLabel(pm).toString());
        viewHolder.ivAppIcon.setImageDrawable(app.loadIcon(pm));
        viewHolder.startApp.setVisibility(View.GONE);
        viewHolder.changePass.setVisibility(View.GONE);
        viewHolder.addPass.setVisibility(View.GONE);

        return view;
    }

    static class ViewHolder {
        private TextView tvAppLabel;
        private ImageView ivAppIcon;
        private Button startApp;
        private Button changePass;
        private Button addPass;
    }

}