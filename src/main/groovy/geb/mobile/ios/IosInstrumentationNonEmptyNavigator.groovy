package geb.mobile.ios

import geb.Browser
import geb.Page
import geb.error.UndefinedAtCheckerException
import geb.error.UnexpectedPageException
import geb.navigator.AbstractNavigator
import geb.navigator.EmptyNavigator
import geb.navigator.Navigator
import geb.navigator.SelectFactory
import geb.textmatching.TextMatcher
import geb.waiting.WaitTimeoutException
import groovy.util.logging.Slf4j
import org.openqa.selenium.By
import org.openqa.selenium.WebElement
import sun.reflect.generics.reflectiveObjects.NotImplementedException

import java.util.regex.Pattern

import static java.util.Collections.EMPTY_LIST

/**
 * Created by gmueksch on 23.06.14.
 */
@Slf4j
class IosInstrumentationNonEmptyNavigator extends AbstractNavigator {

    protected final static BOOLEAN_ATTRIBUTES = ['async', 'autofocus', 'autoplay', 'checked', 'compact', 'complete',
                                                 'controls', 'declare', 'defaultchecked', 'defaultselected', 'defer', 'disabled', 'draggable', 'ended',
                                                 'formnovalidate', 'hidden', 'indeterminate', 'iscontenteditable', 'ismap', 'itemscope', 'loop',
                                                 'multiple', 'muted', 'nohref', 'noresize', 'noshade', 'novalidate', 'nowrap', 'open', 'paused',
                                                 'pubdate', 'readonly', 'required', 'reversed', 'scoped', 'seamless', 'seeking', 'selected',
                                                 'spellcheck', 'truespeed', 'willvalidate']

    protected final List<WebElement> contextElements

    IosInstrumentationNonEmptyNavigator(Browser browser, Collection<? extends WebElement> contextElements) {
        super(browser)
        this.contextElements = contextElements.toList().asImmutable()
    }

    protected Navigator navigatorFor(Collection<WebElement> contextElements) {
        browser.navigatorFactory.createFromWebElements(contextElements)
    }

    static def pat = ~/(\w+)='?([\w ]+)'?/

    @Override
    Navigator find(String selectorString) {
        log.debug "Selector: $selectorString"

        if( selectorString.startsWith("//") ) {
            return navigatorFor(browser.driver.findElements(By.xpath(selectorString)))
        }
        String all,key,value
        if (selectorString.startsWith("#")) {
            key = "id"
            value = selectorString.substring(1)
        } else {
            def m = pat.matcher(selectorString)
            if (m.matches()) {
                (all,key,value)=m[0]
                log.debug "Match for ${key}='${value}' in $selectorString"
            }
        }
        if( key && value ) {
            log.debug("Key:$key , Value: $value")
            navigatorFor browser.driver.findElements(By."$key"(value))
        }else{
            log.warn("Ether key '$key' or value '$value' is not filled")
            new EmptyNavigator()
        }
    }

    @Override
    Navigator find(Map<String, Object> predicates, String selector) {
        selector = optimizeSelector(selector, predicates)
        if (selector) {
            find(selector).filter(predicates)
        } else {
            find(predicates)
        }
    }

    @Override
    Navigator filter(String selectorString) {
        throw new NotImplementedException("no filtering by css on selendroid elements yet")
    }

    @Override
    Navigator filter(Map<String, Object> predicates) {
        navigatorFor contextElements.findAll { matches(it, predicates) }
    }

    @Override
    Navigator not(String selectorString) {
        throw new NotImplementedException("no filtering by css on selendroid elements yet")
//        navigatorFor contextElements.findAll { element ->
//            !CssSelector.matches(element, selectorString)
//        }
    }

    @Override
    Navigator not(Map<String, Object> predicates, String selectorString) {
        throw new NotImplementedException("no filtering by css on selendroid elements yet")
//        navigatorFor contextElements.findAll { element ->
//            !(CssSelector.matches(element, selectorString) && matches(element, predicates))
//        }
    }

    @Override
    Navigator not(Map<String, Object> predicates) {
        navigatorFor contextElements.findAll { element ->
            !matches(element, predicates)
        }
    }

    @Override
    Navigator getAt(int index) {
        navigatorFor(Collections.singleton(getElement(index)))
    }

    @Override
    Navigator getAt(Range range) {
        navigatorFor getElements(range)
    }

    Navigator getAt(EmptyRange range) {
        new EmptyNavigator(browser)
    }

    @Override
    Navigator getAt(Collection indexes) {
        navigatorFor getElements(indexes)
    }

    @Override
    Collection<WebElement> allElements() {
        contextElements as WebElement[]
    }

    @Override
    WebElement getElement(int index) {
        contextElements[index]
    }

    @Override
    List<WebElement> getElements(Range range) {
        contextElements[range]
    }

    List<WebElement> getElements(EmptyRange range) {
        EMPTY_LIST
    }

    @Override
    List<WebElement> getElements(Collection indexes) {
        contextElements[indexes]
    }

    @Override
    Navigator remove(int index) {
        int size = size()
        if (!(index in -size..<size)) {
            this
        } else if (size == 1) {
            new EmptyNavigator(browser)
        } else {
            navigatorFor(contextElements - contextElements[index])
        }
    }

    @Override
    Navigator next() {
        navigatorFor collectElements {
            it.findElement By.xpath("following-sibling::*")
        }
    }

    @Override
    Navigator next(String selectorString) {
        throw new NotImplementedException("no next by xpath on selendroid elements yet")
//        navigatorFor collectElements {
//            def siblings = it.findElements(By.xpath("following-sibling::*"))
//            siblings.find { CssSelector.matches(it, selectorString) }
//        }
    }

    @Override
    Navigator nextAll() {
        throw new NotImplementedException("no next by xpath on selendroid elements yet")
//        navigatorFor collectElements {
//            it.findElements By.xpath("following-sibling::*")
//        }
    }

    @Override
    Navigator nextAll(String selectorString) {
        navigatorFor collectElements {
            def siblings = it.findElements(By.xpath("following-sibling::*"))
            siblings.findAll { CssSelector.matches(it, selectorString) }
        }
    }

    @Override
    Navigator nextUntil(String selectorString) {
        throw new NotImplementedException("no next by xpath on selendroid elements yet")
//        navigatorFor collectElements { element ->
//            def siblings = element.findElements(By.xpath("following-sibling::*"))
//            collectUntil(siblings, selectorString)
//        }
    }

    @Override
    Navigator previous() {
        throw new NotImplementedException("no next by xpath on selendroid elements yet")
//        navigatorFor collectElements {
//            def siblings = it.findElements(By.xpath("preceding-sibling::*"))
//            siblings ? siblings.last() : EMPTY_LIST
//        }
    }

    @Override
    Navigator previous(String selectorString) {
        throw new NotImplementedException("no previous by xpath on selendroid elements yet")
//        navigatorFor collectElements {
//            def siblings = it.findElements(By.xpath("preceding-sibling::*")).reverse()
//            siblings.find { CssSelector.matches(it, selectorString) }
//        }
    }

    @Override
    Navigator prevAll() {
        throw new NotImplementedException("no previous by xpath on selendroid elements yet")
//        navigatorFor collectElements {
//            it.findElements(By.xpath("preceding-sibling::*"))
//        }
    }

    @Override
    Navigator prevAll(String selectorString) {
        navigatorFor collectElements {
            def siblings = it.findElements(By.xpath("preceding-sibling::*")).reverse()
            siblings.findAll { CssSelector.matches(it, selectorString) }
        }
    }

    @Override
    Navigator prevUntil(String selectorString) {
        throw new NotImplementedException("no previous by xpath on selendroid elements yet")
//        navigatorFor collectElements { element ->
//            def siblings = element.findElements(By.xpath("preceding-sibling::*")).reverse()
//            collectUntil(siblings, selectorString)
//        }
    }

    @Override
    Navigator parent() {
        navigatorFor collectElements {
            it.findElement By.xpath("parent::*")
        }
    }

    @Override
    Navigator parent(String selectorString) {
        parent().filter(selectorString)
    }

    @Override
    Navigator parents() {
        navigatorFor collectElements {
            it.findElements(By.xpath("ancestor::*")).reverse()
        }
    }

    @Override
    Navigator parents(String selectorString) {
//        navigatorFor collectElements {
//            def ancestors = it.findElements(By.xpath("ancestor::*")).reverse()
//            ancestors.findAll { CssSelector.matches(it, selectorString) }
//        }
        null
    }

    @Override
    Navigator parentsUntil(String selectorString) {
        navigatorFor collectElements { element ->
            def ancestors = element.findElements(By.xpath("ancestor::*")).reverse()
            collectUntil(ancestors, selectorString)
        }
    }

    @Override
    Navigator closest(String selectorString) {
        throw new NotImplementedException("no css on selendroid elements yet")
//        navigatorFor collectElements {
//            def parents = it.findElements(By.xpath("ancestor::*")).reverse()
//            parents.find { CssSelector.matches(it, selectorString) }
//        }
    }

    @Override
    Navigator children() {
        navigatorFor collectElements {
            it.findElements By.xpath("child::*")
        }
    }

    @Override
    Navigator children(String selectorString) {
        children().filter(selectorString)
    }

    @Override
    Navigator siblings() {
        navigatorFor collectElements {
            it.findElements(By.xpath("preceding-sibling::*")) + it.findElements(By.xpath("following-sibling::*"))
        }
    }

    @Override
    Navigator siblings(String selectorString) {
        siblings().filter(selectorString)
    }

    @Override
    boolean hasClass(String valueToContain) {
        any { valueToContain in it.classes() }
    }

    @Override
    boolean is(String tag) {
        contextElements.any { tag.equalsIgnoreCase(it.tagName) }
    }

    @Override
    boolean isDisplayed() {
        firstElement()?.displayed ?: false
    }

    @Override
    String tag() {
        firstElement().tagName
    }

    @Override
    String text() {
        firstElement().text
    }

    @Override
    String getAttribute(String name) {
        def attribute = firstElement().getAttribute(name)
        if (attribute == 'false' && name in BOOLEAN_ATTRIBUTES) {
            attribute = null
        }

        attribute == null ? "" : attribute
    }

    @Override
    List<String> classes() {
        contextElements.head().getAttribute("class")?.tokenize()?.unique()?.sort() ?: EMPTY_LIST
    }

    @Override
    def value() {
        getInputValue(contextElements.head())
    }

    @Override
    Navigator value(value) {
        setInputValues(contextElements, value)
        this
    }

    @Override
    Navigator leftShift(value) {
        contextElements.each {
            it.sendKeys value
        }
        this
    }

    @Override
    Navigator click() {
        contextElements.first().click()
        this
    }

    @Override
    Navigator click(Class<? extends Page> pageClass) {
        click()
        browser.page(pageClass)
        def at = false
        def error = null
        try {
            at = browser.verifyAt()
        } catch (AssertionError e) {
            error = e
        } catch (WaitTimeoutException e) {
            error = e
        } catch (UndefinedAtCheckerException e) {
            at = true
        } finally {
            if (!at) {
                throw new UnexpectedPageException(pageClass, error)
            }
        }
        this
    }

    @Override
    Navigator click(List<Class<? extends Page>> potentialPageClasses) {
        click()
        browser.page(*potentialPageClasses)
        this
    }

    @Override
    int size() {
        contextElements.size()
    }

    @Override
    boolean isEmpty() {
        size() == 0
    }

    @Override
    Navigator head() {
        first()
    }

    @Override
    Navigator first() {
        navigatorFor(Collections.singleton(firstElement()))
    }

    @Override
    Navigator last() {
        navigatorFor(Collections.singleton(lastElement()))
    }

    @Override
    Navigator tail() {
        navigatorFor contextElements.tail()
    }

    @Override
    Navigator verifyNotEmpty() {
        this
    }

    @Override
    Navigator unique() {
        new IosInstrumentationNonEmptyNavigator(browser, contextElements.unique(false))
    }

    @Override
    String toString() {
        contextElements*.toString()
    }

    def methodMissing(String name, arguments) {
        if (!arguments) {
            navigatorFor collectElements {
                it.findElements By.name(name)
            }
        } else {
            throw new MissingMethodException(name, getClass(), arguments)
        }
    }


    def propertyMissing(String name) {
        switch (name) {
            case ~/@.+/:
                return getAttribute(name.substring(1))
            default:
                def inputs = collectElements {
                    it.findElements(By.name(name))
                }

                if (inputs) {
                    return getInputValues(inputs)
                } else {
                    throw new MissingPropertyException(name, getClass())
                }
        }
    }

    def propertyMissing(String name, value) {
        def inputs = collectElements {
            it.findElements(By.name(name))
        }

        if (inputs) {
            setInputValues(inputs, value)
        } else {
            throw new MissingPropertyException(name, getClass())
        }
    }

    /**
     * Optimizes the selector if the predicates contains `class` or `id` keys that map to strings. Note this method has
     * a side-effect in that it _removes_ those keys from the predicates map.
     */
    protected String optimizeSelector(String selector, Map<String, Object> predicates) {
        if (!selector) {
            return selector
        }

        def buffer = new StringBuilder(selector)
        if (predicates.containsKey("id") && predicates["id"] in String) {
            buffer << "#" << CssSelector.escape(predicates.remove("id"))
        }
        if (predicates.containsKey("class") && predicates["class"] in String) {
            predicates.remove("class").split(/\s+/).each { className ->
                buffer << "." << CssSelector.escape(className)
            }
        }
        if (buffer[0] == "*" && buffer.length() > 1) buffer.deleteCharAt(0)
        return buffer.toString()
    }

    protected boolean matches(WebElement element, Map<String, Object> predicates) {
        def result = predicates.every { name, requiredValue ->
            def actualValue
            switch (name) {
                case "text": actualValue = element.text; break
                case "class": actualValue = element.getAttribute("class")?.tokenize(); break
                default: actualValue = element.getAttribute(name)
            }
            matches(actualValue, requiredValue)
        }
        result
    }

    protected boolean matches(String actualValue, String requiredValue) {
        actualValue == requiredValue
    }

    protected boolean matches(String actualValue, Pattern requiredValue) {
        actualValue ==~ requiredValue
    }

    protected boolean matches(String actualValue, TextMatcher matcher) {
        matcher.matches(actualValue)
    }

    protected boolean matches(Collection<String> actualValue, String requiredValue) {
        requiredValue in actualValue
    }

    protected boolean matches(Collection<String> actualValue, Pattern requiredValue) {
        actualValue.any { it ==~ requiredValue }
    }

    protected boolean matches(Collection<String> actualValue, TextMatcher matcher) {
        actualValue.any { matcher.matches(it) }
    }

    protected getInputValues(Collection<WebElement> inputs) {
        def values = []
        inputs.each { WebElement input ->
            def value = getInputValue(input)
            if (value != null) values << value
        }
        return values.size() < 2 ? values[0] : values
    }

    protected getInputValue(WebElement input) {
        def value = null
        def type = input.getAttribute("type")
        if (input.tagName == "select") {
            def select = new SelectFactory().createSelectFor(input)
            if (select.multiple) {
                value = select.allSelectedOptions.collect { getValue(it) }
            } else {
                value = getValue(select.firstSelectedOption)
            }
        } else if (type in ["checkbox", "radio"]) {
            if (input.isSelected()) {
                value = getValue(input)
            } else {
                if (type == "checkbox") {
                    value = false
                }
            }
        } else {
            value = getValue(input)
        }
        value
    }

    protected void setInputValues(Collection<WebElement> inputs, value) {
        inputs.each { WebElement input ->
            setInputValue(input, value)
        }
    }

    protected void setInputValue(WebElement input, value) {
        def attrType = input.getAttribute("type")
        if (attrType == "UIASelect") {
            setSelectValue(input, value)
        } else if (attrType == "UIACheckbox") {
            if (getValue(input) == value.toString() || value == true) {
                if (!input.isSelected()) {
                    input.click()
                }
            } else if (input.isSelected()) {
                input.click()
            }
        } else if (attrType == "UIARadio") {
            if (getValue(input) == value.toString() || labelFor(input) == value.toString()) {
                input.click()
            }
        } else {
            input.clear()
            input.sendKeys value as String
        }
    }

    protected getValue(WebElement input) {
        input?.getAttribute("value")
    }

    protected setSelectValue(WebElement element, value) {
        def select = new SelectFactory().createSelectFor(element)

        if (value == null || (value instanceof Collection && value.empty)) {
            select.deselectAll()
            return
        }

        def multiple = select.multiple
        def valueStrings
        if (multiple) {
            valueStrings = (value instanceof Collection ? new LinkedList(value) : [value])*.toString()
        } else {
            valueStrings = [value.toString()]
        }

        for (valueString in valueStrings) {
            try {
                select.selectByValue(valueString)
            } catch (org.openqa.selenium.NoSuchElementException e1) {
                try {
                    select.selectByVisibleText(valueString)
                } catch (org.openqa.selenium.NoSuchElementException e2) {
                    throw new IllegalArgumentException("couldn't select option with text or value: $valueString")
                }
            }
        }

        if (multiple) {
            def selectedOptions = select.getAllSelectedOptions()
            for (selectedOption in selectedOptions) {
                if (!valueStrings.contains(selectedOption.getAttribute("value")) && !valueStrings.contains(selectedOption.text)) {
                    selectedOption.click()
                    assert !selectedOption.isSelected()
                }
            }
        }
    }

    /**
     * This works around an inconsistency in some of the WebDriver implementations.
     * According to the spec WebElement.getAttribute should return the Strings "true" or "false"
     * however ChromeDriver and HtmlUnitDriver will return "" or null.
     */
    protected boolean getBooleanAttribute(WebElement input, String attribute) {
        !(input.getAttribute(attribute) in [null, false, "false"])
    }

    protected WebElement firstElementInContext(Closure closure) {
        def result = null
        for (int i = 0; !result && i < contextElements.size(); i++) {
            try {
                result = closure(contextElements[i])
            } catch (org.openqa.selenium.NoSuchElementException e) {
            }
        }
        result
    }

    protected List<WebElement> collectElements(Closure closure) {
        List<WebElement> list = []
        contextElements.each {
            try {
                def value = closure(it)
                switch (value) {
                    case Collection:
                        list.addAll value
                        break
                    default:
                        if (value) list << value
                }
            } catch (org.openqa.selenium.NoSuchElementException e) {
            }
        }
        list
    }

    protected Collection<WebElement> collectUntil(Collection<WebElement> elements, String selectorString) {
        int index = elements.findIndexOf { CssSelector.matches(it, selectorString) }
        index == -1 ? elements : elements[0..<index]
    }

    @Override
    boolean isDisabled() {
        def dis = firstElement().getAttribute("disabled")
        if( dis == null ){
            dis = firstElement().properties.disabled
        }
        return dis
    }

    @Override
    boolean isEnabled() {
        return !isDisabled()
    }

    @Override
    boolean isReadOnly() {
        return false
    }

    @Override
    boolean isEditable() {
        return true
    }

}