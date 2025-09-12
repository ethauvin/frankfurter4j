/*
 * Frankfurter4JBuild.java
 *
 * Copyright 2025 Erik C. Thauvin (erik@thauvin.net)
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
import rife.bld.extension.ExecOperation;
import rife.bld.extension.JUnitReporterOperation;
import rife.bld.extension.JacocoReportOperation;
import rife.bld.extension.PmdOperation;
import rife.bld.publish.PomBuilder;
import rife.bld.publish.PublishDeveloper;
import rife.bld.publish.PublishLicense;
import rife.bld.publish.PublishScm;
import rife.tools.exceptions.FileUtilsErrorException;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import static rife.bld.dependencies.Repository.*;
import static rife.bld.dependencies.Scope.compile;
import static rife.bld.dependencies.Scope.test;

public class Frankfurter4JBuild extends Project {
    static final String TEST_RESULTS_DIR = "build/test-results/test/";
    final PmdOperation pmdOp = new PmdOperation()
            .fromProject(this)
            .failOnViolation(true)
            .ruleSets("config/pmd.xml");


    public Frankfurter4JBuild() {
        pkg = "net.thauvin.erik";
        name = "frankfurter4j";
        version = version(0, 9, 0, "SNAPSHOT");

        javaRelease = 17;

        downloadSources = true;
        autoDownloadPurge = true;

        repositories = List.of(MAVEN_LOCAL, CENTRAL_SNAPSHOTS, MAVEN_CENTRAL, RIFE2_SNAPSHOTS, RIFE2_RELEASES);

        var gson = version(2, 13, 2);
        scope(compile)
                .include(dependency("com.uwyn", "urlencoder",
                        version(1, 3, 5)))
                .include(dependency("net.thauvin.erik.httpstatus", "httpstatus",
                        version(2, 0, 0, "SNAPSHOT")))
                .include(dependency("com.google.code.gson", "gson", gson));
        scope(test)
                .include(dependency("com.uwyn.rife2", "bld-extensions-testing-helpers",
                        version(0, 9, 0, "SNAPSHOT")))
                .include(dependency("org.mockito", "mockito-core",
                        version(5, 19, 0)))
                .include(dependency("org.junit.jupiter",
                        "junit-jupiter", version(5, 13, 4)))
                .include(dependency("org.junit.platform",
                        "junit-platform-console-standalone",
                        version(1, 13, 4)));

        publishOperation()
                .repository(version.isSnapshot() ? repository(CENTRAL_SNAPSHOTS.location())
                        .withCredentials(property("central.user"), property("central.password"))
                        : repository(CENTRAL_RELEASES.location())
                        .withCredentials(property("central.user"), property("central.password")))
                .repository(repository("github"))
                .info()
                .groupId(pkg)
                .artifactId(name)
                .description("Retrieve reference exchange rates from Frankfurter.dev")
                .url("https://github.com/ethauvin/" + name)
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
                        .connection("scm:git:https://github.com/ethauvin/" + name + ".git")
                        .developerConnection("scm:git:git@github.com:ethauvin/" + name + ".git")
                        .url("https://github.com/ethauvin/" + name)
                )
                .signKey(property("sign.key"))
                .signPassphrase(property("sign.passphrase"));

        javadocOperation()
                .javadocOptions()
                .author()
                .link("https://www.javadoc.io/doc/com.google.code.gson/gson/" + gson);
    }

    public static void main(String[] args) {
        new Frankfurter4JBuild().start(args);
    }

    @BuildCommand(summary = "Generates JaCoCo Reports")
    public void jacoco() throws Exception {
        var op = new JacocoReportOperation().fromProject(this);
        op.testToolOptions("--reports-dir=" + TEST_RESULTS_DIR);

        Exception ex = null;
        try {
            op.execute();
        } catch (Exception e) {
            ex = e;
        }

        renderWithXunitViewer();

        if (ex != null) {
            throw ex;
        }
    }

    @BuildCommand(summary = "Runs PMD analysis")
    public void pmd() throws Exception {
        pmdOp.execute();
    }

    @BuildCommand(value = "pmd-cli", summary = "Runs PMD analysis (CLI)")
    public void pmdCli() throws Exception {
        pmdOp.includeLineNumber(false).execute();
    }

    private void pomRoot() throws FileUtilsErrorException {
        PomBuilder.generateInto(publishOperation().fromProject(this).info(), dependencies(),
                new File(workDirectory, "pom.xml"));
    }

    private void renderWithXunitViewer() throws Exception {
        var npmPackagesEnv = System.getenv("NPM_PACKAGES");
        if (npmPackagesEnv != null && !npmPackagesEnv.isEmpty()) {
            var xunitViewer = Path.of(npmPackagesEnv, "bin", "xunit-viewer").toFile();
            if (xunitViewer.exists() && xunitViewer.canExecute()) {
                var reportsDir = "build/reports/tests/test/";

                Files.createDirectories(Path.of(reportsDir));

                new ExecOperation()
                        .fromProject(this)
                        .command(xunitViewer.getPath(), "-r", TEST_RESULTS_DIR, "-o", reportsDir + "index.html")
                        .execute();
            }
        }
    }

    @BuildCommand(summary = "Runs the JUnit reporter")
    public void reporter() throws Exception {
        new JUnitReporterOperation()
                .fromProject(this)
                .failOnSummary(true)
                .execute();
    }

    @Override
    public void test() throws Exception {
        var op = testOperation().fromProject(this);
        op.testToolOptions().reportsDir(new File(TEST_RESULTS_DIR));

        Exception ex = null;
        try {
            op.execute();
        } catch (Exception e) {
            ex = e;
        }

        renderWithXunitViewer();

        if (ex != null) {
            throw ex;
        }
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
}
