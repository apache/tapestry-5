// Copyright 2022 The Apache Software Foundation
//
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

package org.apache.tapestry5.internal.services;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.function.BiConsumer;
import java.util.stream.Collectors;

import org.apache.tapestry5.commons.MappedConfiguration;
import org.apache.tapestry5.commons.Resource;
import org.apache.tapestry5.corelib.base.AbstractComponentEventLink;
import org.apache.tapestry5.corelib.base.AbstractField;
import org.apache.tapestry5.corelib.base.AbstractLink;
import org.apache.tapestry5.corelib.base.AbstractPropertyOutput;
import org.apache.tapestry5.corelib.base.AbstractTextField;
import org.apache.tapestry5.corelib.components.ActionLink;
import org.apache.tapestry5.corelib.components.Alerts;
import org.apache.tapestry5.corelib.components.Any;
import org.apache.tapestry5.corelib.components.BeanEditForm;
import org.apache.tapestry5.corelib.components.BeanEditor;
import org.apache.tapestry5.corelib.components.Delegate;
import org.apache.tapestry5.corelib.components.DevTool;
import org.apache.tapestry5.corelib.components.Errors;
import org.apache.tapestry5.corelib.components.EventLink;
import org.apache.tapestry5.corelib.components.Form;
import org.apache.tapestry5.corelib.components.Glyphicon;
import org.apache.tapestry5.corelib.components.If;
import org.apache.tapestry5.corelib.components.Label;
import org.apache.tapestry5.corelib.components.Loop;
import org.apache.tapestry5.corelib.components.Output;
import org.apache.tapestry5.corelib.components.OutputRaw;
import org.apache.tapestry5.corelib.components.PageLink;
import org.apache.tapestry5.corelib.components.PasswordField;
import org.apache.tapestry5.corelib.components.PropertyDisplay;
import org.apache.tapestry5.corelib.components.PropertyEditor;
import org.apache.tapestry5.corelib.components.RenderObject;
import org.apache.tapestry5.corelib.components.Submit;
import org.apache.tapestry5.corelib.components.TextField;
import org.apache.tapestry5.corelib.components.TextOutput;
import org.apache.tapestry5.corelib.components.Zone;
import org.apache.tapestry5.corelib.mixins.FormGroup;
import org.apache.tapestry5.corelib.mixins.RenderDisabled;
import org.apache.tapestry5.integration.app1.base.BaseLayoutPage;
import org.apache.tapestry5.integration.app1.components.Border;
import org.apache.tapestry5.integration.app1.components.ErrorComponent;
import org.apache.tapestry5.integration.app1.components.OuterAny;
import org.apache.tapestry5.integration.app1.components.SubclassWithImport;
import org.apache.tapestry5.integration.app1.components.SuperclassWithImport;
import org.apache.tapestry5.integration.app1.components.TextOnlyOnDisabledTextField;
import org.apache.tapestry5.integration.app1.mixins.AltTitleDefault;
import org.apache.tapestry5.integration.app1.mixins.EchoValue;
import org.apache.tapestry5.integration.app1.mixins.EchoValue2;
import org.apache.tapestry5.integration.app1.mixins.TextOnlyOnDisabled;
import org.apache.tapestry5.integration.app1.pages.AlertsDemo;
import org.apache.tapestry5.integration.app1.pages.AtComponentType;
import org.apache.tapestry5.integration.app1.pages.BlockCaller;
import org.apache.tapestry5.integration.app1.pages.BlockHolder;
import org.apache.tapestry5.integration.app1.pages.InstanceMixinDependencies;
import org.apache.tapestry5.integration.app1.pages.MixinParameterDefault;
import org.apache.tapestry5.internal.services.ComponentDependencyRegistry.DependencyType;
import org.apache.tapestry5.internal.services.templates.DefaultTemplateLocator;
import org.apache.tapestry5.internal.services.templates.PageTemplateLocator;
import org.apache.tapestry5.ioc.internal.QuietOperationTracker;
import org.apache.tapestry5.ioc.internal.util.AbstractResource;
import org.apache.tapestry5.model.ComponentModel;
import org.apache.tapestry5.modules.TapestryModule;
import org.apache.tapestry5.plastic.PlasticManager;
import org.apache.tapestry5.services.ComponentClassResolver;
import org.apache.tapestry5.services.pageload.PageClassLoaderContextManager;
import org.apache.tapestry5.services.templates.ComponentTemplateLocator;
import org.easymock.EasyMock;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

/**
 * Tests {@link ComponentDependencyRegistryImpl}.
 */
@SuppressWarnings("deprecation")
public class ComponentDependencyRegistryImplTest
{
    
    private ComponentDependencyRegistryImpl componentDependencyRegistry;
    
    private PageClassLoaderContextManager pageClassLoaderContextManager;
    
    private ComponentClassResolver resolver;
    
    private PlasticManager plasticManager;
    
    private TemplateParser templateParser;
    
    private ComponentTemplateLocator componentTemplateLocator;
    
    @BeforeMethod
    public void setup()
    {
        assertFalse(
                String.format("During testing, %s shouldn't exist", ComponentDependencyRegistry.FILENAME), 
                new File(ComponentDependencyRegistry.FILENAME).exists());
        
        MockMappedConfiguration<String, URL> templateConfiguration = new MockMappedConfiguration<String, URL>();
        TapestryModule.contributeTemplateParser(templateConfiguration);
        templateParser = new TemplateParserImpl(templateConfiguration.map, false, new QuietOperationTracker());
        
        final String rootFolder = "src/test/app1/";
        final File folderFile = new File(rootFolder);
        final Resource app1ContextFolderResource = new FileResource("/", folderFile);
        
        final PageTemplateLocator pageTemplateLocator = new PageTemplateLocator(app1ContextFolderResource, resolver, "");
        final DefaultTemplateLocator defaultTemplateLocator = new DefaultTemplateLocator();
        componentTemplateLocator = new ComponentTemplateLocator() 
        {
            @Override
            public Resource locateTemplate(ComponentModel model, Locale locale) 
            {
                Resource resource = defaultTemplateLocator.locateTemplate(model, locale);
                if (resource == null)
                {
                    resource = pageTemplateLocator.locateTemplate(model, locale);
                }
                return resource != null && resource.exists() ? resource : null;
            }
        };
        
        resolver = EasyMock.createMock(ComponentClassResolver.class);
        
        EasyMock.expect(resolver.resolvePageClassNameToPageName(EasyMock.anyString()))
                .andAnswer(() -> {
                    String s = ((String) EasyMock.getCurrentArguments()[0]);
                    final String pageName = s.substring(s.lastIndexOf('.') + 1);
                    return pageName;
                }).anyTimes();
        
        expectResolveComponent(TextField.class);
        expectResolveComponent(PasswordField.class);
        expectResolveComponent(Border.class);
        expectResolveComponent(BeanEditForm.class);
        expectResolveComponent(Zone.class);
        expectResolveComponent(ActionLink.class);
        expectResolveComponent(If.class);
        expectResolveComponent(ErrorComponent.class);
        expectResolveComponent(EventLink.class);        
        expectResolveComponent(Output.class);
        expectResolveComponent(Delegate.class);
        expectResolveComponent(TextOutput.class);
        expectResolveComponent(Label.class);
        expectResolveComponent(BeanEditor.class);
        expectResolveComponent(Loop.class);
        expectResolveComponent(PropertyEditor.class);
        expectResolveComponent(PropertyDisplay.class);
        expectResolveComponent(Errors.class);
        expectResolveComponent(Submit.class);
        expectResolveComponent(PageLink.class);
        expectResolveComponent(DevTool.class);
        expectResolveComponent(Alerts.class);
        expectResolveComponent(RenderObject.class);
        expectResolveComponent(Form.class);
        expectResolveComponent(Glyphicon.class);
        expectResolveComponent(SubclassWithImport.class);
        expectResolveComponent(OutputRaw.class);
        
        EasyMock.expect(resolver.resolveMixinTypeToClassName("textonlyondisabled"))
            .andReturn(TextOnlyOnDisabled.class.getName()).anyTimes();
        EasyMock.expect(resolver.resolveMixinTypeToClassName("echovalue2"))
            .andReturn(EchoValue2.class.getName()).anyTimes();
        EasyMock.expect(resolver.resolveMixinTypeToClassName("alttitledefault"))
            .andReturn(AltTitleDefault.class.getName()).anyTimes();
        EasyMock.expect(resolver.resolveMixinTypeToClassName("formgroup"))
            .andReturn(FormGroup.class.getName()).anyTimes();
        
        EasyMock.expect(resolver.isPage(EasyMock.anyString())).andAnswer(() -> {
            String string = (String) EasyMock.getCurrentArguments()[0];
            return string.contains(".pages.");
        }).anyTimes();
        
        pageClassLoaderContextManager = EasyMock.createMock(PageClassLoaderContextManager.class);
        plasticManager = EasyMock.createMock(PlasticManager.class);
        EasyMock.expect(plasticManager.shouldInterceptClassLoading(EasyMock.anyString()))
            .andAnswer(() -> {
                String className = (String) EasyMock.getCurrentArguments()[0];
                return className.contains(".pages.") || className.contains(".mixins.") ||
                        className.contains(".components.") || className.contains(".base.");
            }).anyTimes();
        
        componentDependencyRegistry = new ComponentDependencyRegistryImpl(
                pageClassLoaderContextManager, plasticManager, resolver, templateParser, 
                componentTemplateLocator, ComponentDependencyRegistry.FILENAME, false);
        EasyMock.replay(pageClassLoaderContextManager, plasticManager, resolver);
    }

    private void expectResolveComponent(final Class<?> clasz) {
        final String className = clasz.getName();
        final java.lang.String simpleName = clasz.getSimpleName();
        EasyMock.expect(resolver.resolveComponentTypeToClassName(simpleName))
            .andReturn(className).anyTimes();
        EasyMock.expect(resolver.resolveComponentTypeToClassName(simpleName.toLowerCase()))
            .andReturn(className).anyTimes();
        EasyMock.expect(resolver.resolveComponentTypeToClassName(
                simpleName.substring(0, 1).toLowerCase() + simpleName.substring(1)))
            .andReturn(className).anyTimes();
    }
    
    private void configurePCCM(boolean merging)
    {
        EasyMock.reset(pageClassLoaderContextManager);
        EasyMock.expect(pageClassLoaderContextManager.isMerging()).andReturn(merging).anyTimes();
        EasyMock.replay(pageClassLoaderContextManager);
    }
    
    @Test(timeOut = 5000)
    public void listen()
    {
        
        componentDependencyRegistry.setEnableEnsureClassIsAlreadyProcessed(false);
        
        add("foo", "bar");
        add("d", "a");
        add("dd", "aa");
        add("dd", "a");
        add("dd", "a");
        
        final List<String> resources = Arrays.asList("a", "aa", "none");
        
        configurePCCM(true);
        List<String> result = componentDependencyRegistry.listen(resources);
        assertEquals(0, result.size());
        
        configurePCCM(false);
        result = componentDependencyRegistry.listen(resources);
        Collections.sort(result);
        assertEquals(result, Arrays.asList("d", "dd"));
        assertEquals("bar", getDependencies("foo").iterator().next());
        assertEquals("foo", componentDependencyRegistry.getDependents("bar").iterator().next());        
        
        configurePCCM(false);
        result = componentDependencyRegistry.listen(Collections.emptyList());
        assertEquals(result, Collections.emptyList());
        assertEquals(componentDependencyRegistry.getDependents("bar").size(), 0);
        assertEquals(getDependencies("foo").size(), 0);
        
    }
    
    private Set<String> getDependencies(String className)
    {
        Set<String> dependencies = new HashSet<>();
        dependencies.addAll(componentDependencyRegistry.getDependencies(className, DependencyType.USAGE));
        dependencies.addAll(componentDependencyRegistry.getDependencies(className, DependencyType.SUPERCLASS));
        dependencies.addAll(componentDependencyRegistry.getDependencies(className, DependencyType.INJECT_PAGE));
        return Collections.unmodifiableSet(dependencies);
    }
    
    @Test
    public void dependency_methods()
    {
        
        final String foo = "foo";
        final String bar = "bar";
        final String something = "something";
        final String other = "other";
        final String fulano = "fulano";
        final String beltrano = "beltrano";
        final String sicrano = "sicrano";
        
        componentDependencyRegistry.setEnableEnsureClassIsAlreadyProcessed(false);
        
        assertEquals(
                "getDependents() should never return null", 
                Collections.emptySet(),
                getDependencies(foo));

        assertEquals(
                "getDependents() should never return null", 
                Collections.emptySet(),
                componentDependencyRegistry.getDependents(foo));

        // In Brazil, Fulano, Beltrano and Sicrano are the most used people
        // placeholder names, in that order.
        
        add(foo, bar);
        add(something, fulano);
        add(other, beltrano);
        add(other, fulano);
        add(other, fulano);
        add(bar, null);
        add(fulano, null);
        add(beltrano, null);
        add(beltrano, sicrano);
        add(sicrano, null);
        
        assertEquals(new HashSet<>(Arrays.asList(fulano, beltrano)), getDependencies(other));
        assertEquals(new HashSet<>(Arrays.asList(fulano)), getDependencies(something));
        assertEquals(new HashSet<>(Arrays.asList()), getDependencies(fulano));
        assertEquals(new HashSet<>(Arrays.asList(bar)), getDependencies(foo));
        assertEquals(new HashSet<>(Arrays.asList()), getDependencies(bar));
        assertEquals(new HashSet<>(Arrays.asList(sicrano)), getDependencies(beltrano));

        assertEquals(new HashSet<>(Arrays.asList(foo)), componentDependencyRegistry.getDependents(bar));
        assertEquals(new HashSet<>(Arrays.asList(other, something)), componentDependencyRegistry.getDependents(fulano));
        assertEquals(new HashSet<>(Arrays.asList()), componentDependencyRegistry.getDependents(foo));
        assertEquals(new HashSet<>(Arrays.asList(other)), componentDependencyRegistry.getDependents(beltrano));
        assertEquals(new HashSet<>(Arrays.asList(beltrano)), componentDependencyRegistry.getDependents(sicrano));
        
        assertEquals(new HashSet<>(Arrays.asList(bar, fulano, sicrano)), 
                componentDependencyRegistry.getRootClasses());
        
        assertTrue(componentDependencyRegistry.contains(foo));
        assertTrue(componentDependencyRegistry.contains(bar));
        assertTrue(componentDependencyRegistry.contains(other));
        assertTrue(componentDependencyRegistry.contains(something));
        assertTrue(componentDependencyRegistry.contains(fulano));
        assertTrue(componentDependencyRegistry.contains(beltrano));
        assertTrue(componentDependencyRegistry.contains(sicrano));
        assertFalse(componentDependencyRegistry.contains("blah"));

        assertTrue(componentDependencyRegistry.getClassNames().contains(foo));
        assertTrue(componentDependencyRegistry.getClassNames().contains(bar));
        assertTrue(componentDependencyRegistry.getClassNames().contains(other));
        assertTrue(componentDependencyRegistry.getClassNames().contains(something));
        assertTrue(componentDependencyRegistry.getClassNames().contains(fulano));
        assertTrue(componentDependencyRegistry.getClassNames().contains(beltrano));
        assertTrue(componentDependencyRegistry.getClassNames().contains(sicrano));
        assertFalse(componentDependencyRegistry.getClassNames().contains("blah"));
        
        // Testing the clear method
        componentDependencyRegistry.clear(beltrano);
        
        assertEquals(new HashSet<>(Arrays.asList(fulano)), getDependencies(other));
        assertEquals(new HashSet<>(Arrays.asList(fulano)), getDependencies(something));
        assertEquals(new HashSet<>(Arrays.asList()), getDependencies(fulano));
        assertEquals(new HashSet<>(Arrays.asList(bar)), getDependencies(foo));
        assertEquals(new HashSet<>(Arrays.asList()), getDependencies(bar));
        assertEquals(new HashSet<>(Arrays.asList()), getDependencies(sicrano));

        assertEquals(new HashSet<>(Arrays.asList(foo)), componentDependencyRegistry.getDependents(bar));
        assertEquals(new HashSet<>(Arrays.asList(other, something)), componentDependencyRegistry.getDependents(fulano));
        assertEquals(new HashSet<>(Arrays.asList()), componentDependencyRegistry.getDependents(foo));
        assertEquals(new HashSet<>(Arrays.asList()), componentDependencyRegistry.getDependents(beltrano));
        assertEquals(new HashSet<>(Arrays.asList()), componentDependencyRegistry.getDependents(sicrano));
        
        assertEquals(new HashSet<>(Arrays.asList(bar, fulano, sicrano)), componentDependencyRegistry.getRootClasses());
        
        assertTrue(componentDependencyRegistry.contains(foo));
        assertTrue(componentDependencyRegistry.contains(bar));
        assertTrue(componentDependencyRegistry.contains(other));
        assertTrue(componentDependencyRegistry.contains(something));
        assertTrue(componentDependencyRegistry.contains(fulano));
        assertTrue(componentDependencyRegistry.contains(sicrano));
        assertFalse(componentDependencyRegistry.contains(beltrano));
        assertFalse(componentDependencyRegistry.contains("blah"));

        assertTrue(componentDependencyRegistry.getClassNames().contains(foo));
        assertTrue(componentDependencyRegistry.getClassNames().contains(bar));
        assertTrue(componentDependencyRegistry.getClassNames().contains(other));
        assertTrue(componentDependencyRegistry.getClassNames().contains(something));
        assertTrue(componentDependencyRegistry.getClassNames().contains(fulano));
        assertTrue(componentDependencyRegistry.getClassNames().contains(sicrano));
        assertFalse(componentDependencyRegistry.getClassNames().contains(beltrano));
        assertFalse(componentDependencyRegistry.getClassNames().contains("blah"));
        
    }
    
    @Test
    public void get_all_non_page_dependencies()
    {
        
        final String page = "page";
        final String className = "root";
        final String superclass = "superclass";
        final Set<String> expectedAllNonPageDependencies = new HashSet<>();
        final BiConsumer<String, String> add = (c, d) -> {
            expectedAllNonPageDependencies.add(d);
            add(c, d, DependencyType.USAGE);
        };
        
        componentDependencyRegistry.setEnableEnsureClassIsAlreadyProcessed(false);
        
        add(className, page, DependencyType.INJECT_PAGE);
        
        add(className, superclass, DependencyType.SUPERCLASS);
        expectedAllNonPageDependencies.add(superclass);
        
        add.accept(className, "child1");
        add.accept(className, "child2");
        add.accept("child1", "child1.1");
        add.accept("child2", "child2.1");
        add.accept("child2.1", className);
        add.accept(superclass, "child2.1");
        add.accept(superclass, "superclassDependency");
        
        expectedAllNonPageDependencies.remove(className);

        final Set<String> allNonPageDependencies = componentDependencyRegistry.getAllNonPageDependencies(className);
        assertFalse(allNonPageDependencies.contains(className));
        assertEquals(expectedAllNonPageDependencies, allNonPageDependencies);
        
    }
    
    @Test
    public void register()
    {
        
        componentDependencyRegistry.setEnableEnsureClassIsAlreadyProcessed(true);
        
        componentDependencyRegistry.clear();
        
        // Dynamic dependency definitions
        componentDependencyRegistry.register(PropertyDisplay.class);
        assertDependencies(PropertyDisplay.class, AbstractPropertyOutput.class);

        componentDependencyRegistry.register(PropertyEditor.class);
        assertDependencies(PropertyEditor.class);
        
        // Superclass
        componentDependencyRegistry.register(EventLink.class);

        // Superclass, recursively
        assertDependencies(AbstractComponentEventLink.class, AbstractLink.class);
        assertDependencies(AbstractLink.class);        
        
        // @InjectComponent 
        // Components declared in templates
        componentDependencyRegistry.register(AlertsDemo.class);
        assertDependencies(AlertsDemo.class, Zone.class, // @InjectComponent
                // Components declared in template
                Border.class, BeanEditForm.class, Zone.class, If.class, 
                ActionLink.class, ErrorComponent.class, EventLink.class);
        
        // Mixins defined in in templates (t:mixins="...").
        componentDependencyRegistry.register(MixinParameterDefault.class);
        assertDependencies(MixinParameterDefault.class, AltTitleDefault.class, // Mixin
                ActionLink.class, Border.class); // Components declared in template);
        
        // @Component, type() not defined
        componentDependencyRegistry.register(OuterAny.class);
        assertDependencies(OuterAny.class, Any.class);
        
        // @Component, type() defined
        componentDependencyRegistry.register(AtComponentType.class);
        assertDependencies(AtComponentType.class, TextField.class, Border.class);

        // @Mixin, type() not defined
        componentDependencyRegistry.register(AbstractTextField.class);
        assertDependencies(AbstractTextField.class, 
                RenderDisabled.class, AbstractField.class);

        // @Mixin, type() defined
        componentDependencyRegistry.register(TextOnlyOnDisabledTextField.class);
        assertDependencies(TextOnlyOnDisabledTextField.class, 
                TextOnlyOnDisabled.class, TextField.class);
        
        // @MixinClasses and @Mixins
        componentDependencyRegistry.register(InstanceMixinDependencies.class);
        assertDependencies(InstanceMixinDependencies.class, 
                EchoValue.class, EchoValue2.class, TextField.class);

        // Templates with <t:extension-point>
        componentDependencyRegistry.register(BaseLayoutPage.class);
        assertDependencies(BaseLayoutPage.class,
                Delegate.class, SubclassWithImport.class, Border.class);
        
        // Templates with <t:replace>
        componentDependencyRegistry.register(SubclassWithImport.class);
        assertDependencies(SubclassWithImport.class,
                OutputRaw.class, SuperclassWithImport.class, Loop.class);
        
        // Circular dependency: BlockHolder <-> BlockCaller
        componentDependencyRegistry.register(BlockHolder.class);
        assertDependencies(BlockHolder.class, 
                BlockCaller.class, Loop.class, ActionLink.class);
        assertDependencies(BlockCaller.class, 
                BlockHolder.class, Border.class, PageLink.class, Delegate.class);
        
    }
    
    private void assertDependencies(Class<?> clasz, Class<?>... dependencies) {
        assertEquals(
                setOf(dependencies),
                getDependencies(clasz.getName()));
    }

    private static Set<String> setOf(Class<?> ... classes)
    {
        return Arrays.asList(classes).stream()
            .map(Class::getName)
            .collect(Collectors.toSet());
    }

    private void add(String component, String dependency)
    {
        add(component, dependency, DependencyType.USAGE);
    }
    
    private void add(String component, String dependency, DependencyType type)
    {
        componentDependencyRegistry.add(component, dependency, type, true);
    }

    @SuppressWarnings("hiding")
    private static final class MockMappedConfiguration<String, URL> implements MappedConfiguration<String, URL>
    {
        
        private final Map<String, URL> map = new HashMap<>();

        @Override
        public void add(String key, URL value) 
        {
            map.put(key, value);
        }

        @Override
        public void override(String key, URL value) 
        {
            throw new UnsupportedOperationException();
        }

        @Override
        public void addInstance(String key, Class<? extends URL> clazz) 
        {
            throw new UnsupportedOperationException();
        }

        @Override
        public void overrideInstance(String key, Class<? extends URL> clazz) 
        {
            throw new UnsupportedOperationException();            
        }
        
    }
    
    final private static class FileResource extends AbstractResource
    {
        
        final private File file;
        
        final private String path;

        public FileResource(String path, File file) 
        {
            super(path);
            this.file = file;
            this.path = path;
        }

        @Override
        public java.net.URL toURL() 
        {
            try 
            {
                final File actualFile = new File(file, path);
                return actualFile.exists() ? actualFile.toURL() : null;
            } catch (MalformedURLException e) 
            {
                throw new RuntimeException(e);
            }
        }

        @Override
        protected Resource newResource(String path) 
        {
            return new FileResource(path, file);
        }

        @Override
        public String toString() 
        {
            return "FileResource [file=" + file + "/" + path + "]";
        }
        
    }
    
}
