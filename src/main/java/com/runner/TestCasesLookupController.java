package com.runner;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.junit.Test;
import org.junit.runner.JUnitCore;
import org.junit.runner.Request;
import org.junit.runner.Result;
import org.reflections.Reflections;
import org.reflections.scanners.ResourcesScanner;
import org.reflections.scanners.SubTypesScanner;
import org.reflections.util.ClasspathHelper;
import org.reflections.util.ConfigurationBuilder;
import org.reflections.util.FilterBuilder;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;

import java.lang.annotation.Annotation;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.*;

@RestController
public class TestCasesLookupController {

    // inject via application.properties
    @Value("${welcome.message:test}")
    private String message = "Hello World";

    @RequestMapping("/")
    public String welcome(Map<String, Object> model) {
        model.put("message", this.message);
        return "index";
    }
    @RequestMapping("/tests")
    public JSONArray getTests(@RequestParam(required = false) final String param) {
        JSONArray tests = new JSONArray();

        List<ClassLoader> classLoadersList = new LinkedList<ClassLoader>();
        classLoadersList.add(ClasspathHelper.contextClassLoader());
        classLoadersList.add(ClasspathHelper.staticClassLoader());
        try {
            Reflections reflections = new Reflections(new ConfigurationBuilder()
                    .setScanners(new SubTypesScanner(false), new ResourcesScanner())
                    .setUrls(ClasspathHelper.forClassLoader(classLoadersList.toArray(new ClassLoader[0])))
                    .filterInputsBy(new FilterBuilder().include(FilterBuilder.prefix("com.runner.tests"))));
            Set<Class<? extends Object>> allClasses = reflections.getSubTypesOf(Object.class);
                allClasses.stream().forEach(cls -> {
                List<Method> methods = getMethodsAnnotatedWith(cls, Test.class);
                if(methods.size() > 0) { //skip the tests with no methods
                    JSONObject response = new JSONObject();
                    response.put("name", cls.getName());
                    response.put("runTests", false);
                    JSONArray methodsArray = new JSONArray();
                    methods.stream().forEach(method -> {
                        JSONObject methodObj = new JSONObject();
                        methodObj.put("methodName", method.getName());
                        methodObj.put("runMethod", false);
                        methodsArray.add(methodObj);
                    });
                    response.put("methods", methodsArray);
                    tests.add(response);
                }
            });
        }catch (Exception e) {
            throw new RuntimeException(e);
        }
        return tests;
    }


    private static List<Method> getMethodsAnnotatedWith(final Class<?> type, final Class<? extends Annotation> annotation) {
        final List<Method> methods = new ArrayList<Method>();
        Class<?> klass = type;
        while (klass != Object.class) { // need to iterated thought hierarchy in order to retrieve methods from above the current instance
            // iterate though the list of methods declared in the class represented by klass variable, and add those annotated with the specified annotation
            final List<Method> allMethods = new ArrayList<Method>(Arrays.asList(klass.getDeclaredMethods()));
            for (final Method method : allMethods) {
                if (method.isAnnotationPresent(annotation)) {
                    Annotation annotInstance = method.getAnnotation(annotation);
                    methods.add(method);
                }
            }
            // move to the upper class in the hierarchy in search for more methods
            klass = klass.getSuperclass();
        }
        return methods;
    }
    @RequestMapping(value = "/runTests", method = RequestMethod.POST, consumes = MediaType.APPLICATION_JSON_VALUE)
    public List<LinkedHashMap> runTests(@RequestBody final JSONObject data) throws ClassNotFoundException,
            NoSuchMethodException, InvocationTargetException, IllegalAccessException {
        List<LinkedHashMap> tests = (ArrayList) data.get("tests");
        final JUnitCore junit = new JUnitCore();
        Request req = null;
        for(LinkedHashMap test: tests) {
            try {
                Class testClass = Class.forName(test.get("name").toString());
                List<LinkedHashMap> methods = (ArrayList)test.get("methods");
                for (LinkedHashMap m : methods) {
                    req = Request.method(testClass, m.get("methodName").toString());
                    final Result result = junit.run(req);
                    m.put("result", result.wasSuccessful());
                }
            }catch (Exception e) {
               e.printStackTrace();
            }
        }
        return tests;
    }
}

