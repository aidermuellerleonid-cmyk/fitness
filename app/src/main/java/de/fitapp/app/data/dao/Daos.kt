package de.fitapp.app.data.dao

import androidx.room.*
import de.fitapp.app.data.entity.*
import kotlinx.coroutines.flow.Flow

@Dao
interface UserProfileDao {
    @Query("SELECT * FROM user_profile WHERE id = 1")
    fun observeProfile(): Flow<UserProfile?>

    @Query("SELECT * FROM user_profile WHERE id = 1")
    suspend fun getProfileOnce(): UserProfile?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(profile: UserProfile)
}

@Dao
interface FoodDao {
    @Query("SELECT * FROM food_item WHERE barcode = :barcode LIMIT 1")
    suspend fun findByBarcode(barcode: String): FoodItemEntity?

    @Query("SELECT * FROM food_item WHERE name LIKE '%' || :query || '%' ORDER BY isCustom DESC, name ASC LIMIT 50")
    suspend fun search(query: String): List<FoodItemEntity>

    @Query("SELECT * FROM food_item WHERE source = 'LOCAL_DB' ORDER BY name ASC")
    suspend fun getBasicFoods(): List<FoodItemEntity>

    @Query("SELECT * FROM food_item WHERE name = :name LIMIT 1")
    suspend fun findByName(name: String): FoodItemEntity?

    @Query("""
        SELECT food_item.*, MAX(diary_entry.dateEpochDay) AS lastUsedEpochDay,
               COUNT(diary_entry.id) AS useCount
        FROM food_item
        INNER JOIN diary_entry ON diary_entry.foodItemId = food_item.id
        GROUP BY food_item.id
        ORDER BY MAX(diary_entry.id) DESC
        LIMIT 30
    """)
    fun observeRecentlyUsed(): Flow<List<FoodHistoryItem>>

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertAll(items: List<FoodItemEntity>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(item: FoodItemEntity): Long

    @Query("SELECT COUNT(*) FROM food_item")
    suspend fun count(): Int
}

@Dao
interface DiaryDao {
    @Insert
    suspend fun insert(entry: DiaryEntryEntity): Long

    @Delete
    suspend fun delete(entry: DiaryEntryEntity)

    @Query("SELECT * FROM diary_entry WHERE dateEpochDay = :day ORDER BY id ASC")
    fun observeForDay(day: Long): Flow<List<DiaryEntryEntity>>

    @Query("SELECT SUM(kcal) FROM diary_entry WHERE dateEpochDay = :day")
    fun observeKcalSumForDay(day: Long): Flow<Float?>

    @Query("SELECT DISTINCT dateEpochDay FROM diary_entry ORDER BY dateEpochDay DESC")
    suspend fun getAllLoggedDays(): List<Long>
}

@Dao
interface ExerciseDao {
    @Insert
    suspend fun insert(entry: ExerciseLogEntity): Long

    @Delete
    suspend fun delete(entry: ExerciseLogEntity)

    @Query("SELECT * FROM exercise_log ORDER BY dateEpochDay DESC, id DESC")
    fun observeAll(): Flow<List<ExerciseLogEntity>>

    @Query("SELECT * FROM exercise_log WHERE muscleGroup = :group AND dateEpochDay >= :sinceDay ORDER BY dateEpochDay ASC")
    suspend fun getForMuscleGroupSince(group: MuscleGroup, sinceDay: Long): List<ExerciseLogEntity>
}

@Dao
interface ExerciseBaselineDao {
    @Query("SELECT * FROM exercise_baseline ORDER BY muscleGroup, id")
    fun observeAll(): Flow<List<ExerciseBaselineEntity>>

    @Query("DELETE FROM exercise_baseline")
    suspend fun deleteAll()

    @Insert
    suspend fun insertAll(entries: List<ExerciseBaselineEntity>)
}

@Dao
interface SleepDao {
    @Insert
    suspend fun insert(entry: SleepEntryEntity): Long

    @Delete
    suspend fun delete(entry: SleepEntryEntity)

    @Query("SELECT * FROM sleep_entry ORDER BY dateEpochDay DESC")
    fun observeAll(): Flow<List<SleepEntryEntity>>
}

@Dao
interface WeightDao {
    @Insert
    suspend fun insert(entry: WeightEntryEntity): Long

    @Query("SELECT * FROM weight_entry ORDER BY dateEpochDay DESC")
    fun observeAll(): Flow<List<WeightEntryEntity>>
}
