package ru.rienel.clicker.db;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import ru.rienel.clicker.db.domain.BlockChainDbSchema.BlocksTable;

public class BlockChainBaseHelper extends SQLiteOpenHelper {
	public static final int VERSION = 1;
	public static final String DATABASE_NAME = "storage.db";

	public BlockChainBaseHelper(Context context) {
		super(context, DATABASE_NAME, null, VERSION);
	}

	@Override
	public void onCreate(SQLiteDatabase db) {
		db.execSQL("CREATE TABLE " + BlockChainBaseHelper.DATABASE_NAME + "(" +
				BlocksTable.Columns.ID + " INTEGER PRIMARY KEY AUTOINCREMENT," +
				BlocksTable.Columns.MESSAGE + " VARCHAR(255)," +
				BlocksTable.Columns.GOAL + " INTEGER," +
				BlocksTable.Columns.CREATION_TIME + " INTEGER," +
				BlocksTable.Columns.HASH_OF_PREVIOUS_BLOCK + " VARCHAR(32)," +
				BlocksTable.Columns.HASH_OF_BLOCK + " VARCHAR(32)" +
				" )");
	}

	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

	}
}
