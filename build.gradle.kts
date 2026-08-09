val javaFundamentalsBuild = gradle.includedBuild("kafka-fundamentals-java")

tasks.register("buildJavaFundamentals") {
  group = "build"
  description = "Builds the Java fundamentals included build."
  dependsOn(javaFundamentalsBuild.task(":build"))
}

tasks.register("build") {
  group = "build"
  description = "Builds all registered projects in the repository workspace."
  dependsOn(tasks.named("buildJavaFundamentals"))
}
