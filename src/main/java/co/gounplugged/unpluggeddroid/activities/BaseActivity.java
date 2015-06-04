package co.gounplugged.unpluggeddroid.activities;

import android.support.v7.app.ActionBarActivity;

public class BaseActivity extends ActionBarActivity {


    protected void hideActionBar() {
        getSupportActionBar().hide();
    }

    protected void displayHomeAsUp() {
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
    }
}
