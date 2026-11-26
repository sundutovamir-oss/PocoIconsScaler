package com.custom.pocoiconscaler

import de.robv.android.xposed.IXposedHookLoadPackage
import de.robv.android.xposed.XC_MethodHook
import de.robv.android.xposed.XSharedPreferences
import de.robv.android.xposed.XposedBridge
import de.robv.android.xposed.XposedHelpers
import de.robv.android.xposed.callbacks.XC_LoadPackage

class MainHook : IXposedHookLoadPackage {

    override fun handleLoadPackage(lpparam: XC_LoadPackage.LoadPackageParam) {
        if (lpparam.packageName != "com.mi.android.globallauncher") return

        val prefs = XSharedPreferences("com.custom.pocoiconscaler", "scaler_prefs")
        prefs.reload()
        val scalePercent = prefs.getInt("icon_scale", 100)
        if (scalePercent == 100) return

        val scale = scalePercent / 100f

        try {
            val deviceProfileClass = XposedHelpers.findClass(
                "com.android.launcher3.DeviceProfile",
                lpparam.classLoader
            )

            XposedBridge.hookAllConstructors(deviceProfileClass, object : XC_MethodHook() {
                override fun afterHookedMethod(param: MethodHookParam) {
                    val profile = param.thisObject

                    val standardFields = listOf("iconSizePx", "allAppsIconSizePx", "folderIconSizePx")
                    var hooked = false

                    for (fieldName in standardFields) {
                        try {
                            val oldVal = XposedHelpers.getIntField(profile, fieldName)
                            if (oldVal > 0) {
                                XposedHelpers.setIntField(profile, fieldName, (oldVal * scale).toInt())
                                hooked = true
                            }
                        } catch (_: Throwable) {}
                    }

                    if (!hooked) {
                        for (field in deviceProfileClass.declaredFields) {
                            if (field.type == Int::class.javaPrimitiveType) {
                                field.isAccessible = true
                                val value = field.getInt(profile)
                                if (value in 115..210) {
                                    field.setInt(profile, (value * scale).toInt())
                                }
                            }
                        }
                    }
                }
            })
        } catch (t: Throwable) {
            XposedBridge.log("PocoIconScaler Error: ${t.message}")
        }
    }
}
