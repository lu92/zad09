package com.zad09.listview.AppList;

import android.app.AlertDialog;
import android.app.ListActivity;
import android.app.ProgressDialog;
import android.content.ActivityNotFoundException;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;

public class AppsActivity extends ListActivity implements ListenerActivity {
    private static String filterValue;
    private PackageManager packageManager = null;
    private static List<ApplicationInfo> applist = null;
    private static ApplicationAdapter listadaptor = null;
    private EditText filter;
    private static ButtonEnum pushedButton = ButtonEnum.NONE_BUTTON;
    private Button nameButton;
    private Button ratingButton;


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main_app);

        packageManager = getPackageManager();

        nameButton = (Button) findViewById(R.id.nameButton);
        nameButton.setOnClickListener(onClickNameButtonListener());

        ratingButton = (Button) findViewById(R.id.ratingButton);
        ratingButton.setOnClickListener(onClickRatingButtonListener());


        filter = (EditText) findViewById(R.id.filter);
        filter.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (listadaptor != null) {
                    if (count < before) {
                        // We're deleting char so we need to reset the adapter data
                        listadaptor.resetData();
                    }
                    filterValue = s.toString();
//                    listadaptor.getFilter().filter(s.toString());
                }
            }

            @Override
            public void afterTextChanged(Editable s) {
                if (listadaptor != null) {
                    listadaptor.getFilter().filter(s.toString());
                }
            }
        });

        new LoadApplications().execute();
    }

    @Override
    protected void onResume() {
        super.onResume();
        switch (pushedButton) {
            case SORT_BY_NAME_BUTTON:
                nameButton.setEnabled(false);
                ratingButton.setEnabled(true);
                break;

            case SORT_BY_RATING_BUTTON:
                ratingButton.setEnabled(false);
                nameButton.setEnabled(true);
                break;

            case NONE_BUTTON:
                nameButton.setEnabled(true);
                ratingButton.setEnabled(true);
                break;
        }
    }

    private View.OnClickListener onClickNameButtonListener() {
        return new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.i("NameButton", "clicked");
                listadaptor.sortByName();
                pushedButton = ButtonEnum.SORT_BY_NAME_BUTTON;
                nameButton.setEnabled(false);
                ratingButton.setEnabled(true);
            }
        };
    }

    private View.OnClickListener onClickRatingButtonListener() {
        return new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.i("RatingButton", "clicked");
                listadaptor.sortByRating();
                pushedButton = ButtonEnum.SORT_BY_RATING_BUTTON;
                ratingButton.setEnabled(false);
                nameButton.setEnabled(true);
            }
        };
    }

    @Override
    public ListView getListView() {
        return super.getListView();
    }

//    public boolean onCreateOptionsMenu(Menu menu) {
//        MenuInflater inflater = getMenuInflater();
//        inflater.inflate(R.menu.menu, menu);
//
//        return true;
//    }

    public boolean onOptionsItemSelected(MenuItem item) {
        boolean result = true;

        switch (item.getItemId()) {
            case R.id.menu_about: {
                displayAboutDialog();

                break;
            }
            default: {
                result = super.onOptionsItemSelected(item);

                break;
            }
        }

        return result;
    }

    private void displayAboutDialog() {
        final AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(getString(R.string.about_title));
        builder.setMessage(getString(R.string.about_desc));

        builder.setPositiveButton("Know More", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse("http://javatechig.com"));
                startActivity(browserIntent);
                dialog.cancel();
            }
        });
        builder.setNegativeButton("No Thanks!", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                dialog.cancel();
            }
        });

        builder.show();
    }

    @Override
    protected void onListItemClick(ListView l, View v, int position, long id) {
        super.onListItemClick(l, v, position, id);

        ApplicationInfo app = applist.get(position);
        try {
            Intent intent = packageManager
                    .getLaunchIntentForPackage(app.packageName);

            if (null != intent) {
                startActivity(intent);
            }
        } catch (ActivityNotFoundException e) {
            Toast.makeText(AppsActivity.this, e.getMessage(),
                    Toast.LENGTH_LONG).show();
        } catch (Exception e) {
            Toast.makeText(AppsActivity.this, e.getMessage(),
                    Toast.LENGTH_LONG).show();
        }
    }

    private List<ApplicationInfo> checkForLaunchIntent(List<ApplicationInfo> list) {
        ArrayList<ApplicationInfo> applist = new ArrayList<ApplicationInfo>();
        for (ApplicationInfo info : list) {
            try {
                if (null != packageManager.getLaunchIntentForPackage(info.packageName)) {
                    applist.add(info);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        return applist;
    }

    @Override
    public void runSelectedApp(int position) {
        ApplicationInfo app = applist.get(position);

        try {
            Intent intent = packageManager.getLaunchIntentForPackage(app.packageName);

            if (intent != null) {
                startActivity(intent);
            }
        } catch (ActivityNotFoundException e) {
            Toast.makeText(AppsActivity.this, e.getMessage(), Toast.LENGTH_LONG).show();
        } catch (Exception e) {
            Toast.makeText(AppsActivity.this, e.getMessage(), Toast.LENGTH_LONG).show();
        }
    }

    @Override
    public ButtonEnum getPushedButton() {
        return pushedButton;
    }

    private class LoadApplications extends AsyncTask<Void, Void, Void> {
        private ProgressDialog progress = null;

        @Override
        protected Void doInBackground(Void... params) {
            if (applist == null) {
                applist = checkForLaunchIntent(packageManager.getInstalledApplications(PackageManager.GET_META_DATA));

                listadaptor = new ApplicationAdapter(AppsActivity.this,
                        R.layout.snippet_list_row, applist);
            }
            return null;
        }

        @Override
        protected void onCancelled() {
            super.onCancelled();
        }

        @Override
        protected void onPostExecute(Void result) {
            setListAdapter(listadaptor);
            progress.dismiss();
            filter.setText(filterValue);
            super.onPostExecute(result);
        }

        @Override
        protected void onPreExecute() {
            progress = ProgressDialog.show(AppsActivity.this, null,
                    "Loading application info...");
            super.onPreExecute();
        }

        @Override
        protected void onProgressUpdate(Void... values) {
            super.onProgressUpdate(values);
        }
    }
}