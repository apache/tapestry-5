// Copyright 2006, 2007, 2008, 2009 The Apache Software Foundation
//
// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at
//
//     http://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.

package org.apache.tapestry5.integration.app1.pages;

import org.apache.tapestry5.ComponentResources;
import org.apache.tapestry5.Link;
import org.apache.tapestry5.annotations.InjectPage;
import org.apache.tapestry5.ioc.annotations.Inject;
import org.apache.tapestry5.ioc.internal.util.CollectionFactory;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 * Have to start somewhere!
 */
public class Index
{
    public static class Item implements Comparable<Item>
    {
        private final String pageName;
        private final String label;
        private final String description;

        public Item(String pageName, String label, String description)
        {
            this.pageName = pageName;
            this.label = label;
            this.description = description;
        }

        public String getPageName()
        {
            return pageName;
        }

        public String getLabel()
        {
            return label;
        }

        public String getDescription()
        {
            return description;
        }

        public int compareTo(Item o)
        {
            return label.compareTo(o.label);
        }
    }

    private static final List<Item> ITEMS = CollectionFactory.newList(

            new Item("MultiLevelInheritDemo", "Multi-Level Inherit Demo",
                     "Use of inherit: binding prefix across three levels"),

            new Item("HiddenDemo", "Hidden Demo", "Demo the use of the Hidden component."),

            new Item("FormZoneDemo", "Form Zone Demo", "Use a form to update a zone."),

            new Item("ZoneUpdateNamespace", "Zone/Namespace Interaction", "Prove that TAP5-573 is fixed"),

            new Item("AbstractComponentDemo", "Abstract Component Demo", "Error when a component is abstract"),

            new Item("TemplateOverrideDemo", "Template Override Demo",
                     "Child component extends and overrides parent template."),

            new Item("MultiZoneUpdateDemo", "Multiple Zone Update Demo",
                     "A single request can now update multiple Zones"),

            new Item("LinkSubmitInZoneDemo", "LinkSubmit inside Zone",
                     "Ensure that a LinkSubmit works correctly when its containing Form updates a Zone"),

            new Item("SlowAjaxDemo", "Slow Ajax Demo", "Handling of client-side Ajax before the page is fully loaded"),

            new Item("ProgressiveDemo", "ProgressiveDisplay Demo", "Progressive Enhancement via a component"),

            new Item("ClientNumericValidationDemo", "Client-Side Numeric Validation",
                     "Client-side locale-specific validation"),

            new Item("PublishParametersDemo", "Publish Parameters Demo",
                     "Use of @Component.publishParameters attribute."),

            new Item("LinkSubmitDemo", "LinkSubmit Demo", "JavaScript LinkSubmit component"),

            new Item("PerFormValidationMessageDemo", "Per-Form Validation Messages",
                     "Per-form configuration of validation messages and constraints."),

            new Item("EmptyLoopDemo", "Empty Loop Demo", "Use of empty parameter with the Loop component."),

            new Item("BlankPasswordDemo", "Blank Password Demo",
                     "Show that a blank value in a PasswordField does not update the server side value."),

            new Item("GridFormEncoderDemo", "Grid Form Encoder Demo",
                     "Grid inside a Form using the PrimaryKeyEncoder option"),

            new Item("DateFieldAjaxFormLoop", "DateField inside AjaxFormLoop",
                     "Show that DateField component works correctly inside AjaxFormLoop"),

            new Item("NestedForm", "Nested Form Demo", "Error when a Form is nested inside another Form."),

            new Item("UnhandledEventDemo", "Unhandled Event Demo",
                     "Events that don't have matching event handlers cause exceptions"),

            new Item("PrimitiveDefaultDemo", "Primitive Default Demo",
                     "Primitive value returned from parameter default method"),

            new Item("ValidateFormValidationExceptionDemo",
                     "ValidationForm ValidationException Demo",
                     "Throwing a ValidationException from the validateForm event handler."),

            new Item("ClientFormatDemo", "Client Format Validation", "Client-side input format validation"),

            new Item("ShortGrid", "Short Grid",
                     "Grid where the number of claimed rows is less than the number of actual rows"),

            new Item("NullParameterDemo", "Null Parameter Demo", "Binding a not-null parameter to null."),

            new Item("nestedbeaneditor", "Nested BeanEditor",
                     "BeanEditor as override for property editor in BeanEditForm"),

            new Item("actionpage", "Action Page", "tests fixture for ActionLink component"),

            new Item("cleancachedemo", "Clean Cache Demo", "cache cleared properly during Ajax calls"),

            new Item("numberbeaneditordemo", "Number BeanEditor Demo",
                     "use of nulls and wrapper types with BeanEditor"),

            new Item("forminjectordemo", "FormInjector Demo", "extending a form dynamically via Ajax"),

            new Item("music", "Music Page", "demo handling of edge cases of page naming"),

            new Item("PersistentDemo", "Persistent Demo", "storing and clearing persistent properties"),

            new Item("ActionViaLinkDemo", "Action via Link Demo", "tests creating an action link explicitly"),

            new Item("FormFragmentDemo", "Form Fragment Demo", "page with dynamic form sections"),

            new Item("BooleanDemo", "Boolean Property Demo", "demo boolean properties using both is and get prefixes"),

            new Item("DeleteFromGridDemo", "Delete From Grid", "demo deleting items form a Grid"),

            new Item("RenderErrorDemo", "Render Error Demo", "reporting of errors while rendering"),

            new Item("nested/AssetDemo", "AssetDemo", "declaring an image using Assets"),

            new Item("nested/ActionDemo", "Action With Context Demo",
                     "using action links with context on page with activation context"),

            new Item("blockdemo", "BlockDemo", "use of blocks to control rendering"),

            new Item("countdown", "Countdown Page", "defining component using @Component annotation"),

            new Item("injectdemo", "Inject Demo", "use of various kinds of injection"),

            new Item("instancemixin", "InstanceMixin", "mixin added to a particular component instance"),

            new Item("TextFieldWrapperTypeDemo", "TextField Wrapper Types",
                     "use of TextField to edit numeric wrapper types (not primitives) "),

            new Item("EnvironmentalDemo", "Environmental Annotation Usage",
                     "Storing and retrieving Environmental values"),

            new Item("Expansion", "Expansion Page", "Use of expansions in templates"),

            new Item("ExpansionSubclass", "ExpansionSubclass", "components can inherit templates from base classes"),

            new Item("Localization", "Localization", "access localized messages from the component catalog"),

            new Item("NumberSelect", "NumberSelect", "passivate/activate page context demo"),

            new Item("ParameterConflict", "Template Overridden by Class Page",
                     "Parameters in the class override those in the template"),

            new Item("ParameterDefault", "ParameterDefault", "defaulter methods for component parameters"),

            new Item("passwordfielddemo", "PasswordFieldDemo", "test for the PasswordField component"),

            new Item("rendercomponentdemo", "RenderComponentDemo",
                     "components that \"nominate\" other components to render"),

            new Item("renderphaseorder", "RenderPhaseOrder", "order of operations when invoking render phase methods"),

            new Item("simpleform", "SimpleForm", "first pass at writing Form and TextField components"),

            new Item("validform", "ValidForm", "server-side input validation"),

            new Item("ToDoListVolatile", "ToDo List (Volatile)", "Loops and Submit inside Form, volatile mode"),

            new Item("MissingTemplate", "Missing Template Demo",
                     "Demo for what happens when a template is not found for a page"),

            new Item("nested/zonedemo", "Zone Demo", "dynamic updates within a page"),

            new Item("todolist", "ToDo List", "Loops and Submit inside Form using primary key encoder"),

            new Item("flashdemo", "FlashDemo", "demonstrate 'flash' persistence"),

            new Item("beaneditordemo", "BeanEditor Demo", "demonstrate the BeanEditor mega-component"),

            new Item("pageloadeddemo", "PageLoaded Demo", "shows that page lifecycle methods are invoked"),

            new Item("griddemo", "Grid Demo", "default Grid component"),

            new Item("nullgrid", "Null Grid", "handling of null source for Grid"),

            new Item("gridsetdemo", "Grid Set Demo", "handling of Set sources for Grid"),

            new Item("gridenumdemo", "Grid Enum Demo", "handling of enum types in the Grid"),

            new Item("GridRemoveReorderDemo", "Grid Remove/Reorder Demo", "handling of remove and reorder parameters"),

            new Item("protected", "Protected Page",
                     "Demonstrate result of non-void return from a page's activate method"),

            new Item("Kicker", "Kicker", "demos complex page and component context in links"),

            new Item("simpletrackgriddemo", "SimpleTrack Grid Demo",
                     "customizing the model for a Grid around an interface"),

            new Item("pagelinkcontext", "PageLink Context Demo", "passing explicit context in a page render link"),

            new Item("pagecontextinform", "Page Context in Form", "passivate/activate page context in Form"),

            new Item("ValidBeanEditorDemo", "Client Validation Demo", "BeanEditor with validation enabled"),

            new Item("Unreachable", "Unreachable Page", "page not reachable due to IgnoredPathsFilter"),

            new Item("renderabledemo", "Renderable Demo", "shows that render phase methods can return a Renderable"),

            new Item("inheritedbindingsdemo", "Inherited Bindings Demo",
                     "Tests for components that inherit bindings from containing components"),

            new Item("ClientPersistenceDemo", "Client Persistence Demo",
                     "component field values persisted on the client side"),

            new Item("attributeExpansionsDemo", "Attribute Expansions Demo",
                     "use expansions inside attributes of ordinary elements"),

            new Item("PaletteDemo", "Palette Demo", "multiple selection component"),

            new Item("ReturnTypes", "Return Types", "tests various event handler return types"),

            new Item("FormEncodingType", "Form Encoding Type", "Test ability to set an encoding type for a Form"),

            new Item("RadioDemo", "RadioDemo", "Use of the RadioGroup and Radio components"),

            new Item("RegexpDemo", "Regexp Demo", "Use of the Regexp validator"),

            new Item("BeanEditRemoveReorder", "BeanEdit Remove/Reorder",
                     "Use of the remove and reorder parameters with BeanEditForm"),

            new Item("MultiBeanEditDemo", "MultiBeanEdit Demo", "Multiple BeanEditor components in a single form"),

            new Item("GridFormDemo", "Grid Form Demo", "Grid operating inside a Form"),

            new Item("DateFieldDemo", "DateField Demo", "using DateField by itself on a page"),

            new Item("BeanEditDateDemo", "BeanEditor / Date Demo",
                     "Use of date properties inside BeanEditor and BeanDisplay"),

            new Item("eventmethodtranslate", "EventMethod Translator",
                     "Demo ability to provide toclient and parseclient event handler methods"),

            new Item("autocompletedemo", "Autocomplete Mixin Demo", "Demo the autocomplete mixin for text fields"),

            new Item("componentparameter", "ComponentParameter Demo",
                     " Demo using a component type as a parameter type and succesfuly passing a component"),

            new Item("inheritinformalsdemo", "Inherit Informal Parameters Demo",
                     "Demo a component which inherits informal parameters from its container"),

            new Item("disabledfields", "Disabled Fields",
                     "Demonstrate a bunch of disabled fields, to verify that the RenderDisabled mixin works and is being used properly"),

            new Item("BeanEditorOverride", "BeanEditor Override",
                     "Property editor overrides work for the BeanEditor component itself (not just the BeanEditForm component)"),

            new Item("varbindingdemo", "Var Binding Demo", "use of the var: binding prefix"),

            new Item("leangriddemo", "Lean Grid Demo",
                     "Grid component with lean parameter turned on, to eliminate CSS class attributes in TD and TH elements"),

            new Item("blockcaller", "Action Links off of Active Page",
                     "Actions can exist on pages other than the active page, via Blocks."),

            new Item("unlessdemo", "Unless Demo", "use of the Unless component"),

            new Item("MagicValueEncoder", "Magic ValueEncoder Demo",
                     "Automatic creation of ValueEncoder using the TypeCoercer"),

            new Item("NullStrategyDemo", "Null Field Strategy Demo", "use of the nulls parameter of TextField"),

            new Item("OverrideValidationDecorator", "Override Validation Decorator",
                     "override the default validation decorator"),

            new Item("ExceptionEventDemo", "Exception Event Demo", "handling component event exceptions"),

            new Item("AddedGridColumnsDemo", "Added Grid Columns Demo", "programatically adding grid columns"),

            new Item("PrimitiveArrayParameterDemo", "Primitive Array Parameter Demo",
                     "use primitive array as parameter type"),

            new Item("RenderPhaseMethodExceptionDemo", "Render Phase Method Exception Demo",
                     "render phase methods may throw checked exceptions"),

            new Item("TrackEditor", "Generic Page Class Demo",
                     "demo use of generics with component classes and, particularily, with property types"),

            new Item("IndirectProtectedFields", "Protected Fields Demo",
                     "demo exception when component class contains protected fields"),

            new Item("injectcomponentdemo", "Inject Component Demo",
                     "inject component defined in template"),

            new Item("cachedpage", "Cached Annotation", "Caching method return values"),

            new Item("cachedpage2", "Cached Annotation2", "Caching method return values w/ inheritence"),

            new Item("inplacegriddemo", "In-Place Grid Demo", "Grid that updates in-place using Ajax"),

            new Item("methodadvicedemo", "Method Advice Demo", "Advising component methods."),

            new Item("HasBodyDemo", "Has Body Demo", "Verify the hasBody() method of ComponentResources"),

            new Item("BeanEditorBeanEditContext", "BeanEditor BeanEditContext",
                     "BeanEditContext is pushed into enviroment by BeanEditor."),

            new Item("InformalParametersDemo", "Informal Parameters Demo",
                     "Access to informal parameters names and values"),

            new Item("FormFieldOutsideForm", "Form Field Outside Form",
                     "Nice exception message for common problem of form fields outside forms"),

            new Item("SubmitWithContext", "Submit With Context",
                     "Providing a context for Submit component")
    );

    static
    {
        Collections.sort(ITEMS);
    }

    private Item item;

    @InjectPage
    private SecurePage securePage;

    @Inject
    private ComponentResources resources;

    public List<Item> getItems()
    {
        return ITEMS;
    }

    public Item getItem()
    {
        return item;
    }

    public void setItem(Item item)
    {
        this.item = item;
    }

    Object onActionFromSecurePage()
    {
        return securePage.initialize("Triggered from Index");
    }

    public Link getInjectDemoLink()
    {
        return resources.createPageLink(InjectDemo.class, false);
    }

    public List getDemoContext()
    {
        return Arrays.asList(1, 2, 3);
    }

    /* This will fail, because component classes are not instantiable. */
    public Object onActionFromInstantiatePage()
    {
        return new Music();
    }
}
