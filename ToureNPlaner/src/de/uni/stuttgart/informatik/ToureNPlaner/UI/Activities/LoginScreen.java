/*
 * Copyright 2012 ToureNPlaner
 *
 *    Licensed under the Apache License, Version 2.0 (the "License");
 *    you may not use this file except in compliance with the License.
 *    You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 *    Unless required by applicable law or agreed to in writing, software
 *    distributed under the License is distributed on an "AS IS" BASIS,
 *    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *    See the License for the specific language governing permissions and
 *    limitations under the License.
 */

package de.uni.stuttgart.informatik.ToureNPlaner.UI.Activities;

import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.Toast;
import com.actionbarsherlock.app.SherlockFragmentActivity;

import de.uni.stuttgart.informatik.ToureNPlaner.Handler.Observer;
import de.uni.stuttgart.informatik.ToureNPlaner.Net.Handler.AuthRequestHandler;
import de.uni.stuttgart.informatik.ToureNPlaner.Net.Session;
import de.uni.stuttgart.informatik.ToureNPlaner.R;
import de.uni.stuttgart.informatik.ToureNPlaner.UI.Dialogs.MyProgressDialog;
import de.uni.stuttgart.informatik.ToureNPlaner.Util.Base64;

public class LoginScreen extends SherlockFragmentActivity implements Observer {
	private AuthRequestHandler handler;
	private Session session;
	public static final String SHARED_PREFERENCES_CREDENTIALS = "credentials";
	SharedPreferences.Editor preferencesEditor;
	CheckBox chk_saveCredentials;

	@Override
	protected void onSaveInstanceState(Bundle outState) {
		outState.putSerializable(Session.IDENTIFIER, session);
		super.onSaveInstanceState(outState);
	}

	public static class ConnectionProgressDialog extends MyProgressDialog {
		public static ConnectionProgressDialog newInstance(String title, String message) {
			return (ConnectionProgressDialog) MyProgressDialog.newInstance(new ConnectionProgressDialog(), title, message);
		}

		@Override
		public void onCancel(DialogInterface dialog) {
			((LoginScreen) getActivity()).cancelConnection();
		}
	}

	/**
	 * Called when the activity is first created.
	 */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.loginscreen);

		SharedPreferences applicationPreferences = getSharedPreferences(
				SHARED_PREFERENCES_CREDENTIALS, MODE_PRIVATE);
		preferencesEditor = applicationPreferences.edit();


		chk_saveCredentials = (CheckBox) findViewById(R.id.chkb_login_screen_save_credentials);
		EditText emailTextfield = (EditText) findViewById(R.id.emailTextfield);

		// TODO reset credentials with empty strings
		emailTextfield.setText(applicationPreferences.getString("user", ""));
		EditText passwordTextfield = (EditText) findViewById(R.id.passwordTextfield);
		passwordTextfield.setText(Base64.decodeString(applicationPreferences.getString("password", Base64.encodeString(""))));
		chk_saveCredentials.setChecked(applicationPreferences.getBoolean("chk_isChecked", false));

		// If we get created for the first time we get our data from the intent
		Bundle data = savedInstanceState != null ? savedInstanceState : getIntent().getExtras();
		session = (Session) data.getSerializable(Session.IDENTIFIER);

		setupLoginButton();

		initializeHandler();
	}

	private void setupLoginButton() {
		Button btnlogin = (Button) findViewById(R.id.btnlogin);
		btnlogin.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View view) {
				// gets Values of TextEditors
				EditText emailTextfield = (EditText) findViewById(R.id.emailTextfield);
				EditText passwordTextfield = (EditText) findViewById(R.id.passwordTextfield);

				// set userdata
				session.setUsername(emailTextfield.getText().toString());
				session.setPassword(passwordTextfield.getText().toString());

				if (chk_saveCredentials.isChecked()) {
					preferencesEditor.putString("user", emailTextfield.getText().toString());
					preferencesEditor.putString("password", Base64.encodeString(passwordTextfield.getText().toString()));
					preferencesEditor.putBoolean("chk_isChecked", true);
					preferencesEditor.commit();
				} else {
					preferencesEditor.clear();
					preferencesEditor.commit();
				}
				ConnectionProgressDialog.newInstance(getString(R.string.login_dialog_title), session.getUsername()).show(getSupportFragmentManager(), "login");
				handler = new AuthRequestHandler(LoginScreen.this, session);
				handler.execute();
			}
		});
	}

	private void initializeHandler() {
		handler = (AuthRequestHandler) getLastCustomNonConfigurationInstance();

		if (handler != null)
			handler.setListener(this);
		else {
			MyProgressDialog dialog = (MyProgressDialog) getSupportFragmentManager().findFragmentByTag("login");
			if (dialog != null)
				dialog.dismiss();
		}
	}

	private void cancelConnection() {
		if (handler != null)
			handler.cancel(true);
		handler = null;
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
		if (handler != null)
			handler.setListener(null);
	}

	@Override
	public Object onRetainCustomNonConfigurationInstance() {
		return handler;
	}

	@Override
	public void onCompleted(Object caller, Object object) {
		handler = null;
		MyProgressDialog dialog = (MyProgressDialog) getSupportFragmentManager().findFragmentByTag("login");
		try {
			dialog.dismiss();
		} catch (IllegalStateException e) {
			// Can not perform this action after onSaveInstanceState
		}
		Intent myIntent = new Intent(getBaseContext(), AlgorithmScreen.class);
		myIntent.putExtra(Session.IDENTIFIER, session);
		startActivity(myIntent);
	}

	@Override
	public void onError(Object caller, Object object) {
		handler = null;
		MyProgressDialog dialog = (MyProgressDialog) getSupportFragmentManager().findFragmentByTag("login");
		try {
			dialog.dismiss();
		} catch (IllegalStateException e) {
			// Can not perform this action after onSaveInstanceState
		}
		Toast.makeText(getApplicationContext(), object.toString(), Toast.LENGTH_LONG).show();
	}
}
