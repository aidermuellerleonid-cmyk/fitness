package de.fitapp.app.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import androidx.room.TypeConverter
import androidx.room.TypeConverters
import de.fitapp.app.data.dao.*
import de.fitapp.app.data.entity.*

class Converters {
    @TypeConverter fun goalToString(g: Goal) = g.name
    @TypeConverter fun stringToGoal(s: String) = Goal.valueOf(s)

    @TypeConverter fun genderToString(g: Gender) = g.name
    @TypeConverter fun stringToGender(s: String) = Gender.valueOf(s)

    @TypeConverter fun activityToString(a: ActivityLevel) = a.name
    @TypeConverter fun stringToActivity(s: String) = ActivityLevel.valueOf(s)

    @TypeConverter fun expToString(e: TrainingExperience) = e.name
    @TypeConverter fun stringToExp(s: String) = TrainingExperience.valueOf(s)

    @TypeConverter fun sourceToString(s: FoodSource) = s.name
    @TypeConverter fun stringToSource(s: String) = FoodSource.valueOf(s)

    @TypeConverter fun mealToString(m: MealType) = m.name
    @TypeConverter fun stringToMeal(s: String) = MealType.valueOf(s)

    @TypeConverter fun muscleToString(m: MuscleGroup) = m.name
    @TypeConverter fun stringToMuscle(s: String) = MuscleGroup.valueOf(s)
}

@Database(
    entities = [
        UserProfile::class,
        FoodItemEntity::class,
        DiaryEntryEntity::class,
        ExerciseLogEntity::class,
        SleepEntryEntity::class,
        WeightEntryEntity::class,
        ExerciseBaselineEntity::class
    ],
    version = 3,
    exportSchema = false
)
@TypeConverters(Converters::class)
abstract class AppDatabase : RoomDatabase() {
    abstract fun userProfileDao(): UserProfileDao
    abstract fun foodDao(): FoodDao
    abstract fun diaryDao(): DiaryDao
    abstract fun exerciseDao(): ExerciseDao
    abstract fun sleepDao(): SleepDao
    abstract fun weightDao(): WeightDao
    abstract fun exerciseBaselineDao(): ExerciseBaselineDao

    companion object {
        private val MIGRATION_1_2 = object : Migration(1, 2) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("ALTER TABLE user_profile ADD COLUMN benchPressMaxKg REAL")
                db.execSQL("ALTER TABLE user_profile ADD COLUMN squatMaxKg REAL")
                db.execSQL("ALTER TABLE user_profile ADD COLUMN legPressMaxKg REAL")
            }
        }
        private val MIGRATION_2_3 = object : Migration(2, 3) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("CREATE TABLE IF NOT EXISTS exercise_baseline (id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, exerciseName TEXT NOT NULL, muscleGroup TEXT NOT NULL, maxWeightKg REAL NOT NULL)")
            }
        }
        @Volatile private var INSTANCE: AppDatabase? = null

        fun getInstance(context: Context): AppDatabase =
            INSTANCE ?: synchronized(this) {
                INSTANCE ?: Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "fitapp.db"
                ).addMigrations(MIGRATION_1_2, MIGRATION_2_3).build().also { INSTANCE = it }
            }
    }
}
