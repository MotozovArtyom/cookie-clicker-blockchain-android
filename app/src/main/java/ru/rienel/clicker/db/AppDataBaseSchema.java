package ru.rienel.clicker.db;

public class AppDataBaseSchema {
	public static final class BlocksTable {
		public static final String NAME = "blocks";

		public static final class Columns {
			public static final String ID = "id";
			public static final String MESSAGE = "message";
			public static final String GOAL = "goal";
			public static final String CREATION_TIME = "creation_time";
			public static final String OPPONENT = "opponent";
			public static final String HASH_OF_PREVIOUS_BLOCK = "previous_block_hash";
			public static final String HASH_OF_BLOCK = "hash_of_block";
		}

		public static String[] allColumns() {
			return new String[]{
					Columns.ID,
					Columns.MESSAGE,
					Columns.GOAL,
					Columns.CREATION_TIME,
					Columns.OPPONENT,
					Columns.HASH_OF_PREVIOUS_BLOCK,
					Columns.HASH_OF_BLOCK
			};
		}
	}
}
