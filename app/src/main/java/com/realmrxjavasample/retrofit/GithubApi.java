package com.realmrxjavasample.retrofit;

import retrofit.http.GET;
import retrofit.http.Path;
import rx.Observable;

/**
 * Created by techteam on 28/09/16.
 */

interface GithubApi {
  /**
   * See https://developer.github.com/v3/users/
   */
  @GET("/users/{user}") Observable<GitHubUser> user(@Path("user") String user);
}