package com.realmrxjavasample;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import com.realmrxjavasample.animation.AnimationActivity;
import com.realmrxjavasample.gotchas.GotchasActivity;
import com.realmrxjavasample.retrofit.RetrofitExample;
import com.realmrxjavasample.throttle.ThrottleSearchActivity;
import java.util.Map;
import java.util.TreeMap;

public class MainActivity extends Activity {

  private ViewGroup container;
  private final TreeMap<String, Class<? extends Activity>> buttons = new TreeMap<String, Class<? extends Activity>>() {{
    put("Animation", AnimationActivity.class);
    put("Throttle search", ThrottleSearchActivity.class);
    put("Network", RetrofitExample.class);
    put("Working with Realm", GotchasActivity.class);
  }};

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_main);
    container = (ViewGroup) findViewById(R.id.list);
    setupButtons();
  }

  private void setupButtons() {
    for (final Map.Entry<String, Class<? extends Activity>> entry : buttons.entrySet()) {
      Button button = new Button(this);
      button.setText(entry.getKey());
      button.setOnClickListener(new View.OnClickListener() {
        @Override
        public void onClick(View v) {
          startActivity(entry.getValue());
        }
      });
      container.addView(button);
    }
  }

  private void startActivity(Class<? extends Activity> activityClass) {
    startActivity(new Intent(this, activityClass));
  }
}
