// (c) Copyright [2018] EntIT Software LLC, a Micro Focus Company
//
// Licensed under the Apache License, Version 2.0 (the "License");
// You may not use this file except in compliance with the License.
// You may obtain a copy of the License at
//
// http://www.apache.org/licenses/LICENSE-2.0
//
// Apache License 2.0 - Apache Software Foundation
// www.apache.org
// Apache License Version 2.0, January 2004 http://www.apache.org/licenses/ TERMS AND CONDITIONS FOR USE, REPRODUCTION ...
//
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.

package com.hpe.leanft.selenium;

import org.openqa.selenium.*;
import org.openqa.selenium.internal.WrapsDriver;
import org.openqa.selenium.remote.RemoteWebDriver;
import org.openqa.selenium.remote.RemoteWebElement;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.regex.Pattern;

class InternalUtils {
    static String getResourceFile(String fileName) {
        StringBuilder jsMethodStringBuilder = new StringBuilder();

        try (InputStream resourceStream = Thread.currentThread().getContextClassLoader().getResourceAsStream(fileName);
             InputStreamReader inputStreamReader = new InputStreamReader(resourceStream, StandardCharsets.UTF_8);
             BufferedReader reader = new BufferedReader(inputStreamReader)) {
            String line;
            jsMethodStringBuilder.append(reader.readLine());
            while ((line = reader.readLine()) != null) {
                jsMethodStringBuilder.append("\n").append(line);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        return jsMethodStringBuilder.toString();
    }

    /**
     * Converts the Java Pattern flags property to string.
     */
    static String flagsToString(Pattern pattern) {
        String flags = "";

        boolean isTrue = (pattern.flags() & Pattern.CASE_INSENSITIVE) == Pattern.CASE_INSENSITIVE;

        if (isTrue) {
            flags += "i";
        }

        return flags;
    }

    static RemoteWebDriver getDriver(SearchContext context) {
        if (context instanceof RemoteWebDriver) {
            return (RemoteWebDriver) context;
        }

        if (context instanceof WebElement) {
            return (RemoteWebDriver) ((WrapsDriver) context).getWrappedDriver();
        }

        return null;
    }

    static JavascriptExecutor getExecutor(SearchContext context) {

        if (context instanceof JavascriptExecutor) {
            return (JavascriptExecutor) context;
        }

        if (!(context instanceof WebElement)) {
            throw new UnsupportedOperationException(
                    "Cannot find driver of this element.");
        }


        WebDriver wrapsDriver = ((WrapsDriver) context).getWrappedDriver();

        return (JavascriptExecutor) wrapsDriver;
    }

    static void setFoundBy(List<WebElement> matchedElements, SearchContext context, String by, String using) {
        RemoteWebElement rwe = (RemoteWebElement) matchedElements.get(0);
        try {
            Method setFoundBy = rwe.getClass().getDeclaredMethod("setFoundBy", SearchContext.class, String.class, String.class);
            setFoundBy.setAccessible(true);

            for (Object element : matchedElements) {
                setFoundBy.invoke(element, context, by, using);
            }
        } catch (NoSuchMethodException | InvocationTargetException | IllegalAccessException e) {
            e.printStackTrace();
        }
    }

    static boolean isNullOrEmpty(String str) {
        return (str == null || str.trim().isEmpty());
    }

    static boolean isVisible(WebElement element) {

        Dimension eleSize = element.getSize();
        Point eleLocation = element.getLocation();

        WebDriver wrapsDriver = ((WrapsDriver) element).getWrappedDriver();
        Dimension winSize = wrapsDriver.manage().window().getSize();

        return !((eleSize.width + eleLocation.x > winSize.width) || (eleSize.height + eleLocation.y > winSize.height));
    }
}