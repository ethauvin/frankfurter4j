/*
 * Frankfurter4jBuild.java
 *
 * Copyright (c) 2025-2026 Erik C. Thauvin (erik@thauvin.net)
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *
 *   Redistributions of source code must retain the above copyright notice, this
 *   list of conditions and the following disclaimer.
 *
 *   Redistributions in binary form must reproduce the above copyright notice,
 *   this list of conditions and the following disclaimer in the documentation
 *   and/or other materials provided with the distribution.
 *
 *   Neither the name of this project nor the names of its contributors may be
 *   used to endorse or promote products derived from this software without
 *   specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE LIABLE
 * FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL
 * DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR
 * SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER
 * CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY,
 * OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE
 * OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */

package net.thauvin.erik.frankfurter;

import rife.bld.BuildCommand;
import rife.bld.Project;
import rife.bld.extension.*;
import rife.bld.extension.tools.IOTools;
import rife.bld.publish.PomBuilder;
import rife.bld.publish.PublishDeveloper;
import rife.bld.publish.PublishLicense;
import rife.bld.publish.PublishScm;
import rife.tools.exceptions.FileUtilsErrorException;

import java.io.File;
import java.util.List;

import static rife.bld.dependencies.Repository.*;
import static rife.bld.dependencies.Scope.*;

public class Frankfurter4jBuild extends Project {

    final File generatedDirectory = new File(srcDirectory(), "generated");

    final PmdOperation pmdOp = new PmdOperation()
            .failOnViolation(true)
            .ruleSets("config/pmd.xml")
            .fromProject(this);
    final File testResultsDirectory = IOTools.resolveFile(buildDirectory(), "test-results", "test");

    public Frankfurter4jBuild() {
        pkg = "net.thauvin.erik";
        name = "Frankfurter4j";
        archiveBaseName = "frankfurter4j";
        version = version(1, 0, 0, "SNAPSHOT");

        javaRelease = 17;

        downloadSources = true;
        autoDownloadPurge = true;

        repositories = List.of(MAVEN_LOCAL, MAVEN_CENTRAL, RIFE2_SNAPSHOTS);

        var junit = version(6, 1, 1);
        var gson = version(2, 14, 0);
        scope(compile)
                .include(dependency("com.uwyn", "urlencoder",
                        version(1, 3, 5)))
                .include(dependency("com.google.code.gson", "gson", gson));
        scope(provided)
                .include(dependency("com.github.spotbugs", "spotbugs-annotations",
                        version(4, 10, 2)));
        scope(test)
                .include(dependency("com.uwyn.rife2", "bld-extensions-testing-helpers",
                        version(1, 1, 0, "SNAPSHOT")))
                .include(dependency("org.mockito", "mockito-core",
                        version(5, 23, 0)))
                .include(dependency("org.junit.jupiter", "junit-jupiter", junit))
                .include(dependency("org.junit.platform", "junit-platform-console-standalone", junit));

        publishOperation()
                .repository(version.isSnapshot() ? repository(CENTRAL_SNAPSHOTS.location())
                        .withCredentials(property("central.user"), property("central.password"))
                        : repository(CENTRAL_RELEASES.location())
                        .withCredentials(property("central.user"), property("central.password")))
                .repository(repository("github"))
                .info()
                .groupId(pkg)
                .artifactId(archiveBaseName)
                .description("Retrieve reference exchange rates from Frankfurter.dev")
                .url("https://github.com/ethauvin/" + archiveBaseName)
                .developer(new PublishDeveloper()
                        .id("ethauvin")
                        .name("Erik C. Thauvin")
                        .email("erik@thauvin.net")
                        .url("https://erik.thauvin.net/")
                )
                .license(new PublishLicense()
                        .name("BSD 3-Clause")
                        .url("https://opensource.org/licenses/BSD-3-Clause")
                )
                .scm(new PublishScm()
                        .connection("scm:git:https://github.com/ethauvin/" + archiveBaseName + ".git")
                        .developerConnection("scm:git:git@github.com:ethauvin/" + archiveBaseName + ".git")
                        .url("https://github.com/ethauvin/" + archiveBaseName)
                )
                .signKey(property("sign.key"))
                .signPassphrase(property("sign.passphrase"));

        compileOperation().mainSourceDirectories(generatedDirectory);

        javadocOperation()
                .javadocOptions()
                .docTitle("Frankfurter4j API Specification")
                .tag("apiNote", "a", "API Note:")
                .author()
                .link("https://www.javadoc.io/doc/com.google.code.gson/gson/" + gson)
                .link("https://findbugs.sourceforge.net/api/");
    }

    @Override
    public void compile() throws Exception {
        genver();
        super.compile();
    }

    @Override
    public void test() throws Exception {
        var op = testOperation().fromProject(this);
        op.testToolOptions().reportsDir(testResultsDirectory);
        op.execute();
    }

    @Override
    public void publish() throws Exception {
        super.publish();
        pomRoot();
    }

    @Override
    public void publishLocal() throws Exception {
        super.publishLocal();
        pomRoot();
    }

    public static void main(String[] args) {
        new Frankfurter4jBuild().start(args);
    }

    @BuildCommand(summary = "Generates version class")
    public void genver() throws Exception {
        new GeneratedVersionOperation()
                .fromProject(this)
                .directory(generatedDirectory)
                .packageName(pkg + ".frankfurter.internal")
                .classTemplate("GeneratedVersion.txt")
                .generateAnnotation(true)
                .execute();
    }

    @BuildCommand(summary = "Generates JaCoCo Reports")
    public void jacoco() throws Exception {
        var op = new JacocoReportOperation().fromProject(this);
        op.testToolOptions("--reports-dir=" + testResultsDirectory.getAbsolutePath());
        op.execute();
    }

    @BuildCommand(summary = "Runs PMD analysis")
    public void pmd() throws Exception {
        pmdOp.execute();
    }

    @BuildCommand(value = "pmd-cli", summary = "Runs PMD analysis (CLI)")
    public void pmdCli() throws Exception {
        pmdOp.includeLineNumber(false).execute();
    }

    @BuildCommand(value = "pom-root", summary = "Generates the POM file in the root directory")
    public void pomRoot() throws FileUtilsErrorException {
        PomBuilder.generateInto(publishOperation().fromProject(this).info(), dependencies(),
                new File("pom.xml"));
    }

    @BuildCommand(summary = "Runs the JUnit reporter")
    public void reporter() throws Exception {
        new JUnitReporterOperation()
                .fromProject(this)
                .failOnSummary(true)
                .execute();
    }

    @BuildCommand(summary = "Runs SpotBugs on this project")
    public void spotbugs() throws Exception {
        new SpotBugsOperation()
                .fromProject(this)
                .home("/opt/spotbugs")
                .execute();
    }
}
