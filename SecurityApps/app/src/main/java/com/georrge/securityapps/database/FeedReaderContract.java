package com.georrge.securityapps.database;

import android.provider.BaseColumns;

/**
 * Created by Георгий on 28.01.2017.
 */

public final class FeedReaderContract {
    public FeedReaderContract() {}

    /* Inner class that defines the table contents */
    public static abstract class FeedEntry implements BaseColumns {

        public static final String TABLE_NAME = "name_and_pass";
        public static final String COLUMN_NAME_NAME_APP = "name_app";
        public static final String COLUMN_NAME_PASS = "pass";

        private static final String TEXT_TYPE = " TEXT";
        private static final String COMMA_SEP = ",";

        public static final String SQL_CREATE_ENTRIES =
                "CREATE TABLE " + FeedEntry.TABLE_NAME + " (" +
                        FeedEntry.COLUMN_NAME_NAME_APP + TEXT_TYPE + COMMA_SEP +
                        FeedEntry.COLUMN_NAME_PASS + TEXT_TYPE +
                " )";

        public static final String SQL_DELETE_ENTRIES =
                "DROP TABLE IF EXISTS " + FeedEntry.TABLE_NAME;
    }
}
