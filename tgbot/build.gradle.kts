import com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar

plugins {
    kotlin("jvm")
    id("application")
    id("com.gradleup.shadow")
}

dependencies{
    implementation(platform("io.github.jan-tennert.supabase:bom:3.0.3"))

    implementation("dev.inmo:tgbotapi:23.1.0")
    implementation(projects.shared)
    implementation("io.github.jan-tennert.supabase:realtime-kt")

}

application{
    mainClass = "su.pank.exhelp.tgbot.MainKt"
}

tasks.withType<ShadowJar> {
    archiveClassifier.set("") // Убираем стандартный "-all" из имени файла
    mergeServiceFiles() // Если есть сервисные файлы, они будут корректно объединены
    manifest {
        attributes(mapOf(
            "Main-Class" to application.mainClass.get()
        ))
    }
}