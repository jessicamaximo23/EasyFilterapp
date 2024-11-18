buildscript {
    repositories {
        google()       // Repositório do Google
        mavenCentral() // Repositório Maven Central
    }
    dependencies {
        classpath("com.android.tools.build:gradle:8.6.0")
        classpath("com.google.gms:google-services:4.3.15")
    }
}

allprojects {
    repositories {
       // Repositório Maven Central
    }
}
