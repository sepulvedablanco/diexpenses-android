package es.upsa.mimo.android.diexpenses.activities;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;

import butterknife.BindView;
import butterknife.ButterKnife;
import es.upsa.mimo.android.diexpenses.R;

/**
 * Created by Diego on 3/4/16.
 */
public class AboutActivity extends AppCompatActivity {

    @BindView(R.id.about_toolbar) Toolbar toolbar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.fragment_about);

        ButterKnife.bind(this);

        setSupportActionBar(toolbar);
        setTitle(R.string.drawer_nav_about_diexpenses);

        if (getSupportActionBar() != null){
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowHomeEnabled(true);
        }

    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            finish();
        }

        return super.onOptionsItemSelected(item);
    }
}
