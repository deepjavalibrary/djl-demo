plugins {
    java
}

var File.text
    get() = readText()
    set(value) = writeText(value)

tasks {
    register("increaseDjlVersion") {
        doLast {
            val djlVersion = project.property("djl_version").toString()
            val versions = djlVersion.split(Regex("\\.")).toMutableList();
            versions[1] = (Integer.parseInt(versions[1]) - 1).toString()
            val previousVersion = versions.joinToString(".")
            val collection = fileTree(projectDir).filter { it.name.endsWith(".ipynb") }

            println("Upgrading to $djlVersion ...")
            for (file in collection) {
                file.text = file.text.replace(Regex("/${previousVersion}/"), "/${djlVersion}/")
                    .replace(">${previousVersion}<", ">${djlVersion}<")
                    .replace("'${previousVersion}'", "'${djlVersion}'")
                    .replace("-${previousVersion}", "-${djlVersion}")
                    .replace("_${previousVersion}", "_${djlVersion}")
                    .replace(Regex(":${previousVersion}([\"\\\\\\n])"), ":${djlVersion}\$1")
            }
        }
    }
}
