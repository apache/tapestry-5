package t5build

import org.gradle.api.Project

class TapestryBuildLogic {

    static boolean isSnapshot(Project project) {
        return tapestryVersion(project).endsWith('SNAPSHOT')
    }

    static boolean isWindows() {
        return System.properties['os.name'].toLowerCase().contains('windows')
    }

    static String tapestryVersion(Project project) {
        String major = project.rootProject.ext.tapestryMajorVersion
        String minor = project.rootProject.ext.tapestryMinorVersion
      
        boolean isCiBuild = project.rootProject.hasProperty('continuousIntegrationBuild') && project.rootProject.continuousIntegrationBuild
        return isCiBuild ? major + '-SNAPSHOT' : major + minor
    }
}