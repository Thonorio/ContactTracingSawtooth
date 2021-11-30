package com.example.covid_track_2021;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;

import com.example.covid_track_2021.beacon.BeaconService;
import com.example.covid_track_2021.blockchainAPI.BlockchainHandler;
import com.example.covid_track_2021.database.UserInformation.UserService;
import com.example.covid_track_2021.subscriber.SubscriberService;
import com.example.covid_track_2021.uniqueIdentifier.AlarmReceiver;
import com.google.android.material.bottomnavigation.BottomNavigationView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;

import com.example.covid_track_2021.databinding.ActivityMainBinding;

import java.util.Calendar;

import sawtooth.sdk.signing.PrivateKey;

public class MainActivity extends AppCompatActivity {

    private final static int REQUEST_ENABLE_BT = 1;
    private static final String TAG = "BeaconReferenceApp";
    private ActivityMainBinding binding;
    private Context applicationContext = null;
    private UserService userService;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        BottomNavigationView navView = findViewById(R.id.nav_view);
        // Passing each menu ID as a set of Ids because each
        // menu should be considered as top level destinations.
        AppBarConfiguration appBarConfiguration = new AppBarConfiguration.Builder(
                R.id.navigation_home, R.id.navigation_dashboard, R.id.navigation_notifications)
                .build();
        NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment_activity_main);
        NavigationUI.setupWithNavController(binding.navView, navController);

        userService = new UserService(getApplicationContext());

        startGeneratingNewUUIDs();
        startSubscriberService();
        startBeaconService();
        generateUserKey();

    }

    public void startGeneratingNewUUIDs(){
        Calendar cal = Calendar.getInstance();
        cal.add(Calendar.SECOND, 30);

        Intent intent = new Intent(this, AlarmReceiver.class);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(this.getApplicationContext(), 234324243, intent, 0);
        AlarmManager alarmManager = (AlarmManager) getSystemService(ALARM_SERVICE);
        alarmManager.setRepeating(AlarmManager.RTC_WAKEUP, cal.getTimeInMillis(), 5000, pendingIntent);
    }

    public void startSubscriberService(){
        getApplicationContext().startService(new Intent(getApplicationContext(), SubscriberService.class));
    }

    public void startBeaconService(){
        getApplicationContext().startService(new Intent(getApplicationContext(), BeaconService.class));
    }

    private void generateUserKey() {
        final PrivateKey privateKey;
        if(!userService.doesKeyExists()){
            privateKey = BlockchainHandler.generatePrivateKey();
            userService.storePrivateKey(privateKey);

        }
    }
}