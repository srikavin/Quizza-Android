package me.srikavin.quiz;

import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;

import com.google.android.material.bottomnavigation.BottomNavigationView;

import java.util.ArrayList;
import java.util.List;

import androidx.annotation.NonNull;
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
        setContentView(R.layout.activity_main);

        List<Fragment> possibleStates = new ArrayList<>();
        possibleStates.add(profileFragment);
        possibleStates.add(battleFragment);
        possibleStates.add(createFragment);

        final ViewPager viewPager = findViewById(R.id.main_viewpager);
        viewPager.setAdapter(new SlidingPagerAdapter(getSupportFragmentManager(), possibleStates));

        final BottomNavigationView bottomNavigationView = findViewById(R.id.bottom_bar);

        bottomNavigationView.setOnNavigationItemSelectedListener(new BottomNavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
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
            }
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

        if (!getIntent().getBooleanExtra("auth", false)) {
            // Require login if not sent here by log-in activity
            Intent intent = new Intent(this, LoginActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
        }
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
}
