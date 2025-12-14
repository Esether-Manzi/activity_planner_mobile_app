package com.example.activityplanner.db


import android.content.ContentValues
import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import com.example.activityplanner.model.Task

/**
 * TaskDatabaseHelper manages SQLite database for Activity Planner.
 * It contains two tables:
 *  - tasks: stores all task details
 *  - users: stores user accounts for login/signup
 */
class TaskDatabaseHelper(context: Context) :
    SQLiteOpenHelper(context, DATABASE_NAME, null, DATABASE_VERSION) {

    companion object {
        private const val DATABASE_NAME = "activity_planner.db"
        private const val DATABASE_VERSION = 3   // bumped version to add user table

        // Task table
        const val TABLE_TASKS = "tasks"
        const val COLUMN_TASK_ID = "id"
        const val COLUMN_TITLE = "title"
        const val COLUMN_DESCRIPTION = "description"
        const val COLUMN_START = "start_datetime"
        const val COLUMN_DEADLINE = "deadline_datetime"
        const val COLUMN_PRIORITY = "priority"
        const val COLUMN_CATEGORY = "category"
        const val COLUMN_COMPLETED = "completed"

            // User table
        const val TABLE_USERS = "users"
        const val COLUMN_USER_ID = "user_id"
        const val COLUMN_USER_EMAIL = "email"
        const val COLUMN_USER_PASSWORD = "password"
        const val COLUMN_USER_NAME = "name"

    }

    override fun onCreate(db: SQLiteDatabase) {
        // Create tasks table
        val createTasks = """
            CREATE TABLE $TABLE_TASKS (
                $COLUMN_TASK_ID INTEGER PRIMARY KEY AUTOINCREMENT,
                $COLUMN_TITLE TEXT NOT NULL,
                $COLUMN_DESCRIPTION TEXT,
                $COLUMN_START INTEGER NOT NULL,
                $COLUMN_DEADLINE INTEGER NOT NULL,
                $COLUMN_PRIORITY TEXT NOT NULL,
                $COLUMN_CATEGORY TEXT,
                $COLUMN_COMPLETED INTEGER DEFAULT 0
            )
        """.trimIndent()
        db.execSQL(createTasks)

        // Create users table
        val createUsers = """
            CREATE TABLE $TABLE_USERS (
                $COLUMN_USER_ID INTEGER PRIMARY KEY AUTOINCREMENT,
                $COLUMN_USER_EMAIL TEXT UNIQUE NOT NULL,
                $COLUMN_USER_PASSWORD TEXT NOT NULL,
                $COLUMN_USER_NAME TEXT
            )
        """.trimIndent()
        db.execSQL(createUsers)
    }

    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        if (oldVersion < 3){
            db.execSQL("ALTER TABLE tasks ADD COLUMN completed INTEGER DEFAULT 0")
        }
        // Drop and recreate tables if upgrading
        db.execSQL("DROP TABLE IF EXISTS $TABLE_TASKS")
        db.execSQL("DROP TABLE IF EXISTS $TABLE_USERS")
        onCreate(db)
    }

    // ---------------- TASK CRUD ----------------

    fun insertTask(task: Task): Long {
        val db = writableDatabase
        val values = ContentValues().apply {
            put(COLUMN_TITLE, task.title)
            put(COLUMN_DESCRIPTION, task.description)
            put(COLUMN_START, task.startDateTime)
            put(COLUMN_DEADLINE, task.deadlineDateTime)
            put(COLUMN_PRIORITY, task.priority)
            put(COLUMN_CATEGORY, task.category)
        }
        return db.insert(TABLE_TASKS, null, values)
    }

    fun updateTask(task: Task): Int {
        val db = writableDatabase
        val values = ContentValues().apply {
            put(COLUMN_TITLE, task.title)
            put(COLUMN_DESCRIPTION, task.description)
            put(COLUMN_START, task.startDateTime)
            put(COLUMN_DEADLINE, task.deadlineDateTime)
            put(COLUMN_PRIORITY, task.priority)
            put(COLUMN_CATEGORY, task.category)
        }
        return db.update(TABLE_TASKS, values, "$COLUMN_TASK_ID = ?", arrayOf(task.id.toString()))
    }

    fun deleteTask(id: Int): Int {
        val db = writableDatabase
        return db.delete(TABLE_TASKS, "$COLUMN_TASK_ID = ?", arrayOf(id.toString()))
    }

    fun deleteTasks(ids: List<Int>): Int {
        val db = writableDatabase
        val placeholders = ids.joinToString(",") { "?" }
        return db.delete(TABLE_TASKS, "$COLUMN_TASK_ID IN ($placeholders)", ids.map { it.toString() }.toTypedArray())
    }

    fun getAllTasks(): List<Task> {
        val db = readableDatabase
        val tasks = mutableListOf<Task>()
        val cursor = db.query(TABLE_TASKS, null, null, null, null, null, "$COLUMN_DEADLINE ASC")
        cursor.use {
            while (it.moveToNext()) {
                tasks.add(
                    Task(
                        id = it.getInt(it.getColumnIndexOrThrow(COLUMN_TASK_ID)),
                        title = it.getString(it.getColumnIndexOrThrow(COLUMN_TITLE)),
                        description = it.getString(it.getColumnIndexOrThrow(COLUMN_DESCRIPTION)),
                        startDateTime = it.getLong(it.getColumnIndexOrThrow(COLUMN_START)),
                        deadlineDateTime = it.getLong(it.getColumnIndexOrThrow(COLUMN_DEADLINE)),
                        priority = it.getString(it.getColumnIndexOrThrow(COLUMN_PRIORITY)),
                        category = it.getString(it.getColumnIndexOrThrow(COLUMN_CATEGORY))
                    )
                )
            }
        }
        return tasks
    }

    fun getTaskById(id: Int): Task? {
        val db = readableDatabase
        val cursor = db.query(TABLE_TASKS, null, "$COLUMN_TASK_ID = ?", arrayOf(id.toString()), null, null, null)
        cursor.use {
            if (it.moveToFirst()) {
                return Task(
                    id = it.getInt(it.getColumnIndexOrThrow(COLUMN_TASK_ID)),
                    title = it.getString(it.getColumnIndexOrThrow(COLUMN_TITLE)),
                    description = it.getString(it.getColumnIndexOrThrow(COLUMN_DESCRIPTION)),
                    startDateTime = it.getLong(it.getColumnIndexOrThrow(COLUMN_START)),
                    deadlineDateTime = it.getLong(it.getColumnIndexOrThrow(COLUMN_DEADLINE)),
                    priority = it.getString(it.getColumnIndexOrThrow(COLUMN_PRIORITY)),
                    category = it.getString(it.getColumnIndexOrThrow(COLUMN_CATEGORY))
                )
            }
        }
        return null
    }

    // ---------------- USER CRUD ----------------

    /**
     * Inserts a new user account.
     * @return rowId of inserted user or -1 if failed.
     */
    fun insertUser(email: String, password: String, name: String?): Long {
        val db = writableDatabase
        val values = ContentValues().apply {
            put(COLUMN_USER_EMAIL, email)
            put(COLUMN_USER_PASSWORD, password)
            put(COLUMN_USER_NAME, name)
        }
        return db.insert(TABLE_USERS, null, values)
    }

    /**
     * Validates login credentials.
     * @return true if email/password match a user.
     */
    fun validateUser(email: String, password: String): Boolean {
        val db = readableDatabase
        val cursor = db.query(
            TABLE_USERS,
            arrayOf(COLUMN_USER_ID),
            "$COLUMN_USER_EMAIL = ? AND $COLUMN_USER_PASSWORD = ?",
            arrayOf(email, password),
            null, null, null
        )
        cursor.use {
            return it.count > 0
        }
    }

    /**
     * Fetch user by email.
     */
    fun getUserByEmail(email: String): Map<String, String?>? {
        val db = readableDatabase
        val cursor = db.query(
            TABLE_USERS,
            null,
            "$COLUMN_USER_EMAIL = ?",
            arrayOf(email),
            null, null, null
        )
        cursor.use {
            if (it.moveToFirst()) {
                return mapOf(
                    "id" to it.getInt(it.getColumnIndexOrThrow(COLUMN_USER_ID)).toString(),
                    "email" to it.getString(it.getColumnIndexOrThrow(COLUMN_USER_EMAIL)),
                    "name" to it.getString(it.getColumnIndexOrThrow(COLUMN_USER_NAME))
                )
            }
        }
        return null
    }

    fun getTasksBetween(startMillis: Long, endMillis: Long): List<Task> {
        val tasks = mutableListOf<Task>()
        val db = readableDatabase
        val cursor = db.query(
            "tasks",
            null,
            "startDateTime >= ? AND startDateTime <= ?",
            arrayOf(startMillis.toString(), endMillis.toString()),
            null, null, "startDateTime ASC"
        )
        while (cursor.moveToNext()) {
            tasks.add(Task.fromCursor(cursor))
        }
        cursor.close()
        return tasks
    }

    fun updateTaskCompletion(taskId: Int, completed: Boolean) {
        val db = writableDatabase
        val values = ContentValues().apply {
            put("completed", if (completed) 1 else 0)
        }
        db.update("tasks", values, "id = ?", arrayOf(taskId.toString()))
    }


}
