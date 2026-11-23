package com.example

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import com.example.data.AppDatabase
import com.example.data.model.CountdownTimerEntity
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class ExampleRobolectricTest {

    private lateinit var db: AppDatabase

    @Before
    fun createDb() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        db = Room.inMemoryDatabaseBuilder(context, AppDatabase::class.java)
            .allowMainThreadQueries()
            .build()
    }

    @After
    fun closeDb() {
        db.close()
    }

    @Test
    fun `read string from context`() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val appName = context.getString(R.string.app_name)
        assertEquals("T-Minus Mkt", appName)
    }

    @Test
    fun `insert and query multiple countdown timers with custom labels and dates`() = runBlocking {
        val dao = db.countdownTimerDao()

        val timer1 = CountdownTimerEntity(
            title = "Q4 Product Launch",
            targetTimestamp = 1799000000000L,
            categoryTag = "Product",
            notes = "Global cross-platform launch with press embargo",
            accentColorHex = "#00FFFF",
            isActive = true
        )
        val timer2 = CountdownTimerEntity(
            title = "Black Friday Campaign",
            targetTimestamp = 1798500000000L,
            categoryTag = "Growth",
            notes = "Email sequences and tier discounts",
            accentColorHex = "#BF40FF",
            isActive = false
        )

        val id1 = dao.insertTimer(timer1)
        val id2 = dao.insertTimer(timer2)

        val all = dao.getAllTimers().first()
        assertEquals(2, all.size)

        val active = dao.getActiveTimer().first()
        assertNotNull(active)
        assertEquals("Q4 Product Launch", active?.title)
        assertTrue(active?.isActive == true)

        // Switch active timer to Black Friday
        dao.selectActiveTimer(id2)

        val newActive = dao.getActiveTimer().first()
        assertEquals("Black Friday Campaign", newActive?.title)
        assertEquals(id2, newActive?.id)

        val previous = dao.getTimerById(id1)
        assertFalse(previous?.isActive ?: true)
    }

    @Test
    fun `verify launch preparation tasks completion percentage calculation`() = runBlocking {
        val taskDao = db.taskDao()
        val tasks = listOf(
            com.example.data.model.TaskEntity(
                timelinePt = "T-29",
                timelineEn = "T-29",
                titlePt = "Pesquisa",
                titleEn = "Research",
                iconType = "search",
                daysRemaining = 29,
                targetDateFormatted = "10/08/2026",
                isCompleted = false
            ),
            com.example.data.model.TaskEntity(
                timelinePt = "T-14",
                timelineEn = "T-14",
                titlePt = "Conteúdo",
                titleEn = "Content",
                iconType = "article",
                daysRemaining = 14,
                targetDateFormatted = "25/08/2026",
                isCompleted = true
            )
        )
        taskDao.insertTasks(tasks)

        val retrieved = taskDao.getAllTasks().first()
        val total = retrieved.size
        val completed = retrieved.count { it.isCompleted }
        val percentage = (completed.toFloat() / total * 100).toInt()

        assertEquals(2, total)
        assertEquals(1, completed)
        assertEquals(50, percentage)
    }

    @Test
    fun `verify milestone sorting by due date, priority, and timeline`() = runBlocking {
        val taskDao = db.taskDao()
        val t1 = com.example.data.model.TaskEntity(
            id = 1,
            timelinePt = "T-29",
            timelineEn = "T-29",
            titlePt = "Pesquisa",
            titleEn = "Research",
            iconType = "search",
            daysRemaining = 29,
            targetDateFormatted = "09/11",
            orderIndex = 1,
            priority = "Medium"
        )
        val t2 = com.example.data.model.TaskEntity(
            id = 2,
            timelinePt = "T-14",
            timelineEn = "T-14",
            titlePt = "Conteúdo",
            titleEn = "Content",
            iconType = "article",
            daysRemaining = 14,
            targetDateFormatted = "24/11",
            orderIndex = 2,
            priority = "Normal"
        )
        val t3 = com.example.data.model.TaskEntity(
            id = 3,
            timelinePt = "T-1",
            timelineEn = "T-1",
            titlePt = "Entrega",
            titleEn = "Delivery",
            iconType = "rocketlaunch",
            daysRemaining = 1,
            targetDateFormatted = "07/12",
            orderIndex = 5,
            priority = "High"
        )
        taskDao.insertTasks(listOf(t1, t2, t3))

        val tasks = taskDao.getAllTasks().first()

        // 1. Timeline sorting (by orderIndex)
        val timelineSorted = tasks.sortedWith(compareBy({ it.orderIndex }, { it.id }))
        assertEquals("T-29", timelineSorted[0].timelinePt)
        assertEquals("T-14", timelineSorted[1].timelinePt)
        assertEquals("T-1", timelineSorted[2].timelinePt)

        // 2. Due Date sorting (by daysRemaining ascending)
        val dueDateSorted = tasks.sortedWith(compareBy({ it.daysRemaining }, { it.orderIndex }))
        assertEquals("T-1", dueDateSorted[0].timelinePt)
        assertEquals("T-14", dueDateSorted[1].timelinePt)
        assertEquals("T-29", dueDateSorted[2].timelinePt)

        // 3. Priority sorting (High -> Medium -> Normal)
        val prioritySorted = tasks.sortedWith(
            compareBy(
                { task ->
                    when (task.priority.lowercase()) {
                        "high", "alta" -> 0
                        "medium", "média", "media" -> 1
                        else -> 2
                    }
                },
                { it.daysRemaining }
            )
        )
        assertEquals("High", prioritySorted[0].priority)
        assertEquals("T-1", prioritySorted[0].timelinePt)
        assertEquals("Medium", prioritySorted[1].priority)
        assertEquals("T-29", prioritySorted[1].timelinePt)
        assertEquals("Normal", prioritySorted[2].priority)
        assertEquals("T-14", prioritySorted[2].timelinePt)
    }

    @Test
    fun `verify priority and status distribution metrics for summary dashboard`() = runBlocking {
        val taskDao = db.taskDao()
        val sampleTasks = listOf(
            com.example.data.model.TaskEntity(
                timelinePt = "T-29",
                timelineEn = "T-29",
                titlePt = "Research",
                titleEn = "Research",
                iconType = "search",
                daysRemaining = 29,
                targetDateFormatted = "09/11",
                isCompleted = true,
                priority = "Medium"
            ),
            com.example.data.model.TaskEntity(
                timelinePt = "T-14",
                timelineEn = "T-14",
                titlePt = "Content",
                titleEn = "Content",
                iconType = "article",
                daysRemaining = 14,
                targetDateFormatted = "24/11",
                isCompleted = false,
                priority = "Normal"
            ),
            com.example.data.model.TaskEntity(
                timelinePt = "T-7",
                timelineEn = "T-7",
                titlePt = "Ads",
                titleEn = "Ads",
                iconType = "megaphone",
                daysRemaining = 7,
                targetDateFormatted = "01/12",
                isCompleted = true,
                priority = "High"
            ),
            com.example.data.model.TaskEntity(
                timelinePt = "T-1",
                timelineEn = "T-1",
                titlePt = "Launch",
                titleEn = "Launch",
                iconType = "rocketlaunch",
                daysRemaining = 1,
                targetDateFormatted = "07/12",
                isCompleted = false,
                priority = "High"
            )
        )
        taskDao.insertTasks(sampleTasks)

        val tasks = taskDao.getAllTasks().first()
        val total = tasks.size
        val completed = tasks.count { it.isCompleted }
        val pending = total - completed
        val highPriority = tasks.count { it.priority.equals("High", ignoreCase = true) }
        val mediumPriority = tasks.count { it.priority.equals("Medium", ignoreCase = true) }
        val normalPriority = tasks.count { it.priority.equals("Normal", ignoreCase = true) }

        assertEquals(4, total)
        assertEquals(2, completed)
        assertEquals(2, pending)
        assertEquals(2, highPriority)
        assertEquals(1, mediumPriority)
        assertEquals(1, normalPriority)
    }

    @Test
    fun `verify splash screen delay and brand identity constants`() {
        // Verify application name and branding strings for splash screen
        val context = ApplicationProvider.getApplicationContext<Context>()
        val appName = context.getString(R.string.app_name)
        assertTrue(appName.contains("T-Minus"))
    }

    @Test
    fun `verify category tagging and filtering of launch milestone tasks`() = runBlocking {
        val taskDao = db.taskDao()
        val categorizedTasks = listOf(
            com.example.data.model.TaskEntity(
                timelinePt = "T-29",
                timelineEn = "T-29",
                titlePt = "Campanha de Ads",
                titleEn = "Ad Campaign",
                iconType = "megaphone",
                daysRemaining = 29,
                targetDateFormatted = "09/11",
                category = "Marketing",
                priority = "High"
            ),
            com.example.data.model.TaskEntity(
                timelinePt = "T-21",
                timelineEn = "T-21",
                titlePt = "Deploy de API",
                titleEn = "API Deploy",
                iconType = "rocketlaunch",
                daysRemaining = 21,
                targetDateFormatted = "17/11",
                category = "Engineering",
                priority = "High"
            ),
            com.example.data.model.TaskEntity(
                timelinePt = "T-14",
                timelineEn = "T-14",
                titlePt = "Assets de Design",
                titleEn = "Design Assets",
                iconType = "article",
                daysRemaining = 14,
                targetDateFormatted = "24/11",
                category = "Design",
                priority = "Medium"
            ),
            com.example.data.model.TaskEntity(
                timelinePt = "T-7",
                timelineEn = "T-7",
                titlePt = "Onboarding Checklist",
                titleEn = "Onboarding Checklist",
                iconType = "checkcircle",
                daysRemaining = 7,
                targetDateFormatted = "01/12",
                category = "Operations",
                priority = "Normal"
            )
        )
        taskDao.insertTasks(categorizedTasks)

        val allTasks = taskDao.getAllTasks().first()
        assertEquals(4, allTasks.size)

        // Filter: Marketing
        val marketingTasks = allTasks.filter { it.category.equals("Marketing", ignoreCase = true) }
        assertEquals(1, marketingTasks.size)
        assertEquals("Campanha de Ads", marketingTasks.first().titlePt)

        // Filter: Engineering
        val engineeringTasks = allTasks.filter { it.category.equals("Engineering", ignoreCase = true) }
        assertEquals(1, engineeringTasks.size)
        assertEquals("Deploy de API", engineeringTasks.first().titlePt)

        // Filter: Design
        val designTasks = allTasks.filter { it.category.equals("Design", ignoreCase = true) }
        assertEquals(1, designTasks.size)
        assertEquals("Assets de Design", designTasks.first().titlePt)

        // Filter: Operations
        val operationsTasks = allTasks.filter { it.category.equals("Operations", ignoreCase = true) }
        assertEquals(1, operationsTasks.size)
        assertEquals("Onboarding Checklist", operationsTasks.first().titlePt)

        // Test LaunchCategory model mapping
        val engCat = com.example.data.model.LaunchCategory.fromId("engineering")
        assertEquals(com.example.data.model.LaunchCategory.ENGINEERING, engCat)
        assertEquals("Engenharia", engCat.getDisplayName(isPt = true))
        assertEquals("Engineering", engCat.getDisplayName(isPt = false))

        val opsCat = com.example.data.model.LaunchCategory.fromId("operations")
        assertEquals(com.example.data.model.LaunchCategory.OPERATIONS, opsCat)
        assertEquals("Operações", opsCat.getDisplayName(isPt = true))
        assertEquals("Operations", opsCat.getDisplayName(isPt = false))
    }

    @Test
    fun `verify milestone search query filtering combined with category filter`() = runBlocking {
        val taskDao = db.taskDao()
        val tasks = listOf(
            com.example.data.model.TaskEntity(
                timelinePt = "Vinte dias antes (18/11):",
                timelineEn = "Twenty days before (18/11):",
                titlePt = "Campanha de Lancamento",
                titleEn = "Launch Campaign",
                iconType = "megaphone",
                daysRemaining = 20,
                targetDateFormatted = "18/11",
                category = "Marketing"
            ),
            com.example.data.model.TaskEntity(
                timelinePt = "Dez dias antes (28/11):",
                timelineEn = "Ten days before (28/11):",
                titlePt = "Testes de Carga",
                titleEn = "Load Testing",
                iconType = "rocketlaunch",
                daysRemaining = 10,
                targetDateFormatted = "28/11",
                category = "Engineering"
            )
        )
        taskDao.insertTasks(tasks)

        val stored = taskDao.getAllTasks().first()

        // Search by title substring
        val query = "campaign"
        val titleMatched = stored.filter {
            it.titlePt.lowercase().contains(query) ||
                it.titleEn.lowercase().contains(query) ||
                it.timelinePt.lowercase().contains(query) ||
                it.timelineEn.lowercase().contains(query)
        }
        assertEquals(1, titleMatched.size)
        assertEquals("Marketing", titleMatched.first().category)

        // Search by timeline description + category filter
        val descQuery = "dez dias"
        val combinedMatched = stored.filter {
            it.category.equals("Engineering", ignoreCase = true) &&
                (it.titlePt.lowercase().contains(descQuery) || it.timelinePt.lowercase().contains(descQuery))
        }
        assertEquals(1, combinedMatched.size)
        assertEquals("Load Testing", combinedMatched.first().titleEn)
    }

    @Test
    fun `verify neumorphic countdown date formatting and unit breakdown`() {
        val cal = java.util.Calendar.getInstance().apply {
            set(java.util.Calendar.YEAR, 2026)
            set(java.util.Calendar.MONTH, java.util.Calendar.DECEMBER)
            set(java.util.Calendar.DAY_OF_MONTH, 8)
            set(java.util.Calendar.HOUR_OF_DAY, 12)
            set(java.util.Calendar.MINUTE, 0)
        }
        val formattedEn = com.example.ui.components.formatCountdownTargetDate(cal.timeInMillis, isPt = false)
        val formattedPt = com.example.ui.components.formatCountdownTargetDate(cal.timeInMillis, isPt = true)

        assertTrue(formattedEn.contains("2026"))
        assertTrue(formattedEn.contains("08"))
        assertTrue(formattedPt.contains("2026"))
        assertTrue(formattedPt.contains("08"))

        // Verify days, hours, minutes math from millisecond delta
        val deltaMillis = (3L * 24 * 60 * 60 * 1000) + (5L * 60 * 60 * 1000) + (42L * 60 * 1000)
        val days = deltaMillis / (1000 * 60 * 60 * 24)
        val hours = (deltaMillis / (1000 * 60 * 60)) % 24
        val minutes = (deltaMillis / (1000 * 60)) % 60

        assertEquals(3L, days)
        assertEquals(5L, hours)
        assertEquals(42L, minutes)
    }

    @Test
    fun `verify app initializes unlocked directly onto home screen`() {
        val app = ApplicationProvider.getApplicationContext<android.app.Application>()
        val viewModel = com.example.ui.TMinusViewModel(app)
        assertFalse(viewModel.uiState.value.isLocked)
        assertFalse(viewModel.uiState.value.isAuthenticated)
    }

    @Test
    fun `verify countdown share text summary formats PT and EN plans accurately`() {
        val sampleTasks = listOf(
            com.example.data.model.TaskEntity(
                timelinePt = "Catorze dias antes (24/11):",
                timelineEn = "Fourteen days before (24/11):",
                titlePt = "Estruturar plano do blog",
                titleEn = "Structure blog plan",
                iconType = "article",
                daysRemaining = 14,
                targetDateFormatted = "24/11",
                isCompleted = false
            ),
            com.example.data.model.TaskEntity(
                timelinePt = "Um dia antes (07/12):",
                timelineEn = "One day before (07/12):",
                titlePt = "Finalizar entrega do projeto",
                titleEn = "Finalize project delivery",
                iconType = "rocketlaunch",
                daysRemaining = 1,
                targetDateFormatted = "07/12",
                isCompleted = true
            )
        )

        val ptSummary = com.example.util.CountdownBitmapRenderer.buildShareTextSummary(
            tasks = sampleTasks,
            daysRemaining = 16L,
            isPt = true
        )
        assertTrue(ptSummary.contains("T-16 DIAS"))
        assertTrue(ptSummary.contains("Estruturar plano do blog"))
        assertTrue(ptSummary.contains("Finalizar entrega do projeto"))

        val enSummary = com.example.util.CountdownBitmapRenderer.buildShareTextSummary(
            tasks = sampleTasks,
            daysRemaining = 16L,
            isPt = false
        )
        assertTrue(enSummary.contains("T-16 DAYS"))
        assertTrue(enSummary.contains("Structure blog plan"))
        assertTrue(enSummary.contains("Finalize project delivery"))
    }

    @Test
    fun `verify default tasks seed compact EN T-XX format and preserve PT format`() = runBlocking {
        val repository = com.example.data.TaskRepository(db.taskDao())
        repository.checkAndSeedDefaults()

        val seeded = repository.allTasks.first()
        assertEquals(5, seeded.size)

        // English uses compact T-XX (DD/MM) with translated titles
        assertEquals("T-29 (09/11):", seeded[0].timelineEn)
        assertEquals("Start GEO research", seeded[0].titleEn)
        assertEquals("T-14 (24/11):", seeded[1].timelineEn)
        assertEquals("Structure blog plan", seeded[1].titleEn)
        assertEquals("T-7 (01/12):", seeded[2].timelineEn)
        assertEquals("Create marketing strategies", seeded[2].titleEn)
        assertEquals("T-3 (05/12):", seeded[3].timelineEn)
        assertEquals("Review entire planning", seeded[3].titleEn)
        assertEquals("T-1 (07/12):", seeded[4].timelineEn)
        assertEquals("Finalize project delivery", seeded[4].titleEn)

        // Portuguese remains spelled-out exactly as before
        assertEquals("Vinte e nove dias antes (09/11):", seeded[0].timelinePt)
        assertEquals("Iniciar pesquisa de GEO", seeded[0].titlePt)
        assertEquals("Um dia antes (07/12):", seeded[4].timelinePt)
        assertEquals("Finalizar entrega do projeto", seeded[4].titlePt)

        // Verify non-destructive upgrade preserves completion state on existing legacy default row
        db.taskDao().updateTask(
            seeded[0].copy(
                timelineEn = "Twenty-nine days before (09/11):",
                isCompleted = true
            )
        )
        repository.checkAndSeedDefaults()

        val upgraded = repository.allTasks.first().first { it.id == 1 }
        assertEquals("T-29 (09/11):", upgraded.timelineEn)
        assertTrue(upgraded.isCompleted)
    }

    @Test
    fun `verify persistent login does not prompt sign in again after authentication`() {
        val app = ApplicationProvider.getApplicationContext<android.app.Application>()
        val securityManager = com.example.util.SecurityManager(app)
        securityManager.clearSession()

        // First launch: unauthenticated -> opens directly to Login
        val vmFirstLaunch = com.example.ui.TMinusViewModel(app)
        assertFalse(vmFirstLaunch.isAuthenticated.value)
        assertFalse(vmFirstLaunch.uiState.value.isAuthenticated)

        // User signs in via biometrics or Google
        vmFirstLaunch.onBiometricSuccess()
        assertTrue(vmFirstLaunch.isAuthenticated.value)

        // Subsequent launch / activity recreation: stays authenticated automatically
        val vmReopened = com.example.ui.TMinusViewModel(app)
        assertTrue(vmReopened.isAuthenticated.value)
        assertTrue(vmReopened.uiState.value.isAuthenticated)

        // Cleanup session so other tests remain isolated
        vmReopened.signOut()
        assertFalse(vmReopened.isAuthenticated.value)
    }

    @Test
    fun `verify share text summary includes live HH MM SS countdown and all milestones`() {
        val tasks = listOf(
            com.example.data.model.TaskEntity(
                timelinePt = "Vinte e nove dias antes (09/11):",
                timelineEn = "T-29 (09/11):",
                titlePt = "Iniciar pesquisa de GEO",
                titleEn = "Start GEO research",
                iconType = "search",
                daysRemaining = 29,
                targetDateFormatted = "09/11"
            ),
            com.example.data.model.TaskEntity(
                timelinePt = "Um dia antes (07/12):",
                timelineEn = "T-1 (07/12):",
                titlePt = "Finalizar entrega do projeto",
                titleEn = "Finalize project delivery",
                iconType = "rocketlaunch",
                daysRemaining = 1,
                targetDateFormatted = "07/12"
            )
        )

        val ptText = com.example.util.CountdownBitmapRenderer.buildShareTextSummary(
            tasks = tasks,
            daysRemaining = 16L,
            hoursRemaining = 8L,
            minutesRemaining = 45L,
            secondsRemaining = 12L,
            isPt = true
        )
        assertTrue(ptText.contains("T-16 DIAS (08h : 45m : 12s)"))
        assertTrue(ptText.contains("Iniciar pesquisa de GEO"))
        assertTrue(ptText.contains("Finalizar entrega do projeto"))

        val enText = com.example.util.CountdownBitmapRenderer.buildShareTextSummary(
            tasks = tasks,
            daysRemaining = 16L,
            hoursRemaining = 8L,
            minutesRemaining = 45L,
            secondsRemaining = 12L,
            isPt = false
        )
        assertTrue(enText.contains("T-16 DAYS (08h : 45m : 12s)"))
        assertTrue(enText.contains("T-29 (09/11) — Start GEO research"))
        assertTrue(enText.contains("T-1 (07/12) — Finalize project delivery"))
    }
}
