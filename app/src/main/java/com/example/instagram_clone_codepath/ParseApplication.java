package com.example.instagram_clone_codepath;

import android.app.Application;
import com.parse.Parse;
import com.parse.ParseObject;

public class ParseApplication extends Application {

    @Override
    public void onCreate() {
        super.onCreate();

        ParseObject.registerSubclass(Post.class);

        Parse.initialize(new Parse.Configuration.Builder(this)
                .applicationId("kZ8QHXqIfSrZ4HdzSRHeJWyqMHGrI9caSQKKzLau")
                .clientKey("e8dYkl8HWnGmLrXvpt2H4VmnbWD1N7eGLZEsRAYK")
                .server("https://parseapi.back4app.com")
                .build()
        );
    }
}
