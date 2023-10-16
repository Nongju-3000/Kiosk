package com.wook.web.lighten.nanumAED.db;

import androidx.room.TypeConverter;

import java.util.ArrayList;
import java.util.List;

public class Converters {

    @TypeConverter
    public List<Float> gettingListFromString(String genreIds) {
        List<Float> list = new ArrayList<>();

        String[] array = genreIds.split(",");

        for (String s : array) {
            if (!s.isEmpty()) {
                list.add(Float.valueOf(s));
            }
        }
        return list;
    }

    @TypeConverter
    public List<Integer> gettingIntegerListFromString(String genreIds){
        List<Integer> list = new ArrayList<>();
        String[] array = genreIds.split(",");

        for (String s : array) {
            if (!s.isEmpty()) {
                list.add(Integer.valueOf(s));
            }
        }
        return list;
    }

    @TypeConverter
    public String writingStringFromList(List<Float> list) {
        String genreIds = "";
        for (Float i : list) {
            genreIds += "," + i;
        }
        return genreIds;
    }
    @TypeConverter
    public String writingIntegerStringFromList(List<Integer> list) {
        String genreIds = "";
        for (Integer i : list) {
            genreIds += "," + i;
        }
        return genreIds;
    }
}
