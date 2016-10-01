package com.realmrxjavasample.animation;

/**
 * Created by techteam on 28/09/16.
 */

import android.app.Activity;
import android.os.Bundle;
import android.view.ViewGroup;
import android.widget.TextView;
import com.realmrxjavasample.R;
import com.realmrxjavasample.model.Person;
import io.realm.Realm;
import io.realm.RealmResults;
import java.util.concurrent.TimeUnit;
import rx.Observable;
import rx.Subscription;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Action1;
import rx.functions.Func1;
import rx.functions.Func2;

public class AnimationActivity extends Activity {

  private Realm realm;
  private Subscription subscription;
  private ViewGroup container;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_animations);
    container = (ViewGroup) findViewById(R.id.list);
    realm = Realm.getDefaultInstance();
  }

  @Override
  protected void onResume() {
    super.onResume();

    // Load all persons and start inserting them with 1 sec. intervals.
    // All RealmObject access has to be done on the same thread `findAllAsync` was called on.
    // Warning: This example doesn't handle back pressure well.
    subscription = realm.where(Person.class).findAllAsync().asObservable()
        .flatMap(new Func1<RealmResults<Person>, Observable<Person>>() {
          @Override
          public Observable<Person> call(RealmResults<Person> persons) {
            return Observable.from(persons);
          }
        })
        .zipWith(Observable.interval(1, TimeUnit.SECONDS), new Func2<Person, Long, Person>() {
          @Override
          public Person call(Person person, Long tick) {
            return person;
          }
        })
        .observeOn(AndroidSchedulers.mainThread())
        .subscribe(new Action1<Person>() {
          @Override
          public void call(Person person) {
            TextView personView = new TextView(AnimationActivity.this);
            personView.setText(person.getName());
            container.addView(personView);
          }
        });
  }

  @Override
  protected void onPause() {
    super.onPause();
    subscription.unsubscribe();
  }

  @Override
  protected void onDestroy() {
    super.onDestroy();
    realm.close();
  }
}