// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at
//
// http://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.

package t5build

import org.gradle.api.tasks.Exec
import org.gradle.api.tasks.Internal

/**
 * Gradle task that runs an npm command, either via Docker or the local npm executable.
 * <p>
 * When Docker is available, the task mounts the project's {@code src/main} directory into
 * a container and runs {@cpde npm} there, ensuring a consistent Node.js environment.
 * <p>
 * On Linux, the container process runs as the current host user to avoid file-ownership
 * issues on mounted volumes.
 * <p>
 * When Docker is not available, the task falls back to invoking the local {@code npm} (or
 * {@code npm.cmd} on Windows) directly.
 * <p>
 * The Docker image used can be overridden via the {@code nodeDockerImage} Gradle project
 * property (defaults to {@code node:lts-alpine}).
 * <p>
 * Usage in a build script:
 * <pre>
 * tasks.register('npmInstall', NpmTask) {
 *     npmArgs = ['install']
 * }
 * </pre>
 */
class NpmTask extends Exec {

    private static Boolean dockerAvailableCache = null

    /**
     * Arguments passed to the npm command (e.g. {@code ['install']}).
     */
    @Internal
    List<String> npmArgs = []

    /**
     * Docker image used to run npm when Docker is available.
     * <p>
     * Default: {@code node:lts-alpine}
     */
    @Internal
    String nodeDockerImage = (project.findProperty('nodeDockerImage') ?: 'node:lts-alpine') as String

    NpmTask() {
        workingDir = 'src/main/typescript'
    }

    @Override
    protected void exec() {
        commandLine(buildCommand())
        super.exec()
    }

    private List<String> buildCommand() {
        return isDockerAvailable() ? buildDockerCommand() : [npmExecutable()] + npmArgs
    }

    private List<String> buildDockerCommand() {
        def cmd = [
            'docker', 'run', '--rm',
            '-v', "${project.projectDir}/src/main:/work",
            '-w', '/work/typescript']
        if (isLinux()) {
            cmd += ['--user', "${['id', '-u'].execute().text.trim()}:${['id', '-g'].execute().text.trim()}"]
        }
        cmd += [nodeDockerImage, 'npm'] + npmArgs
        return cmd
    }

    private static boolean isDockerAvailable() {
        if (dockerAvailableCache == null) {
            try {
                def proc = ['docker', 'info'].execute()
                proc.waitFor()
                dockerAvailableCache = proc.exitValue() == 0
            } catch (Exception ignored) {
                dockerAvailableCache = false
            }
        }
        return dockerAvailableCache
    }

    private static String npmExecutable() {
        return isWindows() ? 'npm.cmd' : 'npm'
    }

    private static boolean isWindows() {
        return System.properties['os.name'].toLowerCase().contains('windows')
    }

    private static boolean isLinux() {
        def os = System.properties['os.name'].toLowerCase()
        return !os.contains('windows') && !os.contains('mac')
    }
}
