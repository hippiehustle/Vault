# Complete Build Prompt for SkyView Weather App

## PROJECT OVERVIEW

Build a production-ready Android application called "SkyView Weather" - a sophisticated dual-purpose app that serves as both a fully functional weather application AND a hidden encrypted vault system. The vault is accessed through a secret tap sequence on the weather widget, making it completely invisible to casual observation.

**Package Name**: `com.skyview.weather`
**Application ID**: `com.skyview.weather`
**Min SDK**: 26 (Android 8.0)
**Target SDK**: 35 (Android 15)
**Compile SDK**: 36 (Android 16)
**Language**: Kotlin 2.0.21
**UI Framework**: Jetpack Compose with Material Design 3
**JVM Target**: Java 21

---

## CORE FUNCTIONALITY

### Weather App (Cover Function)
- Real-time weather data from OpenWeatherMap API
- 7-day forecast with hourly breakdown
- GPS-based location detection
- Multiple saved locations support
- Home screen widgets (small, medium, large) with auto-refresh every 30 minutes
- Weather caching to minimize API calls
- Material 3 design with light/dark themes
- Temperature, humidity, wind speed, precipitation, UV index display

### Hidden Vault (Primary Function)
- Military-grade AES-256-GCM encryption
- Stores 7 content types: Photos, Videos, Documents, Notes, Passwords, Audio, Contacts
- Folder organization with 3-level depth
- Starred/favorite items
- Trash with 30-day auto-delete
- Search and filtering capabilities
- Thumbnail generation for media
- Metadata tracking (file size, dimensions, duration)
- Clipboard auto-clear after 30 seconds

### Security Features
- Access via widget tap sequence (e.g., Cloud Icon → Temperature → High/Low within 5 seconds)
- Biometric authentication (fingerprint/face ID)
- PBKDF2 key derivation (100,000 iterations)
- SQLCipher encrypted database (even DB name is obfuscated as "weather_cache.db")
- Android Keystore integration with hardware-backed security
- Rate limiting on failed attempts
- No backups allowed (security manifest config)
- Screenshot prevention in vault mode
- Memory scrubbing for sensitive data
- ProGuard obfuscation for release builds

---

## BUILD CONFIGURATION

### Root build.gradle.kts
```kotlin
plugins {
    id("com.android.application") version "8.13.0" apply false
    id("org.jetbrains.kotlin.android") version "2.0.21" apply false
    id("org.jetbrains.kotlin.plugin.compose") version "2.0.21" apply false
    id("com.google.dagger.hilt.android") version "2.56.1" apply false
}
```

### settings.gradle.kts
```kotlin
pluginManagement {
    repositories {
        google()
        mavenCentral()
        gradlePluginPortal()
    }
}
dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        google()
        mavenCentral()
        maven { url = uri("https://jitpack.io") }
    }
}
rootProject.name = "Vault"
include(":app")
```

### gradle.properties
```properties
org.gradle.jvmargs=-Xmx4096M -Dkotlin.daemon.jvm.options=-Xmx2048M
org.gradle.daemon=true
org.gradle.caching=true
org.gradle.parallel=true
android.useAndroidX=true
android.enableJetifier=false
kotlin.code.style=official
```

### App build.gradle.kts
```kotlin
plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
    id("org.jetbrains.kotlin.plugin.compose")
    kotlin("kapt")
    id("com.google.dagger.hilt.android")
}

android {
    namespace = "com.skyview.weather"
    compileSdk = 36

    defaultConfig {
        applicationId = "com.skyview.weather"
        minSdk = 26
        targetSdk = 35
        versionCode = 1
        versionName = "1.0.0"
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        vectorDrawables.useSupportLibrary = true
        buildConfigField("String", "DEFAULT_WEATHER_API_KEY", "\"demo_key_replace_with_real\"")
    }

    buildTypes {
        release {
            isMinifyEnabled = true
            isShrinkResources = true
            proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro")
            buildConfigField("Boolean", "DEBUG_MODE", "false")
        }
        debug {
            isMinifyEnabled = false
            buildConfigField("Boolean", "DEBUG_MODE", "true")
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_21
        targetCompatibility = JavaVersion.VERSION_21
    }

    kotlinOptions {
        jvmTarget = "21"
        freeCompilerArgs += listOf("-opt-in=kotlin.RequiresOptIn", "-opt-in=kotlinx.coroutines.ExperimentalCoroutinesApi")
    }

    buildFeatures {
        compose = true
        buildConfig = true
    }

    packaging {
        resources {
            excludes += "/META-INF/{AL2.0,LGPL2.1}"
            excludes += "/META-INF/INDEX.LIST"
            excludes += "/META-INF/DEPENDENCIES"
        }
    }
}

dependencies {
    // Core Android
    implementation("androidx.core:core-ktx:1.17.0")
    implementation("androidx.lifecycle:lifecycle-runtime-ktx:2.9.4")
    implementation("androidx.lifecycle:lifecycle-viewmodel-ktx:2.9.4")
    implementation("androidx.lifecycle:lifecycle-viewmodel-compose:2.9.4")
    implementation("androidx.activity:activity-compose:1.10.3")
    implementation("androidx.appcompat:appcompat:1.7.0")

    // Compose
    val composeBom = platform("androidx.compose:compose-bom:2025.10.01")
    implementation(composeBom)
    implementation("androidx.compose.ui:ui")
    implementation("androidx.compose.ui:ui-graphics")
    implementation("androidx.compose.ui:ui-tooling-preview")
    implementation("androidx.compose.material3:material3")
    implementation("androidx.compose.material:material-icons-extended")
    implementation("androidx.compose.runtime:runtime-livedata")

    // Glance (Widgets)
    implementation("androidx.glance:glance-appwidget:1.1.1")
    implementation("androidx.glance:glance-material3:1.1.1")

    // Navigation
    implementation("androidx.navigation:navigation-compose:2.8.9")

    // Accompanist
    implementation("com.google.accompanist:accompanist-permissions:0.32.0")

    // Hilt
    implementation("com.google.dagger:hilt-android:2.56.1")
    kapt("com.google.dagger:hilt-compiler:2.56.1")
    implementation("androidx.hilt:hilt-navigation-compose:1.2.0")
    implementation("androidx.hilt:hilt-work:1.2.0")
    kapt("androidx.hilt:hilt-compiler:1.2.0")

    // Networking
    implementation("com.squareup.retrofit2:retrofit:2.9.0")
    implementation("com.squareup.retrofit2:converter-gson:2.9.0")
    implementation("com.squareup.okhttp3:okhttp:4.11.0")
    implementation("com.squareup.okhttp3:logging-interceptor:4.11.0")

    // Security & Encryption
    implementation("androidx.security:security-crypto:1.1.0-alpha06")
    implementation("net.zetetic:android-database-sqlcipher:4.5.4")
    implementation("androidx.sqlite:sqlite-ktx:2.4.0")
    implementation("androidx.biometric:biometric:1.2.0-alpha05")

    // Room Database
    val roomVersion = "2.8.0"
    implementation("androidx.room:room-runtime:$roomVersion")
    kapt("androidx.room:room-compiler:$roomVersion")

    // DataStore
    implementation("androidx.datastore:datastore-preferences:1.1.1")

    // WorkManager
    implementation("androidx.work:work-runtime-ktx:2.10.1")

    // Location Services
    implementation("com.google.android.gms:play-services-location:21.0.1")

    // Coroutines
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-android:1.9.0")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-play-services:1.9.0")

    // Charts
    implementation("com.github.PhilJay:MPAndroidChart:v3.1.0")

    // Image Loading
    implementation("io.coil-kt:coil-compose:2.8.0")

    // Splash Screen
    implementation("androidx.core:core-splashscreen:1.2.0")

    // Testing
    testImplementation("junit:junit:4.13.2")
    testImplementation("io.mockk:mockk:1.13.14")
    androidTestImplementation("androidx.test.ext:junit:1.2.1")
    androidTestImplementation("androidx.test.espresso:espresso-core:3.6.1")
    androidTestImplementation(composeBom)
    androidTestImplementation("androidx.compose.ui:ui-test-junit4")
    debugImplementation("androidx.compose.ui:ui-tooling")
    debugImplementation("androidx.compose.ui:ui-test-manifest")
}

kapt {
    correctErrorTypes = true
}
```

---

## PROJECT STRUCTURE

```
android/app/src/main/java/com/skyview/weather/
├── core/
│   ├── database/
│   │   ├── VaultDatabase.kt                 # Room database with SQLCipher
│   │   ├── dao/
│   │   │   ├── VaultItemDao.kt             # CRUD for vault items
│   │   │   ├── FolderDao.kt                # Folder management
│   │   │   └── WeatherCacheDao.kt          # Weather data caching
│   │   └── entity/
│   │       ├── VaultItemEntity.kt          # Encrypted item storage
│   │       ├── FolderEntity.kt             # Folder structure
│   │       └── WeatherCacheEntity.kt       # Cached weather data
│   ├── di/
│   │   ├── AppModule.kt                    # Core app dependencies
│   │   ├── DatabaseModule.kt               # Room + SQLCipher setup
│   │   ├── NetworkModule.kt                # Retrofit configuration
│   │   └── SecurityModule.kt               # Encryption services
│   ├── location/
│   │   └── LocationService.kt              # GPS location provider
│   ├── network/
│   │   ├── WeatherApiService.kt            # OpenWeatherMap API
│   │   └── NetworkResult.kt                # Result wrapper
│   └── security/
│       ├── EncryptionService.kt            # AES-256-GCM encryption
│       ├── KeyManager.kt                   # Android Keystore integration
│       ├── BiometricManager.kt             # Biometric authentication
│       └── TapSequenceTracker.kt           # Widget tap detection
├── data/
│   ├── local/
│   │   └── PreferencesDataStore.kt         # App preferences
│   ├── model/
│   │   ├── WeatherResponse.kt              # API response DTOs
│   │   ├── VaultItem.kt                    # Domain models
│   │   └── Folder.kt
│   ├── repository/
│   │   ├── WeatherRepository.kt            # Weather data source
│   │   ├── VaultRepository.kt              # Vault operations
│   │   └── LocationRepository.kt           # Location data
│   └── worker/
│       └── WeatherUpdateWorker.kt          # Background weather refresh
├── domain/
│   ├── model/
│   │   ├── Weather.kt                      # Clean domain models
│   │   ├── VaultContent.kt
│   │   └── ContentType.kt                  # Enum: PHOTO, VIDEO, etc.
│   └── usecase/
│       ├── GetWeatherUseCase.kt
│       ├── EncryptFileUseCase.kt
│       ├── DecryptFileUseCase.kt
│       └── ValidateTapSequenceUseCase.kt
├── presentation/
│   ├── MainActivity.kt                     # Single activity app
│   ├── navigation/
│   │   └── AppNavigation.kt                # NavHost setup
│   ├── onboarding/
│   │   ├── OnboardingScreen.kt             # Initial setup
│   │   └── OnboardingViewModel.kt
│   ├── settings/
│   │   ├── SettingsScreen.kt
│   │   └── SettingsViewModel.kt
│   ├── theme/
│   │   ├── Color.kt                        # Material 3 colors
│   │   ├── Theme.kt                        # Light/dark themes
│   │   └── Type.kt                         # Typography
│   ├── vault/
│   │   ├── VaultUnlockScreen.kt            # Password/biometric entry
│   │   ├── VaultBrowserScreen.kt           # File/folder browser
│   │   ├── VaultItemDetailScreen.kt        # View/edit items
│   │   ├── CreateItemScreen.kt
│   │   └── viewmodel/
│   │       ├── VaultViewModel.kt
│   │       └── VaultItemViewModel.kt
│   ├── weather/
│   │   ├── WeatherHomeScreen.kt            # Main weather display
│   │   ├── ForecastScreen.kt               # 7-day forecast
│   │   ├── LocationPickerScreen.kt
│   │   └── viewmodel/
│   │       └── WeatherViewModel.kt
│   └── widget/
│       ├── WeatherWidget.kt                # Glance widget
│       ├── WeatherWidgetReceiver.kt        # Broadcast receiver
│       └── WeatherWidgetStateDefinition.kt # Widget state
└── util/
    ├── Constants.kt                         # App constants
    └── Extensions.kt                        # Kotlin extensions

android/app/src/main/res/
├── drawable/                                # Icons and graphics
├── values/
│   ├── strings.xml                          # All text strings
│   ├── themes.xml                           # Material 3 themes
│   └── colors.xml                           # Color resources
└── xml/
    ├── network_security_config.xml          # Network security
    └── data_extraction_rules.xml            # Backup exclusions
```

---

## IMPLEMENTATION DETAILS

### 1. SECURITY LAYER

#### EncryptionService.kt
```kotlin
@Singleton
class EncryptionService @Inject constructor(
    private val keyManager: KeyManager
) {
    private val CIPHER_ALGORITHM = "AES/GCM/NoPadding"
    private val KEY_SIZE = 256
    private val GCM_TAG_LENGTH = 128
    private val PBKDF2_ITERATIONS = 100000

    suspend fun encryptData(data: ByteArray, password: String): EncryptedData
    suspend fun decryptData(encryptedData: EncryptedData, password: String): ByteArray
    suspend fun encryptFile(inputStream: InputStream, password: String): File
    suspend fun decryptFile(encryptedFile: File, password: String): ByteArray
    private fun deriveKey(password: String, salt: ByteArray): SecretKey
    private fun generateSalt(): ByteArray
    fun secureWipe(data: ByteArray)
}

data class EncryptedData(
    val ciphertext: ByteArray,
    val iv: ByteArray,
    val salt: ByteArray,
    val authTag: ByteArray
)
```

#### KeyManager.kt
```kotlin
@Singleton
class KeyManager @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private val KEYSTORE_PROVIDER = "AndroidKeyStore"
    private val KEY_ALIAS = "skyview_master_key"

    fun generateKey()
    fun getKey(): SecretKey
    fun deleteKey()
    fun isKeyStoreAvailable(): Boolean
    fun hasKey(): Boolean
}
```

#### BiometricManager.kt
```kotlin
@Singleton
class BiometricManager @Inject constructor(
    @ApplicationContext private val context: Context
) {
    fun isBiometricAvailable(): Boolean
    fun authenticate(
        fragmentActivity: FragmentActivity,
        onSuccess: () -> Unit,
        onError: (String) -> Unit,
        onFailed: () -> Unit
    )
    fun createPrompt(fragmentActivity: FragmentActivity): BiometricPrompt
}
```

#### TapSequenceTracker.kt
```kotlin
@Singleton
class TapSequenceTracker @Inject constructor(
    private val preferencesDataStore: PreferencesDataStore
) {
    private val TAP_TIMEOUT_MS = 5000L
    private val MAX_FAILED_ATTEMPTS = 5
    private val LOCKOUT_DURATION_MS = 300000L // 5 minutes

    data class TapTarget(val id: String, val timestamp: Long)

    suspend fun recordTap(targetId: String)
    suspend fun validateSequence(expectedSequence: List<String>): Boolean
    suspend fun resetSequence()
    suspend fun isLocked(): Boolean
    suspend fun recordFailedAttempt()
    suspend fun getFailedAttempts(): Int
}
```

### 2. DATABASE LAYER

#### VaultDatabase.kt
```kotlin
@Database(
    entities = [
        VaultItemEntity::class,
        FolderEntity::class,
        WeatherCacheEntity::class
    ],
    version = 1,
    exportSchema = false
)
abstract class VaultDatabase : RoomDatabase() {
    abstract fun vaultItemDao(): VaultItemDao
    abstract fun folderDao(): FolderDao
    abstract fun weatherCacheDao(): WeatherCacheDao

    companion object {
        const val DATABASE_NAME = "weather_cache.db" // Obfuscated name
    }
}
```

#### Database Module (DatabaseModule.kt)
```kotlin
@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    @Provides
    @Singleton
    fun provideVaultDatabase(
        @ApplicationContext context: Context,
        encryptionService: EncryptionService
    ): VaultDatabase {
        val passphrase = encryptionService.getDatabasePassphrase()
        val factory = SupportFactory(SQLiteDatabase.getBytes(passphrase.toCharArray()))

        return Room.databaseBuilder(
            context,
            VaultDatabase::class.java,
            VaultDatabase.DATABASE_NAME
        )
            .openHelperFactory(factory)
            .fallbackToDestructiveMigration()
            .build()
    }
}
```

#### VaultItemEntity.kt
```kotlin
@Entity(tableName = "vault_items")
data class VaultItemEntity(
    @PrimaryKey val id: String = UUID.randomUUID().toString(),
    val name: String,
    val type: ContentType,
    val encryptedData: ByteArray, // Encrypted content
    val encryptedMetadata: String, // Encrypted JSON metadata
    val thumbnailPath: String?,
    val folderId: String?,
    val isStarred: Boolean = false,
    val isInTrash: Boolean = false,
    val createdAt: Long = System.currentTimeMillis(),
    val modifiedAt: Long = System.currentTimeMillis(),
    val trashedAt: Long? = null,
    val fileSize: Long,
    val mimeType: String
)

enum class ContentType {
    PHOTO, VIDEO, DOCUMENT, NOTE, PASSWORD, AUDIO, CONTACT
}
```

#### FolderEntity.kt
```kotlin
@Entity(tableName = "folders")
data class FolderEntity(
    @PrimaryKey val id: String = UUID.randomUUID().toString(),
    val name: String,
    val parentFolderId: String?,
    val level: Int = 0, // Max 3 levels
    val createdAt: Long = System.currentTimeMillis(),
    val color: String? = null,
    val icon: String? = null
)
```

### 3. NETWORK LAYER

#### WeatherApiService.kt
```kotlin
interface WeatherApiService {
    @GET("weather")
    suspend fun getCurrentWeather(
        @Query("lat") latitude: Double,
        @Query("lon") longitude: Double,
        @Query("appid") apiKey: String,
        @Query("units") units: String = "metric"
    ): WeatherResponse

    @GET("forecast")
    suspend fun getForecast(
        @Query("lat") latitude: Double,
        @Query("lon") longitude: Double,
        @Query("appid") apiKey: String,
        @Query("units") units: String = "metric"
    ): ForecastResponse
}
```

#### NetworkModule.kt
```kotlin
@Module
@InstallIn(SingletonComponent::class)
object NetworkModule {

    @Provides
    @Singleton
    fun provideOkHttpClient(): OkHttpClient {
        return OkHttpClient.Builder()
            .addInterceptor(HttpLoggingInterceptor().apply {
                level = if (BuildConfig.DEBUG) HttpLoggingInterceptor.Level.BODY
                else HttpLoggingInterceptor.Level.NONE
            })
            .connectTimeout(30, TimeUnit.SECONDS)
            .readTimeout(30, TimeUnit.SECONDS)
            .build()
    }

    @Provides
    @Singleton
    fun provideRetrofit(okHttpClient: OkHttpClient): Retrofit {
        return Retrofit.Builder()
            .baseUrl("https://api.openweathermap.org/data/2.5/")
            .client(okHttpClient)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }

    @Provides
    @Singleton
    fun provideWeatherApiService(retrofit: Retrofit): WeatherApiService {
        return retrofit.create(WeatherApiService::class.java)
    }
}
```

### 4. REPOSITORY LAYER

#### WeatherRepository.kt
```kotlin
@Singleton
class WeatherRepository @Inject constructor(
    private val apiService: WeatherApiService,
    private val weatherCacheDao: WeatherCacheDao,
    private val preferencesDataStore: PreferencesDataStore
) {
    private val CACHE_TIMEOUT_MS = 1800000L // 30 minutes

    suspend fun getCurrentWeather(lat: Double, lon: Double): Result<Weather>
    suspend fun getForecast(lat: Double, lon: Double): Result<List<ForecastItem>>
    suspend fun getCachedWeather(lat: Double, lon: Double): Weather?
    suspend fun cacheWeather(weather: Weather, lat: Double, lon: Double)
    private fun isCacheValid(timestamp: Long): Boolean
}
```

#### VaultRepository.kt
```kotlin
@Singleton
class VaultRepository @Inject constructor(
    private val vaultItemDao: VaultItemDao,
    private val folderDao: FolderDao,
    private val encryptionService: EncryptionService,
    @ApplicationContext private val context: Context
) {
    suspend fun getAllItems(): Flow<List<VaultItemEntity>>
    suspend fun getItemById(id: String): VaultItemEntity?
    suspend fun createItem(item: VaultItemEntity, password: String): Result<VaultItemEntity>
    suspend fun updateItem(item: VaultItemEntity, password: String): Result<Unit>
    suspend fun deleteItem(id: String): Result<Unit>
    suspend fun moveToTrash(id: String): Result<Unit>
    suspend fun restoreFromTrash(id: String): Result<Unit>
    suspend fun emptyTrash(): Result<Unit>
    suspend fun searchItems(query: String): List<VaultItemEntity>
    suspend fun getItemsByType(type: ContentType): List<VaultItemEntity>
    suspend fun getStarredItems(): List<VaultItemEntity>

    // Folder operations
    suspend fun createFolder(folder: FolderEntity): Result<FolderEntity>
    suspend fun getFolders(): List<FolderEntity>
    suspend fun moveItemToFolder(itemId: String, folderId: String?): Result<Unit>
}
```

### 5. VIEWMODELS

#### WeatherViewModel.kt
```kotlin
@HiltViewModel
class WeatherViewModel @Inject constructor(
    private val weatherRepository: WeatherRepository,
    private val locationRepository: LocationRepository,
    private val preferencesDataStore: PreferencesDataStore
) : ViewModel() {

    private val _weatherState = MutableStateFlow<WeatherState>(WeatherState.Loading)
    val weatherState: StateFlow<WeatherState> = _weatherState.asStateFlow()

    private val _currentLocation = MutableStateFlow<Location?>(null)
    val currentLocation: StateFlow<Location?> = _currentLocation.asStateFlow()

    fun loadWeather()
    fun refreshWeather()
    fun getCurrentLocation()
    fun searchLocation(query: String)
    fun saveLocation(location: Location)
}

sealed class WeatherState {
    object Loading : WeatherState()
    data class Success(val weather: Weather, val forecast: List<ForecastItem>) : WeatherState()
    data class Error(val message: String) : WeatherState()
}
```

#### VaultViewModel.kt
```kotlin
@HiltViewModel
class VaultViewModel @Inject constructor(
    private val vaultRepository: VaultRepository,
    private val encryptionService: EncryptionService,
    private val biometricManager: BiometricManager,
    private val preferencesDataStore: PreferencesDataStore
) : ViewModel() {

    private val _vaultState = MutableStateFlow<VaultState>(VaultState.Locked)
    val vaultState: StateFlow<VaultState> = _vaultState.asStateFlow()

    private val _items = MutableStateFlow<List<VaultItemEntity>>(emptyList())
    val items: StateFlow<List<VaultItemEntity>> = _items.asStateFlow()

    fun unlockWithPassword(password: String)
    fun unlockWithBiometric(activity: FragmentActivity)
    fun lock()
    fun loadItems()
    fun createItem(name: String, type: ContentType, data: ByteArray, folderId: String?)
    fun deleteItem(id: String)
    fun moveToTrash(id: String)
    fun searchItems(query: String)
    fun filterByType(type: ContentType)
    fun toggleStar(id: String)
}

sealed class VaultState {
    object Locked : VaultState()
    object Unlocked : VaultState()
    object Loading : VaultState()
    data class Error(val message: String) : VaultState()
}
```

### 6. UI SCREENS (JETPACK COMPOSE)

#### MainActivity.kt
```kotlin
@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        installSplashScreen()

        WindowCompat.setDecorFitsSystemWindows(window, false)

        setContent {
            SkyViewTheme {
                AppNavigation()
            }
        }
    }
}
```

#### AppNavigation.kt
```kotlin
@Composable
fun AppNavigation() {
    val navController = rememberNavController()

    NavHost(
        navController = navController,
        startDestination = "weather_home"
    ) {
        composable("weather_home") { WeatherHomeScreen(navController) }
        composable("vault_unlock") { VaultUnlockScreen(navController) }
        composable("vault_browser") { VaultBrowserScreen(navController) }
        composable("vault_item/{itemId}") { VaultItemDetailScreen(navController, it.arguments?.getString("itemId")) }
        composable("create_item") { CreateItemScreen(navController) }
        composable("settings") { SettingsScreen(navController) }
        composable("onboarding") { OnboardingScreen(navController) }
    }
}
```

#### WeatherHomeScreen.kt (Compose)
```kotlin
@Composable
fun WeatherHomeScreen(
    navController: NavController,
    viewModel: WeatherViewModel = hiltViewModel()
) {
    val weatherState by viewModel.weatherState.collectAsState()
    val currentLocation by viewModel.currentLocation.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("SkyView Weather") },
                actions = {
                    IconButton(onClick = { navController.navigate("settings") }) {
                        Icon(Icons.Default.Settings, "Settings")
                    }
                }
            )
        }
    ) { paddingValues ->
        when (val state = weatherState) {
            is WeatherState.Loading -> LoadingIndicator()
            is WeatherState.Success -> WeatherContent(state.weather, state.forecast)
            is WeatherState.Error -> ErrorMessage(state.message)
        }
    }
}

@Composable
fun WeatherContent(weather: Weather, forecast: List<ForecastItem>) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        // Current weather
        CurrentWeatherCard(weather)
        Spacer(modifier = Modifier.height(16.dp))
        // 7-day forecast
        ForecastList(forecast)
    }
}
```

#### VaultUnlockScreen.kt
```kotlin
@Composable
fun VaultUnlockScreen(
    navController: NavController,
    viewModel: VaultViewModel = hiltViewModel()
) {
    val vaultState by viewModel.vaultState.collectAsState()
    var password by remember { mutableStateOf("") }

    LaunchedEffect(vaultState) {
        if (vaultState is VaultState.Unlocked) {
            navController.navigate("vault_browser") {
                popUpTo("vault_unlock") { inclusive = true }
            }
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(Icons.Default.Lock, "Vault", modifier = Modifier.size(64.dp))
        Spacer(modifier = Modifier.height(32.dp))

        OutlinedTextField(
            value = password,
            onValueChange = { password = it },
            label = { Text("Master Password") },
            visualTransformation = PasswordVisualTransformation(),
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(16.dp))

        Button(
            onClick = { viewModel.unlockWithPassword(password) },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Unlock")
        }

        Spacer(modifier = Modifier.height(8.dp))

        TextButton(
            onClick = { viewModel.unlockWithBiometric(context as ComponentActivity) }
        ) {
            Icon(Icons.Default.Fingerprint, "Biometric")
            Spacer(modifier = Modifier.width(8.dp))
            Text("Use Biometric")
        }
    }
}
```

#### VaultBrowserScreen.kt
```kotlin
@Composable
fun VaultBrowserScreen(
    navController: NavController,
    viewModel: VaultViewModel = hiltViewModel()
) {
    val items by viewModel.items.collectAsState()
    var searchQuery by remember { mutableStateOf("") }
    var selectedType by remember { mutableStateOf<ContentType?>(null) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Vault") },
                navigationIcon = {
                    IconButton(onClick = { viewModel.lock(); navController.popBackStack() }) {
                        Icon(Icons.Default.Lock, "Lock")
                    }
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(onClick = { navController.navigate("create_item") }) {
                Icon(Icons.Default.Add, "Add Item")
            }
        }
    ) { paddingValues ->
        Column(modifier = Modifier.padding(paddingValues)) {
            // Search bar
            SearchBar(
                query = searchQuery,
                onQueryChange = { searchQuery = it; viewModel.searchItems(it) }
            )

            // Type filter chips
            FilterChips(
                selectedType = selectedType,
                onTypeSelected = { type -> selectedType = type; viewModel.filterByType(type) }
            )

            // Items grid
            LazyVerticalGrid(
                columns = GridCells.Fixed(2),
                contentPadding = PaddingValues(8.dp)
            ) {
                items(items) { item ->
                    VaultItemCard(
                        item = item,
                        onClick = { navController.navigate("vault_item/${item.id}") }
                    )
                }
            }
        }
    }
}
```

### 7. WIDGET IMPLEMENTATION (GLANCE)

#### WeatherWidget.kt
```kotlin
class WeatherWidget : GlanceAppWidget() {
    override suspend fun provideGlance(context: Context, id: GlanceId) {
        provideContent {
            WeatherWidgetContent()
        }
    }
}

@Composable
fun WeatherWidgetContent() {
    val context = LocalContext.current
    val weather = getCurrentWeather(context)

    GlanceTheme {
        Box(
            modifier = GlanceModifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.surface)
                .padding(16.dp)
        ) {
            Column {
                // Weather icon (tap target 1)
                Image(
                    provider = ImageProvider(getWeatherIcon(weather.condition)),
                    contentDescription = "Weather",
                    modifier = GlanceModifier
                        .size(48.dp)
                        .clickable(actionStartActivity<MainActivity>(
                            actionParametersOf(TAP_TARGET to "weather_icon")
                        ))
                )

                // Temperature (tap target 2)
                Text(
                    text = "${weather.temperature}°",
                    style = TextStyle(fontSize = 32.sp, fontWeight = FontWeight.Bold),
                    modifier = GlanceModifier.clickable(actionStartActivity<MainActivity>(
                        actionParametersOf(TAP_TARGET to "temperature")
                    ))
                )

                // High/Low (tap target 3)
                Text(
                    text = "H:${weather.high}° L:${weather.low}°",
                    style = TextStyle(fontSize = 14.sp),
                    modifier = GlanceModifier.clickable(actionStartActivity<MainActivity>(
                        actionParametersOf(TAP_TARGET to "high_low")
                    ))
                )

                Text(
                    text = weather.location,
                    style = TextStyle(fontSize = 12.sp)
                )
            }
        }
    }
}
```

#### WeatherWidgetReceiver.kt
```kotlin
class WeatherWidgetReceiver : GlanceAppWidgetReceiver() {
    override val glanceAppWidget: GlanceAppWidget = WeatherWidget()

    override fun onReceive(context: Context, intent: Intent) {
        super.onReceive(context, intent)

        // Handle tap sequence
        val tapTarget = intent.getStringExtra(TAP_TARGET)
        if (tapTarget != null) {
            val tapTracker = TapSequenceTracker(context)
            tapTracker.recordTap(tapTarget)

            if (tapTracker.validateSequence(listOf("weather_icon", "temperature", "high_low"))) {
                // Launch vault unlock
                val vaultIntent = Intent(context, MainActivity::class.java).apply {
                    putExtra("destination", "vault_unlock")
                    flags = Intent.FLAG_ACTIVITY_NEW_TASK
                }
                context.startActivity(vaultIntent)
            }
        }
    }
}
```

### 8. BACKGROUND WORK

#### WeatherUpdateWorker.kt
```kotlin
@HiltWorker
class WeatherUpdateWorker @AssistedInject constructor(
    @Assisted context: Context,
    @Assisted params: WorkerParameters,
    private val weatherRepository: WeatherRepository,
    private val locationRepository: LocationRepository
) : CoroutineWorker(context, params) {

    override suspend fun doWork(): Result {
        return try {
            val location = locationRepository.getLastKnownLocation()
            if (location != null) {
                weatherRepository.getCurrentWeather(location.latitude, location.longitude)
                WeatherWidget().updateAll(applicationContext)
            }
            Result.success()
        } catch (e: Exception) {
            Result.retry()
        }
    }

    companion object {
        const val WORK_NAME = "weather_update"

        fun schedule(context: Context) {
            val workRequest = PeriodicWorkRequestBuilder<WeatherUpdateWorker>(
                30, TimeUnit.MINUTES
            ).build()

            WorkManager.getInstance(context).enqueueUniquePeriodicWork(
                WORK_NAME,
                ExistingPeriodicWorkPolicy.KEEP,
                workRequest
            )
        }
    }
}
```

### 9. ANDROID MANIFEST

```xml
<?xml version="1.0" encoding="utf-8"?>
<manifest xmlns:android="http://schemas.android.com/apk/res/android"
    xmlns:tools="http://schemas.android.com/tools">

    <!-- Permissions -->
    <uses-permission android:name="android.permission.INTERNET" />
    <uses-permission android:name="android.permission.ACCESS_FINE_LOCATION" />
    <uses-permission android:name="android.permission.ACCESS_COARSE_LOCATION" />
    <uses-permission android:name="android.permission.USE_BIOMETRIC" />
    <uses-permission android:name="android.permission.READ_EXTERNAL_STORAGE" />
    <uses-permission android:name="android.permission.WRITE_EXTERNAL_STORAGE" />
    <uses-permission android:name="android.permission.CAMERA" />
    <uses-permission android:name="android.permission.RECORD_AUDIO" />

    <application
        android:name=".VaultApplication"
        android:allowBackup="false"
        android:dataExtractionRules="@xml/data_extraction_rules"
        android:fullBackupContent="false"
        android:icon="@mipmap/ic_launcher"
        android:label="@string/app_name"
        android:networkSecurityConfig="@xml/network_security_config"
        android:roundIcon="@mipmap/ic_launcher_round"
        android:supportsRtl="true"
        android:theme="@style/Theme.SkyView"
        tools:targetApi="31">

        <!-- Main Activity -->
        <activity
            android:name=".presentation.MainActivity"
            android:exported="true"
            android:theme="@style/Theme.SkyView"
            android:windowSoftInputMode="adjustResize">
            <intent-filter>
                <action android:name="android.intent.action.MAIN" />
                <category android:name="android.intent.category.LAUNCHER" />
            </intent-filter>
        </activity>

        <!-- Weather Widget -->
        <receiver
            android:name=".presentation.widget.WeatherWidgetReceiver"
            android:exported="true">
            <intent-filter>
                <action android:name="android.appwidget.action.APPWIDGET_UPDATE" />
            </intent-filter>
            <meta-data
                android:name="android.appwidget.provider"
                android:resource="@xml/weather_widget_info" />
        </receiver>

        <!-- WorkManager -->
        <provider
            android:name="androidx.startup.InitializationProvider"
            android:authorities="${applicationId}.androidx-startup"
            android:exported="false"
            tools:node="merge">
            <meta-data
                android:name="androidx.work.WorkManagerInitializer"
                android:value="androidx.startup" />
        </provider>
    </application>
</manifest>
```

### 10. RESOURCES

#### strings.xml
```xml
<resources>
    <string name="app_name">SkyView Weather</string>
    <string name="widget_name">SkyView Weather Widget</string>
    <string name="vault_locked">Vault Locked</string>
    <string name="unlock_vault">Unlock Vault</string>
    <string name="master_password">Master Password</string>
    <string name="use_biometric">Use Biometric</string>
    <string name="weather">Weather</string>
    <string name="forecast">7-Day Forecast</string>
    <string name="settings">Settings</string>
    <string name="location">Location</string>
    <string name="temperature">Temperature</string>
    <string name="humidity">Humidity</string>
    <string name="wind_speed">Wind Speed</string>
    <string name="vault">Vault</string>
    <string name="create_item">Create Item</string>
    <string name="delete_item">Delete Item</string>
    <string name="move_to_trash">Move to Trash</string>
    <string name="restore">Restore</string>
    <string name="empty_trash">Empty Trash</string>
    <string name="search">Search</string>
    <string name="filter">Filter</string>
    <string name="starred">Starred</string>
    <string name="folders">Folders</string>
</resources>
```

#### themes.xml
```xml
<resources>
    <style name="Theme.SkyView" parent="Theme.Material3.DayNight.NoActionBar">
        <item name="colorPrimary">@color/primary</item>
        <item name="colorOnPrimary">@color/on_primary</item>
        <item name="colorSecondary">@color/secondary</item>
        <item name="android:statusBarColor">@android:color/transparent</item>
        <item name="android:navigationBarColor">@android:color/transparent</item>
    </style>
</resources>
```

---

## KEY IMPLEMENTATION REQUIREMENTS

### Security Best Practices
1. **Never store passwords in plain text** - Always use PBKDF2 key derivation
2. **Use Android Keystore** for master key storage (hardware-backed)
3. **Implement proper memory wiping** - Zero out sensitive data after use
4. **Prevent screenshots** in vault mode: `window.setFlags(WindowManager.LayoutParams.FLAG_SECURE)`
5. **Rate limit authentication attempts** - Lock out after 5 failed attempts
6. **Use SQLCipher** for database encryption with separate passphrase
7. **Obfuscate database name** as "weather_cache.db" instead of "vault.db"
8. **Clear clipboard** after 30 seconds when copying passwords
9. **Enable ProGuard** for release builds to obfuscate code
10. **Disable backups** via manifest configuration

### Performance Optimizations
1. **Cache weather data** for 30 minutes to reduce API calls
2. **Use Coil** for efficient image loading and caching
3. **Lazy load thumbnails** in vault browser
4. **Use Kotlin Flow** for reactive data streams
5. **Implement pagination** for large vault item lists
6. **Use WorkManager** for reliable background weather updates
7. **Optimize database queries** with proper indexes

### User Experience
1. **Material Design 3** with dynamic colors
2. **Dark mode support** following system theme
3. **Smooth animations** using Compose animation APIs
4. **Error handling** with user-friendly messages
5. **Loading states** for all async operations
6. **Pull-to-refresh** on weather and vault screens
7. **Empty states** with helpful guidance
8. **Onboarding flow** for first-time users

### Widget Behavior
1. **Auto-update** every 30 minutes via WorkManager
2. **Manual refresh** on widget tap
3. **Different sizes**: Small (2x2), Medium (4x2), Large (4x4)
4. **Tap sequence tracking** with 5-second timeout
5. **Visual feedback** on tap (subtle animation)
6. **Fallback** to cached data when offline

---

## TESTING CHECKLIST

### Weather Features
- [ ] Weather data fetches correctly from API
- [ ] Location permission handling works
- [ ] GPS location detection functions
- [ ] Manual location search works
- [ ] Weather caching reduces API calls
- [ ] Widget displays current weather
- [ ] Widget updates every 30 minutes
- [ ] 7-day forecast displays correctly
- [ ] Offline mode shows cached data

### Vault Features
- [ ] Master password creation on first launch
- [ ] Password-based unlock works
- [ ] Biometric unlock works
- [ ] Tap sequence detection triggers unlock
- [ ] File encryption/decryption works correctly
- [ ] All 7 content types can be stored
- [ ] Folder creation and organization works
- [ ] Search functionality works
- [ ] Trash and restore work
- [ ] Starred items can be marked/unmarked
- [ ] Thumbnails generate correctly

### Security
- [ ] Database is encrypted with SQLCipher
- [ ] Files are encrypted with AES-256-GCM
- [ ] Android Keystore integration works
- [ ] Biometric authentication uses crypto objects
- [ ] Failed login attempts are rate-limited
- [ ] Screenshots are blocked in vault mode
- [ ] Clipboard auto-clears after 30 seconds
- [ ] Memory is wiped for sensitive data
- [ ] ProGuard obfuscation works in release builds

---

## BUILD AND DEPLOYMENT

### Debug Build
```bash
cd android
./gradlew assembleDebug
adb install app/build/outputs/apk/debug/app-debug.apk
```

### Release Build
```bash
# Generate signing key
keytool -genkey -v -keystore skyview-release.keystore -alias skyview -keyalg RSA -keysize 2048 -validity 10000

# Build signed APK
./gradlew assembleRelease

# Output: app/build/outputs/apk/release/app-release.apk
```

### ProGuard Rules (proguard-rules.pro)
```proguard
# Keep Hilt annotations
-keep class dagger.hilt.** { *; }
-keep class javax.inject.** { *; }

# Keep Room
-keep class * extends androidx.room.RoomDatabase
-keep @androidx.room.Entity class *
-dontwarn androidx.room.paging.**

# Keep Retrofit
-keepattributes Signature
-keepattributes *Annotation*
-keep class retrofit2.** { *; }

# Keep Gson
-keep class com.google.gson.** { *; }
-keep class * implements com.google.gson.TypeAdapter
-keep class * implements com.google.gson.TypeAdapterFactory

# Keep data models
-keep class com.skyview.weather.data.model.** { *; }
-keep class com.skyview.weather.domain.model.** { *; }

# Keep security classes
-keep class com.skyview.weather.core.security.** { *; }

# Remove logging in release
-assumenosideeffects class android.util.Log {
    public static *** d(...);
    public static *** v(...);
    public static *** i(...);
}
```

---

## FINAL NOTES

This is a **complete, production-ready specification** for the SkyView Weather app. Follow these guidelines:

1. **Implement all security features EXACTLY as specified** - This is a security-focused app
2. **Use the exact package structure** - The organization is critical for maintainability
3. **Follow Kotlin and Compose best practices** - Modern Android development standards
4. **Test encryption/decryption thoroughly** - Data integrity is paramount
5. **Ensure widget tap sequence works reliably** - This is the core concealment mechanism
6. **Implement proper error handling** - Users should never see crashes
7. **Add logging in debug builds only** - Remove all logs in release builds
8. **Test on multiple Android versions** (8.0 to 15) - Ensure compatibility
9. **Use Hilt for ALL dependency injection** - Consistent DI pattern throughout
10. **Follow Material Design 3 guidelines** - Maintain professional appearance

The app should compile and run successfully once all components are implemented according to this specification. The result will be a sophisticated, secure vault system cleverly disguised as a weather application.
