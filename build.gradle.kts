import de.qualersoft.robotframework.gradleplugin.tasks.LibdocTask
import de.qualersoft.robotframework.gradleplugin.tasks.RunRobotTask
import de.qualersoft.robotframework.gradleplugin.tasks.TestdocTask
import de.qualersoft.robotframework.gradleplugin.tasks.BasicRobotFrameworkTask
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

plugins {
  java
  `maven-publish`
  id("de.qualersoft.robotframework")
}

group = "de.arvato.systems.aep"

// Defines the root of the robot test suite structure.
// For reuse in tasks
val robotTestSourceRoot = "src/test/robot"
sourceSets {
  test {
    resources.srcDir(robotTestSourceRoot)
  }
}

repositories {
  mavenCentral()

  maven {
    name = "AEP-GH"
    url = uri("https://maven.pkg.github.com/arvato-energy-platform/*")
    credentials {
      username = project.findProperty("gh.aep.repo.user") as String? ?: System.getenv("GH_REPO_USERNAME")
      password = project.findProperty("gh.aep.repo.token") as String? ?: System.getenv("GH_REPO_TOKEN")
    }
  }

  ivy {
    name = "edge"
    url = uri("https://msedgedriver.azureedge.net")
    patternLayout {
      artifact("/[revision]/[artifact](_[classifier]).[ext]")
    }
    metadataSources { artifact() }
  }

  ivy {
    name = "chrome"
    url = uri("https://chromedriver.storage.googleapis.com/")
    patternLayout {
      artifact("/[revision]/[artifact](_[classifier]).[ext]")
    }
    metadataSources { artifact() }
  }
}

val isCI = System.getenv().containsKey("CI")
val driver: Configuration by configurations.creating
dependencies {

  driver(group = "", name = "edgedriver", version = "97.0.1072.62", classifier = "win64", ext = "zip")
  if (isCI) {
    // for unix-ci-runs
    driver(group = "", name = "chromedriver", version = "95.0.4638.69", classifier = "linux64", ext = "zip")
  } else {
    driver(group = "", name = "chromedriver", version = "96.0.4664.45", classifier = "win32", ext = "zip")
  }

  robot(group = "de.arvato.systems.aep", name = "aep-portal-testautomation", version = "0.0.1-SNAPSHOT")

  implementation(group = "de.arvato.systems.aep", name = "aep-testautomation-sdk", version = "0.0.1-SNAPSHOT")

  implementation(group = "org.junit.jupiter", name = "junit-jupiter-api", version = "5.8.2")

  testImplementation(group = "org.springframework.boot", name = "spring-boot-starter-test", version = "2.6.3")
  testRuntimeOnly(group = "org.junit.jupiter", name = "junit-jupiter-engine", version = "5.8.2")
}

robotframework {
  val timestamp = objects.property<String>().value("").map {
    LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmm"))
  }
  val reports = project.layout.buildDirectory.dir("robot-reports").get().dir(timestamp)
  rebot {
    outputDir.set(reports)
  }
  robot {
    outputDir.set(reports)
    // always exclude test case id tags from status-report.
    tagStatExclude = mutableListOf("TID: *")

    // set the RESOURCE_DIR property to make referencing to resource files easier
    variables.put("RESOURCE_DIR", projectDir.resolve("src/test/resources").absolutePath)
  }
  libdoc {
    version.set(project.version.toString())
  }
}

tasks {
  register<RunRobotTask>("allTests") {
    description = "Executes all robot suites under $robotTestSourceRoot"
    robot {
      name.set("All tests")
      val env: String? = System.getenv("env")
      env?.let {
        if ("local" == it) {
          // In local run we exclude tests tagged with 'require-portal'
          exclude = mutableListOf("requires-portal")
        }
      }
    }
    sources = files(robotTestSourceRoot)
  }

  // Task specially to run with 'local' environment excluding tests tagged with `requires-portal`
  register<RunRobotTask>("runLocal") {
    description = "Executes only this tests that can run locally"
    systemProperty("env", "local")
    robot {
      exclude = mutableListOf("requires-portal")
    }
    sources = files(robotTestSourceRoot)
  }

  register<RunRobotTask>("dryRun") {
    description =
      "Run robot framework in dry run mod. Useful to check for typos in keywords or library initialization issues."
    robot {
      dryrun.set(true)
    }
    sources = files(robotTestSourceRoot)
  }

  // only execute tests tagged with 'DEBUG'
  register<RunRobotTask>("debugTestCase") {
    description = "Executs all tests tagged with 'DEBUG'"
    robot {
      include = mutableListOf("DEBUG")
    }
    sources = files(robotTestSourceRoot)
  }

  withType<RunRobotTask>().configureEach {
    // be default we put all run-tasks in the robot group
    group = "robot"

    // >> TODO remove me!!! Just to make showcases work
    if (null == System.getenv("env")) {
      systemProperty("env", "stage")
    }
    // << end

    // set the
    systemProperty("RESOURCE_DIR", "./src/test/resource")

    // dry run is special it only depends on jar
    if (name.contains("dryRun")) {
      dependsOn(jar)
    } else {
      dependsOn(jar, copyDriver)
    }
    // We want to rerun test tasks as often as required
    outputs.upToDateWhen { false }
  }

  // TODO set to the libraries name.
  //  This is the name of the class extending from AepRobotLib. In this template 'TemplateLib'
  val libName = "TemplateLib"
  register<LibdocTask>("robotLibdocHtml") {
    description = "Creates the library documentation as html"
    libdoc {
      libraryOrResourceFile = libName
      outputFile.set(outputDirectory.file("$libName.html"))
    }
    dependsOn(jar)
  }

  register<LibdocTask>("robotLibdoc") {
    description = "Creates the library documentation in 'libspec' format. For use with Robocorps 'Robot Framework Language Server' plugin."
    libdoc {
      libraryOrResourceFile = libName
      // following two settings are important to make Robot Framework Language Server plugin work proper!
      format.set("libspec")
      outputFile.set(outputDirectory.file("$libName.libspec"))
    }
    dependsOn(jar)
  }

  register<TestdocTask>("robotTestdoc") {
    group = "documentation"
    description = "Creates the documentation for the robot tests"
    sources = files(robotTestSourceRoot)
  }

  // open java packages to reduce "illegal access" exceptions when jython access java classes
  withType<BasicRobotFrameworkTask>().configureEach {
    jvmArgs(listOf(
      "--add-opens", "java.base/jdk.internal.misc=ALL-UNNAMED"
    ))
  }
}

tasks.test {
  useJUnitPlatform()
}

val copyDriver = tasks.register<Copy>("copyDriver") {
  description = "Copy the selenium driver specified in 'driver' dependency into the 'drivers' buildDir folder."
  driver.forEach {
    from(zipTree(it))
    into("$buildDir/drivers")
  }
}

java {
  sourceCompatibility = JavaVersion.VERSION_17
  targetCompatibility = JavaVersion.VERSION_17
}

publishing {
  publications {
    create<MavenPublication>("javaLib") {
      from(components["java"])
    }
  }
  repositories {
    maven {
      name = "GitHubPackages"
      // TODO Change to your repository
      url = uri("https://maven.pkg.github.com/arvato-energy-platform/aep-testautomation-template")
      credentials {
        username = System.getenv("USERNAME")
        password = System.getenv("TOKEN")
      }
    }
  }
}
