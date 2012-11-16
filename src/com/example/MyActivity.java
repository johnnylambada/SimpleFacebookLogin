package com.example;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.TextView;
import com.facebook.*;

public class MyActivity extends Activity {
    private final static String TAG = MyActivity.class.getCanonicalName();

    private Session currentSession;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);

        final TextView userNameView = (TextView) findViewById(R.id.profileUserName);
        final ProfilePictureView profilePictureView = (ProfilePictureView) findViewById(R.id.profilePic);

        // Set com.facebook.android.Util.ENABLE_LOG to TRUE for these to work
        Settings.addLoggingBehavior(LoggingBehaviors.CACHE);
        Settings.addLoggingBehavior(LoggingBehaviors.INCLUDE_ACCESS_TOKENS);
        Settings.addLoggingBehavior(LoggingBehaviors.INCLUDE_RAW_RESPONSES);
        Settings.addLoggingBehavior(LoggingBehaviors.REQUESTS);

        Session.StatusCallback sessionStatusCallback = new Session.StatusCallback() {
            @Override public void call(Session session, SessionState state, Exception exception) {
                if (session != currentSession) {
                    Log.i(TAG, "Can this really happen?");
                    return;
                }
                if (state.isOpened()) { // Log in just happened. Let's get some info.
                    Request request = Request.newMeRequest(currentSession, new Request.GraphUserCallback() {
                        @Override public void onCompleted(GraphUser me, Response response) {
                            if (response.getRequest().getSession() == currentSession) {
                                if (me == null) {
                                    profilePictureView.setUserId(null);
                                    userNameView.setText(getString(R.string.greeting_no_user));
                                } else {
                                    profilePictureView.setUserId(me.getId());
                                    userNameView.setText( String.format(getString(R.string.greeting_format), me.getFirstName()));
                                }
                            }
                         }
                    });
                    Request.executeBatchAsync(request);
                } else if (state.isClosed()) { // Log out just happened. Update the UI.
                    profilePictureView.setUserId(null);
                    userNameView.setText(getString(R.string.greeting_no_user));
                }
            }
        };

        currentSession = Session.openActiveSession(this);
        if (currentSession == null || currentSession.isOpened()==false){
            currentSession = new Session.Builder(this).build();
            currentSession.addCallback(sessionStatusCallback);
            currentSession.openForRead( new Session.OpenRequest(this).setLoginBehavior(SessionLoginBehavior.SSO_WITH_FALLBACK).setRequestCode(Session.DEFAULT_AUTHORIZE_ACTIVITY_CODE));
        } else {
            sessionStatusCallback.call(currentSession,currentSession.getState(),null);
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (currentSession != null)
            currentSession.onActivityResult(this, requestCode, resultCode, data);
    }
}
