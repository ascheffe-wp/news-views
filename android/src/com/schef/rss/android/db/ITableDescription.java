package com.schef.rss.android.db;

public interface ITableDescription {
    String getTableName();
    String [] getColumns();
    String [] getColumnsTypes();
    String [] getPostCreationSql();
    String [] getPreDeletionSql();
}
