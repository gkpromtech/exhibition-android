package ru.gkpromtech.exhibition.about;

import android.app.Fragment;
import android.app.FragmentTransaction;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarActivity;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.analytics.GoogleAnalytics;

import java.util.List;
import java.util.Stack;

import ru.gkpromtech.exhibition.R;
import ru.gkpromtech.exhibition.db.DbHelper;
import ru.gkpromtech.exhibition.db.Table;
import ru.gkpromtech.exhibition.model.Organization;
import ru.gkpromtech.exhibition.utils.AnalyticsManager;


public class AboutActivity extends ActionBarActivity implements AboutCallbacks {

    private Stack<Fragment> mFragmentsStack = new Stack<>();
    private Fragment mFragment;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_about);

        ActionBar actionBar = getSupportActionBar();
        actionBar.setDisplayHomeAsUpEnabled(true);

        showFragment(AboutFragment.newInstance());

        AnalyticsManager.sendEvent(this, R.string.about_category, R.string.action_open);
    }

    @Override
    protected void onStart() {
        super.onStart();
        GoogleAnalytics.getInstance(this).reportActivityStart(this);
    }

    @Override
    protected void onStop() {
        super.onStop();
        GoogleAnalytics.getInstance(this).reportActivityStop(this);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                onBackPressed();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onBackPressed() {
        if (mFragmentsStack.isEmpty()) {
            super.onBackPressed();
            return;
        }

        setTitle(R.string.title_activity_about);

        mFragment = mFragmentsStack.pop();
        getFragmentManager().beginTransaction()
                .setCustomAnimations(R.animator.slide_in_right, R.animator.slide_out_left)
                .replace(R.id.container, mFragment).commit();
    }

    private void showFragment(Fragment fragment) {
        FragmentTransaction tr = getFragmentManager().beginTransaction();

        if (mFragment != null) {
            mFragmentsStack.push(mFragment);
            tr.setCustomAnimations(R.animator.slide_in_left, R.animator.slide_out_right);
        }
        tr.replace(R.id.container, fragment).commit();

        mFragment = fragment;
    }


    // AboutFragment

    public void onOrganizersClicked(View v) {
        setTitle(R.string.about_organizers);
        showOrganization(new Integer[] {
                Organization.STATUS_ORGANIZER,
                Organization.STATUS_CO_ORGANIZER
        });
    }

    public void onSponsorsClicked(View v) {
        setTitle(R.string.about_sponsors);
        showOrganization(new Integer[] {
                Organization.STATUS_GENERAL_SPONSOR,
                Organization.STATUS_SPONSOR
        });
    }

    private void showOrganization(Integer[] statuses) {
        Table<Organization> organizations = DbHelper.getInstance(this).getTableFor(Organization.class);

        try {
            String selection = "status IN (" + DbHelper.makePlaceholders(statuses.length) + ") "
                    + " AND fullname IS NOT NULL AND fullname <> ''";
            String[] args = DbHelper.makeArguments(statuses);

            List<Organization> org = organizations.select(selection, args, null, null, "status");
            showFragment(OrganizationsFragment.newInstance(org.toArray(new Organization[org.size()])));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void onContactDevelopersClicked(View v) {
        final String email = "info@gkpromtech.ru";
        Intent intent = new Intent(Intent.ACTION_SENDTO,
                Uri.fromParts("mailto", email, null));
        intent.putExtra(Intent.EXTRA_SUBJECT, getString(R.string.app_name));

        startActivity(Intent.createChooser(intent, ""));
    }


    // OrganizationsFragment

    public void onAddressClicked(View v) {
        try {
            String uri = "geo:0,0?q=" + ((TextView)v).getText().toString();
            Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(uri));
            startActivity(intent);
        } catch (Exception e) {
            Toast.makeText(this, getString(R.string.error_starting_gmaps), Toast.LENGTH_LONG).show();
        }
    }

    public void onPhoneClicked(View v) {
        String phone = ((TextView)v).getText().toString();
        Intent callIntent = new Intent(Intent.ACTION_CALL);
        callIntent.setData(Uri.parse("tel:" + phone));
        startActivity(callIntent);
    }

    public void onEmailClicked(View v) {
        String email = ((TextView)v).getText().toString();
        Intent intent = new Intent(Intent.ACTION_SENDTO,
                Uri.fromParts("mailto", email, null));
        intent.putExtra(Intent.EXTRA_SUBJECT, getString(R.string.app_name));

        startActivity(Intent.createChooser(intent, ""));
    }

    public void onSiteClicked(View v) {
        String site = ((TextView)v).getText().toString();
        Intent intent = new Intent(Intent.ACTION_VIEW);
        intent.setData(Uri.parse(site));
        startActivity(intent);
    }

    @Override
    public void onOrganizationSelected(Organization organization) {
        showFragment(OrganizationDetailsFragment.newInstance(organization));
    }
}
