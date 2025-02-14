package com.gafner.plugin.azd

import kotlinx.serialization.Serializable
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import org.gradle.api.DefaultTask
import org.gradle.api.file.RegularFileProperty
import org.gradle.api.provider.Property
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.OutputFile
import org.gradle.api.tasks.TaskAction
import java.net.HttpURLConnection
import java.net.URI
import java.security.MessageDigest
import java.util.*

abstract class AZDKeyTask : DefaultTask() {

    @get:Input
    abstract val version: Property<String>

    @get:Input
    abstract val azdKey: Property<String>

    @get:OutputFile
    abstract val outputFile: RegularFileProperty

    @TaskAction
    fun action() {
        println("Hello World")
    }


    @Serializable
    data class AZDBaseSetting(val key: String, val value: String) {
        override fun toString(): String {
            return Json.encodeToString(this)
        }
    }
}