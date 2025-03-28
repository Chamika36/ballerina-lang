/*
 * Copyright (c) 2020, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package io.ballerina.projects.internal;

import io.ballerina.projects.DependencyGraph;
import io.ballerina.projects.DependencyManifest;
import io.ballerina.projects.DocumentConfig;
import io.ballerina.projects.DocumentId;
import io.ballerina.projects.ModuleConfig;
import io.ballerina.projects.ModuleDescriptor;
import io.ballerina.projects.ModuleId;
import io.ballerina.projects.ModuleName;
import io.ballerina.projects.PackageConfig;
import io.ballerina.projects.PackageDescriptor;
import io.ballerina.projects.PackageId;
import io.ballerina.projects.PackageManifest;
import io.ballerina.projects.PackageName;
import io.ballerina.projects.PackageOrg;
import io.ballerina.projects.PackageVersion;
import io.ballerina.projects.ProjectException;
import io.ballerina.projects.ResourceConfig;
import io.ballerina.projects.TomlDocument;
import io.ballerina.projects.internal.model.PackageJson;
import io.ballerina.projects.util.ProjectConstants;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Stream;

/**
 * Creates a {@code PackageConfig} instance from the given {@code PackageData} instance.
 *
 * @since 2.0.0
 */
public final class PackageConfigCreator {

    private PackageConfigCreator() {
    }

    public static PackageConfig createBuildProjectConfig(Path projectDirPath, boolean disableSyntaxTree) {
        ProjectFiles.validateBuildProjectDirPath(projectDirPath);

        // TODO Create the PackageManifest from the BallerinaToml file
        // TODO Validate the ballerinaToml content inside the Ballerina toml file
        PackageData packageData = ProjectFiles.loadBuildProjectPackageData(projectDirPath);

        if (packageData.ballerinaToml().isEmpty()) {
            throw new ProjectException(ProjectConstants.BALLERINA_TOML + " not found");
        }

        TomlDocument ballerinaToml = packageData.ballerinaToml()
                .map(d -> TomlDocument.from(ProjectConstants.BALLERINA_TOML, d.content())).orElse(null);
        TomlDocument dependenciesToml = packageData.dependenciesToml()
                .map(d -> TomlDocument.from(ProjectConstants.DEPENDENCIES_TOML, d.content())).orElse(null);
        TomlDocument pluginToml = packageData.compilerPluginToml()
                .map(d -> TomlDocument.from(ProjectConstants.COMPILER_PLUGIN_TOML, d.content())).orElse(null);
        TomlDocument balToolToml = packageData.balToolToml()
                .map(d -> TomlDocument.from(ProjectConstants.BAL_TOOL_TOML, d.content())).orElse(null);
        ManifestBuilder manifestBuilder = ManifestBuilder
                .from(ballerinaToml, pluginToml, balToolToml, projectDirPath);
        PackageManifest packageManifest = manifestBuilder.packageManifest();
        DependencyManifestBuilder dependencyManifestBuilder =
                DependencyManifestBuilder.from(dependenciesToml, packageManifest.descriptor());
        DependencyManifest dependencyManifest = dependencyManifestBuilder.dependencyManifest();

        return createPackageConfig(packageData, packageManifest, dependencyManifest, DependencyGraph.emptyGraph(),
                Collections.emptyMap(), disableSyntaxTree);
    }


    public static PackageConfig createBuildProjectConfig(Path projectDirPath) {
       return createBuildProjectConfig(projectDirPath, false);
    }

    public static PackageConfig createSingleFileProjectConfig(Path filePath, Boolean disableSyntaxTree) {
        ProjectFiles.validateSingleFileProjectFilePath(filePath);

        // Create a PackageManifest instance
        PackageDescriptor packageDesc = PackageDescriptor.from(PackageOrg.from(ProjectConstants.ANON_ORG),
                PackageName.from(ProjectConstants.DOT), PackageVersion.from(ProjectConstants.DEFAULT_VERSION));
        PackageManifest packageManifest = PackageManifest.from(packageDesc);
        DependencyManifest dependencyManifest = DependencyManifest.from(
                null, null, Collections.emptyList(), Collections.emptyList());

        PackageData packageData = ProjectFiles.loadSingleFileProjectPackageData(filePath);
        return createPackageConfig(packageData, packageManifest, dependencyManifest, DependencyGraph.emptyGraph(),
                Collections.emptyMap(), disableSyntaxTree);
    }

    public static PackageConfig createSingleFileProjectConfig(Path filePath) {
        return createSingleFileProjectConfig(filePath, false);
    }

    public static PackageConfig createBalaProjectConfig(Path balaPath) {
        ProjectFiles.validateBalaProjectPath(balaPath);
        PackageManifest packageManifest = BalaFiles.createPackageManifest(balaPath);
        DependencyManifest dependencyManifest = BalaFiles.createDependencyManifest(balaPath);
        PackageJson packageJson = BalaFiles.readPackageJson(balaPath);
        PackageData packageData = BalaFiles.loadPackageData(balaPath, packageJson);
        BalaFiles.DependencyGraphResult packageDependencyGraph = BalaFiles
                .createPackageDependencyGraph(balaPath);

        return createPackageConfig(packageData, packageManifest, dependencyManifest,
                packageDependencyGraph.packageDependencyGraph(), packageDependencyGraph.moduleDependencies());
    }

    public static PackageConfig createPackageConfig(PackageData packageData,
                                                    PackageManifest packageManifest,
                                                    DependencyManifest dependencyManifest) {
        return createPackageConfig(packageData, packageManifest, dependencyManifest, DependencyGraph.emptyGraph(),
                Collections.emptyMap(), false);
    }

    private static PackageConfig createPackageConfig(PackageData packageData,
                                                    PackageManifest packageManifest,
                                                    DependencyManifest dependencyManifest,
                                                    DependencyGraph<PackageDescriptor> packageDependencyGraph,
                                                    Map<ModuleDescriptor, List<ModuleDescriptor>>
                                                            moduleDependencyGraph, boolean disableSyntaxTree) {
        // TODO PackageData should contain the packageName. This should come from the Ballerina.toml file.
        // TODO For now, I take the directory name as the project name. I am not handling the case where the
        //  directory name is not a valid Ballerina identifier.

        // TODO: Should replace with Ballerina Toml parser generic model.

        PackageName packageName = packageManifest.name();
        PackageId packageId = PackageId.create(packageName.value());

        List<ModuleConfig> moduleConfigs = Stream.concat(packageData.otherModules().stream()
                        .map(moduleData -> createModuleConfig(packageManifest.descriptor(), moduleData,
                                packageId, moduleDependencyGraph)),
                Stream.of(createDefaultModuleConfig(packageManifest.descriptor(),
                        packageData.defaultModule(), packageId, moduleDependencyGraph))).toList();


        DocumentConfig ballerinaToml = packageData.ballerinaToml()
                .map(data -> createDocumentConfig(data, null)).orElse(null);
        DocumentConfig dependenciesToml = packageData.dependenciesToml()
                .map(data -> createDocumentConfig(data, null)).orElse(null);
        DocumentConfig cloudToml = packageData.cloudToml()
                .map(data -> createDocumentConfig(data, null)).orElse(null);
        DocumentConfig compilerPluginToml = packageData.compilerPluginToml()
                .map(data -> createDocumentConfig(data, null)).orElse(null);
        DocumentConfig balToolToml = packageData.balToolToml()
                .map(data -> createDocumentConfig(data, null)).orElse(null);

        List<ResourceConfig> resources = new ArrayList<>();
        List<ResourceConfig> testResources = new ArrayList<>();
        if (!packageData.resources().isEmpty()) {
            resources = getResourceConfigs(packageData.resources(), packageData.packagePath());
        }
        if (!packageData.testResources().isEmpty()) {
            testResources = getResourceConfigs(packageData.testResources(), packageData.packagePath());
        }
        DocumentConfig readmeMd = packageData.readmeMd()
                .map(data -> createDocumentConfig(data, null)).orElse(null);

        return PackageConfig
                .from(packageId, packageData.packagePath(), packageManifest, dependencyManifest, ballerinaToml,
                        dependenciesToml, cloudToml, compilerPluginToml, balToolToml, readmeMd, moduleConfigs,
                        packageDependencyGraph, disableSyntaxTree, resources, testResources);
    }
    public static PackageConfig createPackageConfig(PackageData packageData,
                                                    PackageManifest packageManifest,
                                                    DependencyManifest dependencyManifest,
                                                    DependencyGraph<PackageDescriptor> packageDependencyGraph,
                                                    Map<ModuleDescriptor, List<ModuleDescriptor>>
                                                            moduleDependencyGraph) {
        return createPackageConfig(packageData, packageManifest, dependencyManifest, packageDependencyGraph,
                moduleDependencyGraph, true);
    }


    private static ModuleConfig createDefaultModuleConfig(PackageDescriptor pkgDesc,
                                                          ModuleData moduleData,
                                                          PackageId packageId,
                                                          Map<ModuleDescriptor, List<ModuleDescriptor>>
                                                                  moduleDepGraph) {
        ModuleName moduleName = ModuleName.from(pkgDesc.name());
        ModuleDescriptor moduleDescriptor = createModuleDescriptor(pkgDesc, moduleName);
        List<ModuleDescriptor> dependencies = getModuleDependencies(moduleDepGraph, moduleDescriptor);
        return createModuleConfig(moduleDescriptor, moduleData, packageId, dependencies);
    }

    private static ModuleDescriptor createModuleDescriptor(PackageDescriptor pkgDesc, ModuleName moduleName) {
        return ModuleDescriptor.from(moduleName, pkgDesc);
    }

    private static ModuleConfig createModuleConfig(PackageDescriptor pkgDesc,
                                                   ModuleData moduleData,
                                                   PackageId packageId,
                                                   Map<ModuleDescriptor, List<ModuleDescriptor>> moduleDepGraph) {
        Path fileName = moduleData.moduleDirectoryPath().getFileName();
        if (fileName == null) {
            // TODO Proper error handling
            throw new IllegalStateException("This branch cannot be reached");
        }
        ModuleName moduleName = ModuleName.from(pkgDesc.name(), moduleData.moduleName());
        ModuleDescriptor moduleDescriptor = createModuleDescriptor(pkgDesc, moduleName);
        List<ModuleDescriptor> dependencies = getModuleDependencies(moduleDepGraph, moduleDescriptor);
        return createModuleConfig(moduleDescriptor, moduleData, packageId, dependencies);
    }

    private static ModuleConfig createModuleConfig(ModuleDescriptor moduleDescriptor,
                                                   ModuleData moduleData,
                                                   PackageId packageId,
                                                   List<ModuleDescriptor> dependencies) {
        ModuleId moduleId = ModuleId.create(moduleDescriptor.name().toString(), packageId);

        List<DocumentConfig> srcDocs = getDocumentConfigs(moduleId, moduleData.sourceDocs());
        List<DocumentConfig> testSrcDocs = getDocumentConfigs(moduleId, moduleData.testSourceDocs());

        DocumentConfig readmeMd = moduleData.readmeMd()
                .map(data -> createDocumentConfig(data, null)).orElse(null);

        return ModuleConfig.from(moduleId, moduleDescriptor, srcDocs, testSrcDocs, readmeMd, dependencies);
    }

    private static List<ResourceConfig> getResourceConfigs(List<Path> resources, Path packagePath) {
        // TODO: no need Remove duplicate paths before processing
        Set<Path> distinctResources = new HashSet<>(resources);
        return distinctResources.stream().map(
                distinctResource -> createResourceConfig(distinctResource, packagePath)).toList();
    }

    private static ResourceConfig createResourceConfig(Path path, Path packagePath) {
        final DocumentId documentId = DocumentId.create(path.toString(), null);
        return ProvidedResourceConfig.from(documentId, path, packagePath);
    }

    private static List<DocumentConfig> getDocumentConfigs(ModuleId moduleId, List<DocumentData> documentData) {
        return documentData
                .stream()
                .sorted(Comparator.comparing(DocumentData::name))
                .map(srcDoc -> createDocumentConfig(srcDoc, moduleId))
                .toList();
    }

    static DocumentConfig createDocumentConfig(DocumentData documentData, ModuleId moduleId) {
        final DocumentId documentId = DocumentId.create(documentData.name(), moduleId);
        return DocumentConfig.from(documentId, documentData.content(), documentData.name());
    }

    private static List<ModuleDescriptor> getModuleDependencies(Map<ModuleDescriptor, List<ModuleDescriptor>>
                                                                        moduleDepGraph,
                                                                ModuleDescriptor moduleDescriptor) {
        List<ModuleDescriptor> moduleDependencies = moduleDepGraph.get(moduleDescriptor);
        return Objects.requireNonNullElse(moduleDependencies, Collections.emptyList());
    }
}
