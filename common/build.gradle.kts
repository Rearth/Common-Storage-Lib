architectury {
    val enabledPlatforms: String by rootProject
    common(enabledPlatforms.split(","))
}

tasks.jar {
    manifest {
        attributes["Fabric-Loom-Remap"] = true
    }
}

tasks.compileJava {

}

dependencies {
    modCompileOnly(group = "tech.thatgravyboat", name = "commonats", version = "2.0")
}
