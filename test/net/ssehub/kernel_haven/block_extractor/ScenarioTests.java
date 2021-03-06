/*
 * Copyright 2018-2019 University of Hildesheim, Software Systems Engineering
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package net.ssehub.kernel_haven.block_extractor;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

import java.io.File;
import java.io.IOException;
import java.util.Properties;

import org.junit.Test;

import net.ssehub.kernel_haven.SetUpException;
import net.ssehub.kernel_haven.code_model.CodeBlock;
import net.ssehub.kernel_haven.code_model.JsonCodeModelCache;
import net.ssehub.kernel_haven.code_model.SourceFile;
import net.ssehub.kernel_haven.config.Configuration;
import net.ssehub.kernel_haven.config.DefaultSettings;
import net.ssehub.kernel_haven.cpp_utils.CppParsingSettings;
import net.ssehub.kernel_haven.test_utils.TestConfiguration;
import net.ssehub.kernel_haven.util.ExtractorException;
import net.ssehub.kernel_haven.util.FormatException;

/**
 * "Scenario" tests for the {@link CodeBlockExtractor} with "real-world" C files.
 *
 * @author Adam
 */
@SuppressWarnings("null")
public class ScenarioTests {
    

    private static final File TESTDATA = new File("testdata/scenario");
    
    /**
     * First scenario test.
     * 
     * @throws ExtractorException unwanted.
     * @throws SetUpException unwanted.
     * @throws IOException unwanted.
     * @throws FormatException unwanted.
     */
    @Test
    public void test1() throws ExtractorException, SetUpException, IOException, FormatException {
        Configuration config = new TestConfiguration(new Properties());
        config.setValue(DefaultSettings.SOURCE_TREE, TESTDATA);
        
        CodeBlockExtractor extractor = new CodeBlockExtractor();
        extractor.init(config);
        
        SourceFile<CodeBlock> result = extractor.runOnFile(new File("test1.c"));
        
        // compare with cache that was manually verified
        JsonCodeModelCache cache = new JsonCodeModelCache(TESTDATA);
        SourceFile<CodeBlock> expected = cache.read(new File("test1.c")).castTo(CodeBlock.class);
        
        assertThat(result.getPath(), is(expected.getPath()));
        for (int i = 0; i < expected.getTopElementCount(); i++) {
            assertSameBlock(result.getElement(i), expected.getElement(i));
        }
        assertThat(result.getTopElementCount(), is(expected.getTopElementCount()));
        
//        cache.write(result); // this was used to create the valid cache to verify against
    }
    
    /**
     * Tests with the file arch/blackfin/mach-bf537/boards/stamp.c from the Linux Kernel 4.4.
     * This is the file with the most #if* blocks containing CONFIG_ variables that I have found.
     * 
     * @throws ExtractorException unwanted.
     * @throws SetUpException unwanted.
     * @throws IOException unwanted.
     * @throws FormatException unwanted.
     */
    @Test
    public void linux1() throws ExtractorException, SetUpException, IOException, FormatException {
        Configuration config = new TestConfiguration(new Properties());
        config.setValue(DefaultSettings.SOURCE_TREE, TESTDATA);
        config.registerSetting(CppParsingSettings.HANDLE_LINUX_MACROS);
        config.setValue(CppParsingSettings.HANDLE_LINUX_MACROS, true);
        
        CodeBlockExtractor extractor = new CodeBlockExtractor();
        extractor.init(config);
        
        SourceFile<CodeBlock> result = extractor.runOnFile(new File("linux1.c"));
        
        // compare with cache that was manually verified
        JsonCodeModelCache cache = new JsonCodeModelCache(TESTDATA);
        SourceFile<CodeBlock> expected = cache.read(new File("linux1.c")).castTo(CodeBlock.class);
        
        assertThat(result.getPath(), is(expected.getPath()));
        for (int i = 0; i < expected.getTopElementCount(); i++) {
            assertSameBlock(result.getElement(i), expected.getElement(i));
        }
        assertThat(result.getTopElementCount(), is(expected.getTopElementCount()));
        
//        cache.write(result); // this was used to create the valid cache to verify against
    }
    
    /**
     * Tests with the file drivers/atm/firestream.c from the Linux Kernel 4.4.
     * This file caused the extractor to crash.
     * 
     * @throws ExtractorException unwanted.
     * @throws SetUpException unwanted.
     * @throws IOException unwanted.
     * @throws FormatException unwanted.
     */
    @Test
    public void linux2() throws ExtractorException, SetUpException, IOException, FormatException {
        Configuration config = new TestConfiguration(new Properties());
        config.setValue(DefaultSettings.SOURCE_TREE, TESTDATA);
        config.registerSetting(CppParsingSettings.HANDLE_LINUX_MACROS);
        config.setValue(CppParsingSettings.HANDLE_LINUX_MACROS, true);
        
        CodeBlockExtractor extractor = new CodeBlockExtractor();
        extractor.init(config);
        
        SourceFile<CodeBlock> result = extractor.runOnFile(new File("linux2.c"));
        
        // compare with cache that was manually verified
        JsonCodeModelCache cache = new JsonCodeModelCache(TESTDATA);
        SourceFile<CodeBlock> expected = cache.read(new File("linux2.c")).castTo(CodeBlock.class);
        
        assertThat(result.getPath(), is(expected.getPath()));
        for (int i = 0; i < expected.getTopElementCount(); i++) {
            assertSameBlock(result.getElement(i), expected.getElement(i));
        }
        assertThat(result.getTopElementCount(), is(expected.getTopElementCount()));
        
//        cache.write(result); // this was used to create the valid cache to verify against
    }
    
    /**
     * Tests part of the file sound/pci/au88x0/au88x0_core.c  from the Linux Kernel 4.4.
     * This file had wrongly parsed comments in it.
     * 
     * @throws ExtractorException unwanted.
     * @throws SetUpException unwanted.
     * @throws IOException unwanted.
     * @throws FormatException unwanted.
     */
    @Test
    public void linux3() throws ExtractorException, SetUpException, IOException, FormatException {
        Configuration config = new TestConfiguration(new Properties());
        config.setValue(DefaultSettings.SOURCE_TREE, TESTDATA);
        config.registerSetting(CppParsingSettings.HANDLE_LINUX_MACROS);
        config.setValue(CppParsingSettings.HANDLE_LINUX_MACROS, true);
        
        CodeBlockExtractor extractor = new CodeBlockExtractor();
        extractor.init(config);
        
        SourceFile<CodeBlock> result = extractor.runOnFile(new File("linux3.c"));
        
        // compare with cache that was manually verified
        JsonCodeModelCache cache = new JsonCodeModelCache(TESTDATA);
        SourceFile<CodeBlock> expected = cache.read(new File("linux3.c")).castTo(CodeBlock.class);
        
        assertThat(result.getPath(), is(expected.getPath()));
        for (int i = 0; i < expected.getTopElementCount(); i++) {
            assertThat(result.getElement(i), is(expected.getElement(i)));
        }
        assertThat(result.getTopElementCount(), is(expected.getTopElementCount()));
        
//        cache.write(result); // this was used to create the valid cache to verify against
    }
    
    /**
     * Depth-first checks for equality of {@link CodeBlock}s. This is better than {@link CodeBlock#equals(Object)},
     * because we will know which nested block is not as expected.
     * 
     * @param actual The actual block.
     * @param expected The expected block.
     */
    private void assertSameBlock(CodeBlock actual, CodeBlock expected) {
        for (int i = 0; i < expected.getNestedElementCount(); i++) {
            assertSameBlock(actual.getNestedElement(i), expected.getNestedElement(i));
        }
        assertThat(actual.toString(), is(expected.toString()));
    }

}
