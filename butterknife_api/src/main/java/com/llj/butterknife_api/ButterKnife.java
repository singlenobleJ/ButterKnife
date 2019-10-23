package com.llj.butterknife_api;

import android.app.Activity;

import com.llj.butterknife_annotation.ViewBinding;

/**
 * @author: lilinjie
 * @date: 2019-10-22 13:56
 * @description: 公开API
 */
public class ButterKnife {

    public static void bind(Activity activity) {
        ViewBinding viewBinding = findViewBindingActivity(activity);
        viewBinding.bind(activity);
    }

    private static ViewBinding findViewBindingActivity(Activity activity) {
        try {
            Class<?> clazz = activity.getClass();
            Class<?> viewBindingClazz = Class.forName(clazz.getName() + "$ViewBinding");
            return (ViewBinding) viewBindingClazz.newInstance();
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        } catch (InstantiationException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        }
        throw new RuntimeException(String.format("can not find %s , something when compiler.", activity.getClass().getSimpleName() + "$ViewBinding"));
    }
}
