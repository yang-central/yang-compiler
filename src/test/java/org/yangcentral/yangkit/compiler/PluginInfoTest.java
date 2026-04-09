package org.yangcentral.yangkit.compiler;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import static org.junit.jupiter.api.Assertions.*;

/**
 * PluginInfo单元测试
 */
@DisplayName("PluginInfo Tests")
class PluginInfoTest {

    @Test
    @DisplayName("Should create PluginInfo with valid parameters")
    void testCreatePluginInfo() {
        // Given
        String pluginName = "test_plugin";
        MockYangCompilerPlugin plugin = new MockYangCompilerPlugin();
        
        // When
        PluginInfo pluginInfo = new PluginInfo(pluginName, plugin);
        
        // Then
        assertNotNull(pluginInfo);
        assertEquals(pluginName, pluginInfo.getPluginName());
        assertEquals(plugin, pluginInfo.getPlugin());
    }

    @Test
    @DisplayName("Should set and get description")
    void testSetAndGetDescription() {
        // Given
        PluginInfo pluginInfo = new PluginInfo("test", new MockYangCompilerPlugin());
        String description = "This is a test plugin";
        
        // When
        pluginInfo.setDescription(description);
        
        // Then
        assertEquals(description, pluginInfo.getDescription());
    }

    @Test
    @DisplayName("Should add and get parameters")
    void testAddAndGetParameters() {
        // Given
        PluginInfo pluginInfo = new PluginInfo("test", new MockYangCompilerPlugin());
        PluginParameterInfo param1 = new PluginParameterInfo("param1");
        param1.setDescription("First parameter");
        PluginParameterInfo param2 = new PluginParameterInfo("param2");
        param2.setDescription("Second parameter");
        
        // When
        pluginInfo.addParameter(param1);
        pluginInfo.addParameter(param2);
        
        // Then
        assertEquals(2, pluginInfo.getParameters().size());
        assertEquals("param1", pluginInfo.getParameters().get(0).getName());
        assertEquals("param2", pluginInfo.getParameters().get(1).getName());
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
