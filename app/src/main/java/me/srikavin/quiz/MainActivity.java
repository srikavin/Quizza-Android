package me.srikavin.quiz;

import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;

import com.google.android.material.bottomnavigation.BottomNavigationView;

import java.util.ArrayList;
import java.util.List;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.viewpager.widget.ViewPager;
import me.srikavin.quiz.view.BattleFragment;
import me.srikavin.quiz.view.GameActivity;
import me.srikavin.quiz.view.LearnFragment;
import me.srikavin.quiz.view.ProfileFragment;

public class MainActivity extends AppCompatActivity {
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        List<Fragment> possibleStates = new ArrayList<>();
        possibleStates.add(new ProfileFragment());
        possibleStates.add(new BattleFragment());
        possibleStates.add(new LearnFragment());

        final ViewPager viewPager = findViewById(R.id.main_viewpager);
        viewPager.setAdapter(new SlidingPagerAdapter(getSupportFragmentManager(), possibleStates));

        final BottomNavigationView bottomNavigationView = findViewById(R.id.bottom_bar);

        bottomNavigationView.setOnNavigationItemSelectedListener(new BottomNavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                switch (item.getItemId()) {
                    case R.id.bottombaritem_profile:
                        viewPager.setCurrentItem(0);
                        break;
                    case R.id.bottombaritem_battle:
                        viewPager.setCurrentItem(1);
                        break;
                    case R.id.bottombaritem_learn:
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
                switch (position) {
                    case 0:
                        bottomNavigationView.setSelectedItemId(R.id.bottombaritem_profile);
                        break;
                    case 1:
                        bottomNavigationView.setSelectedItemId(R.id.bottombaritem_battle);
                        break;
                    case 2:
                        bottomNavigationView.setSelectedItemId(R.id.bottombaritem_learn);
                        break;
                }
            }

            @Override
            public void onPageScrollStateChanged(int state) {
            }
        });

        Intent intent = new Intent(getApplicationContext(), GameActivity.class);
        startActivity(intent);
    }
}
