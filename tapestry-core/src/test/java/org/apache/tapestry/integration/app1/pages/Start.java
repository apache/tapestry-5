// Copyright 2006, 2007, 2008 The Apache Software Foundation
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

package org.apache.tapestry.integration.app1.pages;

import org.apache.tapestry.annotations.InjectPage;
import org.apache.tapestry.ioc.internal.util.CollectionFactory;

import java.util.Collections;
import java.util.List;

/**
 * Have to start somewhere!
 */
public class Start
{
    public static class Item implements Comparable<Item>
    {
        private final String _pageName;
        private final String _label;
        private final String _description;

        public Item(String pageName, String label, String description)
        {
            _pageName = pageName;
            _label = label;
            _description = description;
        }

        public String getPageName()
        {
            return _pageName;
        }

        public String getLabel()
        {
            return _label;
        }

        public String getDescription()
        {
            return _description;
        }

        public int compareTo(Item o)
        {
            return _label.compareTo(o._label);
        }
    }

    private static final List<Item> ITEMS = CollectionFactory.newList(
            new Item("actionpage", "Action Page", "tests fixture for ActionLink component"),

            new Item("PersistentDemo", "Persistent Demo", "storing and clearing persistent properties"),

            new Item("ActionViaLinkDemo", "Action via Link Demo", "tests creating an action link explicitly"),

            new Item("FormFragmentDemo", "Form Fragment Demo", "page with dynamic form sections"),

            new Item("BooleanDemo", "Boolean Property Demo", "demo boolean properties using both is and get prefixes"),

            new Item("DeleteFromGridDemo", "Delete From Grid", "demo deleting items form a Grid"),

            new Item("RenderErrorDemo", "Render Error Demo", "reporting of errors while rendering"),

            new Item("nested/AssetDemo", "AssetDemo", "declaring an image using Assets"),

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

            new Item("zonedemo", "Zone Demo", "dynamic updates within a page"),

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

            new Item("injectcomponentdemo", "Inject Component Demo",
                     "inject component defined in template"));

    static
    {
        Collections.sort(ITEMS);
    }

    private Item _item;

    public List<Item> getItems()
    {
        return ITEMS;
    }

    public Item getItem()
    {
        return _item;
    }

    public void setItem(Item item)
    {
        _item = item;
    }

    @InjectPage
    private SecurePage _securePage;

    Object onActionFromSecurePage()
    {
        return _securePage.initialize("Triggered from Start");
    }
}
