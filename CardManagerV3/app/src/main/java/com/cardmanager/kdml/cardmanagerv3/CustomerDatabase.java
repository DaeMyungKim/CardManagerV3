package com.cardmanager.kdml.cardmanagerv3;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.os.Environment;
import android.util.Log;

/**
 * Created by kdml on 2016-06-19.
 */
public class CustomerDatabase {
    /**
     * TAG for debugging
     */
    public static final String TAG = "CustomerDatabase";
    /**
     * Singleton instance
     */
    private static CustomerDatabase database;
    /**
     * database name
     */
    public static String DATABASE_NAME = "customerV3.db";
    /**
     * table name
     */
    public static String TABLE_SMS_DATA = "TABLE_SMS_DATA";
    /**
     * version
     */
    public static int DATABASE_VERSION = 1;
    /**
     * Helper class defined
     */
    private DatabaseHelper dbHelper;
    /**
     * Database object
     */
    private SQLiteDatabase db;
    private Context context;
    /**
     * Constructor
     */
    private CustomerDatabase(Context context) {
        this.context = context;
    }
    //클래스 전역변수
    private final String rootFolderName = "/cardManagerV3";
    //public static String root = null; //메모를 저장하는 폴더의 root dir
    private String initPath()
    {
        String root = null;
        String sdcard= Environment.getExternalStorageState();
        if( ! sdcard.equals(Environment.MEDIA_MOUNTED) ) {
            //SD카드 UNMOUNTED
            Log.d("mstag","sdcard unmounted");
            root = "" + Environment.getRootDirectory().getAbsolutePath() + rootFolderName; //내부저장소의 주소를 얻어옴
        } else {
            //SD카드 MOUNT
            Log.d("mstag","sdcard mounted");
            root = "" + Environment.getExternalStorageDirectory().getAbsolutePath() + rootFolderName; //외부저장소의 주소를 얻어옴
        }
        Log.d("mstag","root dir is => "+root);
        return root;
    }
    public static CustomerDatabase getInstance(Context context) {
        if (database == null) {
            database = new CustomerDatabase(context);
        }
        return database;
    }
    /**
     * open database
     *
     * @return
     */
    public boolean open() {
        println("opening database [" + DATABASE_NAME + "].");

        dbHelper = new DatabaseHelper(context);
        db = dbHelper.getWritableDatabase();

        return true;
    }
    /**
     * close database
     */
    public void close() {
        println("closing database [" + DATABASE_NAME + "].");
        db.close();
        database = null;
    }
    /**
     * execute raw query using the input SQL
     * close the cursor after fetching any result
     *
     * @param SQL
     * @return
     */
    public Cursor rawQuery(String SQL) {
        println("\nexecuteQuery called.\n");

        Cursor c1 = null;
        try {
            c1 = db.rawQuery(SQL, null);
            println("cursor count : " + c1.getCount());
        } catch(Exception ex) {
            Log.e(TAG, "Exception in executeQuery", ex);
        }

        return c1;
    }
    public boolean execSQL(String SQL) {
        println("\nexecute called.\n");

        try {
            Log.d(TAG, "SQL : " + SQL);
            db.execSQL(SQL);
        } catch(Exception ex) {
            Log.e(TAG, "Exception in executeQuery", ex);
            return false;
        }

        return true;
    }
    public boolean insert(String tableName, ContentValues values){
        try {
            db.insert(tableName,null,values);
        } catch(Exception ex) {
            Log.e(TAG, "Exception in insert()", ex);
            return false;
        }
        return true;
    }
    public void onUpdateDatabase()
    {

        dbHelper.onUpgrade(db,1,2);

    }

    //// DatabaseHelper 클래스
    public class DatabaseHelper extends SQLiteOpenHelper
    {
        public DatabaseHelper(Context context)
        {
            super(context, initPath()+"/"+DATABASE_NAME, null, DATABASE_VERSION);
        }


        public void onCreate(SQLiteDatabase db)
        {
            // TABLE_SMS_DATA
            println("creating table [" + TABLE_SMS_DATA + "].");

            // create table
            String CREATE_SQL = "CREATE TABLE IF NOT EXISTS " + TABLE_SMS_DATA +"(  " +
                    "dataTime NUMBER NOT NULL ON CONFLICT IGNORE UNIQUE, " +
                    "dateTimeConvert NVARCHAR(20), " +
                    "text TEXT, " +
                    "cost NUMBER, " +
                    "type NVARCHAR(10), " +
                    "company NVARCHAR(10), " +
                    "month INT, " +
                    "year INT, " +
                    "day INT, " +
                    "cardName VARCHAR(20));";
            try {
                db.execSQL(CREATE_SQL);
            } catch(Exception ex) {
                Log.e(TAG, "Exception in CREATE_SQL TABLE_SMS_DATA", ex);
            }
        }

        public void insertCardInfo()
        {
              String[][] cardCompany={
                    {"현대","15776200"},
                    {"신한","15447200"},
                    {"삼성","15888900"},
                    {"KB국민","15881788"},
                    {"롯데","15888100"},
                    {"농협","15881600"},
                    {"하나","18001111"},
                    {"기업","15884000"},
                    {"우리","00000001"},
                    {"씨티","00000002"},
                    {"외환","00000003"},
                    {"SC(스탠다드)","00000004"}
            };
        }

        public void onOpen(SQLiteDatabase db)
        {
            println("opened database [" + DATABASE_NAME + "].");
        }

        public void onUpgrade(SQLiteDatabase db, int oldVersion,
                              int newVersion)
        {
            // TABLE_CUSTOMER_INFO
            println("creating table [" + TABLE_SMS_DATA + "].");

            // drop existing table
            String DROP_SQL = "drop table if exists " + TABLE_SMS_DATA;
            try {
                db.execSQL(DROP_SQL);
            } catch(Exception ex) {
                Log.e(TAG, "Exception in DROP_SQL TABLE_SMS_DATA", ex);
            }

            onCreate(db);


        }

    }

    private void println(String msg) {
        Log.d(TAG, msg);
    }


}
