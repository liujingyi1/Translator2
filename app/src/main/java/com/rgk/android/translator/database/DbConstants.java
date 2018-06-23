package com.rgk.android.translator.database;

public class DbConstants {


    public static final String CREATE_USER_TABLE =
            "CREATE TABLE " + Tables.TABLE_USER + "("
                    + UserColumns.ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
                    + UserColumns.DEVICE_ID + " TEXT NOT NULL, "
                    + UserColumns.ROLE + " TEXT, "
                    + UserColumns.NAME + " TEXT, "
                    + UserColumns.NICK_NAME + " TEXT, "
                    + UserColumns.SEX + " INTEGER, "
                    + UserColumns.PHOTO_ID + " INTEGER, "
                    + UserColumns.LANGUAGE + " TEXT, "
                    + UserColumns.DESCRIPTION + " TEXT"
                    + ");";

    public static final String CREATE_MEMBER_TABLE =
            "CREATE TABLE " + Tables.TABLE_MEMBER + "("
                    + MemberColumns.ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
                    + MemberColumns.DEVICE_ID + " TEXT NOT NULL, "
                    + MemberColumns.NAME + " TEXT, "
                    + MemberColumns.NICK_NAME + " TEXT, "
                    + MemberColumns.SEX + " INTEGER, "
                    + MemberColumns.PHOTO_ID + " INTEGER, "
                    + MemberColumns.LANGUAGE + " TEXT, "
                    + MemberColumns.DESCRIPTION + " TEXT, "
                    + MemberColumns.FAVORITE + " INTEGER"
                    + ");";

    public static final String CREATE_MESSAGE_TABLE =
            "CREATE TABLE " + Tables.TABLE_MESSAGE + "("
                    + MessageColumns.ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
                    + MessageColumns.THREAD_ID + " INTEGER NOT NULL, "
                    + MessageColumns.MEMBER_ID + " INTEGER NOT NULL DEFAULT -1, "
                    + MessageColumns.DATE + " LONG, "
                    + MessageColumns.READ + " INTEGER DEFAULT 0, "
                    + MessageColumns.TYPE + " INTEGER NOT NULL DEFAULT 1, "
                    + MessageColumns.TEXT + " TEXT, "
                    + MessageColumns.URL + " TEXT, "
                    + MessageColumns.LANGUAGE + " TEXT, "
                    + MessageColumns.ERROR_CODE + " INTEGER"
                    + ");";

    public static final String CREATE_THREADS_TABLE =
            "CREATE TABLE " + Tables.TABLE_THREADS + "("
                    + ThreadsColumns.ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
                    + ThreadsColumns.SERVER_THREAD_ID + " TEXT, "
                    + ThreadsColumns.MEMBER_ID + " TEXT, "
                    + ThreadsColumns.DATE + " LONG, "
                    + ThreadsColumns.MESSAGE_COUNT + " INTEGER DEFAULT 0, "
                    + ThreadsColumns.TITLE + " TEXT, "
                    + ThreadsColumns.UNREAD_COUNT + " INTEGER DEFAULT 0"
                    + ");";

    public static final String CREATE_PHOTO_TABLE =
            "CREATE TABLE " + Tables.TABLE_PHOTO + "("
                    + PhotoColumns.ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
                    + PhotoColumns.HEIGHT + " INTEGER, "
                    + PhotoColumns.WIDTH + " INTEGER, "
                    + PhotoColumns.FILE_SIZE + " LONG, "
                    + PhotoColumns.PATH + " TEXT"
                    + ");";

    public static final String CREATE_MESSAGE_VIEW = "CREATE VIEW message_view " +
            "AS SELECT message._id,thread_id,server_thread_id,message.member_id," +
            "device_id,message.date,read,type,message.text,url,message.language," +
            "error_code FROM threads,message,member WHERE message.thread_id=threads._id " +
            "AND message.member_id=member._id;";

    public static final String INSERT_MESSAGE_TRIGGER = "CREATE TRIGGER insert_message_trigger " +
            "AFTER INSERT ON message BEGIN UPDATE threads SET date=NEW.date, " +
            "unread_count=CASE NEW.read WHEN 0 THEN unread_count+1 ELSE unread_count END, " +
            "message_count=message_count+1 WHERE _id=NEW.thread_id; END;";

    public static final String UPDATE_MESSAGE_TRIGGER = "CREATE TRIGGER update_message_trigger " +
            "AFTER UPDATE OF read ON message BEGIN UPDATE threads SET unread_count=" +
            "(SELECT COUNT(*) FROM message WHERE thread_id=NEW.thread_id AND read=0); END;";

    public static final String DELETE_MESSAGE_TRIGGER = "CREATE TRIGGER delete_message_trigger " +
            "AFTER DELETE ON message BEGIN UPDATE threads SET message_count=(SELECT COUNT(*) FROM " +
            "message WHERE thread_id=OLD.thread_id) WHERE _id=OLD.thread_id; END;";

    public static final String DELETE_THREADS_TRIGGER = "CREATE TRIGGER delete_threads_trigger " +
            "AFTER DELETE ON threads BEGIN DELETE FROM message WHERE thread_id=OLD._id; END;";

    public interface Tables {
        String TABLE_USER = "user";
        String TABLE_MEMBER = "member";
        String TABLE_MESSAGE = "message";
        String TABLE_THREADS = "threads";
        String TABLE_PHOTO = "photo_files";
    }

    public interface BaseColumns {
        String ID = "_id";
        String DEVICE_ID = "device_id";
        String NAME = "name";
        String NICK_NAME = "nick_name";
        String SEX = "sex";
        String PHOTO_ID = "photo_id";
        String LANGUAGE = "language";
        String DESCRIPTION = "description";
    }

    public interface UserColumns extends BaseColumns {
        String ROLE = "role";
    }

    public interface MemberColumns extends BaseColumns {
        String FAVORITE = "favorite";
    }

    public interface MessageColumns {
        String ID = "_id";
        String THREAD_ID = "thread_id";
        String MEMBER_ID = "member_id";
        String DATE = "date";
        String READ = "read";
        String TYPE = "type";
        String TEXT = "text";
        String URL = "url";
        String LANGUAGE = "language";
        String ERROR_CODE = "error_code";
    }

    public interface ThreadsColumns {
        String ID = "_id";
        String SERVER_THREAD_ID = "server_thread_id";
        String MEMBER_ID = "member_id";
        String DATE = "date";
        String MESSAGE_COUNT = "message_count";
        String TITLE = "title";
        String UNREAD_COUNT = "unread_count";
    }

    interface PhotoColumns {
        String ID = "_id";
        String HEIGHT = "height";
        String WIDTH = "width";
        String FILE_SIZE = "file_size";
        String PATH = "path";
    }

    public interface MessageType {
        int TYPE_TEXT = 1;
        int TYPE_SOUND = 2;
        int TYPE_SOUND_TEXT = 3;
    }
}
