/*
 * Copyright (c) 2018, WSO2 Inc. (http://wso2.com) All Rights Reserved.
 * <p>
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 * <p>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.ballerinalang.test.runtime.util;

/**
 * Testerina Constant Class holds constants used in runtime.
 *
 * @since 0.970.0
 */
public final class TesterinaConstants {

    public static final String DOT_REPLACER = "&0046";
    public static final String BALLERINA_SOURCE_ROOT = "ballerina.source.root";
    public static final String TESTERINA_TEMP_DIR = ".testerina";
    public static final String TESTERINA_TEST_SUITE = "test_suit.json";
    public static final String TESTERINA_LAUNCHER_CLASS_NAME = "org.ballerinalang.test.runtime.BTestMain";
    public static final String CODE_COV_GENERATOR_CLASS_NAME = "org.ballerinalang.test.runtime.CoverageMain";
    public static final String TEST_RUNTIME_JAR_PREFIX = "testerina-runtime-";
    public static final String TESTERINA_MAIN_METHOD = "main";
    public static final String GET_TEST_EXEC_STATE = "$getTestExecutionState";

    public static final String ORIGINAL_FUNC_NAME_PREFIX = "$ORIG_";
    public static final String MOCK_FUNC_NAME_PREFIX = "$MOCK_";

    public static final String TARGET_DIR_NAME = "target";

    public static final String DOT = ".";

    public static final String IGNORE_PATTERN = "!";

    public static final String FULLY_QULAIFIED_MODULENAME_SEPRATOR = ".";

    public static final String STANDALONE_SRC_PACKAGENAME = ".";

    public static final String HYPHEN = "-";
    public static final String PATH_SEPARATOR = "/";

    public static final String RELATIVE_PATH_PREFIX = "./";
    public static final String JAR_EXTENSION = ".jar";
    public static final String CLASS_EXTENSION = ".class";
    public static final String TESTABLE = "testable";
    public static final String MODIFIED = "mod";
    public static final String CACHE_DIR = "cache";
    public static final String JAVA_PLATFORM_DIR = "java21";
    public static final String ANON_ORG = "$anon";
    public static final String WILDCARD = "*";

    //Coverage constants
    public static final String BIN_DIR = "bin";
    public static final String SRC_DIR = "src";
    public static final String EXEC_FILE_NAME = "ballerina.exec";
    public static final String AGENT_FILE_NAME = "jacocoagent.jar";
    public static final String COVERAGE_DIR = "coverage";
    public static final String JACOCO_INSTRUMENTED_DIR = "instrumented";
    public static final String STATUS_FILE = "module_status.json";
    public static final String COVERAGE_FILE = "module_coverage.json";
    public static final String RESULTS_JSON_FILE = "test_results.json";
    public static final String RERUN_TEST_JSON_FILE = "rerun_test.json";
    public static final String RESULTS_HTML_FILE = "index.html";
    public static final String REPORT_XML_FILE = "coverage-report.xml";
    public static final String TOOLS_DIR_NAME = "tools";
    public static final String REPORT_DIR_NAME = "report";
    public static final String REPORT_ZIP_NAME = REPORT_DIR_NAME + ".zip";
    public static final String REPORT_DATA_PLACEHOLDER = "__data__";
    public static final String FILE_PROTOCOL = "file://";
    public static final int FILE_DEPTH = 5;

    //Coverage dependencies
    public static final String JACOCO_CORE_JAR = "org.jacoco.core-0.8.12.jar";
    public static final String JACOCO_REPORT_JAR = "org.jacoco.report-0.8.12.jar";
    public static final String ASM_JAR = "asm-9.7.jar";
    public static final String ASM_TREE_JAR = "asm-tree-9.7.jar";
    public static final String ASM_COMMONS_JAR = "asm-commons-9.7.jar";

    public static final String BLANG_SRC_FILE_EXT = "bal";
    public static final String BLANG_SRC_FILE_SUFFIX = "." + BLANG_SRC_FILE_EXT;

    public static final String JACOCO_XML_FORMAT = "xml";
    public static final String DATA_KEY_SEPARATOR = "#";
    public static final String MODULE_SEPARATOR = ":";

    public static final String MOCK_FN_DELIMITER = "#";
    public static final String MOCK_LEGACY_DELIMITER = "~";

    public static final int IDENTIFIER_START_INDEX = 1;
    public static final int IDENTIFIER_END_INDEX = 5;
    public static final int DEFAULT_TEST_WORKERS = 1;
    public static final String MODIFIED_JAR_SUFFIX = HYPHEN + MODIFIED + JAR_EXTENSION;

    private TesterinaConstants() {
    }

    /**
     * RuntimeArgs identifies the indices of the BTestMain arg.
     *
     * @since 2201.9.0
     */
    public static final class RunTimeArgs {
        public static final int IS_FAT_JAR_EXECUTION = 0;
        public static final int TEST_SUITE_JSON_PATH = 1;
        public static final int TARGET_DIR = 2;
        public static final int JACOCO_AGENT_PATH = 3;
        public static final int REPORT = 4;
        public static final int COVERAGE = 5;
        public static final int GROUP_LIST = 6;
        public static final int DISABLE_GROUP_LIST = 7;
        public static final int SINGLE_EXEC_TESTS = 8;
        public static final int IS_RERUN_TEST_EXECUTION = 9;
        public static final int LIST_GROUPS = 10;
        public static final int IS_PARALLEL_EXECUTION = 11;

        private RunTimeArgs() {
        }
    }
}
