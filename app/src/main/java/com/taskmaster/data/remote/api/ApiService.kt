package com.taskmaster.data.remote.api

import com.taskmaster.data.remote.dto.TaskDto
import retrofit2.Response
import retrofit2.http.*

interface ApiService {
    @GET("todos")
    suspend fun getTasks(): List<TaskDto>

    @POST("todos")
    suspend fun createTask(@Body taskDto: TaskDto): TaskDto

    @PUT("todos/{id}")
    suspend fun updateTask(
        @Path("id") id: Int,
        @Body taskDto: TaskDto
    ): TaskDto

    @DELETE("todos/{id}")
    suspend fun deleteTask(@Path("id") id: Int): Response<Unit>
}
