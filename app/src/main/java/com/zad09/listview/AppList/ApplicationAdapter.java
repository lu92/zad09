package com.zad09.listview.AppList;

import android.app.ListActivity;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Filter;
import android.widget.ImageView;
import android.widget.RatingBar;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class ApplicationAdapter extends ArrayAdapter<ApplicationInfo> {
    private List<ApplicationInfo> appsList = null;
    private List<ApplicationInfo> originAppsList = null;
    private ListActivity context;
    private PackageManager packageManager;

    private static final String PREFERENCES_NAME = "myPreferences";
    private final SharedPreferences sharedPreferences;

    private NameComparator nameComparator = new NameComparator();
    private RatingComparator ratingComparator = new RatingComparator();

    public ApplicationAdapter(ListActivity context, int textViewResourceId,
                              List<ApplicationInfo> appsList) {
        super(context, textViewResourceId, appsList);
        this.context = context;
        this.appsList = appsList;
        this.originAppsList = appsList;
        packageManager = context.getPackageManager();
        this.sharedPreferences = context.getSharedPreferences(PREFERENCES_NAME, Context.MODE_PRIVATE);
    }

    public void resetData() {
        appsList = originAppsList;
    }

    @Override
    public int getCount() {
        return ((null != appsList) ? appsList.size() : 0);
    }

    @Override
    public ApplicationInfo getItem(int position) {
        return ((null != appsList) ? appsList.get(position) : null);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View v = convertView;

        ViewHolder holder = new ViewHolder();

        if (null == v) {
            LayoutInflater layoutInflater = (LayoutInflater) context
                    .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            v = layoutInflater.inflate(R.layout.snippet_list_row, null);
            TextView appName = (TextView) v.findViewById(R.id.app_name);
            RatingBar appRating = (RatingBar) v.findViewById(R.id.app_rating);
            ImageView iconview = (ImageView) v.findViewById(R.id.app_icon);

            holder.appNameView = appName;
            holder.appIconView = iconview;
            holder.appRatingView = appRating;

            v.setTag(holder);
        } else
            holder = (ViewHolder) v.getTag();

        ApplicationInfo applicationInfo = appsList.get(position);

        holder.appNameView.setText(applicationInfo.loadLabel(packageManager));
        holder.appIconView.setImageDrawable(applicationInfo.loadIcon(packageManager));
        holder.appIconView.setOnClickListener(getIconClickListener(position));
        holder.appRatingView.setOnRatingBarChangeListener(onRatingChangedListener(holder, position));
        holder.appRatingView.setRating(sharedPreferences.getFloat(applicationInfo.loadLabel(packageManager).toString(), 0));

        return v;
    }

    private View.OnClickListener getIconClickListener(final int position) {
        return new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int originIndex = originAppsList.indexOf(appsList.get(position));
                ((ListenerActivity) context).runSelectedApp(originIndex);
            }
        };
    }

    private static class ViewHolder {
        public TextView appNameView;
        public ImageView appIconView;
        public RatingBar appRatingView;
    }

    private RatingBar.OnRatingBarChangeListener onRatingChangedListener(final ViewHolder holder, final int position) {
        return new RatingBar.OnRatingBarChangeListener() {
            @Override
            public void onRatingChanged(RatingBar ratingBar, float v, boolean b) {
                final ApplicationInfo clickedApplicationInfo = getItem(position);
                SharedPreferences.Editor preferencesEditor = sharedPreferences.edit();
                preferencesEditor.putFloat(clickedApplicationInfo.loadLabel(packageManager).toString(), v);
                preferencesEditor.commit();

                Log.i("ButtonType", "" + ((ListenerActivity) context).getPushedButton());
                if (((ListenerActivity) context).getPushedButton() == ButtonEnum.SORT_BY_RATING_BUTTON) {
                    context.runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            Log.i("Rating", "Rating Button clicked");
                            Collections.sort(appsList, ratingComparator);
                            int appIndexAfterSort = appsList.indexOf(clickedApplicationInfo);
                            notifyDataSetChanged();
                            context.getListView().invalidateViews();
                            context.getListView().setSelection(appIndexAfterSort);
                        }
                    });
                }

            }
        };
    }

    private class NameComparator implements Comparator<ApplicationInfo> {

        @Override
        public int compare(ApplicationInfo first, ApplicationInfo second) {
            return first.loadLabel(packageManager).toString().toLowerCase().compareTo(second.loadLabel(packageManager).toString().toLowerCase());
        }
    }

    private class RatingComparator implements Comparator<ApplicationInfo> {

        @Override
        public int compare(ApplicationInfo first, ApplicationInfo second) {
            return Float.compare(sharedPreferences.getFloat(first.loadLabel(packageManager).toString(), 0),
                    sharedPreferences.getFloat(second.loadLabel(packageManager).toString(), 0));
        }
    }

    public void sortByName() {
        Collections.sort(appsList, nameComparator);
        notifyDataSetChanged();
    }

    public void sortByRating() {
        Collections.sort(appsList, ratingComparator);
        notifyDataSetChanged();
    }


    @Override
    public Filter getFilter() {
        return new Filter() {
            @Override
            protected FilterResults performFiltering(CharSequence constraint) {
                FilterResults results = new FilterResults();
                if (constraint == null || constraint.length() == 0) {
                    // No filter implemented we return all the list
                    results.values = originAppsList;
                    results.count = originAppsList.size();
                } else {
                    // We perform filtering operation
                    List<ApplicationInfo> filteredApps = new ArrayList<ApplicationInfo>();

                    for (ApplicationInfo p : appsList) {
                        if (p.loadLabel(packageManager).toString().toUpperCase().startsWith(constraint.toString().toUpperCase()))
                            filteredApps.add(p);
                    }

                    results.values = filteredApps;
                    results.count = filteredApps.size();

                }
                return results;
            }

            @Override
            protected void publishResults(CharSequence constraint, FilterResults results) {
                // Now we have to inform the adapter about the new list filtered
                if (results.count == 0)
                    notifyDataSetInvalidated();
                else {
                    appsList = (List<ApplicationInfo>) results.values;
                    notifyDataSetChanged();
                }
            }
        };
    }
};
