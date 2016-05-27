package th.ac.ku.ixistic.taxifare;

import android.app.ActionBar;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.design.widget.NavigationView;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.view.GravityCompat;
import android.support.v4.view.ViewPager;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.facebook.AccessToken;
import com.facebook.AccessTokenTracker;
import com.facebook.CallbackManager;
import com.facebook.FacebookCallback;
import com.facebook.FacebookException;
import com.facebook.FacebookSdk;
import com.facebook.GraphRequest;
import com.facebook.GraphResponse;
import com.facebook.login.LoginManager;
import com.facebook.login.LoginResult;
import com.facebook.login.widget.LoginButton;
import com.firebase.client.Firebase;
import com.squareup.picasso.Picasso;

import org.json.JSONException;
import org.json.JSONObject;

import java.net.MalformedURLException;
import java.net.URL;

public class MainActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener {

    private ImageView imgView;
    private TextView name;
    private TextView email;
    private CallbackManager callbackManager;
    private LoginButton loginButton;
    private Firebase myFirebaseRef;
    private ViewPager viewPager;
    private AccessTokenTracker accessTokenTracker;

    private Bundle bFacebookData;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if( savedInstanceState != null ) {

        }

        FacebookSdk.sdkInitialize(this.getApplicationContext());
        callbackManager = CallbackManager.Factory.create();
        setContentView(R.layout.activity_main);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
            this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.setDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
//        View header = LayoutInflater.from(this).inflate(R.layout.nav_header_main, null);
        View header = navigationView.getHeaderView(0);

        imgView = (ImageView) header.findViewById(R.id.imageView);
        name = (TextView) header.findViewById(R.id.name);
        email = (TextView) header.findViewById(R.id.textView);
        name.setText("Taxi Fare");
        email.setText("taxifare@taxifare.com");

        LoginManager.getInstance().registerCallback(callbackManager, new FacebookCallback<LoginResult>() {
            @Override
            public void onSuccess(LoginResult loginResult) {
                String accessToken = loginResult.getAccessToken().getToken();
                Log.i("accessToken", accessToken);

                GraphRequest request = GraphRequest.newMeRequest(loginResult.getAccessToken(), new GraphRequest.GraphJSONObjectCallback() {

                    @Override
                    public void onCompleted(JSONObject object, GraphResponse response) {
                        Log.i("LoginActivity", response.toString());
                        // Get facebook data from login
                        bFacebookData = getFacebookData(object);
                        updateProfile();
                    }
                });
                Bundle parameters = new Bundle();
                parameters.putString("fields", "id, first_name, last_name, email,gender, birthday, location"); // Parámetros que pedimos a facebook
                request.setParameters(parameters);
                request.executeAsync();
            }

            @Override
            public void onCancel() {
                // App code
            }

            @Override
            public void onError(FacebookException exception) {
                // App code
            }
        });

        accessTokenTracker = new AccessTokenTracker() {
            @Override
            protected void onCurrentAccessTokenChanged(AccessToken oldAccessToken,
                                                       AccessToken currentAccessToken) {
                if (currentAccessToken == null) {
                    SharedPreferences sp = getSharedPreferences("Main", Context.MODE_PRIVATE);
                    SharedPreferences.Editor editor = sp.edit();
                    editor.clear();
                    editor.apply();
                    Intent intent = getIntent();
                    finish();
                    startActivity(intent);
                }
            }
        };

        navigationView.setNavigationItemSelectedListener(this);

        // Get the ViewPager and set it's PagerAdapter so that it can display items
        viewPager = (ViewPager) findViewById(R.id.viewpager);
        viewPager.setAdapter(new SampleFragmentPagerAdapter(getSupportFragmentManager(),
                MainActivity.this));

        // Give the TabLayout the ViewPager
        TabLayout tabLayout = (TabLayout) findViewById(R.id.sliding_tabs);
        tabLayout.setupWithViewPager(viewPager);

        //map
        Firebase.setAndroidContext(this);
        myFirebaseRef = new Firebase("https://taxifare.firebaseio.com/");

    }

    private void updateProfile() {
        Picasso.with(this).load(bFacebookData.getString("profile_pic")).into(imgView);
        name.setText(bFacebookData.getString("first_name") + " " + bFacebookData.getString("last_name"));
        email.setText(bFacebookData.getString("email"));

        Firebase users = myFirebaseRef.child("users");
        Firebase user = users.child(bFacebookData.getString("idFacebook"));
        Firebase profile = user.child("profile");
        User userProfile = new User(bFacebookData.getString("email"));
        profile.setValue(userProfile);

        Fragment page1 = getSupportFragmentManager().findFragmentByTag("android:switcher:" + R.id.viewpager + ":" + 0);
        Fragment page2 = getSupportFragmentManager().findFragmentByTag("android:switcher:" + R.id.viewpager + ":" + 1);

        if(page1 != null)
            ((MainFragment)page1).updateData(bFacebookData);
        if(page2 != null)
            ((HistoryFragment)page2).updateData(bFacebookData);
        savePref();
    }

    public Bundle loadBundle() {
        return bFacebookData;
    }

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
//        if (id == R.id.action_settings) {
//            return true;
//        }

        return super.onOptionsItemSelected(item);
    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();
//
//        if (id == R.id.nav_camera) {
//            // Handle the camera action
//        } else if (id == R.id.nav_gallery) {
//
//        } else if (id == R.id.nav_slideshow) {
//
//        } else if (id == R.id.nav_manage) {
//
//        } else if (id == R.id.nav_share) {
//
//        } else if (id == R.id.nav_send) {
//
//        }

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    private Bundle getFacebookData(JSONObject object) {

        Bundle bundle = new Bundle();
        try {
            String id = null;
            id = object.getString("id");

            try {
                URL profile_pic = new URL("https://graph.facebook.com/" + id + "/picture?width=150&height=150");
                Log.i("profile_pic", profile_pic + "");
                bundle.putString("profile_pic", profile_pic.toString());

            } catch (MalformedURLException e) {
                e.printStackTrace();
                return null;
            }

            bundle.putString("idFacebook", id);
            if (object.has("first_name"))
                bundle.putString("first_name", object.getString("first_name"));
            if (object.has("last_name"))
                bundle.putString("last_name", object.getString("last_name"));
            if (object.has("email"))
                bundle.putString("email", object.getString("email"));
            if (object.has("gender"))
                bundle.putString("gender", object.getString("gender"));
            if (object.has("birthday"))
                bundle.putString("birthday", object.getString("birthday"));
            if (object.has("location"))
                bundle.putString("location", object.getJSONObject("location").getString("name"));
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return bundle;

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        callbackManager.onActivityResult(requestCode, resultCode, data);
    }

    public void savePref(){
        SharedPreferences sp = getSharedPreferences("Main", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sp.edit();
        editor.putString("id", bFacebookData.getString("idFacebook"));
        editor.putString("first_name", bFacebookData.getString("first_name"));
        editor.putString("last_name", bFacebookData.getString("last_name"));
        editor.putString("email", bFacebookData.getString("email"));
        editor.putString("gender", bFacebookData.getString("gender"));
        editor.putString("birthday", bFacebookData.getString("birthday"));
        editor.putString("location", bFacebookData.getString("location"));
        editor.putString("profile_pic", bFacebookData.getString("profile_pic"));
        editor.putBoolean("status", true);
        editor.apply();
    }

    public void loadPref(){
        SharedPreferences sp = getSharedPreferences("Main", Context.MODE_PRIVATE);
        bFacebookData = new Bundle();
        bFacebookData.putString("idFacebook", sp.getString("id", ""));
        bFacebookData.putString("first_name", sp.getString("first_name", ""));
        bFacebookData.putString("last_name", sp.getString("last_name", ""));
        bFacebookData.putString("email", sp.getString("email", ""));
        bFacebookData.putString("gender", sp.getString("gender", ""));
        bFacebookData.putString("birthday", sp.getString("birthday", ""));
        bFacebookData.putString("location", sp.getString("location", ""));
        bFacebookData.putString("profile_pic", sp.getString("profile_pic", ""));
    }

    @Override
    protected void onResume() {
        super.onResume();
        SharedPreferences sp = getSharedPreferences("Main", Context.MODE_PRIVATE);
        if(sp.getBoolean("status", false)) {
            loadPref();
            updateProfile();
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        accessTokenTracker.stopTracking();
    }
}

