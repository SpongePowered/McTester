package org.spongepowered.mctester

import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.plugins.JavaPluginConvention
import org.gradle.api.tasks.SourceSet
import org.gradle.api.tasks.testing.Test
import org.gradle.plugins.ide.idea.model.IdeaModel

class McTesterGradlePlugin implements Plugin<Project> {

    void apply(Project project) {
        this.addGradleStart(project)
        this.addDependency(project)
        //this.setupTestEnvironment(project)
    }

    private void addGradleStart(Project project) {
        project.getLogger().warn("Hacking ForgeGradle from plugin!")

        def javaConv = (JavaPluginConvention) project.getConvention().getPlugins().get("java")

        def main = javaConv.getSourceSets().getByName(SourceSet.MAIN_SOURCE_SET_NAME);
        def test = javaConv.getSourceSets().getByName(SourceSet.TEST_SOURCE_SET_NAME);


        main.setCompileClasspath(main.getCompileClasspath()
                .plus(project.getConfigurations().getByName("forgeGradleGradleStart"))

        )
        test.setCompileClasspath(test.getCompileClasspath()
                .plus(project.getConfigurations().getByName("forgeGradleGradleStart"))
        )
        test.setRuntimeClasspath(test.getRuntimeClasspath()
                .plus(project.getConfigurations().getByName("forgeGradleGradleStart"))
        )

        def ideaConv = (IdeaModel) project.getExtensions().findByName("idea");
        if (ideaConv != null) {
            ideaConv.getModule().getScopes().get("COMPILE").get("plus").add(project.getConfigurations().getByName("forgeGradleGradleStart"));
        }
    }

    private void addDependency(Project project) {
        version = "@@MCTESTER_VERSION@@"
        project.getLogger().warn("Adding dependency on McTester: " + version);
        project.getDependencies().add("testCompile", version);
    }

    private void setupTestEnvironment(Project project) {
        project.getLogger().warn("Disabling Mixin refmap for McTester")
        project.getTasks().withType(Test.class, { Test test ->
            test.systemProperty "mixin.env.disableRefMap", "true"
        })
    }
}
