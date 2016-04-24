package com.example.xyzreader.remote;

import java.net.MalformedURLException;
import java.net.URL;

public class Config {
    public static final URL BASE_URL;

    static {
        URL url = null;
        try {
            // Original source
//            url = new URL("https://dl.dropboxusercontent.com/u/231329/xyzreader_data/data.json" );
            // New source with color place holder to be used when loading images
            url = new URL("https://www.dropbox.com/s/rc8p8h35pf569kk/data.json?dl=1");
        } catch (MalformedURLException ignored) {
            // TODO: throw a real error
        }

        BASE_URL = url;
    }
}
