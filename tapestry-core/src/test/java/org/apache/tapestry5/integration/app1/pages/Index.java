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

package org.apache.tapestry5.integration.app1.pages;

import org.apache.tapestry5.ComponentResources;
import org.apache.tapestry5.PersistenceConstants;
import org.apache.tapestry5.annotations.InjectPage;
import org.apache.tapestry5.annotations.Persist;
import org.apache.tapestry5.annotations.Property;
import org.apache.tapestry5.commons.util.CollectionFactory;
import org.apache.tapestry5.http.Link;
import org.apache.tapestry5.ioc.annotations.Inject;
import org.apache.tapestry5.ioc.internal.util.InternalUtils;
import org.apache.tapestry5.services.PageRenderLinkSource;
import org.apache.tapestry5.services.StreamPageContent;

import java.util.*;

/**
 * Have to start somewhere!
 */
public class Index
{
    @Persist(PersistenceConstants.FLASH)
    private String alert;

    public static class Item implements Comparable<Item>
    {
        public final String pageName;
        public final String label;
        public final String description;
        public final Object[] context;

        public Item(String pageName, String label, String description, Object... context)
        {
            this.pageName = pageName;
            this.label = label;
            this.description = description;
            this.context = context;
        }

        public int compareTo(Item o)
        {
            return label.compareTo(o.label);
        }
    }

    private static final List<Item> ITEMS = CollectionFactory
            .newList(
                    
                    new Item("PublishEventDemo", "@PublishEvent Demo", "Publishing server-side events to client-side code (JavaScript)"),
                    
                    new Item("Html5DateFieldDemo", "Html5DateField Demo", "Choosing dates using the native HTML5 date picker"),

//                    new Item("ZoneFormDemo", "Zone Form Decoration", "Fields inside an Ajax-updatd Form are still decorated properly."),

                    new Item("AjaxValidationDemo", "Ajax Validation", "Demonstrated proper integration of server-side validation and client-side field decoration."),

                    new Item("OverrideEventHandlerDemo", "Event Handler Override Demo", "Event Handler methods overridden by sub-classes invoke base-class correctly."),

                    new Item("LogoSubclass", "Base class Assets in sub-classes", "Assets are resolved for the parent class if that's where the annotations are."),

                    new Item("MissingRequiredARP", "Missing Query Parameter for @ActivationRequestParameter", "Activating a page with a required @ActivationRequestParameter, but no matching query parameter, is an error."),

//                    new Item("DateFieldValidationDemo", "DateField Validation Demo",
//                            "Use of DateField component when client validation is disabled."),

                    new Item("MixinParameters54", "Strict Mixin Parameters", "In the 5.4 DTD, Parameter Mixins must be qualified with the mixin id."),

                    new Item("AsyncDemo", "Async Links and Forms Demo", "Async (XHR) Updates without a containing Zone."),

                    new Item("FormCancelActionDemo", "Form Cancel Action Demo", "FormSupport.addCancel() support"),

                    new Item("AjaxRadioDemo", "Ajax Radio Demo", "Radio components inside an Ajax form"),

                    new Item("TimeIntervalDemo", "TimeInterval Demo", "Interval component, based on Moment.js"),

                    new Item("LocalDateDemo", "LocalDate Demo", "LocalDate component, based on Moment.js"),

                    new Item("EmptyIfDemo", "Empty If Demo", "Ensure an empty If can still render."),

                    new Item("MissingAssetDemo", "Missing Asset Demo", "Error when injecting an asset that does not exist."),

                    new Item("ConfirmDemo", "Confirm Mixin Demo", "Confirm an action when clicking it."),

                    new Item("SingleErrorDemo", "Single Error", "Using Error component to customize where the errors for a field will be displayed."),

                    new Item("JavaScriptTests", "JavaScript Tests", "Client-side tests using Mocha and Chai"),

                    new Item("ModuleInitDemo", "Module-based Initialization Demo", "Invoke a module function to perform page initialization"),

                    new Item("OperationWorkerDemo", "Operation Worker Demo", "Demonstrate use of @Operation annotation on component methods"),

                    new Item("MixinParameterDefault", "Mixin Parameter with Default", "Ensure that a mixin parameter with a default value is not reported as unbound."),

                    new Item("MixinVsInformalParameter", "Mixin Parameter vs. Informal Parameter", "Informal Paramters vs. Mixin parameter of same name"),

                    new Item("inherit/childa", "TAP5-1656 Demo", "Test a reported bug in component inheritance"),

                    new Item("ComponentInsideBlockDemo", "Component Inside Block Demo", "Verify that a component, inside a block, is still an embedded "),

                    new Item("EventMethodUnmatchedComponentId", "Unmatched Component Id in Event Method Demo", "Show that referencing a component that does not exist in an event handler method name is an error."),

                    new Item("AlertsDemo", "Alerts Demo", "Managing alerts both traditional and Ajax"),

                    new Item("ClientConsoleDemo", "Client Console Demo", "Demo for the JavaScript client-side console"),

                    new Item("InvalidFormalParameterDemo", "Unmatched Formal Parameter with @Component", "Parameters specified with @Component annotation must match formal parameters"),

                    new Item("NullBindingToPrimitive", "Null Bound to Primitive Demo", "Correct exception when a primitive parameter is bound to null"),

                    new Item("TreeDemo", "Tree Component Demo", "Demo of Tree Component"),

                    new Item("TreeSelectionDemo", "Tree Component Selection Demo", "Demo of Selection with Tree Component"),

                    new Item("InvalidExpressionInDynamicTemplate", "Invalid Dynamic Expression",
                            "Invalid expression in a Dynamic Template"),

                    new Item("DynamicDemo", "Dynamic Demo", "Basic Dynamic component tests"),

                    new Item("DynamicExpansionsDemo", "Expansions in Dynamic Templates",
                            "Expansions inside Dynamic component content and attributes"),

                    new Item("PACAnnotationDemo", "PageActivationContext Demo",
                            "Shows that @PageActivationContext fields are set before calls to the activate event handler."),

                    new Item("PACMultipleAnnotationDemo", "PageActivationContext Multiple Demo",
                            "Demonstrates multiple @PageActivationContext fields."),

                    new Item("PublicFieldAccessDemo", "Public Field Access Demo", "Demonstrates TAP5-1222 fix"),

                    new Item("ActivationRequestParameterDemo", "ActivationRequestParameter Annotation Demo",
                            "Use of @ActivationRequestParameter to encode page state into query parameters"),

                    new Item("LibraryMessagesDemo", "Library Messages Demo",
                            "Demo ability to contribute additional message catalog resources to the application global catalog."),

                    new Item("MultiZoneUpdateInsideForm", "MultiZone Update inside a Form",
                            "Update multiple zones within a single Form."),

                    new Item("ZoneFormUpdateDemo", "Zone/Form Update Demo", "Updating a Zone inside a Form"),

                    new Item("MultiZoneStringBodyDemo", "MultiZone String Body Demo",
                            "Multi-zone updates in a loop using strings coerced into blocks"),

                    new Item("RenderNotificationDemo", "RenderNotification Demo", "Use of RenderNotification mixin"),

                    new Item("InjectMessagesDemo", "Inject Global Messages into Service Demo",
                            "Ensure that it is possible to inject the application global message catalog into a service"),

                    new Item("ReloadDemo", "Reloadable Service Implementation Demo",
                            "Used when manually testing service reloads"),

                    new Item("RequestParameterDemo", "RequestParameter Annotation Demo",
                            "Use of @RequestParameter annotation on event handler method parameters"),

                    new Item("CancelDemo", "Cancel Demo", "Use of the cancel option with Submit"),

                    new Item("CanceledEventDemo", "Canceled Event Demo", "Triggering of the canceled event from a form."),

                    new Item("PageResetDemo", "PageReset Annotation Demo",
                            "Use of PageReset annotation to re-initialize page state"),

                    new Item("TestOnlyServiceDemo", "Test Only Service Demo",
                            "IoC module available via web.xml configuration"),

                    new Item("RenderObjectExceptionDemo", "RenderObject Exception Demo",
                            "Demonstrate how exceptions when rendering default objects are displayed."),

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

                    new Item("ProgressiveDemo", "ProgressiveDisplay Demo", "Progressive Enhancement via a component"),

                    new Item("ClientNumericValidationDemo", "Client-Side Numeric Validation",
                            "Client-side locale-specific validation"),

                    new Item("PublishParametersDemo", "Publish Parameters Demo",
                            "Use of @Component.publishParameters attribute."),

                    new Item("LinkSubmitDemo", "LinkSubmit Demo", "JavaScript LinkSubmit component"),

                    new Item("LinkSubmitWithoutValidatorDemo", "LinkSubmit Without Validator Demo",
                            "Demonstrates that the LinkSubmit component is working without a validator on any of fields in the form"),

                    new Item("PerFormValidationMessageDemo", "Per-Form Validation Messages",
                            "Per-form configuration of validation messages and constraints."),

                    new Item("EmptyLoopDemo", "Empty Loop Demo", "Use of empty parameter with the Loop component."),

                    new Item("GenericLoopDemo", "Generic Loop Demo",
                            "Use of generic parameters with the Loop component."),

                    new Item("LoopWithMixinDemo", "Loop With Mixin Demo",
                            "Use a mixin with a Loop component."),

                    new Item("BlankPasswordDemo", "Blank Password Demo",
                            "Show that a blank value in a PasswordField does not update the server side value."),

                    new Item("GridFormEncoderDemo", "Grid Form Encoder Demo",
                            "Grid inside a Form using the ValueEncoder option"),

                    new Item("GridFormWithInitialSortMixinDemo", "Grid Form With Initial Sort Mixin Demo",
                            "Grid inside a Form using the InitialSort mixin"),

                    new Item("DateFieldAjaxFormLoop", "DateField inside AjaxFormLoop",
                            "Show that DateField component works correctly inside AjaxFormLoop"),

                    new Item("NestedForm", "Nested Form Demo", "Error when a Form is nested inside another Form."),

                    new Item("UnhandledEventDemo", "Unhandled Event Demo",
                            "Events that don't have matching event handlers cause exceptions"),

                    new Item("PrimitiveDefaultDemo", "Primitive Default Demo",
                            "Primitive value returned from parameter default method"),

                    new Item("ValidateFormValidationExceptionDemo", "ValidationForm ValidationException Demo",
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

                    new Item("BooleanDemo", "Boolean Property Demo",
                            "demo boolean properties using both is and get prefixes"),

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

                    new Item("ExpansionSubclass", "ExpansionSubclass",
                            "components can inherit templates from base classes"),

                    new Item("Localization", "Localization", "access localized messages from the component catalog"),

                    new Item("NumberSelect", "NumberSelect", "passivate/activate page context demo"),

                    new Item("ParameterConflict", "Template Overridden by Class Page",
                            "Parameters in the class override those in the template"),

                    new Item("ParameterDefault", "ParameterDefault", "defaulter methods for component parameters"),

                    new Item("passwordfielddemo", "PasswordFieldDemo", "test for the PasswordField component"),

                    new Item("rendercomponentdemo", "RenderComponentDemo",
                            "components that \"nominate\" other components to render"),

                    new Item("renderphaseorder", "RenderPhaseOrder",
                            "order of operations when invoking render phase methods"),

                    new Item("simpleform", "SimpleForm", "first pass at writing Form and TextField components"),

                    new Item("OptionGroupForm", "OptionGroupForm Demo", "Select with Option Group"),

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

                    new Item("GridInLoopDemo", "Grid In Loop Demo", "Grid inside loop with different model on each iteration"),

                    new Item("nullgrid", "Null Grid", "handling of null source for Grid"),

                    new Item("gridsetdemo", "Grid Set Demo", "handling of Set sources for Grid"),

                    new Item("gridenumdemo", "Grid Enum Demo", "handling of enum types in the Grid"),

                    new Item("GridRemoveReorderDemo", "Grid Remove/Reorder Demo",
                            "handling of remove and reorder parameters"),

                    new Item("EmptyGrid", "Empty Grid Demo", "show table for empty data sources"),
                    
                    new Item("GridEarlyPagingDemo", "Grid Early Paging", "set a Grid's current page before rendering"),

                    new Item("protected", "Protected Page",
                            "Demonstrate result of non-void return from a page's activate method"),

                    new Item("Kicker", "Kicker", "demos complex page and component context in links"),

                    new Item("simpletrackgriddemo", "SimpleTrack Grid Demo",
                            "customizing the model for a Grid around an interface"),

                    new Item("pagelinkcontext", "PageLink Context Demo",
                            "passing explicit context in a page render link"),

                    new Item("pagecontextinform", "Page Context in Form", "passivate/activate page context in Form",
                            "betty", "wilma", "context with spaces", "context/with/slashes"),

                    new Item("ValidBeanEditorDemo", "Client Validation Demo", "BeanEditor with validation enabled"),

                    new Item("Unreachable", "Unreachable Page", "page not reachable due to IgnoredPathsFilter"),

                    new Item("renderabledemo", "Renderable Demo",
                            "shows that render phase methods can return a Renderable"),

                    new Item("inheritedbindingsdemo", "Inherited Bindings Demo",
                            "Tests for components that inherit bindings from containing components"),

                    new Item("ClientPersistenceDemo", "Client Persistence Demo",
                            "component field values persisted on the client side"),

                    new Item("attributeExpansionsDemo", "Attribute Expansions Demo",
                            "use expansions inside attributes of ordinary elements"),

                    new Item("PaletteDemo", "Palette Demo", "multiple selection component"),

                    new Item("ReturnTypes", "Return Types", "tests various event handler return types"),

                    new Item("FormEncodingType", "Form Encoding Type",
                            "Test ability to set an encoding type for a Form"),

                    new Item("RadioDemo", "RadioDemo", "Use of the RadioGroup and Radio components"),

                    new Item("RegexpDemo", "Regexp Demo", "Use of the Regexp validator"),

                    new Item("BeanEditRemoveReorder", "BeanEdit Remove/Reorder",
                            "Use of the remove and reorder parameters with BeanEditForm"),

                    new Item("MultiBeanEditDemo", "MultiBeanEdit Demo",
                            "Multiple BeanEditor components in a single form"),

                    new Item("GridFormDemo", "Grid Form Demo", "Grid operating inside a Form"),

                    new Item("DateFieldDemo", "DateField Demo", "using DateField by itself on a page"),

                    new Item("BeanEditDateDemo", "BeanEditor / Date Demo",
                            "Use of date properties inside BeanEditor and BeanDisplay"),

                    new Item("eventmethodtranslate", "EventMethod Translator",
                            "Demo ability to provide toclient and parseclient event handler methods"),

                    new Item("autocompletedemo", "Autocomplete Mixin Demo",
                            "Demo the autocomplete mixin for text fields"),

                    new Item("componentparameter", "ComponentParameter Demo",
                            " Demo using a component type as a parameter type and succesfully passing a component"),

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

                    new Item("delegateinline", "Inline Delegate",
                            "Using the delegate component to create inline components"),

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

                    new Item("injectcomponentdemo", "Inject Component Demo", "inject component defined in template"),

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

                    new Item("SubmitWithContext", "Submit With Context", "Providing a context for Submit component"),

                    new Item("MessageConstraintGeneratorDemo", "Validation Constraints From Messages",
                            "Providing validators to apply from a properties file"),

                    new Item("RenderClientIdDemo", "RenderClientId Mixin",
                            "Force render of client-side id of a client element via the RenderClientId mixin"),

                    new Item("BindParameterDemo", "BindParameter mixin annotation",
                            "Accessing component parameter values from a mixin"),

                    new Item("BindParameterNoSuchParameter", "BindParameter error handling",
                            "BindParameter throws exception if the containing component doesn't have a matching parameter"),

                    new Item("BindParameterOnComponent", "BindParameter on component",
                            "Verify that BindParameter can only be used on mixin fields"),

                    new Item("MixinOrderingDemo", "Mixin Ordering Demo", "Various mixin-ordering scenarios"),

                    new Item(
                            "MissingComponentClassException",
                            "Missing Component Class Exception",
                            "Meaningful exception message thrown when component class can't be determined from template or field in containing component."),

                    new Item("SessionAttributeDemo", "SessionAttribute Demo",
                            "Annotation to map a field to a specific session attribute"),

                    new Item("BeanEditCalendarDemo", "BeanEditor / Calendar Demo",
                            "Use of calendar properties inside BeanEditor and BeanDisplay"),

                    new Item("TriggerDemo", "Trigger Demo", "Use of Trigger component"),

                    new Item("ImageSubmitDemo", "Submit with an Image Demo",
                            "Make sure that submit with the image parameter set triggers the 'selected' event."),

                    new Item("SelectZoneDemo", "Select Zone Demo", "Use a Select component to update a zone."),

                    new Item("AssetProtectionDemo", "Asset Protection Demo",
                            "AssetProtectionDispatcher is properly contributed and functioning"),

                    new Item("BeanDisplayEnumDemo", "BeanDisplay Enum Demo",
                            "User represenation of enum values is correctly read from messages"),

                    new Item("unavailablecomponentdemo", "Report Location of Unavailable Component",
                            "Report Location of Unavailable Component"),

                    new Item("discardafterdemo", "@DiscardAfter Demo", "Demo using @DiscardAfter annotation"),

                    new Item("SelectDemo", "Select Demo", "Validation decoration for Select"),

                    new Item("SelectModelFromObjectsAndPropertyNameDemo", "SelectModel from objects and property name",
                            "Creating a SelectModel from a list of objects and a label property name"),

                    new Item("SelectModelFromObjectsDemo", "SelectModel from objects",
                            "Creating a SelectModel from a list of objects"),

                    new Item("SelectModelCoercionDemo", "SelectModel coercion",
                            "Creating a SelectModel from a list of objects using coercion"),

                    new Item("DecoratePageRenderLinkDemo", "Decorate Page Render Link Demo",
                            "Decorating page render links"),

                    new Item("DecorateComponentEventLinkDemo", "Decorate Component Event Link Demo",
                            "Decorating event links"),

                    new Item("ValidatorMacroDemo", "Validator Macro Demo", "Using validator macros"),

                    new Item("AtInjectDemo", "@javax.inject.Inject Demo", "Using @javax.inject.Inject for injection"),

                    new Item("LinkQueryParameters", "Link Query Parameters Demo",
                            "Providing Query Parameters directly to link components as a map of key=parameter name, value=parameter values"),

                    new Item("ChecklistDemo", "Checklist Demo", "Use Checklist component"),

                    new Item("BeanEditFormPrepareBubbling", "BeanEditor Prepare Bubbling Demo", "Prepare event bubbling"),

                    new Item("NestedFormFragment", "Nested Form Fragment Demo", "Nesting Form Fragments work properly"),

                    new Item("MapExpressionInExpansions", "Map Expressions in Expansions Demo", "Maps can be used in expansions"),

                    new Item("ExpressionInJsFunction", "Expressions in JS Functions Demo", "Expressions can be used inside javascript functions"),

                    new Item("FormFieldFocusDemo", "FormFieldFocus (DEPRECATED) Demo", "Setting the Form focus on a specific field"),

                    new Item("FormFragmentExplicitVisibleBoundsDemo", "Form Fragment Explicit Visible Bounds Demo", "Check for form fragment parent visibility can be bounded to"),

                    new Item("OverrideFieldFocusDemo", "OverrideFieldFocus Demo", "Setting the focus in a form to a specific field"),

                    new Item("OverrideLabelClassDemo", "Override Label Class Demo", "Setting class attribute on Label component"),

                    new Item("FormLinkParameters", "FormLinkParameters Demo", "Form link parameters should be unescaped for a hidden field"),

                    new Item("KnownActivationContextDemo", "Known Activation Context Demo", "Page is displayed normally if called without context (TAP5-2070)",
                            "Exact"),

                    new Item("UnknownActivationContextDemo", "Unknown Activation Context Demo", "Page refuse to serve if called with an unknown activation context (TAP5-2070)",
                            "Unwanted", "context"),

                    new Item("ModuleConfigurationCallbackDemo", "ModuleConfigurationCallback Demo", "Shows an example of changing the Require.js configuration using JavaScriptSupport.addModuleConfigurationDemo()"),

                    new Item("PartialTemplateRendererDemo", "PartialTemplateRenderer Demo", "Shows some examples of rendering blocks and components to a String using PartialTemplateRenderer"),

                    new Item("nested/PageThatThrowsException", "Reload on nested page", "Tests a page reload from a nested page's exception report"),

                    new Item("inplacegridinloopdemo", "In-Place Grid in a Loop Demo", "In-place grid in a loop"),

                    new Item("GenericTypeDemo", "Generic bound type demo", "Tests that generic type info is available for generic bindings"),

                    new Item("FormFieldClientIdParameterDemo", "Form Field clientId Parameter Demo", "Shows and tests how to explicitly set the id of a form field component"),

                    new Item("gridwithsubmitwithcontextdemo", "Grid with Submit with context", "A grid whose rows contain a Submit component with context"),

                    new Item("textfieldwithnullvalidateparameter", "TextField with null validate parameter", "A TextField whose validate parameter is bound to null"),

                    new Item("validateCheckboxMustBeChecked", "Validate Checkbox Must Be Checked", "A form that trigger validate in " +
                            "error event on submit when checkbox is not checked"),

                    new Item("validateCheckboxMustBeUnchecked", "Validate Checkbox Must Be Unchecked", "A form that trigger validate in " +
                    		"error event on submit when checkbox is checked"),
                    
                    new Item("validateInErrorEvent", "Validate in error Event", "A form that trigger validate in " +
                            "error event on submit when textfield is empty"),

                    new Item("onactivateredirect", "OnActivateRedirect Demo", "A page that redirects to itself from"
                        + " its activation method"),

                    new Item("BeanEditorWithFormFragmentDemo", "Bean Editor With Form Fragment Demo", "TriggerFragment mixin used inside a BeanEditor"),

                    new Item("ObjectEditorDemo","Object Editor Demo","Edit Bean with address objects"),
                    
                    new Item("IfDemo","If Demo","If component with all its options")
                );

    static
    {
        Collections.sort(ITEMS);
    }

    @Property
    private Item item;

    @InjectPage
    private SecurePage securePage;

    @Inject
    private ComponentResources resources;

    @Inject
    private PageRenderLinkSource linkSource;

    @Property
    private String key;

    @Property
    private Map<String, List<Item>> alphaKeyItems;

    void setupRender()
    {
        alphaKeyItems = new TreeMap<String, List<Item>>();

        for (Item item : ITEMS)
        {
            InternalUtils.addToMapList(alphaKeyItems, item.label.substring(0, 1), item);
        }
    }

    public List<Item> itemsForKey()
    {
        return alphaKeyItems.get(key);
    }

    Object onActionFromSecurePage()
    {
        return securePage.initialize("Triggered from Index");
    }

    public Link getInjectDemoLink()
    {
        return linkSource.createPageRenderLink(InjectDemo.class);
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

    public void setAlert(String alert)
    {
        this.alert = alert;
    }

    public String getAlert()
    {
        return alert;
    }

    Object onActionFromImmediateResponse()
    {
        return new StreamPageContent(BypassActivationTarget.class).withoutActivation();
    }
}
