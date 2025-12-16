
plugins {
    kotlin("jvm")
    kotlin("plugin.serialization")
}

dependencies{
    implementation(platform("io.github.jan-tennert.supabase:bom:3.2.6"))
    implementation("io.github.jan-tennert.supabase:postgrest-kt")
    implementation("io.github.jan-tennert.supabase:realtime-kt")

    implementation("io.ktor:ktor-client-java:3.0.1")
    implementation("org.slf4j:slf4j-simple:2.0.16")

}