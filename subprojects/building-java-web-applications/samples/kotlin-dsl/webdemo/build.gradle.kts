plugins {
    war  // <1>
}

repositories {
    jcenter()
}

dependencies {
    providedCompile("javax.servlet:javax.servlet-api:3.1.0") // <2>
    testCompile("junit:junit:4.12")
}
