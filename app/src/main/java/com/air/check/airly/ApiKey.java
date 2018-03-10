package com.air.check.airly;

import java.util.Random;

/**
 * Created by Lukasz on 16.02.2017.
 */

public class ApiKey {
    public static String[] apikey = {"fae55480ef384880871f8b40e77bbef9", "5f5c4d0463fe44829f463e4bf819bc00​", "0d23d883ef6a4689b938fa0dbf21e8f3"};;
    public static String get() {
        Random randomKey = new Random();
        return apikey[randomKey.nextInt(2)];
    }

}
