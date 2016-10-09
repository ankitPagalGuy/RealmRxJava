package com.realmrxjavasample.gotchas;

import android.app.Activity;
import android.os.Bundle;
import android.view.ViewGroup;
import android.widget.TextView;
import com.realmrxjavasample.R;
import com.realmrxjavasample.model.Person;
import io.realm.Realm;
import io.realm.Sort;
import java.util.List;
import java.util.Random;
import rx.Observable;
import rx.Subscription;
import rx.functions.Action1;
import rx.functions.Func1;
import rx.schedulers.Schedulers;
import rx.subscriptions.CompositeSubscription;

/**
 * Created by techteam on 28/09/16.
 */

public class GotchasActivity extends Activity {
  private Realm realm;
  private Subscription subscription;
  private ViewGroup container;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_gotchas);
    container = (ViewGroup) findViewById(R.id.list);
    realm = Realm.getDefaultInstance();
  }

  @Override
  protected void onResume() {
    super.onResume();

    Subscription distinctSubscription = testDistinct();
    Subscription bufferSubscription = testBuffer();
    Subscription subscribeOnSubscription = testSubscribeOn();

    // Trigger updates
    realm.executeTransaction(new Realm.Transaction() {
      @Override
      public void execute(Realm realm) {
        realm.where(Person.class).findAllSorted( "name", Sort.ASCENDING).get(0).setAge(new Random().nextInt(100));
      }
    });

    subscription = new CompositeSubscription(
        distinctSubscription,
        bufferSubscription,
        subscribeOnSubscription
    );
  }

  /**
   * Shows how to be careful with `subscribeOn()`
   */
  private Subscription testSubscribeOn() {
    Subscription subscribeOn = realm.asObservable()
        .map(new Func1<Realm, Person>() {
          @Override
          public Person call(Realm realm) {
            return realm.where(Person.class).findAllSorted("name").get(0);
          }
        })
        // The Realm was created on the UI thread. Accessing it on `Schedulers.io()` will crash.
        // Avoid using subscribeOn() and use Realms `findAllAsync*()` methods instead.
        .subscribeOn(Schedulers.io()) //
        .subscribe(new Action1<Person>() {
          @Override
          public void call(Person person) {
            // Do nothing
          }
        }, new Action1<Throwable>() {
          @Override
          public void call(Throwable throwable) {
            showStatus("subscribeOn: " + throwable.toString());
          }
        });

    // Use Realms Async API instead
    Subscription asyncSubscribeOn = realm.where(Person.class).findAllSortedAsync("name").get(0).<Person>asObservable()
        .subscribe(new Action1<Person>() {
          @Override
          public void call(Person person) {
            showStatus("subscribeOn/async: " + person.getName() + ":" + person.getAge());
          }
        }, new Action1<Throwable>() {
          @Override
          public void call(Throwable throwable) {
            showStatus("subscribeOn/async: " + throwable.toString());
          }
        });

    return new CompositeSubscription(subscribeOn, asyncSubscribeOn);
  }

  /**
   * Shows how to be careful with `buffer()`
   */
  private Subscription testBuffer() {
    Observable<Person> personObserver = realm.asObservable().map(new Func1<Realm, Person>() {
      @Override
      public Person call(Realm realm) {
        return realm.where(Person.class).findAllSorted("name").get(0);
      }
    });

    // buffer() caches objects until the buffer is full. Due to Realms auto-update of all objects it means
    // that all objects in the cache will contain the same data.
    // Either avoid using buffer or copy data into an unmanaged object.
    return personObserver
        .buffer(2)
        .subscribe(new Action1<List<Person>>() {
          @Override
          public void call(List<Person> persons) {
            showStatus("Buffer[0] : " + persons.get(0).getName() + ":" + persons.get(0).getAge());
            showStatus("Buffer[1] : " + persons.get(1).getName() + ":" + persons.get(1).getAge());
          }
        });
  }

  /**
   * Shows how to to be careful when using `distinct()`
   */
  private Subscription testDistinct() {
    Observable<Person> personObserver = realm.asObservable().map(new Func1<Realm, Person>() {
      @Override
      public Person call(Realm realm) {
        return realm.where(Person.class).findAllSorted("name").get(0);
      }
    });

    // distinct() and distinctUntilChanged() uses standard equals with older objects stored in a HashMap.
    // Realm objects auto-update which means the objects stored will also auto-update.
    // This makes comparing against older objects impossible (even if the new object has changed) because the
    // cached object will also have changed.
    // Use a keySelector function to work around this.
    Subscription distinctItemTest = personObserver
        .distinct() // Because old == new. This will only allow the first version of the "Chris" object to pass.
        .subscribe(new Action1<Person>() {
          @Override
          public void call(Person p) {
            showStatus("distinct(): " + p.getName() + ":" + p.getAge());
          }
        });

    Subscription distinctKeySelectorItemTest = personObserver
        .distinct(new Func1<Person, Integer>() { // Use a keySelector function instead
          @Override
          public Integer call(Person p) {
            return p.getAge();
          }
        })
        .subscribe(new Action1<Person>() {
          @Override
          public void call(Person p) {
            showStatus("distinct(keySelector): " + p.getName() + ":" + p.getAge());
          }
        });


    return new CompositeSubscription(distinctItemTest, distinctKeySelectorItemTest);
  }

  private void showStatus(String message) {
    TextView v = new TextView(this);
    v.setText(message);
    container.addView(v);
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