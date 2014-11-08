package com.schef.rss.android.db;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class CacheDb extends SQLiteOpenHelper {
    public static final String Name = "CacheDb";

    public static final int DB_VERSION = 2;

    private final Context _context;

    public CacheDb(Context context, int version) {
        super(context, Name, null, version);
        _context = context;
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        for(ITableDescription table : new ITableDescription[] {ArsEntity.getTableDescription()}){
            createTable(db, table);
        }
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        for(ITableDescription table : new ITableDescription[] {ArsEntity.getTableDescription()}){
            dropTable(db, table);
            createTable(db, table);
        }
    }

    private static void dropTable(SQLiteDatabase db, ITableDescription table) {
        if(table.getPostCreationSql()!=null) {
            for (String preDeleteSql : table.getPreDeletionSql()) {
                db.execSQL(preDeleteSql);
            }
        }
        db.execSQL("DROP TABLE " + table.getTableName() + ";");
    }

    private static void createTable(SQLiteDatabase db, ITableDescription table) {
        StringBuilder sb = new StringBuilder();
        sb.append("CREATE TABLE ").append(table.getTableName()).append(" (");
        String[] columns = table.getColumns();
        String[] types = table.getColumnsTypes();
        for (int i = 0; i < columns.length; i++) {
            if (i > 0) {
                sb.append(", ");
            }
            sb.append(columns[i]).append(" ").append(types[i]);
        }
        sb.append(");");
        db.execSQL(sb.toString());
        if(table.getPostCreationSql()!=null) {
            for (String sql : table.getPostCreationSql()) {
                db.execSQL(sql);
            }
        }
    }
}
