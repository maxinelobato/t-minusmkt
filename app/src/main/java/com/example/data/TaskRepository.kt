package com.example.data

import com.example.data.dao.TaskDao
import com.example.data.model.TaskEntity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.withContext

class TaskRepository(private val taskDao: TaskDao) {

    val allTasks: Flow<List<TaskEntity>> = taskDao.getAllTasks()

    suspend fun checkAndSeedDefaults() = withContext(Dispatchers.IO) {
        val count = taskDao.getTaskCount()
        if (count == 0) {
            val defaultTasks = listOf(
                TaskEntity(
                    id = 1,
                    timelinePt = "Vinte e nove dias antes (09/11):",
                    timelineEn = "T-29 (09/11):",
                    titlePt = "Iniciar pesquisa de GEO",
                    titleEn = "Start GEO research",
                    iconType = "search",
                    daysRemaining = 29,
                    targetDateFormatted = "09/11",
                    isCompleted = false,
                    isCustom = false,
                    orderIndex = 1,
                    priority = "Medium",
                    category = "Marketing"
                ),
                TaskEntity(
                    id = 2,
                    timelinePt = "Catorze dias antes (24/11):",
                    timelineEn = "T-14 (24/11):",
                    titlePt = "Estruturar plano do blog",
                    titleEn = "Structure blog plan",
                    iconType = "article",
                    daysRemaining = 14,
                    targetDateFormatted = "24/11",
                    isCompleted = false,
                    isCustom = false,
                    orderIndex = 2,
                    priority = "Normal",
                    category = "Design"
                ),
                TaskEntity(
                    id = 3,
                    timelinePt = "Sete dias antes (01/12):",
                    timelineEn = "T-7 (01/12):",
                    titlePt = "Criar estratégias de marketing",
                    titleEn = "Create marketing strategies",
                    iconType = "megaphone",
                    daysRemaining = 7,
                    targetDateFormatted = "01/12",
                    isCompleted = false,
                    isCustom = false,
                    orderIndex = 3,
                    priority = "High",
                    category = "Marketing"
                ),
                TaskEntity(
                    id = 4,
                    timelinePt = "Três dias antes (05/12):",
                    timelineEn = "T-3 (05/12):",
                    titlePt = "Revisar todo o planejamento",
                    titleEn = "Review entire planning",
                    iconType = "checkcircle",
                    daysRemaining = 3,
                    targetDateFormatted = "05/12",
                    isCompleted = false,
                    isCustom = false,
                    orderIndex = 4,
                    priority = "High",
                    category = "Operations"
                ),
                TaskEntity(
                    id = 5,
                    timelinePt = "Um dia antes (07/12):",
                    timelineEn = "T-1 (07/12):",
                    titlePt = "Finalizar entrega do projeto",
                    titleEn = "Finalize project delivery",
                    iconType = "rocketlaunch",
                    daysRemaining = 1,
                    targetDateFormatted = "07/12",
                    isCompleted = false,
                    isCustom = false,
                    orderIndex = 5,
                    priority = "High",
                    category = "Engineering"
                )
            )
            taskDao.insertTasks(defaultTasks)
        } else {
            // Non-destructively upgrade existing default tasks to compact EN T-XX (DD/MM) format
            taskDao.updateDefaultTimelineEn(1, "T-29 (09/11):")
            taskDao.updateDefaultTimelineEn(2, "T-14 (24/11):")
            taskDao.updateDefaultTimelineEn(3, "T-7 (01/12):")
            taskDao.updateDefaultTimelineEn(4, "T-3 (05/12):")
            taskDao.updateDefaultTimelineEn(5, "T-1 (07/12):")
        }
    }

    suspend fun toggleTask(id: Int, currentCompleted: Boolean) = withContext(Dispatchers.IO) {
        taskDao.setTaskCompleted(id, !currentCompleted)
    }

    suspend fun addTask(
        timelinePt: String,
        timelineEn: String,
        titlePt: String,
        titleEn: String,
        iconType: String,
        daysRemaining: Int,
        targetDateFormatted: String,
        priority: String = "Normal",
        category: String = "Marketing"
    ) = withContext(Dispatchers.IO) {
        val newTask = TaskEntity(
            timelinePt = timelinePt,
            timelineEn = timelineEn,
            titlePt = titlePt,
            titleEn = titleEn,
            iconType = iconType,
            daysRemaining = daysRemaining,
            targetDateFormatted = targetDateFormatted,
            isCompleted = false,
            isCustom = true,
            orderIndex = 100 - daysRemaining,
            priority = priority,
            category = category
        )
        taskDao.insertTask(newTask)
    }

    suspend fun deleteTask(id: Int) = withContext(Dispatchers.IO) {
        taskDao.deleteTaskById(id)
    }

    suspend fun updateTask(task: TaskEntity) = withContext(Dispatchers.IO) {
        taskDao.updateTask(task)
    }

    suspend fun insertTask(task: TaskEntity) = withContext(Dispatchers.IO) {
        taskDao.insertTask(task)
    }
}
