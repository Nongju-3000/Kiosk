package com.wook.web.lighten.nanumAED.db;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;

import java.util.List;

@Dao
public interface ReportDao {

    @Query("SELECT * FROM Report")
    List<Report> getAllDevice();

//    @Query("SELECT * FROM Device WHERE Device IN (:deviceIds)")
//    List<Device> loadAllByIds(int[] deviceIds);
//
//    @Query("SELECT * FROM Device WHERE Device LIKE :name AND device_address LIKE :address LIMIT 1")
//    Device findByDevice(String name, String address);

    @Insert
    void insert(Report... devices);

    @Delete
    void delete(Report device);

}
