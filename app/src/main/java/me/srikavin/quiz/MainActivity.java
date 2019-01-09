package me.srikavin.quiz;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;

import com.google.android.material.bottomnavigation.BottomNavigationView;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileWriter;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.List;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.viewpager.widget.ViewPager;
import me.srikavin.quiz.view.BattleFragment;
import me.srikavin.quiz.view.CreateFragment;
import me.srikavin.quiz.view.LoginActivity;
import me.srikavin.quiz.view.ProfileFragment;

public class MainActivity extends AppCompatActivity {
    public static String TAG = "QUIZ";
    ProfileFragment profileFragment = new ProfileFragment();
    BattleFragment battleFragment = new BattleFragment();
    CreateFragment createFragment = new CreateFragment();

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (!getIntent().getBooleanExtra("auth", false)) {
            // Require login if not sent here by log-in activity
            Intent intent = new Intent(this, LoginActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
            return;
        }
        setContentView(R.layout.activity_main);

        List<Fragment> possibleStates = new ArrayList<>();
        possibleStates.add(profileFragment);
        possibleStates.add(battleFragment);
        possibleStates.add(createFragment);

        final ViewPager viewPager = findViewById(R.id.main_viewpager);
        viewPager.setAdapter(new SlidingPagerAdapter(getSupportFragmentManager(), possibleStates));

        final BottomNavigationView bottomNavigationView = findViewById(R.id.bottom_bar);

        bottomNavigationView.setOnNavigationItemSelectedListener(item -> {
            switch (item.getItemId()) {
                case R.id.bottombaritem_profile:
                    updateTitle(0);
                    viewPager.setCurrentItem(0);
                    break;
                case R.id.bottombaritem_battle:
                    updateTitle(1);
                    viewPager.setCurrentItem(1);
                    break;
                case R.id.bottombaritem_create:
                    updateTitle(2);
                    viewPager.setCurrentItem(2);
                    break;
            }
            return true;
        });

        viewPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
            }

            @Override
            public void onPageSelected(int position) {
                updateTitle(position);
                switch (position) {
                    case 0:
                        bottomNavigationView.setSelectedItemId(R.id.bottombaritem_profile);
                        break;
                    case 1:
                        bottomNavigationView.setSelectedItemId(R.id.bottombaritem_battle);
                        break;
                    case 2:
                        bottomNavigationView.setSelectedItemId(R.id.bottombaritem_create);
                        break;
                }
            }

            @Override
            public void onPageScrollStateChanged(int state) {
            }
        });

        updateTitle(1);
        bottomNavigationView.setSelectedItemId(R.id.bottombaritem_battle);
        viewPager.setCurrentItem(1);
    }

    private void updateTitle(int page) {
        ActionBar actionBar = getSupportActionBar();
        assert actionBar != null;
        switch (page) {
            case 0:
                actionBar.setTitle(R.string.bottombar_profile);
                break;
            case 1:
                actionBar.setTitle(R.string.bottombar_battle);
                break;
            case 2:
                actionBar.setTitle(R.string.bottombar_create);
                break;
        }
    }

    private void reportBug() {
        String logCatException = null;
        File logCatTemp = null;
        try {
            Process process = Runtime.getRuntime().exec("logcat -d");
            BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(process.getInputStream()));

            StringBuilder log = new StringBuilder();
            String line;
            while ((line = bufferedReader.readLine()) != null) {
                log.append(line);
                log.append('\n');
            }
            String logcat = log.toString();
            File cacheDir = getApplicationContext().getExternalCacheDir();
            logCatTemp = File.createTempFile("bugreport", "logcat.txt", cacheDir);
            FileWriter writer = new FileWriter(logCatTemp);
            writer.write(logcat);
            writer.close();

        } catch (Exception e) {
            // Exception occurred when saving log cat
            logCatException = "Exception occurred while trying to save logcat:\n";
            StringWriter sw = new StringWriter();
            e.printStackTrace(new PrintWriter(sw));
            logCatException += sw.toString();
        }
        Intent intent = new Intent(Intent.ACTION_SENDTO);
        intent.setData(Uri.parse("mailto:"))
                .putExtra(Intent.EXTRA_EMAIL, new String[]{"quizza.bug.reports@mail.srikavin.me"})
                .putExtra(Intent.EXTRA_SUBJECT, "Quizza Bug Report");
        if (logCatTemp != null) {
            intent.putExtra(Intent.EXTRA_STREAM, Uri.fromFile(logCatTemp));
        }
        intent.putExtra(Intent.EXTRA_TEXT,
                "Steps to Reproduce: \n" +
                        "Details about the bug: \n" +
                        "Would you like to be contacted if more details are necessary? \n" +
                        "\n" +
                        "------------------------------------------------------------------\n" +
                        "LOGCAT:\n" +
                        (logCatException == null ? "Saved successfully" : logCatException)
        );
        if (intent.resolveActivity(getPackageManager()) != null) {
            startActivity(intent);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.activity_main_actionbar_menu, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.report_bug) {
            reportBug();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }
}
