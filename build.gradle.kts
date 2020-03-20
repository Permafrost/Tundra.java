/*
 * The MIT License (MIT)
 *
 * Copyright (c) 2020 Lachlan Dowding
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

plugins {
    `java-library`
}

java {
    sourceCompatibility = JavaVersion.VERSION_1_6
    targetCompatibility = JavaVersion.VERSION_1_6
}

repositories {
    jcenter()
}

dependencies {
    api("javax.activation:javax.activation-api:1.2.0")
    api("com.googlecode.htmlcompressor:htmlcompressor:1.5.2")
    api("com.sun.xml.bind:jaxb-impl:2.3.2")
    api("com.sun.xml.ws:jaxws-ri:2.3.2-1")
    api("javax.xml.bind:jaxb-api:2.3.1")
    api("log4j:log4j:1.2.17")
    api("org.apache.axis2:axis2-kernel:1.7.9")
    api("org.apache.commons:commons-collections4:4.1")
    api("org.apache.commons:commons-compress:1.12")
    api("org.apache.commons:commons-csv:1.4")
    api("org.apache.poi:poi-ooxml-schemas:3.17")
    api("org.apache.poi:poi-ooxml:3.17")
    api("org.apache.poi:poi:3.17")
    api("org.apache.santuario:xmlsec:1.5.8")
    api("org.glassfish:javax.json:1.1.4")
    api("org.hjson:hjson:2.1.1")
    api("org.jscience:jscience:4.3.1")
    api("org.unbescape:unbescape:1.1.6.RELEASE")
    api("org.yaml:snakeyaml:1.18")
    api("oro:oro:2.0.8")
    api(files("libs/wm-isclient.jar", "libs/wm-isserver.jar"))

    testImplementation("junit:junit:4.13")
    testImplementation("org.hamcrest:hamcrest-all:1.3")
}

tasks.withType<JavaCompile> {
    options.encoding = "UTF-8"
}
