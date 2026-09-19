package com.example.util

import android.content.Context
import android.content.SharedPreferences
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Build
import androidx.compose.material.icons.filled.Category
import androidx.compose.material.icons.filled.DirectionsCar
import androidx.compose.material.icons.filled.Inventory
import androidx.compose.material.icons.filled.LocalShipping
import androidx.compose.material.icons.filled.MoreHoriz
import androidx.compose.material.icons.filled.People
import androidx.compose.material.icons.filled.ReceiptLong
import androidx.compose.material.icons.filled.Restaurant
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.ui.graphics.vector.ImageVector
import org.json.JSONArray
import org.json.JSONObject
import java.util.UUID

data class ExpenseCategory(
    val id: String,
    val name: String,
    val iconKey: String = "category",
    val subCategories: List<String> = emptyList()
)

object CategoryManager {

    private const val PREFS_NAME = "symphonix_categories_prefs"
    private const val KEY_CATEGORIES_JSON = "categories_json"

    val DEFAULT_CATEGORIES = listOf(
        ExpenseCategory(
            id = "cat_matieres",
            name = "Matières premières",
            iconKey = "restaurant",
            subCategories = listOf("Œufs", "Volailles", "Farine & Levure", "Beurre & Sucre", "Produits laitiers")
        ),
        ExpenseCategory(
            id = "cat_personnel",
            name = "Personnel",
            iconKey = "people",
            subCategories = listOf("Rémunération", "Avance salaire", "Prime", "Repas")
        ),
        ExpenseCategory(
            id = "cat_frais",
            name = "Frais généraux",
            iconKey = "receipt",
            subCategories = listOf("Emballages", "Électricité / Gaz", "Entretien", "Eau")
        ),
        ExpenseCategory(
            id = "cat_transport",
            name = "Transport",
            iconKey = "car",
            subCategories = listOf("Carburant", "Frais livraison", "Péage")
        ),
        ExpenseCategory(
            id = "cat_divers",
            name = "Divers",
            iconKey = "more",
            subCategories = listOf("Fournitures", "Autre dépense")
        )
    )

    private fun getPrefs(context: Context): SharedPreferences {
        return context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    }

    fun getIconVector(iconKey: String): ImageVector {
        return when (iconKey.lowercase()) {
            "restaurant", "food" -> Icons.Default.Restaurant
            "people", "personnel", "staff" -> Icons.Default.People
            "receipt", "invoice", "frais" -> Icons.Default.ReceiptLong
            "car", "transport", "vehicle" -> Icons.Default.DirectionsCar
            "more", "divers", "other" -> Icons.Default.MoreHoriz
            "shopping", "market" -> Icons.Default.ShoppingCart
            "build", "tools", "maintenance" -> Icons.Default.Build
            "shipping", "delivery" -> Icons.Default.LocalShipping
            "inventory", "stock" -> Icons.Default.Inventory
            else -> Icons.Default.Category
        }
    }

    fun getCategories(context: Context): List<ExpenseCategory> {
        val jsonString = getPrefs(context).getString(KEY_CATEGORIES_JSON, null)
        if (jsonString.isNullOrBlank()) {
            return DEFAULT_CATEGORIES
        }

        return try {
            val jsonArray = JSONArray(jsonString)
            val list = mutableListOf<ExpenseCategory>()
            for (i in 0 until jsonArray.length()) {
                val obj = jsonArray.getJSONObject(i)
                val id = obj.optString("id", UUID.randomUUID().toString())
                val name = obj.optString("name", "Catégorie")
                val iconKey = obj.optString("iconKey", "category")
                val subsArray = obj.optJSONArray("subCategories") ?: JSONArray()
                val subs = mutableListOf<String>()
                for (j in 0 until subsArray.length()) {
                    val sub = subsArray.getString(j)
                    if (sub.isNotBlank()) subs.add(sub.trim())
                }
                list.add(ExpenseCategory(id = id, name = name, iconKey = iconKey, subCategories = subs))
            }
            if (list.isEmpty()) DEFAULT_CATEGORIES else list
        } catch (_: Exception) {
            DEFAULT_CATEGORIES
        }
    }

    fun saveCategories(context: Context, list: List<ExpenseCategory>) {
        val jsonArray = JSONArray()
        list.forEach { cat ->
            val obj = JSONObject()
            obj.put("id", cat.id)
            obj.put("name", cat.name)
            obj.put("iconKey", cat.iconKey)
            val subsArray = JSONArray()
            cat.subCategories.forEach { subsArray.put(it) }
            obj.put("subCategories", subsArray)
            jsonArray.put(obj)
        }
        getPrefs(context).edit().putString(KEY_CATEGORIES_JSON, jsonArray.toString()).apply()
    }

    fun addCategory(context: Context, name: String, iconKey: String = "category", initialSubs: List<String> = emptyList()): List<ExpenseCategory> {
        val current = getCategories(context).toMutableList()
        val newCat = ExpenseCategory(
            id = "cat_" + UUID.randomUUID().toString().take(8),
            name = name.trim(),
            iconKey = iconKey,
            subCategories = initialSubs.map { it.trim() }.filter { it.isNotBlank() }
        )
        current.add(newCat)
        saveCategories(context, current)
        return current
    }

    fun updateCategory(context: Context, updatedCategory: ExpenseCategory): List<ExpenseCategory> {
        val current = getCategories(context).map {
            if (it.id == updatedCategory.id) updatedCategory else it
        }
        saveCategories(context, current)
        return current
    }

    fun deleteCategory(context: Context, categoryId: String): List<ExpenseCategory> {
        val current = getCategories(context).filterNot { it.id == categoryId }
        saveCategories(context, current)
        return current
    }

    fun addSubCategory(context: Context, categoryId: String, subCategoryName: String): List<ExpenseCategory> {
        val trimmed = subCategoryName.trim()
        if (trimmed.isBlank()) return getCategories(context)

        val current = getCategories(context).map { cat ->
            if (cat.id == categoryId && !cat.subCategories.contains(trimmed)) {
                cat.copy(subCategories = cat.subCategories + trimmed)
            } else {
                cat
            }
        }
        saveCategories(context, current)
        return current
    }

    fun removeSubCategory(context: Context, categoryId: String, subCategoryName: String): List<ExpenseCategory> {
        val current = getCategories(context).map { cat ->
            if (cat.id == categoryId) {
                cat.copy(subCategories = cat.subCategories.filterNot { it == subCategoryName })
            } else {
                cat
            }
        }
        saveCategories(context, current)
        return current
    }

    fun resetToDefaults(context: Context): List<ExpenseCategory> {
        saveCategories(context, DEFAULT_CATEGORIES)
        return DEFAULT_CATEGORIES
    }
}
