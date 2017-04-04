package com.sociallogin.app.demosociallogin;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.facebook.AccessToken;
import com.facebook.CallbackManager;
import com.facebook.FacebookCallback;
import com.facebook.FacebookException;
import com.facebook.FacebookSdk;
import com.facebook.GraphRequest;
import com.facebook.GraphResponse;
import com.facebook.Profile;
import com.facebook.login.LoginManager;
import com.facebook.login.LoginResult;
import com.google.android.gms.auth.api.Auth;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.auth.api.signin.GoogleSignInResult;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.common.api.Status;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.Arrays;

public class MainActivity extends AppCompatActivity implements GoogleApiClient.OnConnectionFailedListener {
    private Button btn_fb_login = null;
    private String TAG = getClass().getSimpleName();
    private CallbackManager callbackManager;
    private GoogleApiClient mGoogleApiClient;
    private Button btn_google_login = null;

    private TextView txt_username = null;
    private Button btn_signout = null;

    private final int REQUEST_GOOGLE_SIGN_IN = 101;
    private final int REQUEST_FACEBOOK_SIGNIN = 102;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        FacebookSdk.setApplicationId(getString(R.string.facebook_app_id));
        setContentView(R.layout.activity_main);
        init();
    }

    /**
     * initializes view elements.
     */
    private void init(){
        btn_fb_login = (Button) findViewById(R.id.btn_fb_login);
        btn_google_login = (Button) findViewById(R.id.btn_google_login);
        txt_username = (TextView) findViewById(R.id.txt_username);
        btn_signout = (Button) findViewById(R.id.btn_signout);
        btn_fb_login.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (AccessToken.getCurrentAccessToken() != null)
                    setFacebookData(AccessToken.getCurrentAccessToken());
                else
                    attemptFBLogin();
            }
        });

        btn_google_login.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                googleSignIn();
            }
        });
        btn_signout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                signOut();
            }
        });

        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestEmail()
                .build();

        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .enableAutoManage(this /* FragmentActivity */, this /* OnConnectionFailedListener */)
                .addApi(Auth.GOOGLE_SIGN_IN_API, gso)
                .build();

    }


    /**
     * attempts facebook login
     */
    private void attemptFBLogin(){

        callbackManager = CallbackManager.Factory.create();
        LoginManager.getInstance().registerCallback(callbackManager, new FacebookCallback<LoginResult>() {
            @Override
            public void onSuccess(LoginResult loginResult) {
                Log.e(TAG, "reposnse "+loginResult);
                setFacebookData(loginResult.getAccessToken());

            }

            @Override
            public void onCancel() {
                Log.e(TAG, "it was canceled ");
            }

            @Override
            public void onError(FacebookException error) {
                Log.e(TAG, "an error occurred "+error);
            }});


        LoginManager.getInstance().logInWithReadPermissions(this, Arrays.asList("user_friends", "email", "public_profile"));
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == REQUEST_FACEBOOK_SIGNIN){
            callbackManager.onActivityResult(requestCode, resultCode, data);
            Log.e(TAG, ""+data.getData());
        }

        else if (requestCode == REQUEST_GOOGLE_SIGN_IN) {
            GoogleSignInResult result = Auth.GoogleSignInApi.getSignInResultFromIntent(data);
            handleSignInResult(result);
        }
    }


    private void setFacebookData(final AccessToken loginResult)
    {
        GraphRequest request = GraphRequest.newMeRequest(
                loginResult,
                new GraphRequest.GraphJSONObjectCallback() {
                    @Override
                    public void onCompleted(JSONObject object, GraphResponse response) {
                        // Application code
                        try {
                            Log.i("Response",response.toString());

                            String email = response.getJSONObject().getString("email");
                            String firstName = response.getJSONObject().getString("first_name");
                            String lastName = response.getJSONObject().getString("last_name");
                            String gender = response.getJSONObject().getString("gender");

                            Profile profile = Profile.getCurrentProfile();
                            String id = profile.getId();
                            String link = profile.getLinkUri().toString();
                            Log.i("Link",link);
                            if (Profile.getCurrentProfile()!=null)
                            {
                                Log.i("Login", "ProfilePic" + Profile.getCurrentProfile().getProfilePictureUri(200, 200));
                            }

                            Log.i("id" + "id ", id);
                            Log.i("Login" + "Email", email);
                            Log.i("Login"+ "FirstName", firstName);
                            Log.i("Login" + "LastName", lastName);
                            Log.i("Login" + "Gender", gender);

                            if (email != null && !email.isEmpty())
                                txt_username.setText("facebook : "+email);
                            else if (firstName != null && !firstName.isEmpty())
                                txt_username.setText("facebook : "+firstName);
                            else
                                txt_username.setText("facebook : can not get name");


                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                });
        Bundle parameters = new Bundle();
        parameters.putString("fields", "id,email,first_name,last_name,gender");
        request.setParameters(parameters);
        request.executeAsync();
    }

    /**
     * google signin
     */
    private void googleSignIn(){
        Intent signInIntent = Auth.GoogleSignInApi.getSignInIntent(mGoogleApiClient);
        startActivityForResult(signInIntent, REQUEST_GOOGLE_SIGN_IN);
    }


    @Override
    public void onBackPressed() {
        super.onBackPressed();
    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
        Log.e(TAG, ""+connectionResult);
    }



    private void handleSignInResult(GoogleSignInResult result) {
        Log.d(TAG, "handleSignInResult:" + result.isSuccess());
        if (result.isSuccess()) {
            // Signed in successfully, show authenticated UI.
            GoogleSignInAccount acct = result.getSignInAccount();
            txt_username.setText("google user name "+acct.getEmail());
            Log.e(TAG, ""+acct.getDisplayName());
        } else {

            Log.e(TAG, "some error occured");
        }
    }


    private void signOut(){
        Auth.GoogleSignInApi.signOut(mGoogleApiClient).setResultCallback(
                new ResultCallback<Status>() {
                    @Override
                    public void onResult(Status status) {
                        txt_username.setText("google signout success");
                    }
                });


        if (AccessToken.getCurrentAccessToken() != null)
            LoginManager.getInstance().logOut();
    }

}
