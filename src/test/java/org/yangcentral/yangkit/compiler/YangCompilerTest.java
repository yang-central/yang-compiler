package org.yangcentral.yangkit.compiler;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.BeforeEach;
import static org.junit.jupiter.api.Assertions.*;

/**
 * YangCompiler单元测试
 */
@DisplayName("YangCompiler Tests")
class YangCompilerTest {

    private YangCompiler compiler;

    @BeforeEach
    void setUp() {
        compiler = new YangCompiler();
    }

    @Test
    @DisplayName("Should create YangCompiler instance")
    void testCreateYangCompiler() {
        assertNotNull(compiler);
    }

    @Test
    @DisplayName("Should add and get plugin info")
    void testAddAndGetPluginInfo() {
        // Given
        PluginInfo pluginInfo = new PluginInfo("test_plugin", 
            new MockYangCompilerPlugin());
        
        // When
        boolean added = compiler.addPluginInfo(pluginInfo);
        
        // Then
        assertTrue(added);
        assertNotNull(compiler.getPluginInfo("test_plugin"));
        assertEquals("test_plugin", compiler.getPluginInfo("test_plugin").getPluginName());
    }

    @Test
    @DisplayName("Should not add duplicate plugin info")
    void testNotAddDuplicatePluginInfo() {
        // Given
        PluginInfo pluginInfo1 = new PluginInfo("test_plugin", 
            new MockYangCompilerPlugin());
        PluginInfo pluginInfo2 = new PluginInfo("test_plugin", 
            new MockYangCompilerPlugin());
        
        // When
        boolean added1 = compiler.addPluginInfo(pluginInfo1);
        boolean added2 = compiler.addPluginInfo(pluginInfo2);
        
        // Then
        assertTrue(added1);
        assertFalse(added2); // 不应该添加重复的插件
    }

    @Test
    @DisplayName("Should return null for non-existent plugin")
    void testGetNonExistentPlugin() {
        // When
        PluginInfo pluginInfo = compiler.getPluginInfo("non_existent");
        
        // Then
        assertNull(pluginInfo);
    }

    @Test
    @DisplayName("Should not add null plugin info")
    void testNotAddNullPluginInfo() {
        // When
        boolean added = compiler.addPluginInfo(null);
        
        // Then
        assertFalse(added);
    }

    @Test
    @DisplayName("Should set and get settings")
    void testSetAndGetSettings() {
        // Given
        Settings settings = new Settings();
        
        // When
        compiler.setSettings(settings);
        
        // Then
        assertEquals(settings, compiler.getSettings());
    }

    @Test
    @DisplayName("Should set and get install flag")
    void testSetAndGetInstall() {
        // When
        compiler.setInstall(true);
        
        // Then
        assertTrue(compiler.isInstall());
        
        // When
        compiler.setInstall(false);
        
        // Then
        assertFalse(compiler.isInstall());
    }

    /**
     * 模拟插件用于测试
     */
    private static class MockYangCompilerPlugin implements org.yangcentral.yangkit.compiler.plugin.YangCompilerPlugin {
        @Override
        public void run(org.yangcentral.yangkit.model.api.schema.YangSchemaContext schemaContext,
                       YangCompiler yangCompiler,
                       java.util.List<org.yangcentral.yangkit.compiler.plugin.YangCompilerPluginParameter> parameters) {
            // Mock implementation - do nothing
        }
    }
}
