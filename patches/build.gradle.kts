group = "com.lopikss"

patches {
    about {
        name = "Lopikss Patches"
        description = "Android compatibility and diagnostics patches."
        source = "https://github.com/lopikss/lopikss-patches"
        author = "Lopikss"
        contact = "https://lopikss.com"
        website = "https://lopikss.com"
        license = "GPLv3"
    }
}

val patchListGeneratorClasspath = configurations.create("patchListGeneratorClasspath")

dependencies {
    compileOnly(libs.gson)
    patchListGeneratorClasspath(libs.gson)
}

tasks {
    register<JavaExec>("generatePatchesList") {
        description = "Build patch with patch list"
        dependsOn(build)
        classpath = sourceSets["main"].runtimeClasspath + patchListGeneratorClasspath
        mainClass.set("util.PatchListGeneratorKt")
    }

    publish {
        dependsOn("generatePatchesList")
    }
}
