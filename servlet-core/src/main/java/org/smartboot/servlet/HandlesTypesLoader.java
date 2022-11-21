/*
 * Copyright (c) 2017-2021, org.smartboot. All rights reserved.
 * project name: smart-servlet
 * file name: HandlesTypesLoader.java
 * Date: 2021-06-27
 * Author: sandao (zhengjunweimail@163.com)
 *
 */

package org.smartboot.servlet;

import org.smartboot.servlet.third.bcel.Const;
import org.smartboot.servlet.third.bcel.classfile.AnnotationEntry;
import org.smartboot.servlet.third.bcel.classfile.ClassParser;
import org.smartboot.servlet.third.bcel.classfile.JavaClass;

import javax.servlet.ServletContainerInitializer;
import javax.servlet.annotation.HandlesTypes;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.Collections;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

/**
 * @author 三刀（zhengjunweimail@163.com）
 * @version V1.0 , 2021/6/27
 */
public class HandlesTypesLoader {
    private final Map<ServletContainerInitializer, Set<Class<?>>> initializerClassMap =
            new LinkedHashMap<>();
    /**
     * Map of Types to ServletContainerInitializer that are interested in those
     * types.
     */
    private final Map<Class<?>, Set<ServletContainerInitializer>> typeInitializerMap =
            new HashMap<>();
    private final ClassLoader classLoader;
    /**
     * Flag that indicates if at least one {@link HandlesTypes} entry is present
     * that represents an annotation.
     */
    private boolean handlesTypesAnnotations = false;
    /**
     * Flag that indicates if at least one {@link HandlesTypes} entry is present
     * that represents a non-annotation.
     */
    private boolean handlesTypesNonAnnotations = false;

    public HandlesTypesLoader(ClassLoader classLoader) {
        this.classLoader = classLoader;
    }

    public void add(ServletContainerInitializer initializer, Class handlesType) {
        typeInitializerMap.computeIfAbsent(handlesType, k -> new HashSet<>()).add(initializer);
        if (handlesType.isAnnotation()) {
            handlesTypesAnnotations = true;
        } else {
            handlesTypesNonAnnotations = true;
        }
    }

    public void clear() {
        executorService.shutdownNow();
        executorService = null;
    }

    private ExecutorService executorService = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors());

    public Map<ServletContainerInitializer, Set<Class<?>>> scanHandleTypes() {
        if (typeInitializerMap.isEmpty()) {
            return Collections.emptyMap();
        }
        if (!(classLoader instanceof URLClassLoader)) {
            return Collections.emptyMap();
        }
        Map<String, JavaClassCacheEntry> javaClassCache = new HashMap<>();
        URL[] urls = ((URLClassLoader) classLoader).getURLs();
        CountDownLatch count = new CountDownLatch(urls.length);
        for (URL url : urls) {
            executorService.execute(() -> {
                long start = System.currentTimeMillis();
                try {
                    if ("jar".equals(url.getProtocol()) || url.toString().endsWith(".jar")) {
                        processAnnotationsJar(url, javaClassCache);
                    } else if ("file".equals(url.getProtocol())) {
                        try {
                            processAnnotationsFile(
                                    new File(url.toURI()), javaClassCache);
                        } catch (URISyntaxException e) {
                            e.printStackTrace();
                        }
                    }
                } finally {
//                    System.out.println("scan cost: " + (System.currentTimeMillis() - start));
                    count.countDown();
                }
            });

        }
        try {
            count.await();
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
        javaClassCache.clear();
        return initializerClassMap;
    }

    private void processAnnotationsJar(URL url, Map<String, JavaClassCacheEntry> javaClassCache) {
        try {
            JarFile jarFile = new JarFile(url.getFile());
            Enumeration<JarEntry> entrys = jarFile.entries();

            while (entrys.hasMoreElements()) {

                JarEntry jarEntry = entrys.nextElement();

                String entryName = jarEntry.getName();

                if (entryName.endsWith(".class")) {
                    try {
                        ClassParser parser = new ClassParser(jarFile.getInputStream(jarEntry));
                        JavaClass clazz = parser.parse();
                        checkHandlesTypes(clazz, javaClassCache);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }

            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void processAnnotationsFile(File file, Map<String, JavaClassCacheEntry> javaClassCache) {

        if (file.isDirectory()) {
            // Returns null if directory is not readable
            String[] dirs = file.list();
            if (dirs != null) {
                for (String dir : dirs) {
                    processAnnotationsFile(new File(file, dir), javaClassCache);
                }
            }
        } else if (file.getName().endsWith(".class") && file.canRead()) {
            try (FileInputStream fis = new FileInputStream(file)) {
                ClassParser parser = new ClassParser(fis);
                JavaClass clazz = parser.parse();
                checkHandlesTypes(clazz, javaClassCache);
            } catch (Exception e) {
                e.printStackTrace();
            }
        } else {
            System.out.println("skip file:" + file.getAbsolutePath());
        }
    }

    private void checkHandlesTypes(JavaClass javaClass,
                                   Map<String, JavaClassCacheEntry> javaClassCache) {

        // Skip this if we can
        if (typeInitializerMap.size() == 0) {
            return;
        }

        if ((javaClass.getAccessFlags() & Const.ACC_ANNOTATION) != 0) {
            // Skip annotations.
            return;
        }

        String className = javaClass.getClassName();

        Class<?> clazz = null;
        if (handlesTypesNonAnnotations) {
            // 从 classPath 扫描并加载 class
            populateJavaClassCache(className, javaClass, javaClassCache);
            JavaClassCacheEntry entry = javaClassCache.get(className);
            if (entry == null) {
                return;
            }
            if (entry.getSciSet() == null) {
                populateSCIsForCacheEntry(entry, javaClassCache);
            }
            //当前 class 为 HandlesType 标注的接口
            if (!entry.getSciSet().isEmpty()) {
                // Need to try and load the class
                try {
                    clazz = classLoader.loadClass(className);
                    for (ServletContainerInitializer sci : entry.getSciSet()) {
                        Set<Class<?>> classes = initializerClassMap.computeIfAbsent(sci, k -> new HashSet<>());
                        classes.add(clazz);
                    }
                } catch (ClassNotFoundException e) {
                    e.printStackTrace();
                    return;
                }
            }
        }

        AnnotationEntry[] annotationEntries = javaClass.getAnnotationEntries();
        if (handlesTypesAnnotations && annotationEntries != null) {
            for (Map.Entry<Class<?>, Set<ServletContainerInitializer>> entry :
                    typeInitializerMap.entrySet()) {
                //当前类非注解
                if (!entry.getKey().isAnnotation()) {
                    continue;
                }
                String entryClassName = entry.getKey().getName();
                for (AnnotationEntry annotationEntry : annotationEntries) {
                    if (!entryClassName.equals(
                            getClassName(annotationEntry.getAnnotationType()))) {
                        continue;
                    }
                    if (clazz == null) {
                        try {
                            clazz = classLoader.loadClass(className);
                        } catch (ClassNotFoundException e) {
                            e.printStackTrace();
                            return;

                        }
                    }
                    for (ServletContainerInitializer sci : entry.getValue()) {
                        initializerClassMap.get(sci).add(clazz);
                    }
                    break;
                }
            }
        }
    }

    private void populateSCIsForCacheEntry(JavaClassCacheEntry cacheEntry,
                                           Map<String, JavaClassCacheEntry> javaClassCache) {
        Set<ServletContainerInitializer> result = new HashSet<>();

        // Super class
        String superClassName = cacheEntry.getSuperclassName();
        JavaClassCacheEntry superClassCacheEntry = javaClassCache.get(superClassName);

        // Avoid an infinite loop with java.lang.Object
        if (cacheEntry.equals(superClassCacheEntry)) {
            cacheEntry.setSciSet(Collections.emptySet());
            return;
        }

        // May be null of the class is not present or could not be loaded.
        if (superClassCacheEntry != null) {
            if (superClassCacheEntry.getSciSet() == null) {
                populateSCIsForCacheEntry(superClassCacheEntry, javaClassCache);
            }
            result.addAll(superClassCacheEntry.getSciSet());
        }
        result.addAll(getSCIsForClass(superClassName));

        // Interfaces
        for (String interfaceName : cacheEntry.getInterfaceNames()) {
            JavaClassCacheEntry interfaceEntry = javaClassCache.get(interfaceName);
            // A null could mean that the class not present in application or
            // that there is nothing of interest. Either way, nothing to do here
            // so move along
            if (interfaceEntry != null) {
                if (interfaceEntry.getSciSet() == null) {
                    populateSCIsForCacheEntry(interfaceEntry, javaClassCache);
                }
                result.addAll(interfaceEntry.getSciSet());
            }
            result.addAll(getSCIsForClass(interfaceName));
        }

        cacheEntry.setSciSet(result.isEmpty() ? Collections.emptySet() : result);
    }

    private Set<ServletContainerInitializer> getSCIsForClass(String className) {
        for (Map.Entry<Class<?>, Set<ServletContainerInitializer>> entry :
                typeInitializerMap.entrySet()) {
            Class<?> clazz = entry.getKey();
            if (!clazz.isAnnotation()) {
                if (clazz.getName().equals(className)) {
                    return entry.getValue();
                }
            }
        }
        return Collections.emptySet();
    }


    private void populateJavaClassCache(String className, JavaClass javaClass,
                                        Map<String, JavaClassCacheEntry> javaClassCache) {
        if (javaClassCache.containsKey(className) || className.startsWith("java") || className.startsWith("sun.")) {
            return;
        }

        // 缓存当前加载的类
        javaClassCache.put(className, new JavaClassCacheEntry(javaClass));

        //加载父类
        populateJavaClassCache(javaClass.getSuperclassName(), javaClassCache);

        //加载接口
        for (String interfaceName : javaClass.getInterfaceNames()) {
            populateJavaClassCache(interfaceName, javaClassCache);
        }
    }

    /**
     * 加载并缓存指定类，如果该类有父类或实现了某些接口，则递归加载
     *
     * @param className
     * @param javaClassCache
     */
    private void populateJavaClassCache(String className,
                                        Map<String, JavaClassCacheEntry> javaClassCache) {
        if (javaClassCache.containsKey(className)) {
            return;
        }
        String name = className.replace('.', '/') + ".class";
        try (InputStream is = classLoader.getResourceAsStream(name)) {
            if (is == null) {
                return;
            }
            ClassParser parser = new ClassParser(is);
            JavaClass clazz = parser.parse();
            populateJavaClassCache(clazz.getClassName(), clazz, javaClassCache);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private String getClassName(String internalForm) {
        if (!internalForm.startsWith("L")) {
            return internalForm;
        }

        // Assume starts with L, ends with ; and uses / rather than .
        return internalForm.substring(1,
                internalForm.length() - 1).replace('/', '.');
    }

    static class JavaClassCacheEntry {
        public final String superclassName;

        public final String[] interfaceNames;

        private Set<ServletContainerInitializer> sciSet = null;

        public JavaClassCacheEntry(JavaClass javaClass) {
            superclassName = javaClass.getSuperclassName();
            interfaceNames = javaClass.getInterfaceNames();
        }

        public String getSuperclassName() {
            return superclassName;
        }

        public String[] getInterfaceNames() {
            return interfaceNames;
        }

        public Set<ServletContainerInitializer> getSciSet() {
            return sciSet;
        }

        public void setSciSet(Set<ServletContainerInitializer> sciSet) {
            this.sciSet = sciSet;
        }
    }
}
