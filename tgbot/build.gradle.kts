
plugins {
    kotlin("jvm")
}

dependencies{
    implementation(platform("io.github.jan-tennert.supabase:bom:3.0.3"))

    implementation("dev.inmo:tgbotapi:23.1.0")
    implementation(projects.shared)
    implementation("io.github.jan-tennert.supabase:realtime-kt")

}