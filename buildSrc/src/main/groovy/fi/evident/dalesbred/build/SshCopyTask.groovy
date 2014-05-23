/*
 * Copyright (c) 2014 Evident Solutions Oy
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */

package fi.evident.dalesbred.build
import org.gradle.api.DefaultTask
import org.gradle.api.tasks.TaskAction

import java.util.concurrent.Callable

public class SshCopyTask extends DefaultTask {

    def targetDirectory;
    String sourceDirectory;
    String host = 'uuhi.evident.fi';
    String username = 'evident';
    String keyfile = '${user.home}/.ssh/id_rsa';

    private String getTargetDirectory() {
        return unpack(targetDirectory)
    }

    @TaskAction
    def copy() {
        def target = getTargetDirectory()

        ant.sshexec(host: host,
                username: username,
                command: "mkdir -p $target",
                keyfile: keyfile,
                trust: true)

        ant.scp(todir: "${username}@${host}:${target}",
                keyfile: keyfile,
                trust: true) {
            fileset(dir: sourceDirectory) {
                include(name: '**/**')
            }
        }
    }

    private static Object unpack(Object obj) {
        Object current = obj;
        while (current != null) {
            if (current instanceof Closure) {
                current = ((Closure) current).call();
            } else if (current instanceof Callable) {
                current = ((Callable) current).call();
            } else if (current instanceof org.gradle.internal.Factory) {
                return ((org.gradle.internal.Factory) current).create();
            } else {
                return current;
            }
        }
        return null;
    }
}
