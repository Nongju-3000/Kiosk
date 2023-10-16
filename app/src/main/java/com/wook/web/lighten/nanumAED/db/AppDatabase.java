package com.wook.web.lighten.nanumAED.db;

import android.content.Context;

import androidx.annotation.NonNull;
import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;
import androidx.room.migration.Migration;
import androidx.sqlite.db.SupportSQLiteDatabase;

@Database(entities = {Report.class}, version = 3)

public abstract class AppDatabase extends RoomDatabase {

    public abstract ReportDao reportDao();

    private static AppDatabase INSTANCE;

    public static AppDatabase getDbInstance(Context context){
        if(INSTANCE == null){
            INSTANCE = Room.databaseBuilder(context.getApplicationContext(), AppDatabase.class, "Report")
                    .allowMainThreadQueries()
                    .addMigrations(MIGRATIONFrom1To3)
                    .addMigrations(MIGRATIONFrom2To3)
                    .build();
        }
        return  INSTANCE;
    }

    static final Migration MIGRATIONFrom1To3 = new Migration(1, 3) {
        @Override
        public void migrate(@NonNull SupportSQLiteDatabase database) {
            database.execSQL("DELETE FROM Report");
        }
    };
    static final Migration MIGRATIONFrom2To3 = new Migration(2, 3) {
        @Override
        public void migrate(@NonNull SupportSQLiteDatabase database) {
            database.execSQL("DELETE FROM Report");
        }
    };
}
