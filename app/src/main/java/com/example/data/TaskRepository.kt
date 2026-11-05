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
                    timelineEn = "Twenty-nine days before (09/11):",
                    titlePt = "Iniciar pesquisa de GEO",
                    titleEn = "Start GEO research",
                    iconType = "search",
                    daysRemaining = 29,
                    targetDateFormatted = "09/11",
                    isCompleted = false,
                    isCustom = false,
                    orderIndex = 1
                ),
                TaskEntity(
                    id = 2,
                    timelinePt = "Catorze dias antes (24/11):",
                    timelineEn = "Fourteen days before (24/11):",
                    titlePt = "Estruturar plano do blog",
                    titleEn = "Structure blog plan",
                    iconType = "article",
                    daysRemaining = 14,
                    targetDateFormatted = "24/11",
                    isCompleted = false,
                    isCustom = false,
                    orderIndex = 2
                ),
                TaskEntity(
                    id = 3,
                    timelinePt = "Sete dias antes (01/12):",
                    timelineEn = "Seven days before (01/12):",
                    titlePt = "Criar estratégias de marketing",
                    titleEn = "Create marketing strategies",
                    iconType = "megaphone",
                    daysRemaining = 7,
                    targetDateFormatted = "01/12",
                    isCompleted = false,
                    isCustom = false,
                    orderIndex = 3
                ),
                TaskEntity(
                    id = 4,
                    timelinePt = "Três dias antes (05/12):",
                    timelineEn = "Three days before (05/12):",
                    titlePt = "Revisar todo o planejamento",
                    titleEn = "Review entire planning",
                    iconType = "checkcircle",
                    daysRemaining = 3,
                    targetDateFormatted = "05/12",
                    isCompleted = false,
                    isCustom = false,
                    orderIndex = 4
                ),
                TaskEntity(
                    id = 5,
                    timelinePt = "Um dia antes (07/12):",
                    timelineEn = "One day before (07/12):",
                    titlePt = "Finalizar entrega do projeto",
                    titleEn = "Finalize project delivery",
                    iconType = "rocketlaunch",
                    daysRemaining = 1,
                    targetDateFormatted = "07/12",
                    isCompleted = false,
                    isCustom = false,
                    orderIndex = 5
                )
            )
            taskDao.insertTasks(defaultTasks)
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
        targetDateFormatted: String
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
            orderIndex = 100 - daysRemaining
        )
        taskDao.insertTask(newTask)
    }

    suspend fun deleteTask(id: Int) = withContext(Dispatchers.IO) {
        taskDao.deleteTaskById(id)
    }
}
