package pe.devpicon.clapapp.shared

import android.content.Context

class ResourceReader(val context: Context) {

    fun getRawResourceId(resourceName: String): Int {
        return context.resources.getIdentifier(resourceName, "raw", context.packageName)
    }
}