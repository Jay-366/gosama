package com.example.gosama;

import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.viewpager2.adapter.FragmentStateAdapter;
import androidx.viewpager2.widget.ViewPager2;
import com.google.android.material.tabs.TabLayout;
import com.google.android.material.tabs.TabLayoutMediator;
import android.content.Intent;

public class MessageActivity extends AppCompatActivity {
    private static final String[] TAB_TITLES = {"Chat", "Notification"};

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_message);

        TabLayout tabLayout = findViewById(R.id.tabLayout);
        ViewPager2 viewPager = findViewById(R.id.viewPager);
        viewPager.setAdapter(new MessagePagerAdapter(this));

        new TabLayoutMediator(tabLayout, viewPager, (tab, position) -> tab.setText(TAB_TITLES[position])).attach();

        // Bottom navigation setup
        com.google.android.material.bottomnavigation.BottomNavigationView bottomNav = findViewById(R.id.bottomNavigation);
        bottomNav.setSelectedItemId(R.id.nav_activity);
        bottomNav.setOnItemSelectedListener(item -> {
            int itemId = item.getItemId();
            if (itemId == R.id.nav_home) {
                startActivity(new android.content.Intent(this, HomeActivity.class));
                finish();
                return true;
            } else if (itemId == R.id.nav_rewards) {
                startActivity(new android.content.Intent(this, RewardsActivity.class));
                finish();
                return true;
            } else if (itemId == R.id.nav_chat) {
                startActivity(new android.content.Intent(this, AssistantChatActivity.class));
                return true;
            } else if (itemId == R.id.nav_activity) {
                startActivity(new Intent(MessageActivity.this, LiveTrackingActivity.class));
                return true;
            } else if (itemId == R.id.nav_profile) {
                startActivity(new android.content.Intent(this, ProfileActivity.class));
                finish();
                return true;
            }
            return false;
        });
    }

    private static class MessagePagerAdapter extends FragmentStateAdapter {
        public MessagePagerAdapter(@NonNull FragmentActivity fa) {
            super(fa);
        }

        @NonNull
        @Override
        public Fragment createFragment(int position) {
            switch (position) {
                case 0:
                    return new ChatTabFragment();
                case 1:
                    return new NotificationTabFragment();
                default:
                    return new ChatTabFragment();
            }
        }

        @Override
        public int getItemCount() {
            return 2;
        }
    }
} 