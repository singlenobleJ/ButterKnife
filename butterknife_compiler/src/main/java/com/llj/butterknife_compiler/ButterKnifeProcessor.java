package com.llj.butterknife_compiler;

import com.google.auto.service.AutoService;
import com.llj.butterknife_annotation.BindView;
import com.llj.butterknife_annotation.ViewBinding;
import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.JavaFile;
import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.ParameterSpec;
import com.squareup.javapoet.ParameterizedTypeName;
import com.squareup.javapoet.TypeSpec;

import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.Map;
import java.util.Set;

import javax.annotation.processing.AbstractProcessor;
import javax.annotation.processing.Filer;
import javax.annotation.processing.Messager;
import javax.annotation.processing.ProcessingEnvironment;
import javax.annotation.processing.Processor;
import javax.annotation.processing.RoundEnvironment;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.PackageElement;
import javax.lang.model.element.TypeElement;
import javax.lang.model.element.VariableElement;
import javax.lang.model.util.Elements;
import javax.tools.Diagnostic;

/**
 * @author: lilinjie
 * @date: 2019-10-22 13:56
 * @description: 自定义的注解处理器
 */
@AutoService(Processor.class)
public class ButterKnifeProcessor extends AbstractProcessor {

    private Elements mElementUtils;
    private Filer mFiler;
    private Messager mMessager;
    private Map<String, ClassInfo> mClassInfoMap = new HashMap<>();

    @Override
    public synchronized void init(ProcessingEnvironment processingEnvironment) {
        super.init(processingEnvironment);
        mElementUtils = processingEnvironment.getElementUtils();
        mFiler = processingEnvironment.getFiler();
        mMessager = processingEnvironment.getMessager();
    }

    @Override
    public SourceVersion getSupportedSourceVersion() {
        return SourceVersion.latestSupported();
    }

    @Override
    public Set<String> getSupportedAnnotationTypes() {
        Set<String> annotations = new LinkedHashSet<>();
        annotations.add(BindView.class.getCanonicalName());
        return annotations;
    }

    @Override
    public boolean process(Set<? extends TypeElement> set, RoundEnvironment roundEnvironment) {
        mClassInfoMap.clear();
        // 遍历所有被注解了@BindView的元素
        Set<? extends Element> elements = roundEnvironment.getElementsAnnotatedWith(BindView.class);
        for (Element element : elements) {
            //本例子只实现对Field进行注解
            if (element.getKind() != ElementKind.FIELD) {
                continue;
            }
            //包信息
            PackageElement packageElement = mElementUtils.getPackageOf(element);
            String packageName = packageElement.getQualifiedName().toString();//包名

            // 类信息
            TypeElement typeElement = (TypeElement) element.getEnclosingElement();
            String fullClassName = typeElement.getQualifiedName().toString();//完整类名,如com.example.Test
            String simpleClassName = typeElement.getSimpleName().toString();//简单类名,如Test

            // 字段信息
            VariableElement variableElement = (VariableElement) element;
            String filedName = variableElement.getSimpleName().toString();//字段名称
            String filedType = variableElement.asType().toString();//字段类型

            //封装类信息和字段信息用于创建Java类文件
            ClassInfo classInfo = mClassInfoMap.get(fullClassName);
            if (classInfo == null) {
                classInfo = new ClassInfo();
                classInfo.packageName = packageName;
                classInfo.fullClassName = fullClassName;
                classInfo.simpleClassName = simpleClassName;
                classInfo.fields = new LinkedList<>();
                mClassInfoMap.put(fullClassName, classInfo);
            }
            FieldInfo fieldInfo = new FieldInfo();
            fieldInfo.name = filedName;
            fieldInfo.type = filedType;
            fieldInfo.value = variableElement.getAnnotation(BindView.class).value();
            classInfo.fields.add(fieldInfo);
        }
        for (ClassInfo info : mClassInfoMap.values()) {
            generateJavaFile(info);
        }
        return true;
    }

    /**
     * 生成Java文件
     *
     * @param classInfo
     */
    private void generateJavaFile(ClassInfo classInfo) {
        try {
            //生成类
            TypeSpec.Builder typeSpecBuilder = TypeSpec.classBuilder(classInfo.simpleClassName + "$ViewBinding")
                    .addModifiers(Modifier.PUBLIC)
                    .addSuperinterface(ParameterizedTypeName.get(ClassName.get(ViewBinding.class), ClassName.get(classInfo.packageName, classInfo.simpleClassName)));
            StringBuilder sb = new StringBuilder();
            for (FieldInfo fieldInfo : classInfo.fields) {
                String name = fieldInfo.name;
                int value = fieldInfo.value;
                sb.append("target.")
                        .append(name)
                        .append(" = ")
                        .append("target.findViewById(")
                        .append(value).append(");\n");
            }
            String string = sb.substring(0, sb.length() - 2);
            //生成方法
            MethodSpec methodSpec = MethodSpec.methodBuilder("bind")
                    .addModifiers(Modifier.PUBLIC)
                    .returns(void.class)
                    .addParameter(ParameterSpec.builder(ClassName.get(classInfo.packageName, classInfo.simpleClassName), "target").build())
                    .addStatement(string)
                    .build();
            TypeSpec typeSpec = typeSpecBuilder.addMethod(methodSpec).build();
            //生成Java文件
            JavaFile javaFile = JavaFile.builder(classInfo.packageName, typeSpec).build();
            javaFile.writeTo(mFiler);
            mMessager.printMessage(Diagnostic.Kind.NOTE, "write success!");
        } catch (Exception e) {
            e.printStackTrace();
            mMessager.printMessage(Diagnostic.Kind.ERROR, "write failure!" + ",msg=" + e.getMessage());
        }
    }
}
