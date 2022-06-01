package pt.ulisboa.tecnico.cmov.conversational_ist;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;

public class MainActivity extends AppCompatActivity {

    SharedPreferences sharedPref;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        sharedPref = getPreferences(Context.MODE_PRIVATE);
    }

    public void login(View view) {

        EditText username = (EditText)findViewById(R.id.editText1);

        SharedPreferences.Editor editor = sharedPref.edit();
        editor.putString(getString(R.string.saved_username), username.getText().toString());
        editor.apply();
    }

}