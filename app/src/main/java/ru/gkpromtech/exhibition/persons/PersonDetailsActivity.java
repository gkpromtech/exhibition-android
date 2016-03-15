package ru.gkpromtech.exhibition.persons;

import android.content.Intent;
import android.graphics.drawable.GradientDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarActivity;
import android.util.Pair;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.android.gms.analytics.GoogleAnalytics;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.util.List;

import ru.gkpromtech.exhibition.R;
import ru.gkpromtech.exhibition.db.DbHelper;
import ru.gkpromtech.exhibition.db.Table;
import ru.gkpromtech.exhibition.model.Entity;
import ru.gkpromtech.exhibition.model.Organization;
import ru.gkpromtech.exhibition.model.Person;
import ru.gkpromtech.exhibition.organizations.OrganizationFilesDownloader;
import ru.gkpromtech.exhibition.utils.AnalyticsManager;
import ru.gkpromtech.exhibition.utils.ImageLoader;
import ru.gkpromtech.exhibition.utils.SharedData;

public class PersonDetailsActivity extends ActionBarActivity {

    private Organization mOrganization;
    private Person mPerson;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_person_details);

        setTitle(R.string.representatives);

        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
        }

        int personId = getIntent().getIntExtra("personId", 0);

        DbHelper db = DbHelper.getInstance(this);
        Table<Person> tablePersons = db.getTableFor(Person.class);
        try {
            List<Pair<Entity[], Person>> personsOrgs = tablePersons.selectJoined(
                    new Table.Join[] {
                            new Table.Join("organizationid", Organization.class, "id")
                    },
                    "t.id = ?", new String[]{String.valueOf(personId)}, "name");
            Pair<Entity[], Person> personOrg = personsOrgs.get(0);
            mPerson = personOrg.second;
            mOrganization = (Organization) personOrg.first[0];
        } catch (Exception e) {
            return;
        }

        ImageView imagePhoto = (ImageView) findViewById(R.id.imagePhoto);
        TextView textName = (TextView) findViewById(R.id.textName);
        TextView textPosition = (TextView) findViewById(R.id.textPosition);
        TextView textOrganization = (TextView) findViewById(R.id.textOrganization);
        ImageView imageOrgLogo = (ImageView) findViewById(R.id.imageLogo);
        TextView textEmail = (TextView) findViewById(R.id.textEmail);
        TextView textPhone = (TextView) findViewById(R.id.textPhone);
        TextView textSite = (TextView) findViewById(R.id.textSite);

        textName.setText(mPerson.name);
        textPosition.setText(mPerson.position);
        textOrganization.setText(mOrganization.fullname);

        fillTextView(textEmail, mPerson.email);
        fillTextView(textPhone, mPerson.phone);
        fillTextView(textSite, mPerson.site);

        ImageLoader.load(mPerson.photo, imagePhoto, R.drawable.no_photo);
        ImageLoader.load(mOrganization.logo, imageOrgLogo, R.drawable.no_logo);

        ((GradientDrawable) findViewById(R.id.layoutPersonBk).getBackground())
                .setGradientRadius(getResources().getDimension(
                        R.dimen.person_photo_gradient_radius));

        AnalyticsManager.sendEvent(this, R.string.person_category, R.string.action_open, personId);
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

    private void fillTextView(TextView textView, String text) {
        if (text == null || text.isEmpty()) {
            textView.setVisibility(View.GONE);
        } else {
            textView.setText(text);
        }
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

    public void onAddToContacts(View view) {
        StringBuilder vcf = new StringBuilder("BEGIN:VCARD\nVERSION:3.0\nFN:")
                .append(mPerson.name).append("\nORG:").append(mOrganization.fullname);

        if (mPerson.position != null && !mPerson.position.isEmpty())
            vcf.append("\nTITLE:").append(mPerson.position);

        if (mPerson.site != null && !mPerson.site.isEmpty())
            vcf.append("\nURL:").append(mPerson.site);

        if (mPerson.phone != null && !mPerson.phone.isEmpty())
            vcf.append("\nTEL:").append(mPerson.phone);

        if (mPerson.email != null && !mPerson.email.isEmpty())
            vcf.append("\nEMAIL;TYPE=INTERNET:").append(mPerson.email);

        if (mPerson.photo != null && !mPerson.photo.isEmpty())
            vcf.append("\nPHOTO;VALUE=uri:").append(mPerson.photo);

        if (mOrganization.logo != null && !mOrganization.logo.isEmpty())
            vcf.append("\nLOGO;VALUE=uri:").append(mOrganization.logo);

        vcf.append("\nEND:VCARD\n");

        OutputStreamWriter writer = null;
        final String fileName = SharedData.EXTERNAL_DIR + mPerson.name + ".vcf";
        try {
            FileOutputStream fOut = new FileOutputStream(fileName);
            writer = new OutputStreamWriter(fOut);
            writer.write(vcf.toString());
            writer.flush();
        } catch (Exception e) {
            e.printStackTrace();
            return;
        } finally {
            if (writer != null) {
                try {
                    writer.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }

        Intent intent = new Intent(Intent.ACTION_VIEW);
        intent.setDataAndType(Uri.parse("file://" + fileName), "text/x-vcard");
        startActivity(intent);
    }

    public void onRequestMeeting(View view) {
    }

    public void onSaveMaterialsClicked(View view) {
        OrganizationFilesDownloader.download(this, mOrganization);
    }
}
