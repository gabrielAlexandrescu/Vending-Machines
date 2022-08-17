package com.utils;

import java.util.LinkedHashMap;

public class Utils {
    public static final LinkedHashMap<String,Integer> zeroMoney = formatHashMap(0,0,0,0,0,0,0,0,0,0,0);
    public static LinkedHashMap<String,Integer> formatHashMap(int fiftyEuros,int twentyEuros,int tenEuros,int fiveEuros,int twoEuros,
                                                              int oneEuro,int fiftyCents,int twentyCents,int tenCents,int fiveCents,int oneCent) {
        LinkedHashMap<String,Integer> map = new LinkedHashMap<>();
        map.put("5000",fiftyEuros);
        map.put("2000",twentyEuros);
        map.put("1000",tenEuros);
        map.put("500",fiveEuros);
        map.put("200",twoEuros);
        map.put("100",oneEuro);
        map.put("50",fiftyCents);
        map.put("20",twentyCents);
        map.put("10",tenCents);
        map.put("5",fiveCents);
        map.put("1",oneCent);
        return map;
    }
}
