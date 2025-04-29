# Keep all generated ViewBinding classes
-keep class **ViewBinding { *; }

# Keep all ViewBinding classes in the generated databinding package
-keep class **.databinding.*Binding { *; }

# Keep the inflate method in all ViewBinding classes
-keepclassmembers class **.databinding.*Binding {
    public static ** inflate(android.view.LayoutInflater);
}

# Keep OkHttp library classes
-keep class okhttp3.** { *; }
-keepclassmembers class okhttp3.** { *; }

# Keep Gson library classes
-keep class com.google.gson.** { *; }
-keepclassmembers class com.google.gson.** { *; }