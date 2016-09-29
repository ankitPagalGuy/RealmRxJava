package com.realmrxjavasample;

/**
 * Created by techteam on 28/09/16.
 */

import android.app.Application;
import com.realmrxjavasample.model.Person;
import io.realm.Realm;
import io.realm.RealmConfiguration;
import java.util.Map;
import java.util.Random;
import java.util.TreeMap;


public class MyApplication extends Application {

  private static MyApplication context;
  private static final TreeMap<String, String> testPersons = new TreeMap<>();
  static {
    testPersons.put("Chris", null);
    testPersons.put("Christian", "cmelchior");
    testPersons.put("Christoffer", null);
    testPersons.put("Emanuele", "emanuelez");
    testPersons.put("Dan", null);
    testPersons.put("Donn", "donnfelker");
    testPersons.put("Nabil", "nhachicha");
    testPersons.put("Ron", null);
  }

  @Override
  public void onCreate() {
    super.onCreate();
    context = this;
    //Realm.init(this);
    RealmConfiguration config = new RealmConfiguration.Builder(this).build();
    Realm.deleteRealm(config);
    Realm.setDefaultConfiguration(config);
    createTestData();
  }

  // Create test data
  private void createTestData() {
    final Random r = new Random(42);
    Realm realm = Realm.getDefaultInstance();
    realm.executeTransaction(new Realm.Transaction() {
      @Override
      public void execute(Realm realm) {
        for (Map.Entry<String, String> entry : testPersons.entrySet()) {
          Person p = realm.createObject(Person.class);
          p.setName(entry.getKey());
          p.setGithubUserName(entry.getValue());
          p.setAge(r.nextInt(100));
        }
      }
    });
    realm.close();
  }

  public static MyApplication getContext() {
    return context;
  }
}
